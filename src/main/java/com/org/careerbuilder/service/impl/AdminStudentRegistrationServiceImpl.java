package com.org.careerbuilder.service.impl;

import com.org.careerbuilder.dto.request.AdminStudentRegistrationRequest;
import com.org.careerbuilder.dto.response.AdminOperationResponses;
import com.org.careerbuilder.exceptions.ResourceNotFoundException;
import com.org.careerbuilder.models.*;
import com.org.careerbuilder.models.enums.AdminActivityType;
import com.org.careerbuilder.models.enums.StudentAccountStatus;
import com.org.careerbuilder.models.enums.StudentAdmissionType;
import com.org.careerbuilder.models.enums.StudentDocumentType;
import com.org.careerbuilder.repository.*;
import com.org.careerbuilder.service.AdminStudentRegistrationService;
import com.org.careerbuilder.service.support.AdminActivityLogger;
import com.org.careerbuilder.service.support.AdminFileStorageHelper;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class AdminStudentRegistrationServiceImpl implements AdminStudentRegistrationService {

    private final SchoolRepository schoolRepository;
    private final StudentRepository studentRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final StudentDocumentRepository studentDocumentRepository;
    private final FeeRepository feeRepository;
    private final AdminActivityLogger activityLogger;
    private final AdminFileStorageHelper fileStorageHelper;
    private final Map<String, BulkPreviewSession> bulkPreviewCache = new ConcurrentHashMap<>();

    @Override
    @Transactional(readOnly = true)
    public AdminOperationResponses.RegistrationMetadataResponse getMetadata(Long schoolId) {
        if (!schoolRepository.existsById(schoolId)) {
            throw new ResourceNotFoundException("School not found: " + schoolId);
        }

        Map<String, List<String>> classSections = new TreeMap<>();
        for (Object[] row : studentRepository.findDistinctClassSections(schoolId)) {
            String cn = (String) row[0];
            String sec = (String) row[1];
            classSections.computeIfAbsent(cn, k -> new ArrayList<>()).add(sec);
        }

        List<AdminOperationResponses.ClassSectionOption> classes = classSections.entrySet().stream()
                .map(e -> new AdminOperationResponses.ClassSectionOption(e.getKey(), e.getValue()))
                .toList();

        return new AdminOperationResponses.RegistrationMetadataResponse(
                List.of("Male", "Female", "Other"),
                List.of("A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-"),
                List.of("Hindu", "Muslim", "Christian", "Sikh", "Buddhist", "Jain", "Other"),
                List.of("General", "OBC", "SC", "ST", "EWS"),
                Arrays.stream(StudentAdmissionType.values()).map(Enum::name).toList(),
                Arrays.stream(StudentDocumentType.values()).map(Enum::name).toList(),
                classes
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminOperationResponses.StudentRegistrationResponse register(AdminStudentRegistrationRequest request) {
        School school = schoolRepository.findById(request.getSchoolId())
                .orElseThrow(() -> new ResourceNotFoundException("School not found"));

        if (studentProfileRepository.existsByAdmissionNumberIgnoreCase(request.getBasic().getAdmissionNumber())) {
            throw new IllegalArgumentException("Admission number already exists");
        }

        String email = resolveEmail(request);
        if (studentRepository.findFirstByEmailIgnoreCase(email).isPresent()) {
            throw new IllegalArgumentException("Email already registered");
        }
        if (studentRepository.findFirstByPhone(request.getContact().getPhone()).isPresent()) {
            throw new IllegalArgumentException("Phone already registered");
        }

        String[] names = splitName(request.getBasic().getFullName());
        int age = Period.between(request.getBasic().getDateOfBirth(), LocalDate.now()).getYears();
        if (age < 3) {
            age = 3;
        }

        String section = normalizeSection(request.getAcademic().getSection());
        int rollNo = resolveRollNo(request, school.getId(), request.getAcademic().getClassName(), section);

        String address = buildAddress(request.getContact());
        String parentName = request.getParent() != null && request.getParent().getFatherName() != null
                ? request.getParent().getFatherName()
                : "Guardian";

        Student student = Student.builder()
                .firstName(names[0])
                .lastName(names[1])
                .age(age)
                .className(request.getAcademic().getClassName().trim())
                .section(section)
                .rollNo(rollNo)
                .parentName(parentName)
                .phone(request.getContact().getPhone())
                .email(email)
                .address(address)
                .school(school)
                .build();
        student = studentRepository.save(student);

        StudentAdmissionType admissionType = parseAdmissionType(request.getAcademic().getStudentType());
        AdminStudentRegistrationRequest.AdditionalDetails add = request.getAdditional();

        StudentProfile profile = StudentProfile.builder()
                .student(student)
                .admissionNumber(request.getBasic().getAdmissionNumber().trim())
                .admissionDate(request.getBasic().getAdmissionDate())
                .photoUrl(request.getBasic().getPhotoUrl())
                .gender(request.getBasic().getGender())
                .dateOfBirth(request.getBasic().getDateOfBirth())
                .bloodGroup(request.getBasic().getBloodGroup())
                .aadharNumber(normalizeAadhar(request.getBasic().getAadharNumber()))
                .religion(request.getBasic().getReligion())
                .category(request.getBasic().getCategory())
                .academicYear(request.getAcademic().getAcademicYear())
                .previousSchoolDetails(request.getAcademic().getPreviousSchoolDetails())
                .studentType(admissionType)
                .fatherName(request.getParent() != null ? request.getParent().getFatherName() : null)
                .fatherPhone(request.getParent() != null ? request.getParent().getFatherPhone() : null)
                .motherName(request.getParent() != null ? request.getParent().getMotherName() : null)
                .motherPhone(request.getParent() != null ? request.getParent().getMotherPhone() : null)
                .fatherOccupation(request.getParent() != null ? request.getParent().getFatherOccupation() : null)
                .guardianName(request.getParent() != null ? request.getParent().getGuardianName() : null)
                .guardianPhone(request.getParent() != null ? request.getParent().getGuardianPhone() : null)
                .addressLine1(request.getContact().getAddressLine1())
                .addressLine2(request.getContact().getAddressLine2())
                .city(request.getContact().getCity())
                .stateName(request.getContact().getState())
                .pincode(request.getContact().getPincode())
                .country(request.getContact().getCountry() != null ? request.getContact().getCountry() : "India")
                .transportRequired(add != null && Boolean.TRUE.equals(add.getTransportRequired()))
                .hostelRequired(add != null && Boolean.TRUE.equals(add.getHostelRequired()))
                .medicalConditions(add != null ? add.getMedicalConditions() : null)
                .additionalNotes(add != null ? add.getAdditionalNotes() : null)
                .accountStatus(StudentAccountStatus.NEW_ADMISSION)
                .build();
        studentProfileRepository.save(profile);

        List<String> docUrls = new ArrayList<>();
        if (request.getDocuments() != null) {
            for (AdminStudentRegistrationRequest.DocumentUploadRef ref : request.getDocuments()) {
                StudentDocumentType type = parseDocumentType(ref.getDocumentType());
                studentDocumentRepository.save(StudentDocument.builder()
                        .student(student)
                        .documentType(type)
                        .fileName(ref.getFileName())
                        .fileUrl(ref.getFileUrl())
                        .build());
                docUrls.add(ref.getFileUrl());
            }
        }

        String academicYear = request.getAcademic().getAcademicYear() != null
                ? request.getAcademic().getAcademicYear()
                : profile.getAcademicYear();
        Fee fee = Fee.builder()
                .student(student)
                .feeType("ADMISSION")
                .amount(BigDecimal.valueOf(5000))
                .totalAmount(5000.0)
                .dueDate(LocalDate.now().plusMonths(1))
                .status(Fee.FeeStatus.PENDING)
                .paidAmount(BigDecimal.ZERO)
                .academicYear(academicYear)
                .term("ANNUAL")
                .description("Initial admission fee")
                .build();
        fee = feeRepository.save(fee);

        activityLogger.log(school.getId(), AdminActivityType.STUDENT_REGISTERED,
                "Student registered: " + request.getBasic().getFullName(),
                "Admission " + profile.getAdmissionNumber() + " — Class " + student.getClassName() + " " + student.getSection(),
                "STUDENT", student.getId(), request.getPerformedBy());
        activityLogger.log(school.getId(), AdminActivityType.FEE_INITIALIZED,
                "Initial fee created",
                "Admission fee for " + profile.getAdmissionNumber(),
                "FEE", fee.getId(), request.getPerformedBy());

        return new AdminOperationResponses.StudentRegistrationResponse(
                student.getId(),
                profile.getAdmissionNumber(),
                request.getBasic().getFullName(),
                student.getClassName(),
                student.getSection(),
                student.getRollNo(),
                fee.getId(),
                docUrls
        );
    }

    @Override
    public String uploadPhoto(Long schoolId, MultipartFile file) {
        return fileStorageHelper.store(schoolId, "student-photos", file);
    }

    @Override
    public String uploadDocument(Long schoolId, String documentType, MultipartFile file) {
        String folder = "student-documents/" + (documentType != null ? documentType.toLowerCase() : "other");
        return fileStorageHelper.store(schoolId, folder, file);
    }

    @Override
    public byte[] generateBulkTemplate() {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("students");
            Row header = sheet.createRow(0);
            String[] headers = {"full_name", "admission_number", "class_name", "section", "gender", "date_of_birth",
                    "father_name", "phone", "email", "address_line1", "city", "state", "pincode"};
            for (int i = 0; i < headers.length; i++) {
                header.createCell(i).setCellValue(headers[i]);
                sheet.autoSizeColumn(i);
            }
            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to generate template", e);
        }
    }

    @Override
    public AdminOperationResponses.BulkPreviewResponse previewBulkUpload(Long schoolId, MultipartFile file) {
        if (!schoolRepository.existsById(schoolId)) {
            throw new ResourceNotFoundException("School not found: " + schoolId);
        }
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Template file is required");
        }
        List<BulkCandidate> candidates = new ArrayList<>();
        List<AdminOperationResponses.BulkStudentRowResult> rows = new ArrayList<>();
        int valid = 0;
        int errors = 0;

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }
                String fullName = cell(row, 0);
                String admission = cell(row, 1);
                String className = cell(row, 2);
                String section = normalizeSection(cell(row, 3));
                String gender = cell(row, 4);
                String dob = cell(row, 5);
                String fatherName = cell(row, 6);
                String phone = cell(row, 7);
                String email = cell(row, 8);
                String address = cell(row, 9);
                String city = cell(row, 10);
                String state = cell(row, 11);
                String pincode = cell(row, 12);

                String error = validateBulkRow(admission, fullName, className, section, phone, dob, email);
                if (error != null) {
                    errors++;
                    rows.add(new AdminOperationResponses.BulkStudentRowResult(i + 1, fullName, className, "ERROR", error));
                    continue;
                }

                BulkCandidate candidate = new BulkCandidate(schoolId, fullName, admission, className, section, gender, dob,
                        fatherName, phone, email, address, city, state, pincode);
                candidates.add(candidate);
                valid++;
                rows.add(new AdminOperationResponses.BulkStudentRowResult(i + 1, fullName, className, "VALID", "All good"));
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid template file format", e);
        }

        String previewId = UUID.randomUUID().toString();
        bulkPreviewCache.put(previewId, new BulkPreviewSession(schoolId, candidates, LocalDate.now()));
        return new AdminOperationResponses.BulkPreviewResponse(previewId, valid + errors, valid, errors, rows);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminOperationResponses.BulkCommitResponse commitBulkUpload(String previewId, String performedBy) {
        BulkPreviewSession session = bulkPreviewCache.get(previewId);
        if (session == null) {
            throw new IllegalArgumentException("Preview session expired or not found");
        }
        int registered = 0;
        int skipped = 0;
        for (BulkCandidate c : session.candidates()) {
            if (studentProfileRepository.existsByAdmissionNumberIgnoreCase(c.admissionNumber())
                    || studentRepository.findFirstByPhone(c.phone()).isPresent()) {
                skipped++;
                continue;
            }
            AdminStudentRegistrationRequest req = toRequest(c, performedBy);
            register(req);
            registered++;
        }
        bulkPreviewCache.remove(previewId);
        return new AdminOperationResponses.BulkCommitResponse(previewId, registered, skipped);
    }

    private String resolveEmail(AdminStudentRegistrationRequest request) {
        if (request.getContact().getEmail() != null && !request.getContact().getEmail().isBlank()) {
            return request.getContact().getEmail().trim().toLowerCase();
        }
        String adm = request.getBasic().getAdmissionNumber().replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
        return adm + "@student.school.local";
    }

    private int resolveRollNo(AdminStudentRegistrationRequest request, Long schoolId, String className, String section) {
        if (request.getAcademic().getRollNumber() != null && request.getAcademic().getRollNumber() > 0) {
            return request.getAcademic().getRollNumber();
        }
        Integer max = studentRepository.findMaxRollNo(schoolId, className, section);
        return max == null ? 1 : max + 1;
    }

    private String normalizeSection(String section) {
        if (section == null || section.isBlank()) {
            return "A";
        }
        return section.trim().toUpperCase();
    }

    private String[] splitName(String fullName) {
        String trimmed = fullName.trim();
        int idx = trimmed.lastIndexOf(' ');
        if (idx <= 0) {
            return new String[]{trimmed, "."};
        }
        return new String[]{trimmed.substring(0, idx).trim(), trimmed.substring(idx + 1).trim()};
    }

    private String buildAddress(AdminStudentRegistrationRequest.ContactDetails c) {
        StringBuilder sb = new StringBuilder(c.getAddressLine1());
        if (c.getAddressLine2() != null && !c.getAddressLine2().isBlank()) {
            sb.append(", ").append(c.getAddressLine2());
        }
        sb.append(", ").append(c.getCity()).append(", ").append(c.getState()).append(" ").append(c.getPincode());
        if (c.getCountry() != null && !c.getCountry().isBlank()) {
            sb.append(", ").append(c.getCountry());
        }
        String addr = sb.toString();
        return addr.length() > 300 ? addr.substring(0, 300) : addr;
    }

    private String normalizeAadhar(String aadhar) {
        if (aadhar == null) {
            return null;
        }
        return aadhar.replaceAll("\\s", "");
    }

    private StudentAdmissionType parseAdmissionType(String type) {
        if (type == null) {
            return StudentAdmissionType.NEW_ADMISSION;
        }
        try {
            return StudentAdmissionType.valueOf(type.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            if (type.toLowerCase().contains("transfer")) {
                return StudentAdmissionType.TRANSFER;
            }
            return StudentAdmissionType.NEW_ADMISSION;
        }
    }

    private StudentDocumentType parseDocumentType(String type) {
        if (type == null || type.isBlank()) {
            return StudentDocumentType.OTHER;
        }
        try {
            return StudentDocumentType.valueOf(type.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return StudentDocumentType.OTHER;
        }
    }

    private String cell(Row row, int i) {
        if (row.getCell(i) == null) {
            return "";
        }
        return row.getCell(i).toString().trim();
    }

    private String validateBulkRow(String admission, String fullName, String className, String section, String phone,
                                   String dob, String email) {
        if (fullName == null || fullName.isBlank()) {
            return "Missing full name";
        }
        if (admission == null || admission.isBlank()) {
            return "Missing admission number";
        }
        if (className == null || className.isBlank() || section == null || section.isBlank()) {
            return "Missing class/section";
        }
        if (phone == null || !phone.matches("^[0-9]{10,15}$")) {
            return "Invalid phone";
        }
        if (dob == null || dob.isBlank()) {
            return "Missing DOB";
        }
        if (email != null && !email.isBlank() && !email.contains("@")) {
            return "Invalid email";
        }
        return null;
    }

    private AdminStudentRegistrationRequest toRequest(BulkCandidate c, String performedBy) {
        LocalDate dob = LocalDate.parse(c.dateOfBirth());
        return AdminStudentRegistrationRequest.builder()
                .schoolId(c.schoolId())
                .performedBy(performedBy)
                .basic(AdminStudentRegistrationRequest.BasicDetails.builder()
                        .fullName(c.fullName())
                        .admissionNumber(c.admissionNumber())
                        .admissionDate(LocalDate.now())
                        .gender(c.gender() == null || c.gender().isBlank() ? "Other" : c.gender())
                        .dateOfBirth(dob)
                        .build())
                .academic(AdminStudentRegistrationRequest.AcademicDetails.builder()
                        .className(c.className())
                        .section(c.section())
                        .studentType("NEW_ADMISSION")
                        .academicYear(currentAcademicYear())
                        .build())
                .parent(AdminStudentRegistrationRequest.ParentDetails.builder()
                        .fatherName(c.fatherName() == null || c.fatherName().isBlank() ? "Guardian" : c.fatherName())
                        .fatherPhone(c.phone())
                        .build())
                .contact(AdminStudentRegistrationRequest.ContactDetails.builder()
                        .phone(c.phone())
                        .email(c.email())
                        .addressLine1(c.addressLine1() == null || c.addressLine1().isBlank() ? "Address Not Provided" : c.addressLine1())
                        .city(c.city() == null || c.city().isBlank() ? "NA" : c.city())
                        .state(c.state() == null || c.state().isBlank() ? "NA" : c.state())
                        .pincode(c.pincode() == null || c.pincode().isBlank() ? "000000" : c.pincode())
                        .country("India")
                        .build())
                .build();
    }

    private String currentAcademicYear() {
        int y = LocalDate.now().getYear();
        int m = LocalDate.now().getMonthValue();
        if (m >= 4) {
            return y + "-" + (y + 1);
        }
        return (y - 1) + "-" + y;
    }

    private record BulkCandidate(
            Long schoolId,
            String fullName,
            String admissionNumber,
            String className,
            String section,
            String gender,
            String dateOfBirth,
            String fatherName,
            String phone,
            String email,
            String addressLine1,
            String city,
            String state,
            String pincode
    ) {
    }

    private record BulkPreviewSession(Long schoolId, List<BulkCandidate> candidates, LocalDate createdDate) {
    }
}
