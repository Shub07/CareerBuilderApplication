package com.org.careerbuilder.service.support;

import com.org.careerbuilder.dto.request.AdminExamRequests;
import com.org.careerbuilder.dto.response.AdminExamDtos;
import com.org.careerbuilder.exceptions.ResourceNotFoundException;
import com.org.careerbuilder.models.*;
import com.org.careerbuilder.models.enums.AdminActivityType;
import com.org.careerbuilder.models.enums.MarksSubmissionStatus;
import com.org.careerbuilder.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AdminExamMarksResultsSupport {

    private static final int DEFAULT_MAX_MARKS = 100;
    private static final DateTimeFormatter AUDIT_FMT =
            DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a", Locale.ENGLISH);

    private final ExamRepository examRepository;
    private final ExamScheduleRepository scheduleRepository;
    private final ExamRegistrationRepository registrationRepository;
    private final ExamResultRepository examResultRepository;
    private final ExamMarksSubmissionRepository marksSubmissionRepository;
    private final ClassSubjectTeacherRepository classSubjectTeacherRepository;
    private final AdminActivityLogRepository activityLogRepository;
    private final AdminActivityLogger activityLogger;

    @Transactional(readOnly = true)
    public AdminExamDtos.MarksMonitoringResponse marksMonitoring(Long schoolId, Long examId) {
        Exam exam = requireExam(schoolId, examId);
        ensureMarkRowsExist(exam);
        long totalStudents = registrationRepository.countByExam_IdAndSchoolId(examId, schoolId);
        List<ExamSchedule> schedules = scheduleRepository
                .findByExam_IdAndSchoolIdOrderByScheduledDateAscStartTimeAsc(examId, schoolId);

        Map<Long, ExamSchedule> bySubject = new LinkedHashMap<>();
        for (ExamSchedule s : schedules) {
            bySubject.putIfAbsent(s.getSubject().getId(), s);
        }

        List<AdminExamDtos.MarksMonitoringRow> rows = new ArrayList<>();
        for (ExamSchedule schedule : bySubject.values()) {
            Long subjectId = schedule.getSubject().getId();
            ExamMarksSubmission submission = getOrCreateSubmission(exam, subjectId, schoolId, totalStudents);
            syncSubmissionStatus(submission, examId, subjectId, totalStudents);

            long entered = examResultRepository.countByExam_IdAndSubject_IdAndObtainedMarksIsNotNull(examId, subjectId);
            MarksSubmissionStatus status = submission.getStatus();
            Faculty teacher = resolveTeacher(schoolId, exam.getClassName(), exam.getSection(), subjectId);

            rows.add(new AdminExamDtos.MarksMonitoringRow(
                    subjectId,
                    schedule.getSubject().getName(),
                    teacher != null ? teacher.getId() : null,
                    teacher != null ? teacher.getFirstName() + " " + teacher.getLastName() : "—",
                    status.name(),
                    status.getLabel(),
                    entered,
                    totalStudents,
                    entered + "/" + totalStudents,
                    true,
                    status == MarksSubmissionStatus.SUBMITTED));
        }
        return new AdminExamDtos.MarksMonitoringResponse(rows);
    }

    @Transactional(readOnly = true)
    public AdminExamDtos.SubjectMarksDetailResponse subjectMarksDetail(
            Long schoolId, Long examId, Long subjectId) {
        Exam exam = requireExam(schoolId, examId);
        ensureMarkRowsExist(exam);
        long totalStudents = registrationRepository.countByExam_IdAndSchoolId(examId, schoolId);
        ExamMarksSubmission submission = getOrCreateSubmission(exam, subjectId, schoolId, totalStudents);
        syncSubmissionStatus(submission, examId, subjectId, totalStudents);

        List<ExamResult> results = examResultRepository.findByExamIdAndSubjectId(examId, subjectId);
        Faculty teacher = resolveTeacher(schoolId, exam.getClassName(), exam.getSection(), subjectId);
        int maxMarks = submission.getMaxMarks();
        int passCut = passingMarks(maxMarks);

        double avg = results.stream()
                .filter(r -> r.getObtainedMarks() != null)
                .mapToInt(ExamResult::getObtainedMarks)
                .average()
                .orElse(0.0);
        long pass = results.stream()
                .filter(r -> r.getObtainedMarks() != null && r.getObtainedMarks() >= passCut)
                .count();
        long fail = results.stream()
                .filter(r -> r.getObtainedMarks() != null && r.getObtainedMarks() < passCut)
                .count();

        String classSection = exam.getClassName()
                + (exam.getSection() != null && !exam.getSection().isBlank() ? " [" + exam.getSection() + "]" : "");

        return new AdminExamDtos.SubjectMarksDetailResponse(
                exam.getId(),
                exam.getName(),
                subjectId,
                results.isEmpty() ? subjectName(subjectId, examId, schoolId) : results.get(0).getSubject().getName(),
                classSection,
                maxMarks,
                teacher != null ? teacher.getId() : null,
                teacher != null ? teacher.getFirstName() + " " + teacher.getLastName() : "—",
                submission.getStatus().name(),
                submission.getStatus().getLabel(),
                Math.round(avg * 10.0) / 10.0,
                pass,
                fail,
                totalStudents,
                buildAuditTrail(submission),
                results.stream().map(r -> toStudentMarkRow(r, maxMarks, passCut)).toList());
    }

    @Transactional(rollbackFor = Exception.class)
    public AdminExamDtos.ApproveMarksResponse approveSubjectMarks(
            Long examId, Long subjectId, AdminExamRequests.ApproveSubjectMarksRequest request) {
        if (!request.isConfirmCorrect()) {
            throw new IllegalArgumentException("Confirmation is required to approve marks");
        }
        Exam exam = requireExam(request.getSchoolId(), examId);
        ExamMarksSubmission submission = marksSubmissionRepository
                .findByExam_IdAndSubject_IdAndSchoolId(examId, subjectId, request.getSchoolId())
                .orElseThrow(() -> new ResourceNotFoundException("Marks submission not found"));
        if (submission.getStatus() != MarksSubmissionStatus.SUBMITTED) {
            throw new IllegalStateException("Only submitted marks can be approved");
        }

        LocalDateTime now = LocalDateTime.now();
        submission.setStatus(MarksSubmissionStatus.APPROVED);
        submission.setApprovedAt(now);
        submission.setApprovedBy(request.getPerformedBy());
        marksSubmissionRepository.save(submission);

        List<ExamResult> results = examResultRepository.findByExamIdAndSubjectId(examId, subjectId);
        for (ExamResult r : results) {
            r.setAdminVerified(true);
            if (r.getGrade() == null && r.getObtainedMarks() != null) {
                r.setGrade(letterGrade(r.getObtainedMarks(), r.getTotalMarks()));
            }
            examResultRepository.save(r);
        }

        activityLogger.log(request.getSchoolId(), AdminActivityType.EXAM_MARKS_APPROVED,
                "Marks approved: " + submission.getSubject().getName(),
                exam.getName(),
                "EXAM", examId, request.getPerformedBy());

        return new AdminExamDtos.ApproveMarksResponse(
                true, "Marks approved successfully", MarksSubmissionStatus.APPROVED.name());
    }

    @Transactional(rollbackFor = Exception.class)
    public AdminExamDtos.ApproveMarksResponse rejectSubjectMarks(
            Long examId, Long subjectId, AdminExamRequests.RejectSubjectMarksRequest request) {
        Exam exam = requireExam(request.getSchoolId(), examId);
        ExamMarksSubmission submission = marksSubmissionRepository
                .findByExam_IdAndSubject_IdAndSchoolId(examId, subjectId, request.getSchoolId())
                .orElseThrow(() -> new ResourceNotFoundException("Marks submission not found"));

        submission.setStatus(MarksSubmissionStatus.REJECTED);
        submission.setRejectedAt(LocalDateTime.now());
        submission.setRejectionReason(request.getReason());
        marksSubmissionRepository.save(submission);

        List<ExamResult> results = examResultRepository.findByExamIdAndSubjectId(examId, subjectId);
        for (ExamResult r : results) {
            r.setMarksLocked(false);
            r.setMarksLockedAt(null);
            r.setAdminVerified(false);
            examResultRepository.save(r);
        }

        activityLogger.log(request.getSchoolId(), AdminActivityType.EXAM_MARKS_REJECTED,
                "Marks rejected: " + submission.getSubject().getName(),
                request.getReason(),
                "EXAM", examId, request.getPerformedBy());

        return new AdminExamDtos.ApproveMarksResponse(
                true, "Marks returned to teacher for correction", MarksSubmissionStatus.REJECTED.name());
    }

    @Transactional(rollbackFor = Exception.class)
    public AdminExamDtos.ActionResponse saveMarksDraft(
            Long examId, Long subjectId, AdminExamRequests.SaveMarksDraftRequest request) {
        requireExam(request.getSchoolId(), examId);
        if (request.getUpdates() == null || request.getUpdates().isEmpty()) {
            return new AdminExamDtos.ActionResponse(true, "No changes");
        }
        for (AdminExamRequests.MarkVerificationUpdate update : request.getUpdates()) {
            ExamResult result = examResultRepository.findById(update.getResultId())
                    .orElseThrow(() -> new ResourceNotFoundException("Result not found"));
            if (!result.getExam().getId().equals(examId) || !result.getSubject().getId().equals(subjectId)) {
                throw new IllegalArgumentException("Result does not match exam/subject");
            }
            if (update.getObtainedMarks() != null) {
                int m = update.getObtainedMarks();
                if (m < 0 || m > result.getTotalMarks()) {
                    throw new IllegalArgumentException("Marks out of range");
                }
                result.setObtainedMarks(m);
                result.setGrade(letterGrade(m, result.getTotalMarks()));
            }
            if (update.getAdminVerified() != null) {
                result.setAdminVerified(update.getAdminVerified());
            }
            examResultRepository.save(result);
        }
        return new AdminExamDtos.ActionResponse(true, "Draft saved");
    }

    @Transactional(readOnly = true)
    public AdminExamDtos.ExamResultsSummaryResponse resultsSummary(Long schoolId, Long examId) {
        Exam exam = requireExam(schoolId, examId);
        List<AdminExamDtos.FinalResultRow> all = computeFinalResults(examId, schoolId);
        if (all.isEmpty()) {
            return new AdminExamDtos.ExamResultsSummaryResponse(0, 0, 0, exam.isResultsPublished(), 0);
        }
        long pass = all.stream().filter(r -> "PASS".equals(r.status())).count();
        long total = all.size();
        double passPct = total == 0 ? 0 : (pass * 100.0 / total);
        double avg = all.stream()
                .mapToDouble(r -> r.totalMax() == 0 ? 0 : r.totalObtained() * 100.0 / r.totalMax())
                .average()
                .orElse(0);
        return new AdminExamDtos.ExamResultsSummaryResponse(
                Math.round(passPct * 10.0) / 10.0,
                Math.round((100.0 - passPct) * 10.0) / 10.0,
                Math.round(avg * 10.0) / 10.0,
                exam.isResultsPublished(),
                total);
    }

    @Transactional(readOnly = true)
    public AdminExamDtos.ExamResultsListResponse listFinalResults(Long schoolId, Long examId, int page, int size) {
        requireExam(schoolId, examId);
        List<AdminExamDtos.FinalResultRow> all = computeFinalResults(examId, schoolId);
        int safePage = Math.max(0, page);
        int safeSize = Math.min(Math.max(size, 1), 100);
        int from = safePage * safeSize;
        int to = Math.min(from + safeSize, all.size());
        List<AdminExamDtos.FinalResultRow> pageRows = from >= all.size() ? List.of() : all.subList(from, to);
        int totalPages = safeSize == 0 ? 0 : (int) Math.ceil((double) all.size() / safeSize);
        String showing = all.isEmpty() ? "Showing 0 results"
                : "Showing " + (from + 1) + "-" + to + " of " + all.size();
        return new AdminExamDtos.ExamResultsListResponse(
                pageRows, safePage, safeSize, all.size(), totalPages, showing);
    }

    @Transactional(readOnly = true)
    public byte[] printResults(Long schoolId, Long examId, AdminExamRequests.PrintResultsRequest request) {
        Exam exam = requireExam(schoolId, examId);
        List<AdminExamDtos.FinalResultRow> all = computeFinalResults(examId, schoolId);
        if ("SELECTED".equalsIgnoreCase(request.getStudentScope()) && request.getStudentIds() != null) {
            Set<Long> ids = new HashSet<>(request.getStudentIds());
            all = all.stream().filter(r -> ids.contains(r.studentId())).toList();
        }
        return AdminExamResultsPdfWriter.write(exam.getName(), request.getFormat(), all);
    }

    @Transactional(readOnly = true)
    public AdminExamDtos.ActivityLogResponse enhancedActivityLog(Long schoolId, Long examId, int page, int size) {
        requireExam(schoolId, examId);
        int safePage = Math.max(0, page);
        int safeSize = Math.min(Math.max(size, 1), 50);
        Page<AdminActivityLog> result = activityLogRepository.searchStudentActivityLog(
                schoolId, "EXAM", examId, null, null, PageRequest.of(safePage, safeSize));
        List<AdminExamDtos.ActivityLogRow> rows = result.getContent().stream()
                .map(l -> new AdminExamDtos.ActivityLogRow(
                        l.getId(),
                        l.getActivityType().name(),
                        formatActivityTitle(l),
                        l.getDescription(),
                        formatPerformer(l.getPerformedBy()),
                        l.getCreatedAt() != null ? l.getCreatedAt().format(AUDIT_FMT) : null))
                .toList();
        int totalPages = (int) Math.ceil((double) result.getTotalElements() / safeSize);
        return new AdminExamDtos.ActivityLogResponse(rows, safePage, safeSize, result.getTotalElements(), totalPages);
    }

    // ─── internals ──────────────────────────────────────────────────────────────

    private Exam requireExam(Long schoolId, Long examId) {
        return examRepository.findByIdAndSchoolIdAndDeletedFalse(examId, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found"));
    }

    private void ensureMarkRowsExist(Exam exam) {
        Long examId = exam.getId();
        Long schoolId = exam.getSchoolId();
        List<ExamRegistration> regs = registrationRepository.findByExam_IdAndSchoolId(examId, schoolId);
        List<ExamSchedule> schedules = scheduleRepository
                .findByExam_IdAndSchoolIdOrderByScheduledDateAscStartTimeAsc(examId, schoolId);
        Set<Long> subjectIds = schedules.stream()
                .map(s -> s.getSubject().getId())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        for (ExamRegistration reg : regs) {
            for (Long subjectId : subjectIds) {
                boolean exists = examResultRepository.findByExamIdAndSubjectId(examId, subjectId).stream()
                        .anyMatch(r -> r.getStudent().getId().equals(reg.getStudent().getId()));
                if (!exists) {
                    Subject subject = schedules.stream()
                            .filter(s -> s.getSubject().getId().equals(subjectId))
                            .findFirst()
                            .map(ExamSchedule::getSubject)
                            .orElseThrow();
                    examResultRepository.save(ExamResult.builder()
                            .student(reg.getStudent())
                            .exam(exam)
                            .subject(subject)
                            .totalMarks(DEFAULT_MAX_MARKS)
                            .marksLocked(false)
                            .adminVerified(false)
                            .build());
                }
            }
        }
    }

    private ExamMarksSubmission getOrCreateSubmission(
            Exam exam, Long subjectId, Long schoolId, long totalStudents) {
        return marksSubmissionRepository.findByExam_IdAndSubject_IdAndSchoolId(exam.getId(), subjectId, schoolId)
                .orElseGet(() -> {
                    Subject subject = scheduleRepository
                            .findByExam_IdAndSchoolIdOrderByScheduledDateAscStartTimeAsc(exam.getId(), schoolId)
                            .stream()
                            .filter(s -> s.getSubject().getId().equals(subjectId))
                            .findFirst()
                            .map(ExamSchedule::getSubject)
                            .orElseThrow(() -> new ResourceNotFoundException("Subject not in exam timetable"));
                    Faculty teacher = resolveTeacher(schoolId, exam.getClassName(), exam.getSection(), subjectId);
                    return marksSubmissionRepository.save(ExamMarksSubmission.builder()
                            .exam(exam)
                            .schoolId(schoolId)
                            .subject(subject)
                            .teacher(teacher)
                            .maxMarks(DEFAULT_MAX_MARKS)
                            .status(MarksSubmissionStatus.PENDING)
                            .build());
                });
    }

    private void syncSubmissionStatus(
            ExamMarksSubmission submission, Long examId, Long subjectId, long totalStudents) {
        if (submission.getStatus() == MarksSubmissionStatus.APPROVED
                || submission.getStatus() == MarksSubmissionStatus.REJECTED) {
            return;
        }
        long entered = examResultRepository.countByExam_IdAndSubject_IdAndObtainedMarksIsNotNull(examId, subjectId);
        long locked = examResultRepository.countByExam_IdAndSubject_IdAndMarksLockedTrue(examId, subjectId);
        if (totalStudents > 0 && locked >= totalStudents && entered >= totalStudents) {
            if (submission.getStatus() != MarksSubmissionStatus.SUBMITTED) {
                submission.setStatus(MarksSubmissionStatus.SUBMITTED);
                submission.setSubmittedAt(LocalDateTime.now());
                marksSubmissionRepository.save(submission);
            }
        } else if (entered > 0) {
            submission.setStatus(MarksSubmissionStatus.PENDING);
            marksSubmissionRepository.save(submission);
        }
    }

    private Faculty resolveTeacher(Long schoolId, String className, String section, Long subjectId) {
        String sec = section != null && !section.isBlank() ? section : "A";
        List<ClassSubjectTeacher> assignments = classSubjectTeacherRepository
                .findBySchoolIdAndClassNameAndSectionAndSubject_IdAndActiveTrue(
                        schoolId, className, sec, subjectId);
        if (assignments.isEmpty()) {
            assignments = classSubjectTeacherRepository.findActiveByClass(schoolId, className).stream()
                    .filter(c -> c.getSubject().getId().equals(subjectId))
                    .toList();
        }
        return assignments.isEmpty() ? null : assignments.get(0).getFaculty();
    }

    private String subjectName(Long subjectId, Long examId, Long schoolId) {
        return scheduleRepository.findByExam_IdAndSchoolIdOrderByScheduledDateAscStartTimeAsc(examId, schoolId)
                .stream()
                .filter(s -> s.getSubject().getId().equals(subjectId))
                .findFirst()
                .map(s -> s.getSubject().getName())
                .orElse("Subject");
    }

    private List<AdminExamDtos.MarksAuditEvent> buildAuditTrail(ExamMarksSubmission submission) {
        List<AdminExamDtos.MarksAuditEvent> events = new ArrayList<>();
        if (submission.getSubmittedAt() != null) {
            events.add(new AdminExamDtos.MarksAuditEvent(
                    "Submitted by Teacher",
                    submission.getSubmittedAt().format(AUDIT_FMT),
                    true));
        }
        boolean approved = submission.getStatus() == MarksSubmissionStatus.APPROVED;
        events.add(new AdminExamDtos.MarksAuditEvent(
                approved ? "Approved by Admin" : "Waiting for Approval",
                approved && submission.getApprovedAt() != null
                        ? submission.getApprovedAt().format(AUDIT_FMT)
                        : "Pending",
                approved));
        return events;
    }

    private AdminExamDtos.StudentMarkRow toStudentMarkRow(ExamResult r, int maxMarks, int passCut) {
        Student st = r.getStudent();
        Integer obtained = r.getObtainedMarks();
        boolean failing = obtained != null && obtained < passCut;
        String grade = r.getGrade() != null ? r.getGrade() : (obtained != null ? letterGrade(obtained, r.getTotalMarks()) : "—");
        boolean verified = r.isAdminVerified();
        return new AdminExamDtos.StudentMarkRow(
                r.getId(),
                st.getId(),
                st.getFirstName() + " " + st.getLastName(),
                rollId(st),
                obtained,
                r.getTotalMarks(),
                obtained != null ? obtained + "/" + r.getTotalMarks() : "—",
                grade,
                verified ? "VERIFIED" : "PENDING",
                verified ? "Verified" : "Pending",
                failing);
    }

    private List<AdminExamDtos.FinalResultRow> computeFinalResults(Long examId, Long schoolId) {
        List<ExamRegistration> regs = registrationRepository.findByExam_IdAndSchoolId(examId, schoolId);
        List<ExamSchedule> schedules = scheduleRepository
                .findByExam_IdAndSchoolIdOrderByScheduledDateAscStartTimeAsc(examId, schoolId);
        Set<Long> subjectIds = schedules.stream().map(s -> s.getSubject().getId()).collect(Collectors.toSet());
        if (subjectIds.isEmpty()) {
            return List.of();
        }

        List<StudentAggregate> aggregates = new ArrayList<>();
        for (ExamRegistration reg : regs) {
            Student st = reg.getStudent();
            int obtained = 0;
            int max = 0;
            boolean hasAllMarks = true;
            for (Long subjectId : subjectIds) {
                Optional<ExamResult> result = examResultRepository.findByExamIdAndSubjectId(examId, subjectId).stream()
                        .filter(r -> r.getStudent().getId().equals(st.getId()))
                        .findFirst();
                if (result.isEmpty() || result.get().getObtainedMarks() == null) {
                    hasAllMarks = false;
                    continue;
                }
                obtained += result.get().getObtainedMarks();
                max += result.get().getTotalMarks();
            }
            if (!hasAllMarks || max == 0) {
                continue;
            }
            double pct = obtained * 100.0 / max;
            aggregates.add(new StudentAggregate(st, obtained, max, pct));
        }

        aggregates.sort(Comparator.comparingInt(StudentAggregate::obtained).reversed());
        List<AdminExamDtos.FinalResultRow> rows = new ArrayList<>();
        int rank = 1;
        for (int i = 0; i < aggregates.size(); i++) {
            if (i > 0 && aggregates.get(i).obtained() < aggregates.get(i - 1).obtained()) {
                rank = i + 1;
            }
            StudentAggregate a = aggregates.get(i);
            String grade = letterGrade(a.obtained(), a.max());
            boolean pass = a.pct() >= 40.0;
            rows.add(new AdminExamDtos.FinalResultRow(
                    a.student().getId(),
                    a.student().getFirstName() + " " + a.student().getLastName(),
                    a.obtained(),
                    a.max(),
                    a.obtained() + "/" + a.max(),
                    grade,
                    rank,
                    pass ? "PASS" : "FAIL",
                    pass ? "Pass" : "Fail"));
        }
        return rows;
    }

    private static int passingMarks(int totalMarks) {
        return Math.max(1, (int) Math.ceil(totalMarks * 0.4));
    }

    private static String letterGrade(int obtained, int total) {
        if (total <= 0) {
            return "—";
        }
        double pct = obtained * 100.0 / total;
        if (pct >= 90) return "A+";
        if (pct >= 80) return "A";
        if (pct >= 70) return "B+";
        if (pct >= 60) return "B";
        if (pct >= 50) return "C";
        if (pct >= 40) return "D";
        return "F";
    }

    private static String rollId(Student st) {
        return st.getClassName() + "-" + String.format("%02d", st.getRollNo());
    }

    private static String formatActivityTitle(AdminActivityLog log) {
        return switch (log.getActivityType()) {
            case EXAM_CREATED -> "Exam Created";
            case EXAM_UPDATED -> "Exam Updated";
            case EXAM_STUDENTS_ASSIGNED -> "Students Assigned";
            case EXAM_HALL_TICKETS_GENERATED -> "Hall Tickets Generated";
            case EXAM_VENUE_ALLOCATED -> "Venue Assigned";
            case EXAM_MARKS_APPROVED -> "Marks Approved";
            case EXAM_MARKS_REJECTED -> "Marks Rejected";
            case EXAM_RESULTS_PUBLISHED -> "Results Published";
            default -> log.getTitle() != null ? log.getTitle() : log.getActivityType().name();
        };
    }

    private static String formatPerformer(String performedBy) {
        if (performedBy == null || performedBy.isBlank()) {
            return "School Admin";
        }
        if (performedBy.contains("@")) {
            return "School Admin";
        }
        return performedBy;
    }

    private record StudentAggregate(Student student, int obtained, int max, double pct) {
    }
}
