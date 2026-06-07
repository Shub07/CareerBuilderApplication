package com.org.careerbuilder.service.impl;

import com.org.careerbuilder.dto.request.AdminStudentRequests;
import com.org.careerbuilder.dto.response.AdminStudentProfileDtos;
import com.org.careerbuilder.exceptions.ResourceNotFoundException;
import com.org.careerbuilder.models.*;
import com.org.careerbuilder.models.enums.AdminActivityType;
import com.org.careerbuilder.models.enums.AttendanceStatus;
import com.org.careerbuilder.models.enums.ExamType;
import com.org.careerbuilder.models.enums.StudentAccountStatus;
import com.org.careerbuilder.models.enums.StudentDocumentType;
import com.org.careerbuilder.repository.*;
import com.org.careerbuilder.service.AdminStudentManagementService.AdminStudentExportResult;
import com.org.careerbuilder.service.AdminStudentProfileService;
import com.org.careerbuilder.service.support.AdminActivityLogger;
import com.org.careerbuilder.service.support.AdminFileStorageHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminStudentProfileServiceImpl implements AdminStudentProfileService {

    private static final int ATTENDANCE_WINDOW_DAYS = 120;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    private final StudentRepository studentRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final StudentDocumentRepository studentDocumentRepository;
    private final FeeRepository feeRepository;
    private final ExamResultRepository examResultRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final CertificateRepository certificateRepository;
    private final AdminActivityLogRepository adminActivityLogRepository;
    private final AdminStudentMessageRepository messageRepository;
    private final AppUserRepository appUserRepository;
    private final AdminActivityLogger activityLogger;
    private final AdminFileStorageHelper fileStorageHelper;
    private final DailyLifeActivityRepository dailyLifeActivityRepository;

    @Override
    @Transactional(readOnly = true)
    public AdminStudentProfileDtos.FullProfileResponse getFullProfile(Long schoolId, Long studentId) {
        Student st = requireStudent(schoolId, studentId);
        StudentProfile p = profileOrNull(studentId);
        return new AdminStudentProfileDtos.FullProfileResponse(
                buildHeader(st, p),
                buildPersonal(st, p),
                buildContact(st, p),
                buildPerformance(st.getId()),
                buildAttendance(st.getId(), null, null),
                buildAcademic(st, p),
                buildAdditional(p),
                listDocuments(studentId)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public AdminStudentProfileDtos.PerformanceTabResponse getPerformanceTab(
            Long schoolId, Long studentId, String examType, LocalDate from, LocalDate to) {
        requireStudent(schoolId, studentId);
        ExamType type = parseExamType(examType);
        List<ExamResult> results = examResultRepository.findDetailedResults(studentId, type);
        if (from != null || to != null) {
            results = results.stream()
                    .filter(er -> inRange(er.getExam().getExamDate(), from, to))
                    .toList();
        }
        return new AdminStudentProfileDtos.PerformanceTabResponse(
                buildPerformance(studentId),
                results.stream().limit(50).map(this::toExamRow).toList()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public AdminStudentProfileDtos.AttendanceTabResponse getAttendanceTab(
            Long schoolId, Long studentId, String period, LocalDate from, LocalDate to) {
        requireStudent(schoolId, studentId);
        DateRange range = resolvePeriod(period, from, to);
        LocalDate attFrom = range != null ? range.from() : LocalDate.now().minusDays(ATTENDANCE_WINDOW_DAYS);
        LocalDate attTo = range != null ? range.to() : LocalDate.now();
        return new AdminStudentProfileDtos.AttendanceTabResponse(
                buildAttendance(studentId, attFrom, attTo),
                loadAttendanceRecords(studentId, attFrom, attTo)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public AdminStudentProfileDtos.FeesTabResponse getFeesTab(Long schoolId, Long studentId) {
        requireStudent(schoolId, studentId);
        List<Fee> fees = feeRepository.findAllByStudentId(studentId);
        double paid = fees.stream().mapToDouble(f -> f.getPaidAmount() != null ? f.getPaidAmount().doubleValue() : 0).sum();
        double pending = fees.stream()
                .filter(f -> f.getStatus() != Fee.FeeStatus.PAID && f.getStatus() != Fee.FeeStatus.CANCELLED)
                .mapToDouble(f -> {
                    double amt = f.getAmount() != null ? f.getAmount().doubleValue() : 0;
                    double p = f.getPaidAmount() != null ? f.getPaidAmount().doubleValue() : 0;
                    return Math.max(0, amt - p);
                }).sum();
        String overall = pending > 0 ? "PENDING" : "PAID";
        List<AdminStudentProfileDtos.FeeRow> rows = fees.stream().map(f -> new AdminStudentProfileDtos.FeeRow(
                f.getId(),
                f.getFeeType(),
                f.getAmount() != null ? f.getAmount().doubleValue() : 0,
                f.getPaidAmount() != null ? f.getPaidAmount().doubleValue() : 0,
                f.getStatus() != null ? f.getStatus().name() : "PENDING",
                f.getDueDate() != null ? f.getDueDate().toString() : "",
                f.getAcademicYear()
        )).toList();
        return new AdminStudentProfileDtos.FeesTabResponse(overall, paid, pending, rows);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminStudentProfileDtos.CertificatesTabResponse getCertificatesTab(
            Long schoolId, Long studentId, String academicYear) {
        requireStudent(schoolId, studentId);
        List<Certificate> certs = certificateRepository.findByStudentFiltered(studentId, academicYear, null);
        List<AdminStudentProfileDtos.CertificateRow> rows = certs.stream()
                .map(c -> new AdminStudentProfileDtos.CertificateRow(
                        c.getId(),
                        c.getName(),
                        c.getCategory() != null ? c.getCategory().name() : "",
                        c.getAcademicYear(),
                        c.getIssuedOn() != null ? c.getIssuedOn().toString() : ""
                )).toList();
        return new AdminStudentProfileDtos.CertificatesTabResponse(rows);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminStudentProfileDtos.ActivityLogTabResponse getActivityLogTab(
            Long schoolId, Long studentId, String period, LocalDate from, LocalDate to) {
        requireStudent(schoolId, studentId);
        List<AdminActivityLog> logs = adminActivityLogRepository
                .findBySchool_IdAndEntityTypeAndEntityIdOrderByCreatedAtDesc(
                        schoolId, "STUDENT", studentId, PageRequest.of(0, 100));
        DateRange range = resolvePeriod(period, from, to);
        if (range != null) {
            LocalDateTime start = range.from().atStartOfDay();
            LocalDateTime end = range.to().plusDays(1).atStartOfDay();
            logs = logs.stream()
                    .filter(l -> !l.getCreatedAt().isBefore(start) && l.getCreatedAt().isBefore(end))
                    .toList();
        }
        List<AdminStudentProfileDtos.ActivityLogRow> rows = logs.stream()
                .map(l -> new AdminStudentProfileDtos.ActivityLogRow(
                        l.getId(),
                        l.getActivityType().name(),
                        l.getTitle(),
                        l.getDescription(),
                        l.getPerformedBy(),
                        l.getCreatedAt()
                )).toList();
        return new AdminStudentProfileDtos.ActivityLogTabResponse(rows);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminStudentProfileDtos.StudyActivityTabResponse getStudyActivityTab(
            Long schoolId, Long studentId, String period, LocalDate from, LocalDate to) {
        requireStudent(schoolId, studentId);
        DateRange range = resolvePeriod(period, from, to);
        LocalDate start = range != null ? range.from() : LocalDate.now().minusDays(30);
        LocalDate end = range != null ? range.to() : LocalDate.now();
        List<DailyLifeActivity> activities = dailyLifeActivityRepository
                .findByStudent_IdAndActivityDateBetweenOrderByActivityDateAscStartTimeAsc(studentId, start, end);
        long total = activities.size();
        long done = activities.stream().filter(DailyLifeActivity::isCompleted).count();
        double rate = total == 0 ? 0 : done * 100.0 / total;
        List<AdminStudentProfileDtos.StudyActivityRow> rows = activities.stream()
                .map(a -> new AdminStudentProfileDtos.StudyActivityRow(
                        a.getActivityDate().toString(),
                        a.getNote() != null && !a.getNote().isBlank() ? a.getNote() : a.getActivityType().name(),
                        a.getActivityType() != null ? a.getActivityType().name() : "",
                        a.isCompleted(),
                        a.getStartTime() != null ? a.getStartTime().toString() : "",
                        a.getEndTime() != null ? a.getEndTime().toString() : ""
                )).toList();
        return new AdminStudentProfileDtos.StudyActivityTabResponse(rows, rate);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminStudentProfileDtos.MessageSentResponse sendMessage(
            Long studentId, AdminStudentRequests.SendMessageRequest request) {
        Student st = requireStudent(request.getSchoolId(), studentId);
        AdminStudentMessage msg = AdminStudentMessage.builder()
                .schoolId(request.getSchoolId())
                .student(st)
                .messageBody(request.getMessage().trim())
                .sentBy(request.getSentBy())
                .build();
        msg = messageRepository.save(msg);
        activityLogger.log(request.getSchoolId(), AdminActivityType.OTHER,
                "Message sent to student",
                st.getFirstName() + " " + st.getLastName(),
                "STUDENT", studentId, request.getSentBy());
        return new AdminStudentProfileDtos.MessageSentResponse(msg.getId(), msg.getMessageBody(), msg.getCreatedAt());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminStudentProfileDtos.ActionResponse flagStudent(
            Long studentId, AdminStudentRequests.FlagStudentRequest request) {
        StudentProfile p = requireProfile(request.getSchoolId(), studentId);
        p.setFlagged(true);
        p.setFlagReason(request.getReason().trim());
        p.setFlaggedAt(LocalDateTime.now());
        p.setFlaggedBy(request.getPerformedBy());
        studentProfileRepository.save(p);
        activityLogger.log(request.getSchoolId(), AdminActivityType.OTHER,
                "Student flagged",
                request.getReason(),
                "STUDENT", studentId, request.getPerformedBy());
        return new AdminStudentProfileDtos.ActionResponse("Student flagged successfully");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminStudentProfileDtos.ActionResponse promoteStudent(Long schoolId, Long studentId, String performedBy) {
        Student st = requireStudent(schoolId, studentId);
        st.setClassName(promoteClass(st.getClassName()));
        studentRepository.save(st);
        activityLogger.log(schoolId, AdminActivityType.OTHER,
                "Student promoted",
                st.getFirstName() + " " + st.getLastName() + " promoted to class " + st.getClassName(),
                "STUDENT", studentId, performedBy);
        return new AdminStudentProfileDtos.ActionResponse("Student promoted to class " + st.getClassName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminStudentProfileDtos.ActionResponse transferStudent(
            Long studentId, AdminStudentRequests.TransferStudentRequest request) {
        Student st = requireStudent(request.getSchoolId(), studentId);
        String from = st.getClassName() + "-" + st.getSection();
        st.setClassName(request.getTargetClassName().trim());
        st.setSection(request.getTargetSection().trim());
        if (request.getTargetRollNo() != null) {
            st.setRollNo(request.getTargetRollNo());
        }
        studentRepository.save(st);
        activityLogger.log(request.getSchoolId(), AdminActivityType.OTHER,
                "Student transferred",
                from + " → " + st.getClassName() + "-" + st.getSection(),
                "STUDENT", studentId, request.getPerformedBy());
        return new AdminStudentProfileDtos.ActionResponse("Student transferred successfully");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminStudentProfileDtos.ActionResponse deactivateStudent(Long schoolId, Long studentId, String performedBy) {
        StudentProfile p = requireProfile(schoolId, studentId);
        p.setAccountStatus(StudentAccountStatus.INACTIVE);
        studentProfileRepository.save(p);
        appUserRepository.findByStudent_Id(studentId).ifPresent(u -> {
            u.setActive(false);
            appUserRepository.save(u);
        });
        activityLogger.log(schoolId, AdminActivityType.OTHER,
                "Student deactivated",
                "Account access disabled for student and linked login",
                "STUDENT", studentId, performedBy);
        return new AdminStudentProfileDtos.ActionResponse("Student account deactivated");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminStudentProfileDtos.ActionResponse reactivateStudent(Long schoolId, Long studentId, String performedBy) {
        StudentProfile p = requireProfile(schoolId, studentId);
        p.setAccountStatus(StudentAccountStatus.ACTIVE);
        studentProfileRepository.save(p);
        appUserRepository.findByStudent_Id(studentId).ifPresent(u -> {
            u.setActive(true);
            appUserRepository.save(u);
        });
        activityLogger.log(schoolId, AdminActivityType.OTHER,
                "Student reactivated",
                "Account access restored",
                "STUDENT", studentId, performedBy);
        return new AdminStudentProfileDtos.ActionResponse("Student account reactivated");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminStudentProfileDtos.ActionResponse quickEdit(
            Long studentId, AdminStudentRequests.QuickEditRequest request) {
        Student st = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            String[] parts = request.getFullName().trim().split("\\s+", 2);
            st.setFirstName(parts[0]);
            st.setLastName(parts.length > 1 ? parts[1] : "");
        }
        if (request.getClassName() != null) {
            st.setClassName(request.getClassName().trim());
        }
        if (request.getSection() != null) {
            st.setSection(request.getSection().trim());
        }
        studentRepository.save(st);
        StudentProfile p = profileOrNull(studentId);
        if (p != null && request.getAdmissionNumber() != null) {
            p.setAdmissionNumber(request.getAdmissionNumber().trim());
            studentProfileRepository.save(p);
        }
        activityLogger.log(st.getSchool().getId(), AdminActivityType.OTHER,
                "Student profile edited",
                st.getFirstName() + " " + st.getLastName(),
                "STUDENT", studentId, request.getPerformedBy());
        return new AdminStudentProfileDtos.ActionResponse("Profile updated successfully");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminStudentProfileDtos.DocumentRow uploadDocument(
            Long schoolId, Long studentId, MultipartFile file,
            AdminStudentRequests.UploadStudentDocumentRequest meta) {
        Student st = requireStudent(schoolId, studentId);
        String url = fileStorageHelper.store(schoolId, "student-documents", file);
        StudentDocumentType type = StudentDocumentType.OTHER;
        if (meta.getDocumentType() != null) {
            try {
                type = StudentDocumentType.valueOf(meta.getDocumentType().trim().toUpperCase());
            } catch (Exception ignored) {
                // keep OTHER
            }
        }
        StudentDocument doc = StudentDocument.builder()
                .student(st)
                .documentType(type)
                .fileName(file.getOriginalFilename())
                .fileUrl(url)
                .uploadedBy(meta.getUploadedBy() != null ? meta.getUploadedBy() : "Admin")
                .build();
        doc = studentDocumentRepository.save(doc);
        return toDocumentRow(doc);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDocument(Long schoolId, Long studentId, Long documentId) {
        requireStudent(schoolId, studentId);
        StudentDocument doc = studentDocumentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found"));
        if (!doc.getStudent().getId().equals(studentId)) {
            throw new ResourceNotFoundException("Document not found for student");
        }
        studentDocumentRepository.delete(doc);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] downloadDocument(Long schoolId, Long studentId, Long documentId) {
        requireStudent(schoolId, studentId);
        StudentDocument doc = studentDocumentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found"));
        if (!doc.getStudent().getId().equals(studentId)) {
            throw new ResourceNotFoundException("Document not found for student");
        }
        try {
            return Files.readAllBytes(Paths.get(doc.getFileUrl()));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to read document file", e);
        }
    }

    @Override
    public AdminStudentProfileDtos.ProfileExportCatalogResponse getExportCatalog() {
        Map<String, List<String>> sections = Map.of(
                "basic", List.of("studentName", "registrationNumber", "rollNumber", "gender", "dob", "bloodGroup",
                        "category", "religion", "nationality", "aadharNumber", "phone", "email", "address", "admissionDate"),
                "parent", List.of("fatherName", "motherName", "parentPhone", "parentEmail", "occupation", "parentAddress"),
                "performance", List.of("averageScore", "averageGrade", "subjectScores"),
                "dailyActivity", List.of("activityLog", "timeUsage"),
                "attendance", List.of("overallPercent", "presentPercent", "absentPercent"),
                "fees", List.of("paymentHistory", "pendingDues"),
                "certificates", List.of("issuedCertificates")
        );
        return new AdminStudentProfileDtos.ProfileExportCatalogResponse(
                sections,
                List.of("TODAY", "YESTERDAY", "LAST_7_DAYS", "THIS_WEEK", "THIS_MONTH", "CUSTOM"),
                List.of("excel", "csv", "pdf")
        );
    }

    @Override
    @Transactional(readOnly = true)
    public AdminStudentExportResult exportProfile(Long studentId, AdminStudentRequests.ProfileExportRequest request) {
        Student st = requireStudent(request.getSchoolId(), studentId);
        StudentProfile p = profileOrNull(studentId);
        List<String> headers = new ArrayList<>();
        List<Map<String, String>> rows = new ArrayList<>();
        Map<String, String> row = new LinkedHashMap<>();

        Set<String> selected = flattenSections(request.getSections());
        if (selected.isEmpty()) {
            selected = Set.of("studentName", "registrationNumber", "className", "email", "phone");
        }

        if (selected.contains("studentName")) {
            row.put("Student Name", st.getFirstName() + " " + st.getLastName());
        }
        if (selected.contains("registrationNumber")) {
            row.put("Registration Number", p != null ? p.getAdmissionNumber() : "");
        }
        if (selected.contains("rollNumber")) {
            row.put("Roll Number", String.valueOf(st.getRollNo()));
        }
        if (selected.contains("gender") && p != null) {
            row.put("Gender", p.getGender());
        }
        if (selected.contains("className")) {
            row.put("Class", st.getClassName() + "-" + st.getSection());
        }
        if (selected.contains("email")) {
            row.put("Email", st.getEmail());
        }
        if (selected.contains("phone")) {
            row.put("Phone", st.getPhone());
        }
        if (selected.contains("averageScore")) {
            Double avg = examResultRepository.getAverageScore(studentId);
            row.put("Average Score", avg != null ? String.format("%.1f%%", avg) : "N/A");
        }
        if (selected.contains("overallPercent")) {
            AdminStudentProfileDtos.AttendanceOverview att = buildAttendance(studentId, null, null);
            row.put("Attendance %", String.format("%.1f%%", att.overallPercent()));
        }
        if (selected.contains("pendingDues")) {
            AdminStudentProfileDtos.FeesTabResponse fees = getFeesTab(request.getSchoolId(), studentId);
            row.put("Pending Dues", String.valueOf(fees.totalPending()));
        }

        headers.addAll(row.keySet());
        rows.add(row);

        String format = request.getFormat() == null ? "pdf" : request.getFormat().trim().toLowerCase();
        String fileName = "student-profile-" + studentId;
        return switch (format) {
            case "csv" -> new AdminStudentExportResult(
                    AdminStudentExportWriter.writeCsv(headers, rows),
                    "text/csv",
                    fileName + ".csv"
            );
            case "excel" -> new AdminStudentExportResult(
                    AdminStudentExportWriter.writeExcel(headers, rows),
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    fileName + ".xlsx"
            );
            default -> new AdminStudentExportResult(
                    AdminStudentExportWriter.writePdf(headers, rows),
                    "application/pdf",
                    fileName + ".pdf"
            );
        };
    }

    // ─── builders ─────────────────────────────────────────────────────────────

    private AdminStudentProfileDtos.ProfileHeader buildHeader(Student st, StudentProfile p) {
        return new AdminStudentProfileDtos.ProfileHeader(
                st.getId(),
                st.getFirstName() + " " + st.getLastName(),
                st.getEmail(),
                p != null ? p.getPhotoUrl() : null,
                p != null ? p.getAdmissionNumber() : "INV-" + st.getId(),
                p != null && p.getAccountStatus() != null ? p.getAccountStatus().name() : "ACTIVE",
                st.getClassName(),
                st.getSection(),
                st.getRollNo(),
                p != null && Boolean.TRUE.equals(p.getFlagged()),
                p != null ? p.getFlagReason() : null
        );
    }

    private AdminStudentProfileDtos.PersonalInfo buildPersonal(Student st, StudentProfile p) {
        return new AdminStudentProfileDtos.PersonalInfo(
                st.getFirstName() + " " + st.getLastName(),
                p != null ? p.getAdmissionNumber() : "",
                st.getRollNo(),
                p != null ? p.getGender() : "",
                p != null && p.getDateOfBirth() != null ? p.getDateOfBirth().format(DATE_FMT) : "",
                p != null ? p.getBloodGroup() : "",
                p != null ? p.getCategory() : "",
                p != null ? p.getReligion() : "",
                p != null ? p.getNationality() : "Indian",
                p != null ? p.getAadharNumber() : "",
                p != null && p.getAccountStatus() != null ? p.getAccountStatus().name() : "ACTIVE",
                p != null && p.getAdmissionDate() != null ? p.getAdmissionDate().format(DATE_FMT) : ""
        );
    }

    private AdminStudentProfileDtos.ContactInfo buildContact(Student st, StudentProfile p) {
        String address = formatAddress(st, p);
        return new AdminStudentProfileDtos.ContactInfo(
                "Father",
                p != null ? p.getFatherName() : st.getParentName(),
                p != null ? p.getMotherName() : "",
                st.getPhone(),
                p != null && p.getGuardianPhone() != null ? p.getGuardianPhone() : st.getPhone(),
                st.getEmail(),
                address
        );
    }

    private AdminStudentProfileDtos.PerformanceOverview buildPerformance(long studentId) {
        Double avg = examResultRepository.getAverageScore(studentId);
        double pct = avg != null ? avg : Double.NaN;
        String grade = letterGrade(pct);
        List<Object[]> subjectRows = examResultRepository.getSubjectPerformance(studentId, null);
        List<AdminStudentProfileDtos.SubjectScoreBar> bars = subjectRows.stream()
                .map(r -> {
                    String subject = (String) r[0];
                    Number obtained = (Number) r[1];
                    Number total = (Number) r[2];
                    double p = total.doubleValue() == 0 ? 0 : obtained.doubleValue() * 100 / total.doubleValue();
                    return new AdminStudentProfileDtos.SubjectScoreBar(subject, p);
                }).toList();
        String rankLabel = Double.isNaN(pct) ? "—" : (pct >= 90 ? "Top 5% in class" : "Class average");
        return new AdminStudentProfileDtos.PerformanceOverview(
                grade, labelForGrade(grade), Double.isNaN(pct) ? 0 : pct, rankLabel, bars
        );
    }

    private AdminStudentProfileDtos.AttendanceOverview buildAttendance(long studentId, LocalDate from, LocalDate to) {
        LocalDate f = from != null ? from : LocalDate.now().minusDays(ATTENDANCE_WINDOW_DAYS);
        LocalDate t = to != null ? to : LocalDate.now();
        long present = attendanceRecordRepository.countByStudent_IdAndStatusAndDateBetween(
                studentId, AttendanceStatus.PRESENT, f, t);
        long late = attendanceRecordRepository.countByStudent_IdAndStatusAndDateBetween(
                studentId, AttendanceStatus.LATE, f, t);
        long absent = attendanceRecordRepository.countByStudent_IdAndStatusAndDateBetween(
                studentId, AttendanceStatus.ABSENT, f, t);
        long leave = attendanceRecordRepository.countByStudent_IdAndStatusAndDateBetween(
                studentId, AttendanceStatus.LEAVE, f, t);
        long denom = present + late + absent + leave;
        double overall = denom == 0 ? 0 : (present + late) * 100.0 / denom;
        double pPct = denom == 0 ? 0 : present * 100.0 / denom;
        double lPct = denom == 0 ? 0 : late * 100.0 / denom;
        double aPct = denom == 0 ? 0 : absent * 100.0 / denom;
        double lvPct = denom == 0 ? 0 : leave * 100.0 / denom;
        return new AdminStudentProfileDtos.AttendanceOverview(
                overall,
                new AdminStudentProfileDtos.AttendanceBreakdown(pPct + lPct, lPct, aPct, lvPct)
        );
    }

    private List<AdminStudentProfileDtos.AttendanceDayRow> loadAttendanceRecords(
            long studentId, LocalDate from, LocalDate to) {
        return attendanceRecordRepository
                .findByStudent_IdAndDateBetweenOrderByDateDesc(studentId, from, to, PageRequest.of(0, 90))
                .getContent().stream()
                .map(a -> new AdminStudentProfileDtos.AttendanceDayRow(
                        a.getDate().toString(),
                        a.getStatus().name(),
                        a.getRemarks() != null ? a.getRemarks() : ""
                )).toList();
    }

    private AdminStudentProfileDtos.AcademicInfo buildAcademic(Student st, StudentProfile p) {
        return new AdminStudentProfileDtos.AcademicInfo(
                p != null ? p.getAdmissionNumber() : "",
                st.getRollNo(),
                st.getClassName(),
                st.getSection(),
                p != null ? p.getAcademicYear() : "",
                p != null && p.getAdmissionDate() != null ? p.getAdmissionDate().format(DATE_FMT) : "",
                p != null && p.getStudentType() != null ? p.getStudentType().name() : "",
                p != null ? p.getPreviousSchoolDetails() : ""
        );
    }

    private AdminStudentProfileDtos.AdditionalInfo buildAdditional(StudentProfile p) {
        if (p == null) {
            return new AdminStudentProfileDtos.AdditionalInfo("", "N/A", "None", "", "");
        }
        String hostel = Boolean.TRUE.equals(p.getHostelRequired()) ? "Hostel" : "None";
        return new AdminStudentProfileDtos.AdditionalInfo(
                p.getTransportRoute() != null ? p.getTransportRoute() : "",
                p.getMedicalConditions() != null ? p.getMedicalConditions() : "N/A",
                hostel,
                p.getGuardianPhone() != null ? p.getGuardianPhone() : "",
                p.getAdditionalNotes() != null ? p.getAdditionalNotes() : ""
        );
    }

    private List<AdminStudentProfileDtos.DocumentRow> listDocuments(Long studentId) {
        return studentDocumentRepository.findByStudent_IdOrderByUploadedAtDesc(studentId).stream()
                .map(this::toDocumentRow)
                .toList();
    }

    private AdminStudentProfileDtos.DocumentRow toDocumentRow(StudentDocument doc) {
        return new AdminStudentProfileDtos.DocumentRow(
                doc.getId(),
                doc.getFileName() != null ? doc.getFileName() : doc.getDocumentType().name(),
                doc.getDocumentType().name(),
                doc.getUploadedBy() != null ? doc.getUploadedBy() : "Admin",
                doc.getUploadedAt().toLocalDate().format(DATE_FMT),
                doc.getFileUrl()
        );
    }

    private AdminStudentProfileDtos.ExamResultRow toExamRow(ExamResult er) {
        double pct = er.getTotalMarks() == 0 ? 0 : er.getObtainedMarks() * 100.0 / er.getTotalMarks();
        return new AdminStudentProfileDtos.ExamResultRow(
                er.getExam().getId(),
                er.getExam().getName(),
                er.getExam().getExamType() != null ? er.getExam().getExamType().name() : "",
                er.getSubject().getName(),
                er.getObtainedMarks(),
                er.getTotalMarks(),
                pct,
                letterGrade(pct),
                er.getExam().getExamDate() != null ? er.getExam().getExamDate().toString() : ""
        );
    }

    // ─── helpers ───────────────────────────────────────────────────────────────

    private Student requireStudent(Long schoolId, Long studentId) {
        Student st = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        if (!st.getSchool().getId().equals(schoolId)) {
            throw new ResourceNotFoundException("Student not found in school");
        }
        return st;
    }

    private StudentProfile requireProfile(Long schoolId, Long studentId) {
        requireStudent(schoolId, studentId);
        return studentProfileRepository.findByStudent_Id(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found"));
    }

    private StudentProfile profileOrNull(Long studentId) {
        return studentProfileRepository.findByStudent_Id(studentId).orElse(null);
    }

    private String formatAddress(Student st, StudentProfile p) {
        if (p != null && p.getAddressLine1() != null) {
            return String.join(", ",
                    filterBlank(p.getAddressLine1(), p.getAddressLine2(), p.getCity(), p.getStateName(), p.getPincode()));
        }
        return st.getAddress();
    }

    private static String[] filterBlank(String... parts) {
        return Arrays.stream(parts).filter(s -> s != null && !s.isBlank()).toArray(String[]::new);
    }

    private static String letterGrade(double pct) {
        if (Double.isNaN(pct)) {
            return "-";
        }
        if (pct >= 97) {
            return "A+";
        }
        if (pct >= 90) {
            return "A";
        }
        if (pct >= 85) {
            return "B+";
        }
        if (pct >= 78) {
            return "B";
        }
        if (pct >= 70) {
            return "C+";
        }
        if (pct >= 60) {
            return "C";
        }
        return "F";
    }

    private static String labelForGrade(String grade) {
        return switch (grade) {
            case "A+", "A" -> "Above average";
            case "B+", "B" -> "Good";
            case "C+", "C" -> "Average";
            default -> "Needs improvement";
        };
    }

    private static String promoteClass(String className) {
        if (className == null || className.isBlank()) {
            return className;
        }
        try {
            return String.valueOf(Integer.parseInt(className.trim()) + 1);
        } catch (NumberFormatException e) {
            return className;
        }
    }

    private static ExamType parseExamType(String examType) {
        if (examType == null || examType.isBlank() || "ALL".equalsIgnoreCase(examType)) {
            return null;
        }
        try {
            return ExamType.valueOf(examType.trim().toUpperCase());
        } catch (Exception e) {
            return null;
        }
    }

    private static boolean inRange(LocalDate date, LocalDate from, LocalDate to) {
        if (date == null) {
            return false;
        }
        if (from != null && date.isBefore(from)) {
            return false;
        }
        return to == null || !date.isAfter(to);
    }

    private record DateRange(LocalDate from, LocalDate to) {
    }

    private static DateRange resolvePeriod(String period, LocalDate from, LocalDate to) {
        if (period == null || period.isBlank()) {
            return null;
        }
        LocalDate today = LocalDate.now();
        return switch (period.trim().toUpperCase()) {
            case "TODAY" -> new DateRange(today, today);
            case "YESTERDAY" -> new DateRange(today.minusDays(1), today.minusDays(1));
            case "LAST_7_DAYS" -> new DateRange(today.minusDays(6), today);
            case "THIS_WEEK" -> new DateRange(today.with(DayOfWeek.MONDAY), today);
            case "THIS_MONTH" -> new DateRange(today.withDayOfMonth(1), today);
            case "CUSTOM" -> {
                if (from == null || to == null) {
                    throw new IllegalArgumentException("Custom range requires from and to dates");
                }
                yield new DateRange(from, to);
            }
            default -> null;
        };
    }

    private static Set<String> flattenSections(Map<String, List<String>> sections) {
        if (sections == null || sections.isEmpty()) {
            return Set.of();
        }
        return sections.values().stream()
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .collect(Collectors.toSet());
    }
}
