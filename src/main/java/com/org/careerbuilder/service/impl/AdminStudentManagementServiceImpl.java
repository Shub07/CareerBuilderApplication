package com.org.careerbuilder.service.impl;

import com.org.careerbuilder.dto.request.AdminStudentRequests;
import com.org.careerbuilder.dto.response.AdminOperationResponses;
import com.org.careerbuilder.exceptions.ResourceNotFoundException;
import com.org.careerbuilder.models.*;
import com.org.careerbuilder.models.enums.AdminActivityType;
import com.org.careerbuilder.models.enums.AttendanceStatus;
import com.org.careerbuilder.models.enums.StudentAccountStatus;
import com.org.careerbuilder.models.enums.StudentPerformanceLevel;
import com.org.careerbuilder.models.Fee.FeeStatus;
import com.org.careerbuilder.repository.*;
import com.org.careerbuilder.service.AdminStudentManagementService;
import com.org.careerbuilder.service.support.AdminActivityLogger;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Month;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminStudentManagementServiceImpl implements AdminStudentManagementService {

    private static final int ATTENDANCE_WINDOW_DAYS = 120;
    private static final String PREF_KEY = "students_table";

    private final SchoolRepository schoolRepository;
    private final StudentRepository studentRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final FeeRepository feeRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final ExamResultRepository examResultRepository;
    private final AdminStudentTablePreferenceRepository tablePreferenceRepository;
    private final AdminActivityLogger activityLogger;

    @Override
    @Transactional(readOnly = true)
    public AdminOperationResponses.StudentStats getStats(Long schoolId, String academicYear) {
        assertSchool(schoolId);
        String year = normalizeAcademicYear(academicYear);
        if (year == null) {
            long total = studentRepository.countBySchool_Id(schoolId);
            long active = studentProfileRepository.countByStudent_School_IdAndAccountStatus(schoolId, StudentAccountStatus.ACTIVE);
            long inactive = studentProfileRepository.countByStudent_School_IdAndAccountStatus(schoolId, StudentAccountStatus.INACTIVE);
            long newAdmissions = studentProfileRepository.countByStudent_School_IdAndAccountStatus(schoolId, StudentAccountStatus.NEW_ADMISSION);
            return new AdminOperationResponses.StudentStats(total, active, inactive, newAdmissions);
        }
        List<StudentView> scoped = loadViews(schoolId, null).stream()
                .filter(v -> matchesAcademicYear(v, year))
                .toList();
        long total = scoped.size();
        long active = scoped.stream().filter(v -> StudentAccountStatus.ACTIVE.name().equals(v.accountStatus())).count();
        long inactive = scoped.stream().filter(v -> StudentAccountStatus.INACTIVE.name().equals(v.accountStatus())).count();
        long newAdmissions = scoped.stream().filter(v -> StudentAccountStatus.NEW_ADMISSION.name().equals(v.accountStatus())).count();
        return new AdminOperationResponses.StudentStats(total, active, inactive, newAdmissions);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminOperationResponses.AcademicYearsResponse getAcademicYears(Long schoolId) {
        assertSchool(schoolId);
        List<String> years = new ArrayList<>(studentProfileRepository.findDistinctAcademicYears(schoolId));
        String current = currentAcademicYear();
        if (!years.contains(current)) {
            years.add(0, current);
        }
        return new AdminOperationResponses.AcademicYearsResponse(years, current);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminOperationResponses.StudentFilterOptionsResponse getFilterOptions(Long schoolId) {
        assertSchool(schoolId);
        Map<String, List<String>> classSections = new TreeMap<>();
        for (Object[] row : studentRepository.findDistinctClassSections(schoolId)) {
            String cn = (String) row[0];
            String sec = (String) row[1];
            classSections.computeIfAbsent(cn, k -> new ArrayList<>()).add(sec);
        }
        List<AdminOperationResponses.ClassSectionOption> classes = classSections.entrySet().stream()
                .map(e -> new AdminOperationResponses.ClassSectionOption(e.getKey(), e.getValue()))
                .toList();
        return new AdminOperationResponses.StudentFilterOptionsResponse(
                classes,
                List.of("Male", "Female", "Other"),
                Arrays.stream(StudentAccountStatus.values()).map(Enum::name).toList(),
                List.of("PAID", "PENDING", "PARTIAL", "OVERDUE")
        );
    }

    @Override
    public AdminOperationResponses.AdvancedFilterOptionsResponse getAdvancedFilterOptions() {
        return new AdminOperationResponses.AdvancedFilterOptionsResponse(
                defaultColumnCatalog(),
                Arrays.stream(StudentPerformanceLevel.values()).map(Enum::name).toList(),
                List.of("PAID", "PENDING", "OVERDUE"),
                ATTENDANCE_WINDOW_DAYS
        );
    }

    @Override
    @Transactional(readOnly = true)
    public AdminOperationResponses.TablePreferencesResponse getTablePreferences(
            Long schoolId, Long adminUserId, String adminEmail) {
        assertSchool(schoolId);
        List<String> visible = resolveVisibleColumns(schoolId, adminUserId, adminEmail);
        return new AdminOperationResponses.TablePreferencesResponse(schoolId, visible, defaultColumnCatalog());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminOperationResponses.TablePreferencesResponse saveTablePreferences(
            AdminStudentRequests.SaveTablePreferencesRequest request) {
        assertSchool(request.getSchoolId());
        validateColumnKeys(request.getVisibleColumns());
        String joined = String.join(",", request.getVisibleColumns());

        AdminStudentTablePreference pref = null;
        if (request.getAdminUserId() != null) {
            pref = tablePreferenceRepository
                    .findBySchoolIdAndAdminUserIdAndPreferenceKey(request.getSchoolId(), request.getAdminUserId(), PREF_KEY)
                    .orElse(null);
        } else if (request.getAdminEmail() != null && !request.getAdminEmail().isBlank()) {
            pref = tablePreferenceRepository
                    .findBySchoolIdAndAdminEmailIgnoreCaseAndPreferenceKey(
                            request.getSchoolId(), request.getAdminEmail().trim(), PREF_KEY)
                    .orElse(null);
        }

        if (pref == null) {
            pref = AdminStudentTablePreference.builder()
                    .schoolId(request.getSchoolId())
                    .adminUserId(request.getAdminUserId())
                    .adminEmail(request.getAdminEmail())
                    .preferenceKey(PREF_KEY)
                    .visibleColumns(joined)
                    .build();
        } else {
            pref.setVisibleColumns(joined);
        }
        tablePreferenceRepository.save(pref);
        return new AdminOperationResponses.TablePreferencesResponse(
                request.getSchoolId(), request.getVisibleColumns(), defaultColumnCatalog());
    }

    @Override
    @Transactional(readOnly = true)
    public AdminOperationResponses.StudentSectionResponse listStudents(AdminStudentRequests.StudentListQuery query) {
        assertSchool(query.getSchoolId());
        List<StudentView> filtered = loadViews(query.getSchoolId(), null).stream()
                .filter(v -> matchesAcademicYear(v, normalizeAcademicYear(query.getAcademicYear())))
                .filter(v -> matchesText(v, query.getQuery()))
                .filter(v -> matchesIgnoreCase(v.student().getClassName(), query.getClassName()))
                .filter(v -> matchesIgnoreCase(v.student().getSection(), query.getSection()))
                .filter(v -> matchesIgnoreCase(v.profile() != null ? v.profile().getGender() : null, query.getGender()))
                .filter(v -> matchesIgnoreCase(v.accountStatus(), query.getStatus()))
                .filter(v -> matchesFeeStatus(v, query.getFeeStatus(), query.getFeeStatuses()))
                .filter(v -> matchesAdmissionDate(v, query.getAdmissionDateFrom(), query.getAdmissionDateTo()))
                .filter(v -> matchesPerformanceLevel(v, query.getPerformanceLevel()))
                .filter(v -> matchesAttendanceRange(v, query.getMinAttendance(), query.getMaxAttendance()))
                .toList();

        int safePage = Math.max(query.getPage(), 0);
        int safeSize = Math.min(Math.max(query.getSize(), 1), 100);
        int from = safePage * safeSize;
        int to = Math.min(from + safeSize, filtered.size());
        List<StudentView> pageItems = from >= filtered.size() ? List.of() : filtered.subList(from, to);

        List<AdminOperationResponses.StudentRow> rows = pageItems.stream().map(this::toRow).toList();
        int totalPages = filtered.isEmpty() ? 0 : (int) Math.ceil((double) filtered.size() / safeSize);
        List<String> visibleColumns = resolveVisibleColumns(
                query.getSchoolId(), query.getAdminUserId(), query.getAdminEmail());

        return new AdminOperationResponses.StudentSectionResponse(
                getStats(query.getSchoolId(), query.getAcademicYear()),
                rows,
                safePage,
                safeSize,
                filtered.size(),
                totalPages,
                visibleColumns
        );
    }

    @Override
    @Transactional(readOnly = true)
    public AdminOperationResponses.StudentDetailResponse getStudentDetail(Long schoolId, Long studentId) {
        StudentView view = loadView(schoolId, studentId);
        Student st = view.student();
        StudentProfile p = view.profile();
        return new AdminOperationResponses.StudentDetailResponse(
                st.getId(),
                st.getFirstName() + " " + st.getLastName(),
                st.getEmail(),
                st.getPhone(),
                p != null ? p.getAdmissionNumber() : "INV-" + st.getId(),
                st.getRollNo(),
                st.getClassName(),
                st.getSection(),
                p != null ? p.getGender() : null,
                new AdminOperationResponses.LocalDateFields(
                        p != null && p.getDateOfBirth() != null ? p.getDateOfBirth().toString() : null,
                        p != null && p.getAdmissionDate() != null ? p.getAdmissionDate().toString() : null
                ),
                new AdminOperationResponses.ParentInfo(
                        p != null ? p.getFatherName() : st.getParentName(),
                        p != null ? p.getFatherPhone() : st.getPhone(),
                        p != null ? p.getMotherName() : null,
                        p != null ? p.getMotherPhone() : null,
                        p != null ? p.getFatherOccupation() : null
                ),
                new AdminOperationResponses.ContactInfo(
                        p != null ? p.getAddressLine1() : st.getAddress(),
                        p != null ? p.getAddressLine2() : null,
                        p != null ? p.getCity() : null,
                        p != null ? p.getStateName() : null,
                        p != null ? p.getPincode() : null,
                        p != null ? p.getCountry() : null
                ),
                view.feeStatus(),
                view.accountStatus(),
                p != null ? p.getAcademicYear() : null
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminOperationResponses.StudentDetailResponse updateStudent(
            Long schoolId, Long studentId, AdminStudentRequests.UpdateStudentRequest request) {
        Student st = requireStudent(schoolId, studentId);
        if (request.getFirstName() != null) {
            st.setFirstName(request.getFirstName().trim());
        }
        if (request.getLastName() != null) {
            st.setLastName(request.getLastName().trim());
        }
        if (request.getEmail() != null) {
            st.setEmail(request.getEmail().trim());
        }
        if (request.getPhone() != null) {
            st.setPhone(request.getPhone().trim());
        }
        if (request.getAddress() != null) {
            st.setAddress(request.getAddress().trim());
        }
        if (request.getParentName() != null) {
            st.setParentName(request.getParentName().trim());
        }
        if (request.getClassName() != null) {
            st.setClassName(request.getClassName().trim());
        }
        if (request.getSection() != null) {
            st.setSection(request.getSection().trim());
        }
        if (request.getRollNo() != null) {
            st.setRollNo(request.getRollNo());
        }
        studentRepository.save(st);

        StudentProfile profile = studentProfileRepository.findByStudent_Id(studentId).orElse(null);
        if (profile != null) {
            if (request.getGender() != null) {
                profile.setGender(request.getGender().trim());
            }
            if (request.getPhotoUrl() != null) {
                profile.setPhotoUrl(request.getPhotoUrl().trim());
            }
            if (request.getAcademicYear() != null) {
                profile.setAcademicYear(request.getAcademicYear().trim());
            }
            if (request.getStatus() != null) {
                profile.setAccountStatus(parseAccountStatus(request.getStatus()));
            }
            studentProfileRepository.save(profile);
        }

        activityLogger.log(schoolId, AdminActivityType.OTHER,
                "Student updated",
                st.getFirstName() + " " + st.getLastName(),
                "STUDENT", studentId, request.getPerformedBy());
        return getStudentDetail(schoolId, studentId);
    }

    @Override
    public AdminOperationResponses.ExportFieldCatalogResponse getExportFieldCatalog() {
        Map<String, List<String>> catalog = Map.of(
                "basic", List.of("studentName", "registrationNumber", "rollNumber", "gender", "dob", "phone", "email", "address", "admissionDate"),
                "parent", List.of("fatherName", "motherName", "parentPhone", "parentEmail", "occupation", "parentAddress"),
                "academic", List.of("className", "section", "rollNumber", "academicYear", "feeStatus", "status")
        );
        return new AdminOperationResponses.ExportFieldCatalogResponse(catalog);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminStudentExportResult exportStudents(AdminStudentRequests.ExportRequest request) {
        assertSchool(request.getSchoolId());
        AdminStudentRequests.StudentListQuery listQuery = AdminStudentRequests.StudentListQuery.builder()
                .schoolId(request.getSchoolId())
                .query(request.getQuery())
                .className(request.getClassName())
                .section(request.getSection())
                .gender(request.getGender())
                .status(request.getStatus())
                .feeStatus(request.getFeeStatus())
                .page(0)
                .size(Integer.MAX_VALUE)
                .build();

        List<String> selectedFields = resolveExportFields(request.getFields());
        List<String> headers = selectedFields.stream()
                .map(f -> AdminStudentExportWriter.fieldLabels().getOrDefault(f, f))
                .toList();

        List<StudentView> views;
        if (request.getStudentIds() != null && !request.getStudentIds().isEmpty()) {
            views = loadViews(request.getSchoolId(), request.getStudentIds());
        } else {
            views = loadViews(request.getSchoolId(), null).stream()
                    .filter(v -> matchesText(v, listQuery.getQuery()))
                    .filter(v -> matchesIgnoreCase(v.student().getClassName(), listQuery.getClassName()))
                    .filter(v -> matchesIgnoreCase(v.student().getSection(), listQuery.getSection()))
                    .filter(v -> matchesIgnoreCase(v.profile() != null ? v.profile().getGender() : null, listQuery.getGender()))
                    .filter(v -> matchesIgnoreCase(v.accountStatus(), listQuery.getStatus()))
                    .filter(v -> matchesFeeStatus(v, listQuery.getFeeStatus(), null))
                    .toList();
        }

        List<Map<String, String>> rows = views.stream()
                .map(v -> toExportMap(v, selectedFields, headers))
                .toList();

        String format = request.getFormat() == null ? "excel" : request.getFormat().trim().toLowerCase();
        return switch (format) {
            case "csv" -> new AdminStudentExportResult(
                    AdminStudentExportWriter.writeCsv(headers, rows),
                    "text/csv",
                    "students-export.csv"
            );
            case "pdf" -> new AdminStudentExportResult(
                    AdminStudentExportWriter.writePdf(headers, rows),
                    "application/pdf",
                    "students-export.pdf"
            );
            default -> new AdminStudentExportResult(
                    AdminStudentExportWriter.writeExcel(headers, rows),
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    "students-export.xlsx"
            );
        };
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminOperationResponses.BulkActionResponse bulkChangeStatus(AdminStudentRequests.BulkStatusRequest request) {
        StudentAccountStatus status = parseAccountStatus(request.getStatus());
        int count = 0;
        for (Long id : request.getStudentIds()) {
            StudentProfile profile = requireProfile(request.getSchoolId(), id);
            profile.setAccountStatus(status);
            studentProfileRepository.save(profile);
            count++;
        }
        activityLogger.log(request.getSchoolId(), AdminActivityType.OTHER,
                "Bulk student status update",
                count + " students set to " + status.name(),
                "STUDENT", null, request.getPerformedBy());
        return new AdminOperationResponses.BulkActionResponse(count, count + " student(s) updated");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminOperationResponses.BulkActionResponse bulkPromote(AdminStudentRequests.BulkPromoteRequest request) {
        int count = 0;
        for (Long id : request.getStudentIds()) {
            Student st = requireStudent(request.getSchoolId(), id);
            st.setClassName(promoteClassName(st.getClassName()));
            studentRepository.save(st);
            count++;
        }
        activityLogger.log(request.getSchoolId(), AdminActivityType.OTHER,
                "Bulk student promotion",
                count + " students promoted",
                "STUDENT", null, request.getPerformedBy());
        return new AdminOperationResponses.BulkActionResponse(count, count + " student(s) promoted");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminOperationResponses.BulkActionResponse bulkDelete(AdminStudentRequests.BulkDeleteRequest request) {
        int count = 0;
        for (Long id : request.getStudentIds()) {
            Student st = requireStudent(request.getSchoolId(), id);
            studentRepository.delete(st);
            count++;
        }
        activityLogger.log(request.getSchoolId(), AdminActivityType.OTHER,
                "Bulk student delete",
                count + " students removed",
                "STUDENT", null, request.getPerformedBy());
        return new AdminOperationResponses.BulkActionResponse(count, count + " student(s) deleted");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminOperationResponses.BulkActionResponse updateStudentStatus(
            Long schoolId, Long studentId, AdminStudentRequests.UpdateStatusRequest request) {
        StudentProfile profile = requireProfile(schoolId, studentId);
        profile.setAccountStatus(parseAccountStatus(request.getStatus()));
        studentProfileRepository.save(profile);
        return new AdminOperationResponses.BulkActionResponse(1, "Student status updated");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteStudent(Long schoolId, Long studentId, String performedBy) {
        Student st = requireStudent(schoolId, studentId);
        studentRepository.delete(st);
        activityLogger.log(schoolId, AdminActivityType.OTHER,
                "Student deleted",
                st.getFirstName() + " " + st.getLastName(),
                "STUDENT", studentId, performedBy);
    }

    private List<StudentView> loadViews(Long schoolId, List<Long> studentIds) {
        List<Student> students = studentIds != null && !studentIds.isEmpty()
                ? studentRepository.findBySchool_IdAndIdIn(schoolId, studentIds)
                : studentRepository.findAllBySchool_IdOrderByClass(schoolId);

        Map<Long, StudentProfile> profiles = studentProfileRepository.findByStudent_IdIn(
                students.stream().map(Student::getId).toList()
        ).stream().collect(Collectors.toMap(p -> p.getStudent().getId(), p -> p, (a, b) -> a));

        LocalDate attFrom = LocalDate.now().minusDays(ATTENDANCE_WINDOW_DAYS);
        LocalDate attTo = LocalDate.now();

        List<StudentView> views = new ArrayList<>();
        for (Student st : students) {
            StudentProfile profile = profiles.get(st.getId());
            Fee fee = feeRepository.findTopByStudent_IdOrderByDueDateDesc(st.getId()).orElse(null);
            String feeStatus = fee != null && fee.getStatus() != null ? fee.getStatus().name() : "PENDING";
            String accountStatus = profile != null && profile.getAccountStatus() != null
                    ? profile.getAccountStatus().name()
                    : StudentAccountStatus.ACTIVE.name();
            double attendancePct = attendancePercent(st.getId(), attFrom, attTo);
            double examAvg = averageExamPercent(st.getId());
            String performanceLevel = classifyPerformance(examAvg).name();
            views.add(new StudentView(st, profile, feeStatus, accountStatus, attendancePct, performanceLevel));
        }
        return views;
    }

    private StudentView loadView(Long schoolId, Long studentId) {
        return loadViews(schoolId, List.of(studentId)).stream()
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
    }

    private AdminOperationResponses.StudentRow toRow(StudentView v) {
        Student st = v.student();
        StudentProfile p = v.profile();
        return new AdminOperationResponses.StudentRow(
                st.getId(),
                st.getFirstName() + " " + st.getLastName(),
                st.getEmail(),
                p != null ? p.getPhotoUrl() : null,
                p != null ? p.getAdmissionNumber() : "INV-" + st.getId(),
                st.getRollNo(),
                st.getClassName(),
                st.getSection(),
                p != null ? p.getGender() : "NA",
                st.getParentName(),
                st.getPhone(),
                st.getAddress(),
                v.feeStatus(),
                v.accountStatus(),
                Double.isNaN(v.attendancePercent()) ? null : Math.round(v.attendancePercent() * 10) / 10.0,
                v.performanceLevel()
        );
    }

    private Map<String, String> toExportMap(StudentView v, List<String> fields, List<String> headers) {
        Map<String, String> values = buildExportValues(v);
        Map<String, String> row = new LinkedHashMap<>();
        for (int i = 0; i < fields.size(); i++) {
            row.put(headers.get(i), values.getOrDefault(fields.get(i), ""));
        }
        return row;
    }

    private Map<String, String> buildExportValues(StudentView v) {
        Student st = v.student();
        StudentProfile p = v.profile();
        Map<String, String> m = new LinkedHashMap<>();
        m.put("studentName", st.getFirstName() + " " + st.getLastName());
        m.put("registrationNumber", p != null ? p.getAdmissionNumber() : "");
        m.put("rollNumber", st.getRollNo() != null ? st.getRollNo().toString() : "");
        m.put("gender", p != null ? p.getGender() : "");
        m.put("dob", p != null && p.getDateOfBirth() != null ? p.getDateOfBirth().toString() : "");
        m.put("phone", st.getPhone());
        m.put("email", st.getEmail());
        m.put("address", st.getAddress());
        m.put("admissionDate", p != null && p.getAdmissionDate() != null ? p.getAdmissionDate().toString() : "");
        m.put("fatherName", p != null ? p.getFatherName() : st.getParentName());
        m.put("motherName", p != null ? p.getMotherName() : "");
        m.put("parentPhone", p != null && p.getFatherPhone() != null ? p.getFatherPhone() : st.getPhone());
        m.put("parentEmail", st.getEmail());
        m.put("occupation", p != null ? p.getFatherOccupation() : "");
        m.put("parentAddress", st.getAddress());
        m.put("className", st.getClassName());
        m.put("section", st.getSection());
        m.put("academicYear", p != null ? p.getAcademicYear() : "");
        m.put("feeStatus", v.feeStatus());
        m.put("status", v.accountStatus());
        return m;
    }

    private List<String> resolveExportFields(Map<String, List<String>> fields) {
        if (fields == null || fields.isEmpty()) {
            return List.of("studentName", "registrationNumber", "rollNumber", "className", "section", "feeStatus", "status");
        }
        LinkedHashSet<String> ordered = new LinkedHashSet<>();
        for (List<String> group : fields.values()) {
            if (group != null) {
                ordered.addAll(group);
            }
        }
        return new ArrayList<>(ordered);
    }

    private List<String> resolveVisibleColumns(Long schoolId, Long adminUserId, String adminEmail) {
        if (adminUserId != null) {
            Optional<AdminStudentTablePreference> pref = tablePreferenceRepository
                    .findBySchoolIdAndAdminUserIdAndPreferenceKey(schoolId, adminUserId, PREF_KEY);
            if (pref.isPresent()) {
                return Arrays.asList(pref.get().getVisibleColumns().split(","));
            }
        }
        if (adminEmail != null && !adminEmail.isBlank()) {
            Optional<AdminStudentTablePreference> pref = tablePreferenceRepository
                    .findBySchoolIdAndAdminEmailIgnoreCaseAndPreferenceKey(schoolId, adminEmail.trim(), PREF_KEY);
            if (pref.isPresent()) {
                return Arrays.asList(pref.get().getVisibleColumns().split(","));
            }
        }
        return defaultVisibleColumnKeys();
    }

    private void validateColumnKeys(List<String> columns) {
        Set<String> allowed = defaultColumnCatalog().stream()
                .map(AdminOperationResponses.TableColumnOption::key)
                .collect(Collectors.toSet());
        for (String col : columns) {
            if (!allowed.contains(col)) {
                throw new IllegalArgumentException("Unknown column: " + col);
            }
        }
    }

    private static List<AdminOperationResponses.TableColumnOption> defaultColumnCatalog() {
        return List.of(
                col("studentName", "Student Name", true),
                col("registrationNumber", "Registration Number", true),
                col("rollNumber", "Roll Number", true),
                col("className", "Class", true),
                col("section", "Section", true),
                col("gender", "Gender", true),
                col("parentName", "Parent Name", true),
                col("parentPhone", "Phone", true),
                col("email", "Email", false),
                col("address", "Address", false),
                col("feeStatus", "Fee Status", true),
                col("status", "Status", true)
        );
    }

    private static AdminOperationResponses.TableColumnOption col(String key, String label, boolean visible) {
        return new AdminOperationResponses.TableColumnOption(key, label, visible);
    }

    private static List<String> defaultVisibleColumnKeys() {
        return defaultColumnCatalog().stream()
                .filter(AdminOperationResponses.TableColumnOption::defaultVisible)
                .map(AdminOperationResponses.TableColumnOption::key)
                .toList();
    }

    private double attendancePercent(long studentId, LocalDate from, LocalDate to) {
        long present = attendanceRecordRepository.countByStudent_IdAndStatusAndDateBetween(
                studentId, AttendanceStatus.PRESENT, from, to);
        long late = attendanceRecordRepository.countByStudent_IdAndStatusAndDateBetween(
                studentId, AttendanceStatus.LATE, from, to);
        long absent = attendanceRecordRepository.countByStudent_IdAndStatusAndDateBetween(
                studentId, AttendanceStatus.ABSENT, from, to);
        long leave = attendanceRecordRepository.countByStudent_IdAndStatusAndDateBetween(
                studentId, AttendanceStatus.LEAVE, from, to);
        long denom = present + late + absent + leave;
        if (denom == 0) {
            return Double.NaN;
        }
        return (present + late) * 100.0 / denom;
    }

    private double averageExamPercent(long studentId) {
        Double avg = examResultRepository.getAverageScore(studentId);
        return avg == null ? Double.NaN : avg;
    }

    private static StudentPerformanceLevel classifyPerformance(double examAvg) {
        if (Double.isNaN(examAvg)) {
            return StudentPerformanceLevel.AVERAGE;
        }
        if (examAvg >= 85) {
            return StudentPerformanceLevel.EXCELLENT;
        }
        if (examAvg >= 70) {
            return StudentPerformanceLevel.GOOD;
        }
        if (examAvg >= 50) {
            return StudentPerformanceLevel.AVERAGE;
        }
        return StudentPerformanceLevel.AT_RISK;
    }

    private boolean matchesAcademicYear(StudentView v, String year) {
        if (year == null) {
            return true;
        }
        StudentProfile p = v.profile();
        return p != null && year.equalsIgnoreCase(p.getAcademicYear());
    }

    private boolean matchesAdmissionDate(StudentView v, LocalDate from, LocalDate to) {
        if (from == null && to == null) {
            return true;
        }
        StudentProfile p = v.profile();
        if (p == null || p.getAdmissionDate() == null) {
            return false;
        }
        LocalDate d = p.getAdmissionDate();
        if (from != null && d.isBefore(from)) {
            return false;
        }
        return to == null || !d.isAfter(to);
    }

    private boolean matchesFeeStatus(StudentView v, String single, List<String> multi) {
        if (multi != null && !multi.isEmpty()) {
            String status = v.feeStatus();
            return multi.stream().anyMatch(f -> matchesIgnoreCase(status, f));
        }
        return matchesIgnoreCase(v.feeStatus(), single);
    }

    private boolean matchesPerformanceLevel(StudentView v, String level) {
        if (level == null || level.isBlank() || "ALL".equalsIgnoreCase(level)) {
            return true;
        }
        return matchesIgnoreCase(v.performanceLevel(), level);
    }

    private boolean matchesAttendanceRange(StudentView v, Integer min, Integer max) {
        if (min == null && max == null) {
            return true;
        }
        if (Double.isNaN(v.attendancePercent())) {
            return false;
        }
        double pct = v.attendancePercent();
        if (min != null && pct < min) {
            return false;
        }
        return max == null || pct <= max;
    }

    private Student requireStudent(Long schoolId, Long studentId) {
        Student st = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        if (!st.getSchool().getId().equals(schoolId)) {
            throw new ResourceNotFoundException("Student not found in school");
        }
        return st;
    }

    private StudentProfile requireProfile(Long schoolId, Long studentId) {
        Student st = requireStudent(schoolId, studentId);
        return studentProfileRepository.findByStudent_Id(st.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found"));
    }

    private void assertSchool(Long schoolId) {
        if (!schoolRepository.existsById(schoolId)) {
            throw new ResourceNotFoundException("School not found: " + schoolId);
        }
    }

    private boolean matchesText(StudentView v, String query) {
        if (query == null || query.isBlank()) {
            return true;
        }
        String q = query.trim().toLowerCase();
        Student st = v.student();
        StudentProfile p = v.profile();
        String hay = (st.getFirstName() + " " + st.getLastName() + " " + st.getEmail() + " " + st.getRollNo()
                + " " + (p != null ? p.getAdmissionNumber() : "")).toLowerCase();
        return hay.contains(q);
    }

    private boolean matchesIgnoreCase(String value, String filter) {
        if (filter == null || filter.isBlank() || "ALL".equalsIgnoreCase(filter)) {
            return true;
        }
        if (value == null) {
            return false;
        }
        String f = filter.trim();
        String v = value.trim();
        if (v.equalsIgnoreCase(f)) {
            return true;
        }
        String normalizedFilter = normalizeToken(f);
        String normalizedValue = normalizeToken(v);
        return normalizedValue.equals(normalizedFilter);
    }

    private String normalizeToken(String token) {
        return token.replace(" ", "_").replace("-", "_").toUpperCase();
    }

    private StudentAccountStatus parseAccountStatus(String status) {
        try {
            return StudentAccountStatus.valueOf(status.trim().toUpperCase());
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid status. Use ACTIVE, INACTIVE, NEW_ADMISSION");
        }
    }

    private String promoteClassName(String className) {
        if (className == null || className.isBlank()) {
            return className;
        }
        try {
            int grade = Integer.parseInt(className.trim());
            return String.valueOf(grade + 1);
        } catch (NumberFormatException e) {
            return className;
        }
    }

    private String normalizeAcademicYear(String academicYear) {
        if (academicYear == null || academicYear.isBlank()) {
            return null;
        }
        return academicYear.trim().replace("AY", "").replace("ay", "").trim();
    }

    private String currentAcademicYear() {
        int y = LocalDate.now().getYear();
        if (LocalDate.now().getMonth().getValue() >= Month.JUNE.getValue()) {
            return y + "-" + (y + 1);
        }
        return (y - 1) + "-" + y;
    }

    private record StudentView(
            Student student,
            StudentProfile profile,
            String feeStatus,
            String accountStatus,
            double attendancePercent,
            String performanceLevel
    ) {
    }
}
