package com.org.careerbuilder.service.impl;

import com.org.careerbuilder.dto.request.AdminExamRequests;
import com.org.careerbuilder.dto.response.AdminExamDtos;
import com.org.careerbuilder.exceptions.ResourceNotFoundException;
import com.org.careerbuilder.models.AdminActivityLog;
import com.org.careerbuilder.models.Exam;
import com.org.careerbuilder.models.ExamRegistration;
import com.org.careerbuilder.models.ExamResult;
import com.org.careerbuilder.models.ExamSchedule;
import com.org.careerbuilder.models.ExamVenue;
import com.org.careerbuilder.models.ExamVenueAllocation;
import com.org.careerbuilder.models.Faculty;
import com.org.careerbuilder.models.School;
import com.org.careerbuilder.models.NotificationOutbox;
import com.org.careerbuilder.models.Student;
import com.org.careerbuilder.models.StudentProfile;
import com.org.careerbuilder.models.Subject;
import com.org.careerbuilder.models.enums.AdminActivityType;
import com.org.careerbuilder.models.enums.ExamAssignmentMethod;
import com.org.careerbuilder.models.enums.ExamRegistrationStatus;
import com.org.careerbuilder.models.enums.HallTicketStudentFilter;
import com.org.careerbuilder.models.enums.ExamStatus;
import com.org.careerbuilder.models.enums.ExamType;
import com.org.careerbuilder.models.enums.NotificationChannel;
import com.org.careerbuilder.models.enums.NotificationStatus;
import com.org.careerbuilder.repository.AdminActivityLogRepository;
import com.org.careerbuilder.repository.ExamRegistrationRepository;
import com.org.careerbuilder.repository.ExamRepository;
import com.org.careerbuilder.repository.ExamResultRepository;
import com.org.careerbuilder.repository.ExamScheduleRepository;
import com.org.careerbuilder.repository.ExamVenueAllocationRepository;
import com.org.careerbuilder.repository.ExamVenueRepository;
import com.org.careerbuilder.repository.FacultyRepository;
import com.org.careerbuilder.repository.SchoolRepository;
import com.org.careerbuilder.repository.NotificationOutboxRepository;
import com.org.careerbuilder.repository.StudentProfileRepository;
import com.org.careerbuilder.repository.StudentRepository;
import com.org.careerbuilder.repository.SubjectRepository;
import com.org.careerbuilder.service.AdminExamService;
import com.org.careerbuilder.service.support.AdminActivityLogger;
import com.org.careerbuilder.service.support.AdminExamMarksResultsSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminExamServiceImpl implements AdminExamService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH);
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH);

    private static final int ASSIGN_BATCH_SIZE = 250;

    private final ExamRepository examRepository;
    private final ExamScheduleRepository scheduleRepository;
    private final ExamRegistrationRepository registrationRepository;
    private final ExamVenueRepository venueRepository;
    private final ExamVenueAllocationRepository venueAllocationRepository;
    private final ExamResultRepository examResultRepository;
    private final SchoolRepository schoolRepository;
    private final StudentRepository studentRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final FacultyRepository facultyRepository;
    private final SubjectRepository subjectRepository;
    private final AdminActivityLogRepository activityLogRepository;
    private final NotificationOutboxRepository notificationOutboxRepository;
    private final AdminActivityLogger activityLogger;
    private final AdminExamMarksResultsSupport marksResultsSupport;

    @Override
    @Transactional(readOnly = true)
    public AdminExamDtos.ExamStatsResponse getStats(Long schoolId) {
        return new AdminExamDtos.ExamStatsResponse(
                examRepository.countBySchoolIdAndDeletedFalse(schoolId),
                examRepository.countBySchoolIdAndDeletedFalseAndStatus(schoolId, ExamStatus.ACTIVE),
                examRepository.countBySchoolIdAndDeletedFalseAndStatus(schoolId, ExamStatus.SCHEDULED),
                examRepository.countBySchoolIdAndDeletedFalseAndStatus(schoolId, ExamStatus.COMPLETED));
    }

    @Override
    @Transactional(readOnly = true)
    public AdminExamDtos.ExamListResponse listExams(
            Long schoolId, String q, String className, String examType,
            String status, String academicYear, int page, int size) {
        int safePage = Math.max(0, page);
        int safeSize = Math.min(Math.max(size, 1), 100);
        Page<Exam> result = examRepository.search(
                schoolId,
                blankToNull(q),
                blankToNull(className),
                parseExamTypeOrNull(examType),
                parseStatusOrNull(status),
                blankToNull(academicYear),
                PageRequest.of(safePage, safeSize));

        List<AdminExamDtos.ExamRow> rows = result.getContent().stream()
                .map(this::toRow)
                .toList();
        int totalPages = (int) Math.ceil((double) result.getTotalElements() / safeSize);
        String showing = showingLabel(result.getTotalElements(), safePage, safeSize, rows.size());
        return new AdminExamDtos.ExamListResponse(
                rows, safePage, safeSize, result.getTotalElements(), totalPages, showing);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminExamDtos.ExamFilterOptionsResponse getFilterOptions(Long schoolId) {
        List<AdminExamDtos.ExamOption> types = new ArrayList<>();
        for (ExamType t : ExamType.values()) {
            types.add(new AdminExamDtos.ExamOption(t.name(), t.getLabel()));
        }
        List<AdminExamDtos.ExamOption> statuses = List.of(
                new AdminExamDtos.ExamOption("ALL", "All Status"),
                new AdminExamDtos.ExamOption("DRAFT", "Draft"),
                new AdminExamDtos.ExamOption("SCHEDULED", "Scheduled"),
                new AdminExamDtos.ExamOption("ACTIVE", "Active"),
                new AdminExamDtos.ExamOption("COMPLETED", "Completed"));
        return new AdminExamDtos.ExamFilterOptionsResponse(
                examRepository.findDistinctClassNames(schoolId),
                types,
                statuses,
                examRepository.findDistinctAcademicYears(schoolId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminExamDtos.ExamDetailResponse createExam(AdminExamRequests.CreateExamRequest request) {
        validateDateRange(request.getStartDate(), request.getEndDate());
        Exam exam = Exam.builder()
                .name(request.getExamName().trim())
                .examType(parseExamType(request.getExamType()))
                .className(request.getClassName().trim())
                .section(blankToNull(request.getSection()))
                .academicYear(request.getAcademicYear().trim())
                .examDate(request.getStartDate())
                .endDate(request.getEndDate())
                .description(blankToNull(request.getDescription()))
                .instructions(blankToNull(request.getInstructions()))
                .coordinator(resolveCoordinator(request.getSchoolId(), request.getCoordinatorId()))
                .schoolId(request.getSchoolId())
                .deleted(false)
                .build();
        exam.setStatus(request.isDraft() ? ExamStatus.DRAFT : deriveStatus(exam));
        exam = examRepository.save(exam);

        activityLogger.log(request.getSchoolId(), AdminActivityType.EXAM_CREATED,
                "Exam created: " + exam.getName(),
                classLabel(exam.getClassName(), exam.getSection()) + " • " + exam.getExamType().getLabel(),
                "EXAM", exam.getId(), request.getPerformedBy());
        return toDetail(exam);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminExamDtos.ExamDetailResponse updateExam(Long examId, AdminExamRequests.UpdateExamRequest request) {
        Exam exam = requireExam(request.getSchoolId(), examId);
        if (blankToNull(request.getExamName()) != null) {
            exam.setName(request.getExamName().trim());
        }
        if (blankToNull(request.getExamType()) != null) {
            exam.setExamType(parseExamType(request.getExamType()));
        }
        if (blankToNull(request.getClassName()) != null) {
            exam.setClassName(request.getClassName().trim());
        }
        if (request.getSection() != null) {
            exam.setSection(blankToNull(request.getSection()));
        }
        if (blankToNull(request.getAcademicYear()) != null) {
            exam.setAcademicYear(request.getAcademicYear().trim());
        }
        if (request.getStartDate() != null) {
            exam.setExamDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            exam.setEndDate(request.getEndDate());
        }
        validateDateRange(exam.getExamDate(), exam.getEndDate());
        if (request.getDescription() != null) {
            exam.setDescription(blankToNull(request.getDescription()));
        }
        if (request.getInstructions() != null) {
            exam.setInstructions(blankToNull(request.getInstructions()));
        }
        if (request.getCoordinatorId() != null) {
            exam.setCoordinator(resolveCoordinator(request.getSchoolId(), request.getCoordinatorId()));
        }
        if (Boolean.TRUE.equals(request.getDraft())) {
            exam.setStatus(ExamStatus.DRAFT);
        } else if (request.getDraft() != null && !request.getDraft()) {
            exam.setStatus(deriveStatus(exam));
        } else if (exam.getStatus() != ExamStatus.DRAFT) {
            exam.setStatus(deriveStatus(exam));
        }
        exam = examRepository.save(exam);

        activityLogger.log(request.getSchoolId(), AdminActivityType.EXAM_UPDATED,
                "Exam updated: " + exam.getName(), null,
                "EXAM", exam.getId(), request.getPerformedBy());
        return toDetail(exam);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminExamDtos.ActionResponse deleteExam(Long schoolId, Long examId, String performedBy) {
        Exam exam = requireExam(schoolId, examId);
        exam.setDeleted(true);
        examRepository.save(exam);
        activityLogger.log(schoolId, AdminActivityType.EXAM_DELETED,
                "Exam deleted: " + exam.getName(), null,
                "EXAM", exam.getId(), performedBy);
        return new AdminExamDtos.ActionResponse(true, "Exam deleted successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public AdminExamDtos.ExamDetailResponse getExamDetail(Long schoolId, Long examId) {
        return toDetail(requireExam(schoolId, examId));
    }

    @Override
    @Transactional(readOnly = true)
    public AdminExamDtos.TimetableResponse getTimetable(Long schoolId, Long examId) {
        Exam exam = requireExam(schoolId, examId);
        List<AdminExamDtos.ScheduleRow> rows = scheduleRepository
                .findByExam_IdAndSchoolIdOrderByScheduledDateAscStartTimeAsc(examId, schoolId)
                .stream()
                .map(this::toScheduleRow)
                .toList();
        return new AdminExamDtos.TimetableResponse(exam.getId(), exam.getName(), rows);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminExamDtos.ScheduleRow addSchedule(Long examId, AdminExamRequests.CreateScheduleRequest request) {
        Exam exam = requireExam(request.getSchoolId(), examId);
        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new IllegalArgumentException("End time must be after start time");
        }
        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found"));
        ExamVenue venueRef = resolveVenue(request.getSchoolId(), request.getVenueId(), request.getVenue());
        if (venueRef != null) {
            assertVenueAvailable(request.getSchoolId(), venueRef.getId(), request.getScheduledDate(),
                    request.getStartTime(), request.getEndTime(), null);
        }
        ExamSchedule schedule = ExamSchedule.builder()
                .exam(exam)
                .schoolId(request.getSchoolId())
                .subject(subject)
                .scheduledDate(request.getScheduledDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .venue(venueRef != null ? venueRef.getName() : blankToNull(request.getVenue()))
                .venueRef(venueRef)
                .invigilator(resolveCoordinator(request.getSchoolId(), request.getInvigilatorId()))
                .assistantInvigilator(resolveCoordinator(request.getSchoolId(), request.getAssistantInvigilatorId()))
                .durationMinutes(request.getDurationMinutes())
                .reportingTime(request.getReportingTime())
                .bufferMinutes(request.getBufferMinutes())
                .instructions(blankToNull(request.getInstructions()))
                .build();
        schedule = scheduleRepository.save(schedule);
        activityLogger.log(request.getSchoolId(), AdminActivityType.EXAM_UPDATED,
                "Exam schedule added: " + exam.getName(),
                subject.getName() + " • " + request.getScheduledDate().format(DATE_FMT),
                "EXAM", exam.getId(), request.getPerformedBy());
        return toScheduleRow(schedule);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminExamDtos.ScheduleRow updateSchedule(
            Long scheduleId, AdminExamRequests.UpdateScheduleRequest request) {
        ExamSchedule schedule = scheduleRepository.findByIdAndSchoolId(scheduleId, request.getSchoolId())
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found"));
        if (request.getSubjectId() != null) {
            Subject subject = subjectRepository.findById(request.getSubjectId())
                    .orElseThrow(() -> new ResourceNotFoundException("Subject not found"));
            schedule.setSubject(subject);
        }
        if (request.getScheduledDate() != null) {
            schedule.setScheduledDate(request.getScheduledDate());
        }
        if (request.getStartTime() != null) {
            schedule.setStartTime(request.getStartTime());
        }
        if (request.getEndTime() != null) {
            schedule.setEndTime(request.getEndTime());
        }
        if (!schedule.getEndTime().isAfter(schedule.getStartTime())) {
            throw new IllegalArgumentException("End time must be after start time");
        }
        if (request.getVenueId() != null || request.getVenue() != null) {
            ExamVenue venueRef = resolveVenue(request.getSchoolId(), request.getVenueId(), request.getVenue());
            if (venueRef != null) {
                assertVenueAvailable(request.getSchoolId(), venueRef.getId(), schedule.getScheduledDate(),
                        schedule.getStartTime(), schedule.getEndTime(), schedule.getId());
                schedule.setVenueRef(venueRef);
                schedule.setVenue(venueRef.getName());
            } else if (request.getVenue() != null) {
                schedule.setVenue(blankToNull(request.getVenue()));
                schedule.setVenueRef(null);
            }
        }
        if (request.getInvigilatorId() != null) {
            schedule.setInvigilator(resolveCoordinator(request.getSchoolId(), request.getInvigilatorId()));
        }
        if (request.getAssistantInvigilatorId() != null) {
            schedule.setAssistantInvigilator(
                    resolveCoordinator(request.getSchoolId(), request.getAssistantInvigilatorId()));
        }
        if (request.getDurationMinutes() != null) {
            schedule.setDurationMinutes(request.getDurationMinutes());
        }
        if (request.getReportingTime() != null) {
            schedule.setReportingTime(request.getReportingTime());
        }
        if (request.getBufferMinutes() != null) {
            schedule.setBufferMinutes(request.getBufferMinutes());
        }
        if (request.getInstructions() != null) {
            schedule.setInstructions(blankToNull(request.getInstructions()));
        }
        schedule = scheduleRepository.save(schedule);
        return toScheduleRow(schedule);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminExamDtos.ActionResponse deleteSchedule(Long schoolId, Long scheduleId, String performedBy) {
        ExamSchedule schedule = scheduleRepository.findByIdAndSchoolId(scheduleId, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found"));
        scheduleRepository.delete(schedule);
        activityLogger.log(schoolId, AdminActivityType.EXAM_UPDATED,
                "Exam schedule removed",
                schedule.getSubject().getName(),
                "EXAM", schedule.getExam().getId(), performedBy);
        return new AdminExamDtos.ActionResponse(true, "Schedule deleted");
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminExamDtos.VenueRow> listVenues(Long schoolId) {
        return venueRepository.findBySchoolIdAndActiveTrueOrderByNameAsc(schoolId).stream()
                .map(v -> new AdminExamDtos.VenueRow(v.getId(), v.getName(), v.getCapacity()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AdminExamDtos.VenueAvailabilityResponse venueAvailability(
            Long schoolId, Long venueId, LocalDate date, LocalTime startTime, LocalTime endTime, Long excludeScheduleId) {
        ExamVenue venue = venueRepository.findByIdAndSchoolId(venueId, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Venue not found"));
        long conflicts = scheduleRepository.countVenueConflicts(
                schoolId, venueId, date, startTime, endTime, excludeScheduleId);
        int allocated = (int) Math.min(venue.getCapacity(), conflicts);
        int available = Math.max(0, venue.getCapacity() - allocated);
        return new AdminExamDtos.VenueAvailabilityResponse(
                venue.getId(), venue.getName(), venue.getCapacity(), allocated, available);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminExamDtos.SectionOption> examSections(Long schoolId, Long examId) {
        Exam exam = requireExam(schoolId, examId);
        return studentRepository.findBySchool_IdAndClassName(schoolId, exam.getClassName()).stream()
                .collect(Collectors.groupingBy(Student::getSection, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> new AdminExamDtos.SectionOption(e.getKey(), e.getValue()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AdminExamDtos.AssignmentPreviewResponse previewAssignment(
            Long schoolId, Long examId, AdminExamRequests.AssignmentPreviewRequest request, int page, int size) {
        Exam exam = requireExam(schoolId, examId);
        List<Student> candidates = resolveAssignmentCandidates(exam, parseAssignmentMethod(request.getMethod()),
                request.getSections(), request.getStudentIds());
        Set<Long> already = registrationRepository.findStudentIdsByExamId(examId);
        List<Student> toAssign = candidates.stream().filter(s -> !already.contains(s.getId())).toList();

        int safePage = Math.max(0, page);
        int safeSize = Math.min(Math.max(size, 1), 100);
        int from = safePage * safeSize;
        int to = Math.min(from + safeSize, toAssign.size());
        List<AdminExamDtos.AssignmentPreviewRow> pageRows = (from >= toAssign.size() ? List.<Student>of() : toAssign.subList(from, to))
                .stream()
                .map(this::toPreviewRow)
                .toList();
        int totalPages = safeSize == 0 ? 0 : (int) Math.ceil((double) toAssign.size() / safeSize);
        return new AdminExamDtos.AssignmentPreviewResponse(
                toAssign.size(), pageRows, safePage, safeSize, toAssign.size(), totalPages);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminExamDtos.BulkAssignResponse assignStudents(
            Long examId, AdminExamRequests.BulkAssignStudentsRequest request) {
        Exam exam = requireExam(request.getSchoolId(), examId);
        List<Student> candidates = resolveAssignmentCandidates(exam, parseAssignmentMethod(request.getMethod()),
                request.getSections(), request.getStudentIds());
        Set<Long> already = registrationRepository.findStudentIdsByExamId(examId);
        List<Student> toAssign = candidates.stream().filter(s -> !already.contains(s.getId())).toList();

        Map<Long, StudentProfile> profiles = studentProfileRepository.findByStudent_IdIn(
                toAssign.stream().map(Student::getId).toList()).stream()
                .collect(Collectors.toMap(p -> p.getStudent().getId(), p -> p, (a, b) -> a));

        int assigned = 0;
        List<ExamRegistration> batch = new ArrayList<>();
        for (Student student : toAssign) {
            String hallTicket = resolveHallTicket(profiles.get(student.getId()), student, exam);
            batch.add(ExamRegistration.builder()
                    .exam(exam)
                    .schoolId(request.getSchoolId())
                    .student(student)
                    .hallTicketNumber(hallTicket)
                    .primaryVenue(blankToNull(request.getDefaultPrimaryVenue()))
                    .status(ExamRegistrationStatus.PENDING)
                    .build());
            if (batch.size() >= ASSIGN_BATCH_SIZE) {
                registrationRepository.saveAll(batch);
                assigned += batch.size();
                batch.clear();
            }
        }
        if (!batch.isEmpty()) {
            registrationRepository.saveAll(batch);
            assigned += batch.size();
        }

        activityLogger.log(request.getSchoolId(), AdminActivityType.EXAM_STUDENTS_ASSIGNED,
                "Students assigned to exam: " + exam.getName(),
                assigned + " students assigned",
                "EXAM", exam.getId(), request.getPerformedBy());
        int skipped = Math.max(0, candidates.size() - assigned);
        return new AdminExamDtos.BulkAssignResponse(true, "Assignment completed", assigned, skipped);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminExamDtos.AssignedStudentsResponse listAssignedStudents(
            Long schoolId, Long examId, int page, int size) {
        requireExam(schoolId, examId);
        int safePage = Math.max(0, page);
        int safeSize = Math.min(Math.max(size, 1), 100);
        Page<ExamRegistration> result = registrationRepository.findByExam_IdAndSchoolIdOrderByStudent_RollNoAsc(
                examId, schoolId, PageRequest.of(safePage, safeSize));
        List<AdminExamDtos.AssignedStudentRow> rows = result.getContent().stream()
                .map(this::toAssignedRow)
                .toList();
        int totalPages = (int) Math.ceil((double) result.getTotalElements() / safeSize);
        return new AdminExamDtos.AssignedStudentsResponse(
                rows, safePage, safeSize, result.getTotalElements(), totalPages,
                showingLabel(result.getTotalElements(), safePage, safeSize, rows.size()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminExamDtos.PublishResultsResponse publishResults(
            Long examId, AdminExamRequests.PublishResultsRequest request) {
        Exam exam = requireExam(request.getSchoolId(), examId);
        if (exam.isResultsPublished()) {
            return new AdminExamDtos.PublishResultsResponse(
                    true, "Results already published", true, 0);
        }
        exam.setResultsPublished(true);
        exam.setResultsPublishedAt(LocalDateTime.now());
        examRepository.save(exam);

        int queued = 0;
        if (request.isNotifyStudents()) {
            List<ExamRegistration> regs = registrationRepository.findByExam_IdAndSchoolId(examId, request.getSchoolId());
            List<NotificationOutbox> outbox = new ArrayList<>();
            for (ExamRegistration reg : regs) {
                Student st = reg.getStudent();
                String body = "Results for " + exam.getName() + " are now available. Hall ticket: "
                        + reg.getHallTicketNumber();
                outbox.add(NotificationOutbox.builder()
                        .schoolId(request.getSchoolId())
                        .channel(NotificationChannel.EMAIL)
                        .recipient(st.getEmail())
                        .template("EXAM_RESULTS_PUBLISHED")
                        .subject("Exam results published: " + exam.getName())
                        .body(body)
                        .status(NotificationStatus.PENDING)
                        .relatedStudentId(st.getId())
                        .build());
            }
            if (!outbox.isEmpty()) {
                notificationOutboxRepository.saveAll(outbox);
                queued = outbox.size();
            }
        }

        activityLogger.log(request.getSchoolId(), AdminActivityType.EXAM_RESULTS_PUBLISHED,
                "Results published: " + exam.getName(),
                "Visible to students and parents",
                "EXAM", exam.getId(), request.getPerformedBy());
        return new AdminExamDtos.PublishResultsResponse(
                true, "Results published successfully", true, queued);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminExamDtos.ExportReportResponse exportReport(
            Long schoolId, Long examId, AdminExamRequests.ExportExamReportRequest request) {
        Exam exam = requireExam(schoolId, examId);
        List<String> headers = buildExportHeaders(request);
        List<Map<String, String>> rows = buildExportRows(schoolId, examId, exam, request, headers);
        String format = request.getFormat() != null ? request.getFormat() : "EXCEL";
        byte[] content = AdminExamExportWriter.write(format, headers, rows);
        String fileName = sanitizeFileName(exam.getName()) + "-report." + AdminExamExportWriter.extension(format);
        return new AdminExamDtos.ExportReportResponse(fileName, AdminExamExportWriter.contentType(format), content);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminExamDtos.ActivityLogResponse examActivityLog(Long schoolId, Long examId, int page, int size) {
        return marksResultsSupport.enhancedActivityLog(schoolId, examId, page, size);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminExamDtos.MarksMonitoringResponse marksMonitoring(Long schoolId, Long examId) {
        return marksResultsSupport.marksMonitoring(schoolId, examId);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminExamDtos.SubjectMarksDetailResponse subjectMarksDetail(
            Long schoolId, Long examId, Long subjectId) {
        return marksResultsSupport.subjectMarksDetail(schoolId, examId, subjectId);
    }

    @Override
    public AdminExamDtos.ApproveMarksResponse approveSubjectMarks(
            Long examId, Long subjectId, AdminExamRequests.ApproveSubjectMarksRequest request) {
        return marksResultsSupport.approveSubjectMarks(examId, subjectId, request);
    }

    @Override
    public AdminExamDtos.ApproveMarksResponse rejectSubjectMarks(
            Long examId, Long subjectId, AdminExamRequests.RejectSubjectMarksRequest request) {
        return marksResultsSupport.rejectSubjectMarks(examId, subjectId, request);
    }

    @Override
    public AdminExamDtos.ActionResponse saveSubjectMarksDraft(
            Long examId, Long subjectId, AdminExamRequests.SaveMarksDraftRequest request) {
        return marksResultsSupport.saveMarksDraft(examId, subjectId, request);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminExamDtos.ExamResultsSummaryResponse resultsSummary(Long schoolId, Long examId) {
        return marksResultsSupport.resultsSummary(schoolId, examId);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminExamDtos.ExamResultsListResponse listFinalResults(Long schoolId, Long examId, int page, int size) {
        return marksResultsSupport.listFinalResults(schoolId, examId, page, size);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] printResults(Long schoolId, Long examId, AdminExamRequests.PrintResultsRequest request) {
        return marksResultsSupport.printResults(schoolId, examId, request);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminExamDtos.HallTicketListResponse listHallTickets(Long schoolId, Long examId, int page, int size) {
        requireExam(schoolId, examId);
        int safePage = Math.max(0, page);
        int safeSize = Math.min(Math.max(size, 1), 100);
        Page<ExamRegistration> result = registrationRepository
                .findByExam_IdAndSchoolIdOrderByStudent_RollNoAsc(examId, schoolId, PageRequest.of(safePage, safeSize));
        List<AdminExamDtos.HallTicketRow> rows = result.getContent().stream()
                .map(this::toHallTicketRow)
                .toList();
        int totalPages = (int) Math.ceil((double) result.getTotalElements() / safeSize);
        return new AdminExamDtos.HallTicketListResponse(
                rows, safePage, safeSize, result.getTotalElements(), totalPages,
                showingLabel(result.getTotalElements(), safePage, safeSize, rows.size()));
    }

    @Override
    @Transactional(readOnly = true)
    public AdminExamDtos.HallTicketGeneratePreviewResponse previewHallTicketGeneration(
            Long schoolId, Long examId, AdminExamRequests.HallTicketGeneratePreviewRequest request) {
        requireExam(schoolId, examId);
        List<ExamRegistration> targets = resolveHallTicketTargets(examId, schoolId, parseHallTicketFilter(request.getStudentFilter()));
        int start = request.getStartingNumber() != null ? request.getStartingNumber() : 1;
        String prefix = normalizePrefix(request.getPrefix());
        String rangeStart = formatTicketNumber(prefix, start);
        String rangeEnd = formatTicketNumber(prefix, start + Math.max(0, targets.size() - 1));
        String label = targets.isEmpty()
                ? "No students to generate"
                : rangeStart + " to " + rangeEnd;
        return new AdminExamDtos.HallTicketGeneratePreviewResponse(
                targets.size(), rangeStart, rangeEnd, label);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminExamDtos.HallTicketGenerateResponse generateHallTickets(
            Long examId, AdminExamRequests.GenerateHallTicketsRequest request) {
        Exam exam = requireExam(request.getSchoolId(), examId);
        HallTicketStudentFilter filter = parseHallTicketFilter(request.getStudentFilter());
        List<ExamRegistration> targets = resolveHallTicketTargets(examId, request.getSchoolId(), filter);
        if (targets.isEmpty()) {
            return new AdminExamDtos.HallTicketGenerateResponse(
                    false, "No students match the selected filter", 0, null, null, 0, null);
        }

        String prefix = normalizePrefix(request.getPrefix());
        int counter = request.getStartingNumber() != null ? request.getStartingNumber() : 1;
        int generated = 0;
        List<NotificationOutbox> outbox = new ArrayList<>();

        for (ExamRegistration reg : targets) {
            String ticketNo = formatTicketNumber(prefix, counter++);
            reg.setHallTicketNumber(ticketNo);
            reg.setDocumentHash(documentHash(exam.getId(), reg.getStudent().getId(), ticketNo));
            reg.setHallTicketGenerated(true);
            reg.setStatus(ExamRegistrationStatus.VERIFIED);
            if (request.isSendToPortal()) {
                reg.setPortalNotified(true);
            }
            registrationRepository.save(reg);
            generated++;

            if (request.isSendToPortal()) {
                Student st = reg.getStudent();
                outbox.add(NotificationOutbox.builder()
                        .schoolId(request.getSchoolId())
                        .channel(NotificationChannel.EMAIL)
                        .recipient(st.getEmail())
                        .template("EXAM_HALL_TICKET")
                        .subject("Hall ticket available: " + exam.getName())
                        .body("Your hall ticket " + ticketNo + " for " + exam.getName() + " is now on the student portal.")
                        .status(NotificationStatus.PENDING)
                        .relatedStudentId(st.getId())
                        .build());
            }
        }
        if (!outbox.isEmpty()) {
            notificationOutboxRepository.saveAll(outbox);
        }

        String rangeStart = formatTicketNumber(prefix, request.getStartingNumber());
        String rangeEnd = formatTicketNumber(prefix, (request.getStartingNumber() + generated - 1));
        activityLogger.log(request.getSchoolId(), AdminActivityType.EXAM_HALL_TICKETS_GENERATED,
                "Hall tickets generated: " + exam.getName(),
                generated + " tickets (" + rangeStart + " – " + rangeEnd + ")",
                "EXAM", exam.getId(), request.getPerformedBy());

        String pdfName = request.isGeneratePdf()
                ? sanitizeFileName(exam.getName()) + "-hall-tickets.pdf"
                : null;
        return new AdminExamDtos.HallTicketGenerateResponse(
                true, "Hall tickets generated successfully", generated,
                rangeStart, rangeEnd, outbox.size(), pdfName);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminExamDtos.HallTicketPreviewResponse getHallTicketPreview(
            Long schoolId, Long examId, Long registrationId) {
        ExamRegistration reg = requireRegistration(schoolId, examId, registrationId);
        return buildHallTicketPreview(reg);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] downloadHallTicketPdf(Long schoolId, Long examId, Long registrationId) {
        ExamRegistration reg = requireRegistration(schoolId, examId, registrationId);
        if (!reg.isHallTicketGenerated()) {
            throw new IllegalArgumentException("Generate hall ticket before downloading PDF");
        }
        return AdminHallTicketPdfWriter.writeSingle(buildHallTicketPreview(reg));
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] downloadHallTicketBatchPdf(Long schoolId, Long examId) {
        requireExam(schoolId, examId);
        List<ExamRegistration> regs = registrationRepository
                .findByExam_IdAndSchoolIdOrderByStudent_RollNoAsc(examId, schoolId, PageRequest.of(0, 10_000))
                .getContent().stream()
                .filter(ExamRegistration::isHallTicketGenerated)
                .toList();
        if (regs.isEmpty()) {
            throw new IllegalArgumentException("No generated hall tickets found");
        }
        List<AdminExamDtos.HallTicketPreviewResponse> previews = regs.stream()
                .map(this::buildHallTicketPreview)
                .toList();
        return AdminHallTicketPdfWriter.writeBatch(previews);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminExamDtos.VenueAllocationSummaryResponse getVenueAllocations(Long schoolId, Long examId) {
        requireExam(schoolId, examId);
        List<ExamVenueAllocation> allocations = venueAllocationRepository
                .findByExam_IdAndSchoolIdOrderByVenue_NameAsc(examId, schoolId);
        long totalStudents = registrationRepository.countByExam_IdAndSchoolId(examId, schoolId);
        List<AdminExamDtos.VenueAllocationRow> rows = allocations.stream()
                .map(a -> toVenueAllocationRow(a, examId, schoolId))
                .toList();
        int availableSeats = rows.stream().mapToInt(AdminExamDtos.VenueAllocationRow::availableSeats).sum();
        return new AdminExamDtos.VenueAllocationSummaryResponse(
                rows.size(), totalStudents, availableSeats, rows);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminExamDtos.VenueAllocationDetailResponse assignVenueAllocation(
            Long examId, AdminExamRequests.AssignVenueAllocationRequest request) {
        Exam exam = requireExam(request.getSchoolId(), examId);
        if (venueAllocationRepository.existsByExam_IdAndVenue_Id(examId, request.getVenueId())) {
            throw new IllegalArgumentException("Venue is already assigned to this exam");
        }
        ExamVenue venue = venueRepository.findByIdAndSchoolId(request.getVenueId(), request.getSchoolId())
                .orElseThrow(() -> new ResourceNotFoundException("Venue not found"));

        ExamVenueAllocation allocation = ExamVenueAllocation.builder()
                .exam(exam)
                .schoolId(request.getSchoolId())
                .venue(venue)
                .capacityLimit(request.getCapacityLimit())
                .administrator(resolveCoordinator(request.getSchoolId(), request.getAdministratorId()))
                .primaryInvigilator(resolveCoordinator(request.getSchoolId(), request.getPrimaryInvigilatorId()))
                .assistantInvigilator(resolveCoordinator(request.getSchoolId(), request.getAssistantInvigilatorId()))
                .build();
        allocation = venueAllocationRepository.save(allocation);

        if (request.isDistributeStudents()) {
            distributeStudentsToVenue(allocation);
        }

        activityLogger.log(request.getSchoolId(), AdminActivityType.EXAM_VENUE_ALLOCATED,
                "Venue assigned: " + venue.getName(),
                "Exam: " + exam.getName(),
                "EXAM", exam.getId(), request.getPerformedBy());
        return toVenueAllocationDetail(allocation, examId, request.getSchoolId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminExamDtos.VenueAllocationDetailResponse updateVenueAllocation(
            Long allocationId, AdminExamRequests.UpdateVenueAllocationRequest request) {
        ExamVenueAllocation allocation = venueAllocationRepository.findByIdAndSchoolId(allocationId, request.getSchoolId())
                .orElseThrow(() -> new ResourceNotFoundException("Venue allocation not found"));
        if (request.getCapacityLimit() != null) {
            allocation.setCapacityLimit(request.getCapacityLimit());
        }
        if (request.getAdministratorId() != null) {
            allocation.setAdministrator(resolveCoordinator(request.getSchoolId(), request.getAdministratorId()));
        }
        if (request.getPrimaryInvigilatorId() != null) {
            allocation.setPrimaryInvigilator(
                    resolveCoordinator(request.getSchoolId(), request.getPrimaryInvigilatorId()));
        }
        if (request.getAssistantInvigilatorId() != null) {
            allocation.setAssistantInvigilator(
                    resolveCoordinator(request.getSchoolId(), request.getAssistantInvigilatorId()));
        }
        allocation = venueAllocationRepository.save(allocation);
        if (Boolean.TRUE.equals(request.getDistributeStudents())) {
            distributeStudentsToVenue(allocation);
        }
        return toVenueAllocationDetail(allocation, allocation.getExam().getId(), request.getSchoolId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminExamDtos.ActionResponse deleteVenueAllocation(
            Long schoolId, Long allocationId, String performedBy) {
        ExamVenueAllocation allocation = venueAllocationRepository.findByIdAndSchoolId(allocationId, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Venue allocation not found"));
        Long examId = allocation.getExam().getId();
        venueAllocationRepository.delete(allocation);
        activityLogger.log(schoolId, AdminActivityType.EXAM_VENUE_ALLOCATED,
                "Venue allocation removed: " + allocation.getVenue().getName(), null,
                "EXAM", examId, performedBy);
        return new AdminExamDtos.ActionResponse(true, "Venue allocation removed");
    }

    // ─── helpers ────────────────────────────────────────────────────────────────

    private Exam requireExam(Long schoolId, Long examId) {
        return examRepository.findByIdAndSchoolIdAndDeletedFalse(examId, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found"));
    }

    private Faculty resolveCoordinator(Long schoolId, Long coordinatorId) {
        if (coordinatorId == null) {
            return null;
        }
        Faculty faculty = facultyRepository.findById(coordinatorId)
                .orElseThrow(() -> new ResourceNotFoundException("Coordinator not found"));
        if (faculty.getSchool() == null || !faculty.getSchool().getId().equals(schoolId)) {
            throw new IllegalArgumentException("Coordinator does not belong to this school");
        }
        return faculty;
    }

    private ExamStatus deriveStatus(Exam exam) {
        LocalDate today = LocalDate.now();
        LocalDate start = exam.getExamDate();
        LocalDate end = exam.getEndDate() != null ? exam.getEndDate() : start;
        if (end.isBefore(today)) {
            return ExamStatus.COMPLETED;
        }
        if (!start.isAfter(today)) {
            return ExamStatus.ACTIVE;
        }
        return ExamStatus.SCHEDULED;
    }

    private AdminExamDtos.ExamRow toRow(Exam exam) {
        ExamStatus status = exam.getStatus() != null ? exam.getStatus() : ExamStatus.DRAFT;
        return new AdminExamDtos.ExamRow(
                exam.getId(),
                exam.getName(),
                exam.getExamType().name(),
                exam.getExamType().getLabel(),
                classLabel(exam.getClassName(), exam.getSection()),
                exam.getExamDate(),
                exam.getEndDate(),
                status.name(),
                status.getLabel(),
                status != ExamStatus.COMPLETED,
                true);
    }

    private AdminExamDtos.ExamDetailResponse toDetail(Exam exam) {
        ExamStatus status = exam.getStatus() != null ? exam.getStatus() : ExamStatus.DRAFT;
        Faculty coordinator = exam.getCoordinator();
        return new AdminExamDtos.ExamDetailResponse(
                exam.getId(),
                exam.getName(),
                exam.getExamType().name(),
                exam.getExamType().getLabel(),
                classLabel(exam.getClassName(), exam.getSection()),
                exam.getSection(),
                exam.getAcademicYear(),
                exam.getExamDate(),
                exam.getEndDate(),
                exam.getDescription(),
                exam.getInstructions(),
                coordinator != null ? coordinator.getId() : null,
                coordinator != null ? coordinator.getFirstName() + " " + coordinator.getLastName() : null,
                status.name(),
                status.getLabel(),
                scheduleRepository.countByExam_IdAndSchoolId(exam.getId(), exam.getSchoolId()),
                exam.isResultsPublished(),
                exam.getResultsPublishedAt(),
                registrationRepository.countByExam_IdAndSchoolId(exam.getId(), exam.getSchoolId()));
    }

    private AdminExamDtos.ScheduleRow toScheduleRow(ExamSchedule schedule) {
        Duration duration = Duration.between(schedule.getStartTime(), schedule.getEndTime());
        Faculty inv = schedule.getInvigilator();
        Faculty asst = schedule.getAssistantInvigilator();
        return new AdminExamDtos.ScheduleRow(
                schedule.getId(),
                schedule.getSubject().getId(),
                schedule.getSubject().getName(),
                schedule.getScheduledDate(),
                schedule.getStartTime(),
                schedule.getEndTime(),
                schedule.getStartTime().format(TIME_FMT) + " - " + schedule.getEndTime().format(TIME_FMT),
                formatDuration(duration),
                schedule.getVenue(),
                schedule.getVenueRef() != null ? schedule.getVenueRef().getId() : null,
                inv != null ? inv.getId() : null,
                inv != null ? inv.getFirstName() + " " + inv.getLastName() : null,
                asst != null ? asst.getId() : null,
                asst != null ? asst.getFirstName() + " " + asst.getLastName() : null,
                schedule.getDurationMinutes(),
                schedule.getReportingTime(),
                schedule.getBufferMinutes(),
                schedule.getInstructions());
    }

    private AdminExamDtos.AssignmentPreviewRow toPreviewRow(Student student) {
        return new AdminExamDtos.AssignmentPreviewRow(
                student.getId(),
                student.getFirstName() + " " + student.getLastName(),
                student.getSection(),
                "#" + String.format("%03d", student.getRollNo()));
    }

    private AdminExamDtos.AssignedStudentRow toAssignedRow(ExamRegistration reg) {
        Student st = reg.getStudent();
        ExamRegistrationStatus status = reg.getStatus() != null ? reg.getStatus() : ExamRegistrationStatus.PENDING;
        return new AdminExamDtos.AssignedStudentRow(
                reg.getId(),
                st.getId(),
                st.getFirstName() + " " + st.getLastName(),
                st.getSection(),
                reg.getHallTicketNumber(),
                reg.getPrimaryVenue(),
                status.name(),
                status.getLabel());
    }

    private List<Student> resolveAssignmentCandidates(
            Exam exam, ExamAssignmentMethod method, List<String> sections, List<Long> studentIds) {
        Long schoolId = exam.getSchoolId();
        String className = exam.getClassName();
        return switch (method) {
            case ALL -> studentRepository.findBySchool_IdAndClassName(schoolId, className);
            case SECTION -> {
                Set<String> sectionSet = sections == null ? Set.of() : new LinkedHashSet<>(sections);
                yield studentRepository.findBySchool_IdAndClassName(schoolId, className).stream()
                        .filter(s -> sectionSet.isEmpty() || sectionSet.contains(s.getSection()))
                        .toList();
            }
            case INDIVIDUAL -> {
                if (studentIds == null || studentIds.isEmpty()) {
                    throw new IllegalArgumentException("studentIds required for INDIVIDUAL assignment");
                }
                yield studentRepository.findBySchool_IdAndIdIn(schoolId, studentIds);
            }
        };
    }

    private static ExamAssignmentMethod parseAssignmentMethod(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("Assignment method is required");
        }
        return ExamAssignmentMethod.valueOf(raw.trim().toUpperCase(Locale.ROOT));
    }

    private String resolveHallTicket(StudentProfile profile, Student student, Exam exam) {
        if (profile != null && profile.getAdmissionNumber() != null && !profile.getAdmissionNumber().isBlank()) {
            return profile.getAdmissionNumber().trim();
        }
        return "EX" + exam.getId() + "-" + String.format("%03d", student.getRollNo());
    }

    private ExamVenue resolveVenue(Long schoolId, Long venueId, String venueName) {
        if (venueId != null) {
            return venueRepository.findByIdAndSchoolId(venueId, schoolId)
                    .orElseThrow(() -> new ResourceNotFoundException("Venue not found"));
        }
        if (venueName != null && !venueName.isBlank()) {
            return null;
        }
        return null;
    }

    private void assertVenueAvailable(
            Long schoolId, Long venueId, LocalDate date, LocalTime start, LocalTime end, Long excludeId) {
        long conflicts = scheduleRepository.countVenueConflicts(schoolId, venueId, date, start, end, excludeId);
        if (conflicts > 0) {
            throw new IllegalArgumentException("Venue is not available for the selected time slot");
        }
    }

    private static List<String> buildExportHeaders(AdminExamRequests.ExportExamReportRequest request) {
        List<String> headers = new ArrayList<>();
        List<String> fields = request.getDataFields();
        if (fields == null || fields.isEmpty()) {
            return List.of("student_name", "roll_number", "class", "section",
                    "subject", "marks", "total_marks", "grade", "percentage", "hall_ticket");
        }
        for (String f : fields) {
            headers.add(f.trim().toLowerCase(Locale.ROOT).replace(' ', '_'));
        }
        return headers;
    }

    private List<Map<String, String>> buildExportRows(
            Long schoolId, Long examId, Exam exam,
            AdminExamRequests.ExportExamReportRequest request, List<String> headers) {
        Set<String> sectionFilter = request.getSections() == null
                ? Set.of() : new LinkedHashSet<>(request.getSections());
        Set<Long> subjectFilter = request.getSubjectIds() == null
                ? Set.of() : new LinkedHashSet<>(request.getSubjectIds());

        List<ExamRegistration> regs = registrationRepository.findByExam_IdAndSchoolId(examId, schoolId).stream()
                .filter(r -> sectionFilter.isEmpty() || sectionFilter.contains(r.getStudent().getSection()))
                .toList();

        List<ExamResult> results = examResultRepository.findWithContextByExamId(examId).stream()
                .filter(er -> subjectFilter.isEmpty() || subjectFilter.contains(er.getSubject().getId()))
                .toList();

        Map<Long, List<ExamResult>> byStudent = results.stream()
                .collect(Collectors.groupingBy(er -> er.getStudent().getId()));

        List<Map<String, String>> rows = new ArrayList<>();
        for (ExamRegistration reg : regs) {
            Student st = reg.getStudent();
            List<ExamResult> studentResults = byStudent.getOrDefault(st.getId(), List.of());
            if (studentResults.isEmpty()) {
                rows.add(buildExportRow(headers, st, reg, exam, null));
            } else {
                for (ExamResult er : studentResults) {
                    rows.add(buildExportRow(headers, st, reg, exam, er));
                }
            }
        }
        return rows;
    }

    private Map<String, String> buildExportRow(
            List<String> headers, Student st, ExamRegistration reg, Exam exam, ExamResult er) {
        Map<String, String> row = new LinkedHashMap<>();
        for (String h : headers) {
            row.put(h, exportFieldValue(h, st, reg, exam, er));
        }
        return row;
    }

    private static String exportFieldValue(
            String field, Student st, ExamRegistration reg, Exam exam, ExamResult er) {
        return switch (field) {
            case "student_name" -> st.getFirstName() + " " + st.getLastName();
            case "roll_number" -> String.valueOf(st.getRollNo());
            case "registration_number", "hall_ticket" -> reg.getHallTicketNumber();
            case "class" -> st.getClassName();
            case "section" -> st.getSection();
            case "subject" -> er != null ? er.getSubject().getName() : "";
            case "marks" -> er != null && er.getObtainedMarks() != null ? String.valueOf(er.getObtainedMarks()) : "";
            case "total_marks" -> er != null ? String.valueOf(er.getTotalMarks()) : "";
            case "grade" -> er != null && er.getGrade() != null ? er.getGrade() : "";
            case "percentage" -> {
                if (er == null || er.getObtainedMarks() == null || er.getTotalMarks() == 0) {
                    yield "";
                }
                yield String.format(Locale.ENGLISH, "%.1f",
                        er.getObtainedMarks() * 100.0 / er.getTotalMarks());
            }
            case "exam_name" -> exam.getName();
            default -> "";
        };
    }

    private ExamRegistration requireRegistration(Long schoolId, Long examId, Long registrationId) {
        return registrationRepository.findByIdAndExam_IdAndSchoolId(registrationId, examId, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Registration not found"));
    }

    private AdminExamDtos.HallTicketRow toHallTicketRow(ExamRegistration reg) {
        Student st = reg.getStudent();
        String venue = reg.getPrimaryVenue();
        if ((venue == null || venue.isBlank()) && reg.getVenue() != null) {
            venue = reg.getVenue().getName();
        }
        boolean generated = reg.isHallTicketGenerated();
        return new AdminExamDtos.HallTicketRow(
                reg.getId(),
                st.getId(),
                st.getFirstName() + " " + st.getLastName(),
                reg.getHallTicketNumber(),
                venue,
                generated,
                true,
                generated,
                generated);
    }

    private List<ExamRegistration> resolveHallTicketTargets(
            Long examId, Long schoolId, HallTicketStudentFilter filter) {
        return switch (filter) {
            case ALL -> registrationRepository.findByExam_IdAndSchoolId(examId, schoolId);
            case UNASSIGNED_ONLY -> registrationRepository
                    .findByExam_IdAndSchoolIdAndHallTicketGeneratedFalseOrderByStudent_RollNoAsc(examId, schoolId);
        };
    }

    private static HallTicketStudentFilter parseHallTicketFilter(String raw) {
        if (raw == null || raw.isBlank()) {
            return HallTicketStudentFilter.ALL;
        }
        return HallTicketStudentFilter.valueOf(raw.trim().toUpperCase(Locale.ROOT));
    }

    private static String normalizePrefix(String prefix) {
        return prefix == null ? "" : prefix.trim();
    }

    private static String formatTicketNumber(String prefix, int number) {
        String num = String.format("%03d", number);
        return prefix.isEmpty() ? num : prefix + num;
    }

    private static String documentHash(Long examId, Long studentId, String ticketNumber) {
        try {
            String raw = examId + "|" + studentId + "|" + ticketNumber;
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(raw.getBytes(StandardCharsets.UTF_8));
            return String.format("%04X-%04X-%04X-%04X",
                    ((digest[0] & 0xFF) << 8) | (digest[1] & 0xFF),
                    ((digest[2] & 0xFF) << 8) | (digest[3] & 0xFF),
                    ((digest[4] & 0xFF) << 8) | (digest[5] & 0xFF),
                    ((digest[6] & 0xFF) << 8) | (digest[7] & 0xFF));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to compute document hash", e);
        }
    }

    private AdminExamDtos.HallTicketPreviewResponse buildHallTicketPreview(ExamRegistration reg) {
        Exam exam = reg.getExam();
        Student st = reg.getStudent();
        School school = schoolRepository.findById(reg.getSchoolId()).orElse(null);
        String schoolName = school != null ? school.getSchoolName() : "School";
        StudentProfile profile = studentProfileRepository.findByStudent_Id(st.getId()).orElse(null);
        String admissionId = profile != null ? profile.getAdmissionNumber() : "—";
        String sectionLabel = st.getClassName() + " (" + st.getSection() + ")";

        List<AdminExamDtos.HallTicketScheduleLine> schedule = scheduleRepository
                .findByExam_IdAndSchoolIdOrderByScheduledDateAscStartTimeAsc(exam.getId(), reg.getSchoolId())
                .stream()
                .map(s -> new AdminExamDtos.HallTicketScheduleLine(
                        s.getSubject().getName(),
                        s.getScheduledDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH)),
                        s.getStartTime().format(TIME_FMT) + " - " + s.getEndTime().format(TIME_FMT),
                        s.getVenue() != null ? s.getVenue() : ""))
                .toList();

        List<String> instructions = exam.getInstructions() != null && !exam.getInstructions().isBlank()
                ? Arrays.stream(exam.getInstructions().split("\\r?\\n"))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList()
                : List.of(
                "Reach 30 minutes before start time",
                "Original hall ticket is mandatory",
                "No electronic gadgets allowed",
                "Maintain silence in exam hall");

        String hash = reg.getDocumentHash() != null
                ? reg.getDocumentHash()
                : documentHash(exam.getId(), st.getId(), reg.getHallTicketNumber());

        return new AdminExamDtos.HallTicketPreviewResponse(
                schoolName,
                exam.getName(),
                st.getFirstName() + " " + st.getLastName(),
                reg.getHallTicketNumber(),
                admissionId,
                sectionLabel,
                hash,
                reg.isHallTicketGenerated(),
                schedule,
                instructions);
    }

    private void distributeStudentsToVenue(ExamVenueAllocation allocation) {
        Long examId = allocation.getExam().getId();
        Long schoolId = allocation.getSchoolId();
        int capacity = allocation.effectiveCapacity();
        long assigned = registrationRepository.countByExam_IdAndSchoolIdAndVenue_Id(
                examId, schoolId, allocation.getVenue().getId());
        int slots = capacity - (int) assigned;
        if (slots <= 0) {
            return;
        }
        List<ExamRegistration> unassigned = registrationRepository
                .findByExam_IdAndSchoolIdAndVenue_IdIsNullOrderByStudent_RollNoAsc(examId, schoolId);
        int count = 0;
        for (ExamRegistration reg : unassigned) {
            if (count >= slots) {
                break;
            }
            reg.setVenue(allocation.getVenue());
            reg.setPrimaryVenue(allocation.getVenue().getName());
            registrationRepository.save(reg);
            count++;
        }
    }

    private AdminExamDtos.VenueAllocationRow toVenueAllocationRow(
            ExamVenueAllocation allocation, Long examId, Long schoolId) {
        long assigned = registrationRepository.countByExam_IdAndSchoolIdAndVenue_Id(
                examId, schoolId, allocation.getVenue().getId());
        int capacity = allocation.effectiveCapacity();
        int available = Math.max(0, capacity - (int) assigned);
        Faculty admin = allocation.getAdministrator();
        Faculty primary = allocation.getPrimaryInvigilator();
        Faculty assistant = allocation.getAssistantInvigilator();
        return new AdminExamDtos.VenueAllocationRow(
                allocation.getId(),
                allocation.getVenue().getId(),
                allocation.getVenue().getName(),
                capacity,
                assigned,
                available,
                admin != null ? admin.getId() : null,
                facultyName(admin),
                primary != null ? primary.getId() : null,
                facultyName(primary),
                assistant != null ? assistant.getId() : null,
                facultyName(assistant));
    }

    private AdminExamDtos.VenueAllocationDetailResponse toVenueAllocationDetail(
            ExamVenueAllocation allocation, Long examId, Long schoolId) {
        long assigned = registrationRepository.countByExam_IdAndSchoolIdAndVenue_Id(
                examId, schoolId, allocation.getVenue().getId());
        int capacity = allocation.effectiveCapacity();
        return new AdminExamDtos.VenueAllocationDetailResponse(
                allocation.getId(),
                allocation.getVenue().getId(),
                allocation.getVenue().getName(),
                allocation.getVenue().getCapacity(),
                allocation.getCapacityLimit(),
                assigned,
                Math.max(0, capacity - (int) assigned),
                allocation.getAdministrator() != null ? allocation.getAdministrator().getId() : null,
                allocation.getPrimaryInvigilator() != null ? allocation.getPrimaryInvigilator().getId() : null,
                allocation.getAssistantInvigilator() != null ? allocation.getAssistantInvigilator().getId() : null);
    }

    private static String facultyName(Faculty faculty) {
        if (faculty == null) {
            return null;
        }
        return faculty.getFirstName() + " " + faculty.getLastName();
    }

    private static String sanitizeFileName(String name) {
        return name.replaceAll("[^a-zA-Z0-9-_]", "-").toLowerCase(Locale.ROOT);
    }

    private static String formatDuration(Duration duration) {
        long minutes = duration.toMinutes();
        if (minutes % 60 == 0) {
            return (minutes / 60) + "h";
        }
        double hours = minutes / 60.0;
        return hours == Math.floor(hours) ? ((long) hours) + "h" : String.format(Locale.ENGLISH, "%.1fh", hours);
    }

    private static ExamType parseExamType(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("Exam type is required");
        }
        String normalized = raw.trim().toUpperCase(Locale.ROOT).replace(' ', '_').replace('-', '_');
        return switch (normalized) {
            case "ASSESSMENT" -> ExamType.ASSESSMENT;
            case "UNIT_TEST", "UNIT" -> ExamType.UNIT_TEST;
            case "INTERNAL" -> ExamType.INTERNAL;
            case "WEEKLY", "WEEKLY_TEST" -> ExamType.WEEKLY;
            case "MID_TERM", "MIDTERM" -> ExamType.MID_TERM;
            case "FINAL" -> ExamType.FINAL;
            default -> ExamType.valueOf(normalized);
        };
    }

    private static ExamType parseExamTypeOrNull(String raw) {
        if (raw == null || raw.isBlank() || "ALL".equalsIgnoreCase(raw)) {
            return null;
        }
        return parseExamType(raw);
    }

    private static ExamStatus parseStatusOrNull(String raw) {
        if (raw == null || raw.isBlank() || "ALL".equalsIgnoreCase(raw)) {
            return null;
        }
        return ExamStatus.valueOf(raw.trim().toUpperCase(Locale.ROOT));
    }

    private static void validateDateRange(LocalDate start, LocalDate end) {
        if (start != null && end != null && end.isBefore(start)) {
            throw new IllegalArgumentException("End date must be on or after start date");
        }
    }

    private static String classLabel(String className, String section) {
        if (section == null || section.isBlank()) {
            return className;
        }
        return className + " (" + section + ")";
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private static String showingLabel(long total, int page, int size, int count) {
        if (total == 0) {
            return "Showing 0 exams";
        }
        long from = (long) page * size + 1;
        long to = Math.min(from + count - 1, total);
        return "Showing " + from + "-" + to + " of " + total;
    }
}
