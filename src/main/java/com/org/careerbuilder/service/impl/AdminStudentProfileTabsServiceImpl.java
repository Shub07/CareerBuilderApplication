package com.org.careerbuilder.service.impl;

import com.org.careerbuilder.dto.request.AdminStudentRequests;
import com.org.careerbuilder.dto.response.AdminStudentProfileDtos;
import com.org.careerbuilder.exceptions.ResourceNotFoundException;
import com.org.careerbuilder.models.*;
import com.org.careerbuilder.models.enums.AdminActivityType;
import com.org.careerbuilder.models.enums.AssignmentStatus;
import com.org.careerbuilder.models.enums.AttendanceStatus;
import com.org.careerbuilder.models.enums.DailyLifeActivityType;
import com.org.careerbuilder.models.enums.ExamType;
import com.org.careerbuilder.repository.*;
import com.org.careerbuilder.service.AdminStudentManagementService.AdminStudentExportResult;
import com.org.careerbuilder.service.AdminStudentProfileTabsService;
import com.org.careerbuilder.service.support.AdminActivityLogger;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.file.Paths;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminStudentProfileTabsServiceImpl implements AdminStudentProfileTabsService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("hh:mm a");
    private static final List<String> PERFORMANCE_CATEGORIES = List.of(
            "QUIZ", "WEEKLY_TESTS", "INTERNAL", "MIDTERM", "FINAL", "ASSIGNMENTS"
    );

    private final StudentRepository studentRepository;
    private final StudentQuizResultRepository quizResultRepository;
    private final ExamResultRepository examResultRepository;
    private final AssignmentSubmissionRepository assignmentSubmissionRepository;
    private final DailyLifeActivityRepository dailyLifeActivityRepository;
    private final ClassAttendanceLineRepository classAttendanceLineRepository;
    private final ClassSubjectTeacherRepository classSubjectTeacherRepository;
    private final FeeRepository feeRepository;
    private final StudentFeePaymentLinkRepository paymentLinkRepository;
    private final AdminActivityLogger activityLogger;

    @Override
    @Transactional(readOnly = true)
    public AdminStudentProfileDtos.AcademicPerformanceResponse getAcademicPerformance(
            Long schoolId, Long studentId, String category, String subjectSearch) {
        Student st = requireStudent(schoolId, studentId);
        String cat = normalizeCategory(category);
        List<AdminStudentProfileDtos.PerformanceRecordRow> rows = loadPerformanceRows(st, cat, subjectSearch);
        AdminStudentProfileDtos.PerformanceOverview overview = buildPerformanceOverview(studentId);
        return new AdminStudentProfileDtos.AcademicPerformanceResponse(
                overview, cat, PERFORMANCE_CATEGORIES, rows
        );
    }

    @Override
    public AdminStudentProfileDtos.PerformanceExportCatalogResponse getPerformanceExportCatalog() {
        return new AdminStudentProfileDtos.PerformanceExportCatalogResponse(
                PERFORMANCE_CATEGORIES,
                List.of("marks", "percentage", "grade", "rank", "comments"),
                List.of("excel", "csv", "pdf")
        );
    }

    @Override
    @Transactional(readOnly = true)
    public AdminStudentExportResult exportPerformance(
            Long studentId, AdminStudentRequests.PerformanceExportRequest request) {
        requireStudent(request.getSchoolId(), studentId);
        List<String> cats = request.getCategories() != null && !request.getCategories().isEmpty()
                ? request.getCategories()
                : PERFORMANCE_CATEGORIES;
        Student st = studentRepository.findById(studentId).orElseThrow();
        List<Map<String, String>> rows = new ArrayList<>();
        for (String cat : cats) {
            for (AdminStudentProfileDtos.PerformanceRecordRow r : loadPerformanceRows(st, normalizeCategory(cat), request.getSubjectSearch())) {
                Map<String, String> m = new LinkedHashMap<>();
                m.put("Category", cat);
                m.put("Name", r.name());
                m.put("Subject", r.subjectName());
                m.put("Date", r.date());
                if (request.getFields() == null || request.getFields().contains("marks")) {
                    m.put("Marks", r.obtainedMarks() + " / " + r.totalMarks());
                }
                if (request.getFields() == null || request.getFields().contains("percentage")) {
                    m.put("Percentage", r.percent() != null ? String.format("%.1f%%", r.percent()) : "");
                }
                if (request.getFields() == null || request.getFields().contains("grade")) {
                    m.put("Grade", r.grade() != null ? r.grade() : "");
                }
                if (request.getFields() == null || request.getFields().contains("rank")) {
                    m.put("Rank", r.rank() != null ? String.valueOf(r.rank()) : "");
                }
                if (request.getFields() == null || request.getFields().contains("comments")) {
                    m.put("Comments", r.status() != null ? r.status() : "");
                }
                rows.add(m);
            }
        }
        List<String> headers = rows.isEmpty() ? List.of("Category", "Name", "Subject") : new ArrayList<>(rows.get(0).keySet());
        return toExport(request.getFormat(), "performance-export", headers, rows);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminStudentProfileDtos.AssignmentDetailResponse getAssignmentDetail(
            Long schoolId, Long studentId, Long assignmentId) {
        Student st = requireStudent(schoolId, studentId);
        AssignmentSubmission sub = assignmentSubmissionRepository
                .findByAssignment_IdAndStudent_Id(assignmentId, studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment submission not found"));
        Assignment a = sub.getAssignment();
        Faculty teacher = a.getTeacher();

        List<AdminStudentProfileDtos.SubmittedFileRow> files = new ArrayList<>();
        if (sub.getFilePath() != null && !sub.getFilePath().isBlank()) {
            files.add(new AdminStudentProfileDtos.SubmittedFileRow(
                    Paths.get(sub.getFilePath()).getFileName().toString(),
                    fileExtension(sub.getFilePath()),
                    sub.getSubmittedAt() != null ? sub.getSubmittedAt().toLocalDate().format(DATE_FMT) : "",
                    "—",
                    sub.getFilePath()
            ));
        }

        List<AdminStudentProfileDtos.ReferenceFileRow> refs = new ArrayList<>();
        if (a.getAttachmentPath() != null && !a.getAttachmentPath().isBlank()) {
            refs.add(new AdminStudentProfileDtos.ReferenceFileRow(
                    Paths.get(a.getAttachmentPath()).getFileName().toString(),
                    "—",
                    a.getAttachmentPath()
            ));
        }

        String timing = "On Time";
        if (sub.getStatus() == AssignmentStatus.LATE) {
            timing = "Late";
        } else if (sub.getSubmittedAt() != null && sub.getSubmittedAt().toLocalDate().isAfter(a.getDueDate())) {
            timing = "Late";
        }

        AdminStudentProfileDtos.SubmissionDetail submission = new AdminStudentProfileDtos.SubmissionDetail(
                mapAssignmentStatus(sub),
                sub.getSubmittedAt() != null ? sub.getSubmittedAt().toLocalDate().format(DATE_FMT) : null,
                timing,
                sub.getPointsObtained(),
                sub.getPointsTotal() != null ? sub.getPointsTotal() : a.getTotalMarks(),
                sub.getLetterGrade(),
                sub.getTeacherRemarks() != null ? sub.getTeacherRemarks() : sub.getComments()
        );

        return new AdminStudentProfileDtos.AssignmentDetailResponse(
                a.getId(),
                a.getTitle(),
                a.getSubject().getName(),
                st.getFirstName() + " " + st.getLastName(),
                a.getGivenDate() != null ? a.getGivenDate().format(DATE_FMT) : "",
                a.getDueDate().format(DATE_FMT),
                teacher != null ? teacher.getFirstName() + " " + teacher.getLastName() : "Teacher",
                a.getDescription(),
                files,
                submission,
                refs
        );
    }

    @Override
    @Transactional(readOnly = true)
    public AdminStudentProfileDtos.DailyActivityTabResponse getDailyActivityTab(
            Long schoolId, Long studentId, String period, LocalDate from, LocalDate to, String search) {
        requireStudent(schoolId, studentId);
        DateRange range = resolvePeriod(period, from, to);
        LocalDate start = range != null ? range.from() : LocalDate.now();
        LocalDate end = range != null ? range.to() : LocalDate.now();

        List<DailyLifeActivity> activities = dailyLifeActivityRepository
                .findByStudent_IdAndActivityDateBetweenOrderByActivityDateAscStartTimeAsc(studentId, start, end);

        if (search != null && !search.isBlank()) {
            String q = search.trim().toLowerCase();
            activities = activities.stream()
                    .filter(a -> (a.getNote() != null && a.getNote().toLowerCase().contains(q))
                            || a.getActivityType().name().toLowerCase().contains(q))
                    .toList();
        }

        int studyMin = 0, schoolMin = 0, physicalMin = 0, restMin = 0;
        for (DailyLifeActivity a : activities) {
            int mins = a.getDurationMinutes() != null ? a.getDurationMinutes() : 0;
            switch (a.getActivityType()) {
                case STUDY_HOMEWORK -> studyMin += mins;
                case SCHOOL_TIME -> schoolMin += mins;
                case PHYSICAL_ACTIVITY -> physicalMin += mins;
                case REST_TIME -> restMin += mins;
                default -> studyMin += mins / 2;
            }
        }
        int total = studyMin + schoolMin + physicalMin + restMin;
        AdminStudentProfileDtos.TimeAllocationSummary alloc = total == 0
                ? new AdminStudentProfileDtos.TimeAllocationSummary(25, 45, 10, 20)
                : new AdminStudentProfileDtos.TimeAllocationSummary(
                pct(studyMin, total), pct(schoolMin, total), pct(physicalMin, total), pct(restMin, total));

        List<AdminStudentProfileDtos.ActivityMetricCard> cards = List.of(
                new AdminStudentProfileDtos.ActivityMetricCard("Study Time", formatDuration(studyMin), "study"),
                new AdminStudentProfileDtos.ActivityMetricCard("School Time", formatDuration(schoolMin), "school"),
                new AdminStudentProfileDtos.ActivityMetricCard("Physical Activity", formatDuration(physicalMin), "physical"),
                new AdminStudentProfileDtos.ActivityMetricCard("Rest Time", formatDuration(restMin), "rest")
        );

        List<AdminStudentProfileDtos.ActivityTrendDay> trend = buildActivityTrend(studentId, start, end);
        List<AdminStudentProfileDtos.DailyActivityLogRow> log = activities.stream()
                .map(a -> new AdminStudentProfileDtos.DailyActivityLogRow(
                        a.getActivityDate().format(DATE_FMT),
                        labelForActivityType(a.getActivityType()),
                        a.getStartTime().format(TIME_FMT),
                        formatDuration(a.getDurationMinutes() != null ? a.getDurationMinutes() : 0),
                        a.getNote() != null ? a.getNote() : ""
                )).toList();

        return new AdminStudentProfileDtos.DailyActivityTabResponse(alloc, cards, trend, log);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminStudentExportResult exportDailyActivityReport(
            Long studentId, AdminStudentRequests.ReportDownloadRequest request) {
        requireStudent(request.getSchoolId(), studentId);
        DateRange range = resolveReportDuration(request);
        AdminStudentProfileDtos.DailyActivityTabResponse tab = getDailyActivityTab(
                request.getSchoolId(), studentId, "CUSTOM", range.from(), range.to(), null);
        List<String> headers = List.of("Date", "Activity", "Time", "Duration", "Remarks");
        List<Map<String, String>> rows = tab.activityLog().stream()
                .map(r -> {
                    Map<String, String> m = new LinkedHashMap<>();
                    m.put("Date", r.date());
                    m.put("Activity", r.activity());
                    m.put("Time", r.time());
                    m.put("Duration", r.duration());
                    m.put("Remarks", r.remarks());
                    return m;
                }).toList();
        return toExport(request.getFormat(), "daily-activity-report", headers, rows);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminStudentProfileDtos.AttendanceSummaryTabResponse getAttendanceSummaryTab(Long schoolId, Long studentId) {
        Student st = requireStudent(schoolId, studentId);
        List<ClassAttendanceLine> lines = classAttendanceLineRepository.findByStudentIdWithSession(studentId);

        Map<Long, SubjectAttendanceAgg> bySubject = new LinkedHashMap<>();
        for (ClassAttendanceLine line : lines) {
            Long sid = line.getSession().getSubject().getId();
            bySubject.computeIfAbsent(sid, k -> new SubjectAttendanceAgg(line.getSession().getSubject().getName()))
                    .add(line.getStatus());
        }

        if (bySubject.isEmpty()) {
            for (ClassSubjectTeacher cst : classSubjectTeacherRepository.findBySchoolIdAndClassNameAndSectionAndActiveTrue(
                    schoolId, st.getClassName(), st.getSection())) {
                bySubject.putIfAbsent(cst.getSubject().getId(),
                        new SubjectAttendanceAgg(cst.getSubject().getName()));
            }
        }

        List<AdminStudentProfileDtos.SubjectAttendanceRow> subjects = new ArrayList<>();
        int lowCount = 0;
        int totalAbsent = 0;
        double overallSum = 0;
        for (Map.Entry<Long, SubjectAttendanceAgg> e : bySubject.entrySet()) {
            SubjectAttendanceAgg agg = e.getValue();
            double pct = agg.percent();
            String status = statusLabel(pct);
            if (pct < 75) {
                lowCount++;
            }
            totalAbsent += agg.absent;
            overallSum += pct;
            subjects.add(new AdminStudentProfileDtos.SubjectAttendanceRow(
                    e.getKey(),
                    agg.subjectName,
                    agg.conducted,
                    agg.attended,
                    pct,
                    status,
                    pct >= 75 ? "GOOD STANDING" : "NEEDS ATTENTION"
            ));
        }
        double overall = subjects.isEmpty() ? 0 : overallSum / subjects.size();
        return new AdminStudentProfileDtos.AttendanceSummaryTabResponse(
                overall, lowCount, totalAbsent, subjects
        );
    }

    @Override
    @Transactional(readOnly = true)
    public AdminStudentProfileDtos.SubjectAttendanceDetailResponse getSubjectAttendanceDetail(
            Long schoolId, Long studentId, Long subjectId,
            String academicYear, Integer month, String status, String search) {
        requireStudent(schoolId, studentId);
        List<ClassAttendanceLine> lines = classAttendanceLineRepository.findByStudentIdAndSubjectId(studentId, subjectId);
        String subjectName = lines.isEmpty() ? "Subject" : lines.get(0).getSession().getSubject().getName();

        if (month != null) {
            lines = lines.stream().filter(l -> l.getSession().getSessionDate().getMonthValue() == month).toList();
        }
        if (status != null && !status.isBlank() && !"ALL".equalsIgnoreCase(status)) {
            lines = lines.stream().filter(l -> l.getStatus().name().equalsIgnoreCase(status.trim())).toList();
        }
        if (search != null && !search.isBlank()) {
            String q = search.trim().toLowerCase();
            lines = lines.stream()
                    .filter(l -> (l.getRemarks() != null && l.getRemarks().toLowerCase().contains(q))
                            || l.getSession().getSessionDate().toString().contains(q))
                    .toList();
        }

        int present = 0, absent = 0;
        List<AdminStudentProfileDtos.SubjectAttendanceHistoryRow> history = new ArrayList<>();
        for (ClassAttendanceLine line : lines) {
            if (line.getStatus() == AttendanceStatus.PRESENT || line.getStatus() == AttendanceStatus.LATE) {
                present++;
            } else if (line.getStatus() == AttendanceStatus.ABSENT) {
                absent++;
            }
            String markedBy = line.getSession().getFaculty() != null
                    ? line.getSession().getFaculty().getFirstName() + " " + line.getSession().getFaculty().getLastName()
                    : "Staff";
            history.add(new AdminStudentProfileDtos.SubjectAttendanceHistoryRow(
                    line.getSession().getSessionDate().format(DATE_FMT),
                    line.getSession().getSessionDate().getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH),
                    "10:00 AM",
                    line.getStatus().name(),
                    markedBy,
                    line.getRemarks() != null ? line.getRemarks() : "—"
            ));
        }
        int conducted = present + absent;
        double pct = conducted == 0 ? 0 : present * 100.0 / conducted;
        return new AdminStudentProfileDtos.SubjectAttendanceDetailResponse(
                subjectId,
                subjectName,
                pct >= 75 ? "GOOD STANDING" : "NEEDS ATTENTION",
                pct,
                present,
                absent,
                history
        );
    }

    @Override
    @Transactional(readOnly = true)
    public AdminStudentExportResult exportAttendanceReport(
            Long studentId, Long subjectId, AdminStudentRequests.ReportDownloadRequest request) {
        AdminStudentProfileDtos.SubjectAttendanceDetailResponse detail = getSubjectAttendanceDetail(
                request.getSchoolId(), studentId, subjectId, null, null, null, null);
        List<String> headers = List.of("Date", "Day", "Status", "Marked By", "Remarks");
        List<Map<String, String>> rows = detail.history().stream().map(h -> {
            Map<String, String> m = new LinkedHashMap<>();
            m.put("Date", h.date());
            m.put("Day", h.day());
            m.put("Status", h.status());
            m.put("Marked By", h.markedBy());
            m.put("Remarks", h.remarks());
            return m;
        }).toList();
        return toExport(request.getFormat(), "attendance-report", headers, rows);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminStudentProfileDtos.FeesTabDetailResponse getFeesTabDetail(Long schoolId, Long studentId) {
        requireStudent(schoolId, studentId);
        List<Fee> fees = feeRepository.findAllByStudentId(studentId);
        double totalAnnual = fees.stream().mapToDouble(f -> amount(f)).sum();
        double paid = fees.stream().mapToDouble(f -> f.getPaidAmount() != null ? f.getPaidAmount().doubleValue() : 0).sum();
        double pending = fees.stream()
                .filter(f -> f.getStatus() != Fee.FeeStatus.PAID && f.getStatus() != Fee.FeeStatus.CANCELLED)
                .mapToDouble(f -> Math.max(0, amount(f) - (f.getPaidAmount() != null ? f.getPaidAmount().doubleValue() : 0)))
                .sum();
        String nextDue = fees.stream()
                .filter(f -> f.getStatus() == Fee.FeeStatus.PENDING || f.getStatus() == Fee.FeeStatus.OVERDUE)
                .map(Fee::getDueDate)
                .filter(Objects::nonNull)
                .min(LocalDate::compareTo)
                .map(d -> d.format(DATE_FMT))
                .orElse("—");
        String overall = pending > 0 ? "PENDING" : "PAID";

        AdminStudentProfileDtos.FeeSummaryCard summary = new AdminStudentProfileDtos.FeeSummaryCard(
                totalAnnual, paid, pending, nextDue, overall
        );

        List<AdminStudentProfileDtos.FeePaymentRow> history = fees.stream()
                .sorted(Comparator.comparing(Fee::getDueDate).reversed())
                .map(f -> {
                    String dateOrDue = f.getStatus() == Fee.FeeStatus.PAID
                            ? (f.getUpdatedAt() != null ? f.getUpdatedAt().toLocalDate().format(DATE_FMT) : f.getDueDate().format(DATE_FMT))
                            : "Due " + f.getDueDate().format(DATE_FMT);
                    String action = f.getStatus() == Fee.FeeStatus.PAID ? "receipt" : "pay_now";
                    return new AdminStudentProfileDtos.FeePaymentRow(
                            f.getId(),
                            f.getFeeType() + (f.getTerm() != null ? " (" + f.getTerm() + ")" : ""),
                            amount(f),
                            dateOrDue,
                            f.getStatus().name(),
                            action,
                            f.getStatus() == Fee.FeeStatus.PAID
                    );
                }).toList();

        return new AdminStudentProfileDtos.FeesTabDetailResponse(summary, history);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminStudentProfileDtos.PaymentLinkResponse generatePaymentLink(
            Long studentId, AdminStudentRequests.GeneratePaymentLinkRequest request) {
        Student st = requireStudent(request.getSchoolId(), studentId);
        List<Fee> fees = feeRepository.findAllByStudentId(studentId);
        if (request.getFeeIds() != null && !request.getFeeIds().isEmpty()) {
            Set<Long> ids = new HashSet<>(request.getFeeIds());
            fees = fees.stream().filter(f -> ids.contains(f.getId())).toList();
        } else {
            fees = fees.stream()
                    .filter(f -> f.getStatus() != Fee.FeeStatus.PAID && f.getStatus() != Fee.FeeStatus.CANCELLED)
                    .toList();
        }
        BigDecimal total = fees.stream()
                .map(f -> BigDecimal.valueOf(Math.max(0,
                        amount(f) - (f.getPaidAmount() != null ? f.getPaidAmount().doubleValue() : 0))))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        String token = UUID.randomUUID().toString().replace("-", "");
        String feeIdStr = fees.stream().map(f -> String.valueOf(f.getId())).collect(Collectors.joining(","));
        LocalDateTime expires = LocalDateTime.now().plusDays(7);

        StudentFeePaymentLink link = StudentFeePaymentLink.builder()
                .schoolId(request.getSchoolId())
                .student(st)
                .token(token)
                .feeIds(feeIdStr.isEmpty() ? "0" : feeIdStr)
                .amount(total)
                .expiresAt(expires)
                .createdBy(request.getCreatedBy())
                .build();
        paymentLinkRepository.save(link);

        String url = "/pay/fees?token=" + token;
        activityLogger.log(request.getSchoolId(), AdminActivityType.OTHER,
                "Payment link generated",
                "Amount: " + total,
                "STUDENT", studentId, request.getCreatedBy());
        return new AdminStudentProfileDtos.PaymentLinkResponse(url, token, expires.toString());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminStudentProfileDtos.FeeReminderResponse sendFeeReminder(
            Long studentId, AdminStudentRequests.FeeReminderRequest request) {
        requireStudent(request.getSchoolId(), studentId);
        List<Fee> fees = feeRepository.findAllByStudentId(studentId);
        if (request.getFeeIds() != null && !request.getFeeIds().isEmpty()) {
            Set<Long> ids = new HashSet<>(request.getFeeIds());
            fees = fees.stream().filter(f -> ids.contains(f.getId())).toList();
        } else {
            fees = fees.stream()
                    .filter(f -> f.getStatus() == Fee.FeeStatus.PENDING
                            || f.getStatus() == Fee.FeeStatus.OVERDUE
                            || f.getStatus() == Fee.FeeStatus.PARTIAL)
                    .toList();
        }
        for (Fee f : fees) {
            f.setDueReminderSent(true);
            feeRepository.save(f);
        }
        activityLogger.log(request.getSchoolId(), AdminActivityType.OTHER,
                "Fee reminder sent",
                fees.size() + " fee(s) notified",
                "STUDENT", studentId, request.getSentBy());
        return new AdminStudentProfileDtos.FeeReminderResponse(
                "Reminder sent for " + fees.size() + " pending fee(s)", fees.size());
    }

    @Override
    @Transactional(readOnly = true)
    public AdminStudentExportResult downloadFeeStatement(Long schoolId, Long studentId, String format) {
        AdminStudentProfileDtos.FeesTabDetailResponse detail = getFeesTabDetail(schoolId, studentId);
        List<String> headers = List.of("Fee Type", "Amount", "Date/Due", "Status");
        List<Map<String, String>> rows = detail.paymentHistory().stream().map(r -> {
            Map<String, String> m = new LinkedHashMap<>();
            m.put("Fee Type", r.feeType());
            m.put("Amount", String.valueOf(r.amount()));
            m.put("Date/Due", r.dateOrDue());
            m.put("Status", r.status());
            return m;
        }).toList();
        return toExport(format, "fee-statement", headers, rows);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminStudentExportResult downloadFeeReceipt(Long schoolId, Long studentId, Long feeId) {
        requireStudent(schoolId, studentId);
        Fee fee = feeRepository.findById(feeId).orElseThrow(() -> new ResourceNotFoundException("Fee not found"));
        if (!fee.getStudent().getId().equals(studentId)) {
            throw new ResourceNotFoundException("Fee not found for student");
        }
        List<String> headers = List.of("Field", "Value");
        List<Map<String, String>> rows = List.of(
                Map.of("Field", "Fee Type", "Value", fee.getFeeType()),
                Map.of("Field", "Amount", "Value", String.valueOf(amount(fee))),
                Map.of("Field", "Paid", "Value", fee.getPaidAmount() != null ? fee.getPaidAmount().toString() : "0"),
                Map.of("Field", "Status", "Value", fee.getStatus().name()),
                Map.of("Field", "Due Date", "Value", fee.getDueDate().format(DATE_FMT))
        );
        return toExport("pdf", "fee-receipt-" + feeId, headers, rows);
    }

    // ─── performance loaders ───────────────────────────────────────────────────

    private List<AdminStudentProfileDtos.PerformanceRecordRow> loadPerformanceRows(
            Student st, String category, String subjectSearch) {
        Long studentId = st.getId();
        return switch (category) {
            case "QUIZ" -> loadQuizRows(studentId, subjectSearch);
            case "WEEKLY_TESTS" -> loadExamRows(studentId, ExamType.WEEKLY, subjectSearch);
            case "INTERNAL" -> loadExamRows(studentId, ExamType.INTERNAL, subjectSearch);
            case "MIDTERM" -> loadExamRows(studentId, ExamType.MID_TERM, subjectSearch);
            case "FINAL" -> loadExamRows(studentId, ExamType.FINAL, subjectSearch);
            case "ASSIGNMENTS" -> loadAssignmentRows(studentId, subjectSearch);
            default -> loadQuizRows(studentId, subjectSearch);
        };
    }

    private List<AdminStudentProfileDtos.PerformanceRecordRow> loadQuizRows(Long studentId, String subjectSearch) {
        List<StudentQuizResult> quizzes = subjectSearch != null && !subjectSearch.isBlank()
                ? quizResultRepository.findByStudent_IdAndSubject_NameContainingIgnoreCaseOrderByQuizDateDesc(studentId, subjectSearch)
                : quizResultRepository.findByStudent_IdOrderByQuizDateDesc(studentId);
        return quizzes.stream().map(q -> {
            double pct = q.getTotalMarks() == 0 ? 0 : q.getObtainedMarks() * 100.0 / q.getTotalMarks();
            return new AdminStudentProfileDtos.PerformanceRecordRow(
                    q.getId(), q.getTitle(), q.getSubject().getName(),
                    q.getQuizDate().format(DATE_FMT),
                    q.getObtainedMarks(), q.getTotalMarks(), pct,
                    letterGrade(pct), null, null, null, null
            );
        }).toList();
    }

    private List<AdminStudentProfileDtos.PerformanceRecordRow> loadExamRows(
            Long studentId, ExamType type, String subjectSearch) {
        List<ExamResult> results = examResultRepository.findDetailedResults(studentId, type);
        if (subjectSearch != null && !subjectSearch.isBlank()) {
            String q = subjectSearch.trim().toLowerCase();
            results = results.stream().filter(er -> er.getSubject().getName().toLowerCase().contains(q)).toList();
        }
        return results.stream()
                .filter(er -> er.getObtainedMarks() != null)
                .map(er -> {
                    double pct = er.getTotalMarks() == 0 ? 0 : er.getObtainedMarks() * 100.0 / er.getTotalMarks();
                    Integer rank = er.getRank();
                    return new AdminStudentProfileDtos.PerformanceRecordRow(
                            er.getId(),
                            er.getExam().getName(),
                            er.getSubject().getName(),
                            er.getExam().getExamDate() != null ? er.getExam().getExamDate().format(DATE_FMT) : "",
                            er.getObtainedMarks(),
                            er.getTotalMarks(),
                            pct,
                            er.getGrade() != null ? er.getGrade() : letterGrade(pct),
                            rank,
                            rank != null && rank <= 5 ? "UP" : null,
                            null,
                            null
                    );
                }).toList();
    }

    private List<AdminStudentProfileDtos.PerformanceRecordRow> loadAssignmentRows(Long studentId, String subjectSearch) {
        List<AssignmentSubmission> subs = assignmentSubmissionRepository.findWithAssignmentForStudent(studentId);
        if (subjectSearch != null && !subjectSearch.isBlank()) {
            String q = subjectSearch.trim().toLowerCase();
            subs = subs.stream().filter(s -> s.getAssignment().getSubject().getName().toLowerCase().contains(q)).toList();
        }
        return subs.stream().map(s -> {
            Assignment a = s.getAssignment();
            Integer obtained = s.getPointsObtained();
            Integer total = s.getPointsTotal() != null ? s.getPointsTotal() : a.getTotalMarks();
            Double pct = (obtained != null && total != null && total > 0) ? obtained * 100.0 / total : null;
            return new AdminStudentProfileDtos.PerformanceRecordRow(
                    s.getId(),
                    a.getTitle(),
                    a.getSubject().getName(),
                    s.getSubmittedAt() != null ? s.getSubmittedAt().toLocalDate().format(DATE_FMT) : a.getDueDate().format(DATE_FMT),
                    obtained,
                    total,
                    pct,
                    s.getLetterGrade(),
                    null,
                    null,
                    mapAssignmentStatus(s),
                    a.getId()
            );
        }).toList();
    }

    private AdminStudentProfileDtos.PerformanceOverview buildPerformanceOverview(Long studentId) {
        Double avg = examResultRepository.getAverageScore(studentId);
        double pct = avg != null ? avg : Double.NaN;
        String grade = letterGrade(pct);
        List<Object[]> subjectRows = examResultRepository.getSubjectPerformance(studentId, null);
        List<AdminStudentProfileDtos.SubjectScoreBar> bars = subjectRows.stream()
                .map(r -> {
                    Number obtained = (Number) r[1];
                    Number total = (Number) r[2];
                    double p = total.doubleValue() == 0 ? 0 : obtained.doubleValue() * 100 / total.doubleValue();
                    return new AdminStudentProfileDtos.SubjectScoreBar((String) r[0], p);
                }).toList();
        return new AdminStudentProfileDtos.PerformanceOverview(
                grade, labelForGrade(grade), Double.isNaN(pct) ? 0 : pct,
                Double.isNaN(pct) ? "—" : (pct >= 90 ? "Top 5% in class" : "Class average"),
                bars
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

    private static String normalizeCategory(String category) {
        if (category == null || category.isBlank()) {
            return "QUIZ";
        }
        String c = category.trim().toUpperCase().replace(" ", "_").replace("-", "_");
        if ("MIDTERM".equals(c) || "MID_TERM".equals(c)) {
            return "MIDTERM";
        }
        if ("WEEKLY".equals(c) || "WEEKLY_TEST".equals(c)) {
            return "WEEKLY_TESTS";
        }
        return c;
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

    private static String mapAssignmentStatus(AssignmentSubmission s) {
        if (s.getFilePath() == null || s.getFilePath().isBlank()) {
            return "MISSING";
        }
        if (s.getStatus() == AssignmentStatus.LATE) {
            return "LATE";
        }
        if (s.getStatus() == AssignmentStatus.GRADED || s.getStatus() == AssignmentStatus.SUBMITTED) {
            return "SUBMITTED";
        }
        return s.getStatus() != null ? s.getStatus().name() : "PENDING";
    }

    private static double amount(Fee f) {
        if (f.getAmount() != null) {
            return f.getAmount().doubleValue();
        }
        return f.getTotalAmount() != null ? f.getTotalAmount() : 0;
    }

    private static double pct(int part, int total) {
        return total == 0 ? 0 : Math.round(part * 1000.0 / total) / 10.0;
    }

    private static String formatDuration(int minutes) {
        int h = minutes / 60;
        int m = minutes % 60;
        if (h > 0) {
            return h + "h " + (m > 0 ? m + "m" : "");
        }
        return m + "m";
    }

    private static String labelForActivityType(DailyLifeActivityType type) {
        return switch (type) {
            case SCHOOL_TIME -> "School Hours";
            case STUDY_HOMEWORK -> "Study / Homework";
            case PHYSICAL_ACTIVITY -> "Physical Activity";
            case REST_TIME -> "Rest Time";
            default -> type.getLabel();
        };
    }

    private List<AdminStudentProfileDtos.ActivityTrendDay> buildActivityTrend(Long studentId, LocalDate start, LocalDate end) {
        List<AdminStudentProfileDtos.ActivityTrendDay> trend = new ArrayList<>();
        LocalDate d = start;
        while (!d.isAfter(end) && trend.size() < 7) {
            List<DailyLifeActivity> dayActs = dailyLifeActivityRepository
                    .findByStudent_IdAndActivityDateOrderByStartTimeAsc(studentId, d);
            int study = 0, school = 0, physical = 0, rest = 0;
            for (DailyLifeActivity a : dayActs) {
                int mins = a.getDurationMinutes() != null ? a.getDurationMinutes() : 0;
                double hrs = mins / 60.0;
                switch (a.getActivityType()) {
                    case STUDY_HOMEWORK -> study += hrs;
                    case SCHOOL_TIME -> school += hrs;
                    case PHYSICAL_ACTIVITY -> physical += hrs;
                    case REST_TIME -> rest += hrs;
                    default -> study += hrs / 2;
                }
            }
            trend.add(new AdminStudentProfileDtos.ActivityTrendDay(
                    d.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH),
                    study, school, physical, rest
            ));
            d = d.plusDays(1);
        }
        return trend;
    }

    private static String statusLabel(double pct) {
        if (pct >= 90) {
            return "Excellent";
        }
        if (pct >= 75) {
            return "Good";
        }
        if (pct >= 60) {
            return "Average";
        }
        return "Critical";
    }

    private static String fileExtension(String path) {
        int i = path.lastIndexOf('.');
        return i >= 0 ? path.substring(i + 1).toUpperCase() : "FILE";
    }

    private AdminStudentExportResult toExport(String format, String baseName, List<String> headers, List<Map<String, String>> rows) {
        String fmt = format == null ? "pdf" : format.trim().toLowerCase();
        return switch (fmt) {
            case "csv" -> new AdminStudentExportResult(
                    AdminStudentExportWriter.writeCsv(headers, rows), "text/csv", baseName + ".csv");
            case "excel" -> new AdminStudentExportResult(
                    AdminStudentExportWriter.writeExcel(headers, rows),
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    baseName + ".xlsx");
            default -> new AdminStudentExportResult(
                    AdminStudentExportWriter.writePdf(headers, rows), "application/pdf", baseName + ".pdf");
        };
    }

    private record DateRange(LocalDate from, LocalDate to) {
    }

    private static DateRange resolvePeriod(String period, LocalDate from, LocalDate to) {
        if (period == null || period.isBlank()) {
            return new DateRange(LocalDate.now(), LocalDate.now());
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
            default -> new DateRange(today, today);
        };
    }

    private static DateRange resolveReportDuration(AdminStudentRequests.ReportDownloadRequest request) {
        if ("FULL_ACADEMIC_YEAR".equalsIgnoreCase(request.getDuration())) {
            LocalDate today = LocalDate.now();
            return new DateRange(today.minusMonths(10), today);
        }
        if (request.getFrom() != null && request.getTo() != null) {
            return new DateRange(request.getFrom(), request.getTo());
        }
        return new DateRange(LocalDate.now().minusMonths(1), LocalDate.now());
    }

    private static class SubjectAttendanceAgg {
        final String subjectName;
        int conducted;
        int attended;
        int absent;

        SubjectAttendanceAgg(String subjectName) {
            this.subjectName = subjectName;
        }

        void add(AttendanceStatus status) {
            conducted++;
            if (status == AttendanceStatus.PRESENT || status == AttendanceStatus.LATE) {
                attended++;
            } else if (status == AttendanceStatus.ABSENT) {
                absent++;
            }
        }

        double percent() {
            return conducted == 0 ? 0 : attended * 100.0 / conducted;
        }
    }
}
