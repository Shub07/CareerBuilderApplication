package com.org.careerbuilder.service.impl;

import com.org.careerbuilder.dto.request.AdminTeacherProfileRequests;
import com.org.careerbuilder.dto.request.AdminTeacherRequests;
import com.org.careerbuilder.dto.response.AdminTeacherDtos;
import com.org.careerbuilder.dto.response.AdminTeacherProfileDtos;
import com.org.careerbuilder.exceptions.ResourceNotFoundException;
import com.org.careerbuilder.models.*;
import com.org.careerbuilder.models.enums.AdminActivityType;
import com.org.careerbuilder.models.enums.AssignmentStatus;
import com.org.careerbuilder.models.enums.FacultyAccountStatus;
import com.org.careerbuilder.models.enums.FacultyDocumentType;
import com.org.careerbuilder.models.enums.TeacherAttendanceStatus;
import com.org.careerbuilder.models.enums.TeacherLeaveStatus;
import com.org.careerbuilder.repository.*;
import com.org.careerbuilder.service.AdminTeacherManagementService;
import com.org.careerbuilder.service.AdminTeacherProfileService;
import com.org.careerbuilder.service.support.AdminActivityLogger;
import com.org.careerbuilder.service.support.AdminFileStorageHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminTeacherProfileServiceImpl implements AdminTeacherProfileService {

    private static final long MAX_DOC_BYTES = 10L * 1024 * 1024;
    private static final Set<String> ALLOWED_EXT = Set.of("pdf", "jpg", "jpeg", "png");
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    private final FacultyRepository facultyRepository;
    private final FacultyProfileRepository facultyProfileRepository;
    private final FacultyDocumentRepository facultyDocumentRepository;
    private final ClassSubjectTeacherRepository assignmentRepository;
    private final AssignmentRepository teacherAssignmentRepository;
    private final AssignmentSubmissionRepository submissionRepository;
    private final TeacherLeaveRequestRepository leaveRepository;
    private final TeacherAttendanceEntryRepository attendanceRepository;
    private final TeacherScheduleEntryRepository scheduleRepository;
    private final AdminActivityLogRepository activityLogRepository;
    private final SubjectRepository subjectRepository;
    private final AdminTeacherManagementService teacherManagementService;
    private final AdminFileStorageHelper fileStorageHelper;
    private final AdminActivityLogger activityLogger;

    @Override
    @Transactional(readOnly = true)
    public AdminTeacherProfileDtos.FullProfileResponse getFullProfile(Long schoolId, Long facultyId) {
        requireFaculty(schoolId, facultyId);
        return new AdminTeacherProfileDtos.FullProfileResponse(
                buildHeader(schoolId, facultyId),
                getBasicDetailsTab(schoolId, facultyId),
                List.of(
                        "BASIC_DETAILS", "ALLOCATIONS", "ASSIGNMENTS", "ATTENDANCE_LEAVE",
                        "WORKLOAD", "EMPLOYMENT", "ACTIVITY_LOG"
                )
        );
    }

    @Override
    @Transactional(readOnly = true)
    public AdminTeacherProfileDtos.BasicDetailsTab getBasicDetailsTab(Long schoolId, Long facultyId) {
        Faculty f = requireFaculty(schoolId, facultyId);
        FacultyProfile p = requireProfile(f);
        return new AdminTeacherProfileDtos.BasicDetailsTab(
                new AdminTeacherProfileDtos.PersonalInfo(
                        f.getFirstName() + " " + f.getLastName(),
                        f.getGender(),
                        p.getDateOfBirth(),
                        f.getPhone(),
                        f.getEmail(),
                        f.getAddress()
                ),
                new AdminTeacherProfileDtos.ProfessionalInfo(
                        f.getQualification(),
                        f.getExperience() + " Years",
                        f.getSubject().getName(),
                        p.getEmploymentType() != null ? p.getEmploymentType().name() : "",
                        p.getJoiningDate(),
                        p.getAccountStatus().name(),
                        p.getDepartment()
                ),
                listDocuments(schoolId, facultyId)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public AdminTeacherProfileDtos.AllocationsTabResponse getAllocationsTab(Long schoolId, Long facultyId) {
        requireFaculty(schoolId, facultyId);
        List<AdminTeacherDtos.ClassAssignmentRow> rows = assignmentRepository
                .findByFaculty_IdAndActiveTrueWithSubject(facultyId).stream()
                .map(AdminTeacherManagementServiceImpl::toAssignmentRow)
                .toList();
        return new AdminTeacherProfileDtos.AllocationsTabResponse(rows, rows.size());
    }

    @Override
    @Transactional(readOnly = true)
    public AdminTeacherProfileDtos.AssignmentsTabResponse getAssignmentsTab(Long schoolId, Long facultyId) {
        requireFaculty(schoolId, facultyId);
        List<AdminTeacherProfileDtos.AssignmentRow> rows = teacherAssignmentRepository
                .findByTeacher_IdOrderByDueDateDesc(facultyId).stream()
                .map(a -> {
                    long total = submissionRepository.countByAssignment_Id(a.getId());
                    long submitted = submissionRepository.countSubmittedWithFile(a.getId());
                    int rate = total > 0 ? (int) Math.round(100.0 * submitted / total) : 0;
                    return new AdminTeacherProfileDtos.AssignmentRow(
                            a.getId(),
                            a.getTitle(),
                            classLabel(a.getClassName(), a.getSection()),
                            a.getSubject().getName(),
                            a.getGivenDate() != null ? a.getGivenDate() : a.getDueDate(),
                            rate,
                            deriveAssignmentStatus(a)
                    );
                })
                .toList();
        return new AdminTeacherProfileDtos.AssignmentsTabResponse(rows, rows.size());
    }

    @Override
    @Transactional(readOnly = true)
    public AdminTeacherProfileDtos.AssignmentDetailResponse getAssignmentDetail(
            Long schoolId, Long facultyId, Long assignmentId) {
        requireFaculty(schoolId, facultyId);
        Assignment a = teacherAssignmentRepository.findByIdAndTeacher_Id(assignmentId, facultyId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found for this teacher"));
        List<AssignmentSubmission> subs = submissionRepository.findByAssignment_IdWithStudent(assignmentId);
        int total = subs.size();
        int submitted = (int) subs.stream()
                .filter(s -> s.getStatus() == AssignmentStatus.SUBMITTED
                        || s.getStatus() == AssignmentStatus.LATE
                        || s.getStatus() == AssignmentStatus.GRADED).count();
        int graded = (int) subs.stream().filter(s -> s.getStatus() == AssignmentStatus.GRADED).count();
        int pending = Math.max(0, total - submitted);
        int rate = total > 0 ? (int) Math.round(100.0 * submitted / total) : 0;

        List<AdminTeacherProfileDtos.StudentSubmissionRow> studentRows = subs.stream()
                .map(s -> new AdminTeacherProfileDtos.StudentSubmissionRow(
                        s.getId(),
                        s.getStudent() != null ? s.getStudent().getId() : null,
                        s.getStudent() != null
                                ? (s.getStudent().getFirstName() + " " + s.getStudent().getLastName()).trim()
                                : "—",
                        s.getSubmittedAt() != null ? s.getSubmittedAt().toLocalDate() : null,
                        s.getStatus() != null ? s.getStatus().name() : AssignmentStatus.PENDING.name(),
                        marksLabel(s)
                ))
                .toList();

        return new AdminTeacherProfileDtos.AssignmentDetailResponse(
                a.getId(),
                a.getTitle(),
                classLabel(a.getClassName(), a.getSection()),
                a.getSubject().getName(),
                a.getGivenDate() != null ? a.getGivenDate() : a.getDueDate(),
                a.getDueDate(),
                a.getDescription(),
                a.getTotalMarks(),
                new AdminTeacherProfileDtos.SubmissionSummary(submitted, pending, graded, total, rate),
                studentRows
        );
    }

    @Override
    @Transactional(readOnly = true)
    public AdminTeacherProfileDtos.AttendanceLeaveTabResponse getAttendanceLeaveTab(Long schoolId, Long facultyId) {
        requireFaculty(schoolId, facultyId);
        LocalDate from = LocalDate.now().minusDays(90);
        LocalDate to = LocalDate.now();
        var entries = attendanceRepository.findByFaculty_IdAndWorkDateBetweenOrderByWorkDateAsc(facultyId, from, to);
        int present = (int) entries.stream()
                .filter(e -> e.getStatus() == TeacherAttendanceStatus.PRESENT
                        || e.getStatus() == TeacherAttendanceStatus.LATE).count();
        int absent = (int) entries.stream()
                .filter(e -> e.getStatus() == TeacherAttendanceStatus.LEAVE
                        || e.getStatus() == TeacherAttendanceStatus.HALF_DAY).count();
        int total = Math.max(entries.size(), 1);

        // Most recent attendance first (snapshot shows latest dates on top).
        List<AdminTeacherProfileDtos.RecentAttendanceRow> recent = entries.stream()
                .sorted(Comparator.comparing(TeacherAttendanceEntry::getWorkDate).reversed())
                .limit(10)
                .map(e -> new AdminTeacherProfileDtos.RecentAttendanceRow(
                        e.getWorkDate(),
                        e.getStatus() != null ? e.getStatus().name() : "",
                        attendanceRemark(e)
                ))
                .toList();

        var leaves = leaveRepository.findByFaculty_IdOrderByCreatedAtDesc(facultyId);
        int leaveBalance = Math.max(0, 12 - (int) leaves.stream()
                .filter(l -> l.getStatus() == TeacherLeaveStatus.APPROVED).count());

        List<AdminTeacherProfileDtos.LeaveRow> leaveRows = leaves.stream().limit(20)
                .map(l -> new AdminTeacherProfileDtos.LeaveRow(
                        l.getId(),
                        l.getLeaveType().name(),
                        l.getFromDate(),
                        l.getToDate(),
                        l.getCreatedAt() != null ? l.getCreatedAt().toLocalDate() : null,
                        l.getReason(),
                        l.getStatus().name(),
                        l.getStatus() == TeacherLeaveStatus.APPLIED
                ))
                .toList();

        return new AdminTeacherProfileDtos.AttendanceLeaveTabResponse(
                new AdminTeacherProfileDtos.AttendanceSummary(
                        present, absent, leaveBalance,
                        Math.round(100.0 * present / total * 10) / 10.0
                ),
                recent,
                leaveRows
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminTeacherDtos.ActionResponse reviewLeave(
            Long facultyId, Long leaveId, AdminTeacherProfileRequests.ReviewLeaveRequest request) {
        Faculty faculty = requireFaculty(request.getSchoolId(), facultyId);
        TeacherLeaveRequest leave = leaveRepository.findByIdAndFaculty_Id(leaveId, facultyId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found"));
        if (leave.getStatus() != TeacherLeaveStatus.APPLIED) {
            throw new IllegalArgumentException("Only pending leave requests can be reviewed");
        }
        String action = request.getAction() != null ? request.getAction().trim().toUpperCase() : "";
        String message;
        switch (action) {
            case "APPROVE" -> {
                leave.setStatus(TeacherLeaveStatus.APPROVED);
                message = "Leave approved";
            }
            case "REJECT" -> {
                leave.setStatus(TeacherLeaveStatus.REJECTED);
                leave.setRejectionReason(request.getRemarks());
                message = "Leave rejected";
            }
            default -> throw new IllegalArgumentException("Action must be APPROVE or REJECT");
        }
        leaveRepository.save(leave);
        activityLogger.log(request.getSchoolId(), AdminActivityType.TEACHER_UPDATED,
                message, faculty.getFacultyId() + " — " + leave.getLeaveType().name(),
                "FACULTY", facultyId, request.getPerformedBy());
        return new AdminTeacherDtos.ActionResponse(true, message);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminTeacherProfileDtos.WorkloadTabResponse getWorkloadTab(Long schoolId, Long facultyId) {
        requireFaculty(schoolId, facultyId);
        LocalDate today = LocalDate.now();
        var schedule = scheduleRepository.findRelevantForWeek(
                facultyId, today.with(java.time.DayOfWeek.MONDAY), today.with(java.time.DayOfWeek.SUNDAY));

        List<ClassSubjectTeacher> active = assignmentRepository.findByFaculty_IdAndActiveTrueWithSubject(facultyId);
        int subjectsAssigned = (int) active.stream()
                .map(a -> a.getSubject().getId()).distinct().count();
        int sectionsAssigned = (int) active.stream()
                .map(a -> a.getClassName() + "-" + a.getSection()).distinct().count();
        int weeklyClasses = schedule.size();

        List<AdminTeacherProfileDtos.ScheduleRow> rows = schedule.stream()
                .sorted(Comparator
                        .comparing((TeacherScheduleEntry s) -> s.getDayOfWeek() != null ? s.getDayOfWeek() : 99)
                        .thenComparing(TeacherScheduleEntry::getStartTime))
                .limit(20)
                .map(s -> new AdminTeacherProfileDtos.ScheduleRow(
                        dayName(s.getDayOfWeek()),
                        s.getStartTime() + " - " + s.getEndTime(),
                        s.getClassName() != null ? s.getClassName() + "-" + s.getSection() : s.getTitle(),
                        s.getSubject() != null ? s.getSubject().getName() : s.getTitle()
                )).toList();
        return new AdminTeacherProfileDtos.WorkloadTabResponse(
                weeklyClasses, subjectsAssigned, sectionsAssigned, rows);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminTeacherProfileDtos.EmploymentTabResponse getEmploymentTab(Long schoolId, Long facultyId) {
        Faculty f = requireFaculty(schoolId, facultyId);
        FacultyProfile p = requireProfile(f);
        String base = "/api/admin/teachers/" + facultyId + "/profile/employment-documents/";
        List<AdminTeacherProfileDtos.EmploymentDocumentRow> docs = List.of(
                new AdminTeacherProfileDtos.EmploymentDocumentRow(
                        "Resume / CV",
                        p.getResumePath() != null ? base + "resume" : null,
                        p.getResumePath() != null),
                new AdminTeacherProfileDtos.EmploymentDocumentRow(
                        "Signed Contract",
                        p.getContractPath() != null ? base + "contract" : null,
                        p.getContractPath() != null)
        );
        return new AdminTeacherProfileDtos.EmploymentTabResponse(
                f.getFacultyId(),
                p.getJoiningDate(),
                p.getEmploymentType() != null ? p.getEmploymentType().name() : "",
                p.getDepartment(),
                p.getAccountStatus().name(),
                resolveDesignation(p, f),
                p.getBasicSalary() != null ? "₹" + p.getBasicSalary().stripTrailingZeros().toPlainString() + " / month" : null,
                p.getContractType(),
                docs
        );
    }

    @Override
    @Transactional(readOnly = true)
    public AdminTeacherProfileDtos.ActivityLogTabResponse getActivityLogTab(
            Long schoolId, Long facultyId, int page, int size) {
        requireFaculty(schoolId, facultyId);
        int safePage = Math.max(0, page);
        int safeSize = Math.min(Math.max(size, 1), 50);
        Page<AdminActivityLog> result = activityLogRepository.searchStudentActivityLog(
                schoolId, "FACULTY", facultyId, null, null, PageRequest.of(safePage, safeSize));
        List<AdminTeacherProfileDtos.ActivityLogRow> rows = result.getContent().stream()
                .map(this::toActivityRow)
                .toList();
        return new AdminTeacherProfileDtos.ActivityLogTabResponse(
                rows, safePage, safeSize, result.getTotalElements(), result.hasNext());
    }

    @Override
    @Transactional(readOnly = true)
    public AdminTeacherProfileDtos.MoreActionsMenuResponse getMoreActionsMenu(Long schoolId, Long facultyId) {
        requireFaculty(schoolId, facultyId);
        return new AdminTeacherProfileDtos.MoreActionsMenuResponse(List.of(
                new AdminTeacherProfileDtos.MenuAction("ASSIGN_SUBJECT", "Assign Subject", false),
                new AdminTeacherProfileDtos.MenuAction("MARK_LEAVE", "Mark Leave", false),
                new AdminTeacherProfileDtos.MenuAction("DEACTIVATE", "Deactivate Account", false),
                new AdminTeacherProfileDtos.MenuAction("DELETE", "Delete Teacher", true)
        ));
    }

    @Override
    public List<AdminTeacherProfileDtos.DocumentTypeOption> getDocumentTypes() {
        return Arrays.stream(FacultyDocumentType.values())
                .map(t -> new AdminTeacherProfileDtos.DocumentTypeOption(t.name(), formatDocTypeLabel(t)))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminTeacherProfileDtos.DocumentRow> listDocuments(Long schoolId, Long facultyId) {
        requireFaculty(schoolId, facultyId);
        return facultyDocumentRepository.findByFaculty_IdOrderByUploadedAtDesc(facultyId).stream()
                .map(this::toDocumentRow)
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminTeacherProfileDtos.DocumentRow uploadDocument(
            Long facultyId, MultipartFile file, AdminTeacherProfileRequests.UploadDocumentRequest meta) {
        Faculty faculty = requireFaculty(meta.getSchoolId(), facultyId);
        validateDocumentFile(file);
        String path = fileStorageHelper.store(meta.getSchoolId(), "faculty-documents", file);
        FacultyDocumentType type = FacultyDocumentType.valueOf(meta.getDocumentType().trim().toUpperCase());
        FacultyDocument doc = FacultyDocument.builder()
                .faculty(faculty)
                .documentName(meta.getDocumentName().trim())
                .documentType(type)
                .fileName(file.getOriginalFilename())
                .filePath(path)
                .fileSizeBytes(file.getSize())
                .uploadedBy(meta.getUploadedBy() != null ? meta.getUploadedBy() : "Admin")
                .build();
        doc = facultyDocumentRepository.save(doc);
        activityLogger.log(meta.getSchoolId(), AdminActivityType.TEACHER_UPDATED,
                "Document uploaded",
                doc.getDocumentName(),
                "FACULTY", facultyId, meta.getUploadedBy());
        return toDocumentRow(doc);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDocument(Long schoolId, Long facultyId, Long documentId) {
        requireFaculty(schoolId, facultyId);
        FacultyDocument doc = facultyDocumentRepository.findByIdAndFaculty_Id(documentId, facultyId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found"));
        facultyDocumentRepository.delete(doc);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] downloadDocument(Long schoolId, Long facultyId, Long documentId) {
        requireFaculty(schoolId, facultyId);
        FacultyDocument doc = facultyDocumentRepository.findByIdAndFaculty_Id(documentId, facultyId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found"));
        try {
            return Files.readAllBytes(Paths.get(doc.getFilePath()));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to read document", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] downloadEmploymentDocument(Long schoolId, Long facultyId, String kind) {
        Faculty f = requireFaculty(schoolId, facultyId);
        FacultyProfile p = requireProfile(f);
        String path = switch (kind == null ? "" : kind.trim().toLowerCase()) {
            case "resume" -> p.getResumePath();
            case "contract" -> p.getContractPath();
            default -> throw new IllegalArgumentException("Unknown document kind: " + kind);
        };
        if (path == null || path.isBlank()) {
            throw new ResourceNotFoundException("No " + kind + " on file for this teacher");
        }
        try {
            return Files.readAllBytes(Paths.get(path));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to read employment document", e);
        }
    }

    @Override
    public AdminTeacherProfileDtos.ProfileExportCatalogResponse getProfileExportCatalog() {
        return new AdminTeacherProfileDtos.ProfileExportCatalogResponse(
                teacherManagementService.getExportFieldCatalog().fieldsByCategory(),
                teacherManagementService.getExportFieldCatalog().fieldLabels()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public AdminTeacherManagementService.AdminTeacherExportResult exportProfile(
            Long facultyId, AdminTeacherProfileRequests.ProfileExportRequest request) {
        requireFaculty(request.getSchoolId(), facultyId);
        AdminTeacherRequests.ExportRequest exportReq = AdminTeacherRequests.ExportRequest.builder()
                .schoolId(request.getSchoolId())
                .facultyIds(List.of(facultyId))
                .format(request.getFormat())
                .fields(request.getFields())
                .performedBy(request.getPerformedBy())
                .build();
        return teacherManagementService.exportTeachers(exportReq);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminTeacherDtos.ActionResponse markLeave(Long facultyId, AdminTeacherProfileRequests.MarkLeaveRequest request) {
        Faculty faculty = requireFaculty(request.getSchoolId(), facultyId);
        FacultyProfile profile = requireProfile(faculty);
        profile.setAccountStatus(FacultyAccountStatus.ON_LEAVE);
        facultyProfileRepository.save(profile);
        activityLogger.log(request.getSchoolId(), AdminActivityType.TEACHER_UPDATED,
                "Marked on leave", faculty.getFacultyId(), "FACULTY", facultyId, request.getPerformedBy());
        return new AdminTeacherDtos.ActionResponse(true, "Teacher marked on leave");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminTeacherDtos.ActionResponse deactivate(Long facultyId, AdminTeacherProfileRequests.DeactivateRequest request) {
        Faculty faculty = requireFaculty(request.getSchoolId(), facultyId);
        FacultyProfile profile = requireProfile(faculty);
        profile.setAccountStatus(FacultyAccountStatus.INACTIVE);
        facultyProfileRepository.save(profile);
        activityLogger.log(request.getSchoolId(), AdminActivityType.TEACHER_DEACTIVATED,
                "Account deactivated", faculty.getFacultyId(), "FACULTY", facultyId, request.getPerformedBy());
        return new AdminTeacherDtos.ActionResponse(true, "Teacher deactivated");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminTeacherDtos.ActionResponse assignSubject(Long facultyId, AdminTeacherProfileRequests.AssignSubjectRequest request) {
        Faculty faculty = requireFaculty(request.getSchoolId(), facultyId);
        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found"));
        faculty.setSubject(subject);
        facultyRepository.save(faculty);
        activityLogger.log(request.getSchoolId(), AdminActivityType.TEACHER_ASSIGNED,
                "Primary subject assigned", subject.getName(), "FACULTY", facultyId, request.getPerformedBy());
        return new AdminTeacherDtos.ActionResponse(true, "Primary subject updated");
    }

    private AdminTeacherProfileDtos.ProfileHeader buildHeader(Long schoolId, Long facultyId) {
        Faculty f = requireFaculty(schoolId, facultyId);
        FacultyProfile p = requireProfile(f);
        final String primarySubject = f.getSubject().getName();
        String spec = primarySubject;
        List<ClassSubjectTeacher> assignments = assignmentRepository.findByFaculty_IdAndActiveTrueWithSubject(facultyId);
        if (!assignments.isEmpty()) {
            String extra = assignments.stream()
                    .map(a -> a.getSubject().getName())
                    .filter(n -> !n.equalsIgnoreCase(primarySubject))
                    .distinct()
                    .collect(Collectors.joining(" & "));
            if (!extra.isBlank()) {
                spec = spec + " & " + extra;
            }
        }
        String designation = resolveDesignation(p, f);
        return new AdminTeacherProfileDtos.ProfileHeader(
                f.getId(),
                f.getFirstName() + " " + f.getLastName(),
                f.getFacultyId(),
                spec,
                designation,
                p.getAccountStatus().name(),
                p.getPhotoUrl(),
                f.getPhone(),
                f.getEmail(),
                p.getJoiningDate(),
                designation + " • " + (p.getDepartment() != null ? p.getDepartment() : "General")
        );
    }

    private AdminTeacherProfileDtos.ActivityLogRow toActivityRow(AdminActivityLog log) {
        return new AdminTeacherProfileDtos.ActivityLogRow(
                log.getId(),
                log.getTitle(),
                mapModule(log.getActivityType()),
                log.getCreatedAt() != null ? log.getCreatedAt().format(DATE_FMT) : "",
                log.getPerformedBy() != null ? log.getPerformedBy() : "System"
        );
    }

    private static String mapModule(AdminActivityType type) {
        return switch (type) {
            case TEACHER_ASSIGNED -> "Allocations";
            case CERTIFICATE_UPLOADED -> "Documents";
            case ATTENDANCE_UPDATED -> "Attendance";
            default -> "Teachers";
        };
    }

    private AdminTeacherProfileDtos.DocumentRow toDocumentRow(FacultyDocument doc) {
        return new AdminTeacherProfileDtos.DocumentRow(
                doc.getId(),
                doc.getDocumentName(),
                formatDocTypeLabel(doc.getDocumentType()),
                doc.getFileName(),
                doc.getUploadedAt().format(DATE_FMT),
                doc.getFileSizeBytes() != null ? doc.getFileSizeBytes() : 0
        );
    }

    private static String formatDocTypeLabel(FacultyDocumentType type) {
        return type.name().replace('_', ' ');
    }

    private static String classLabel(String className, String section) {
        if (className == null || className.isBlank()) {
            return "—";
        }
        return section != null && !section.isBlank() ? className + "-" + section : className;
    }

    private static String marksLabel(AssignmentSubmission s) {
        if (s.getLetterGrade() != null && !s.getLetterGrade().isBlank()) {
            return s.getLetterGrade();
        }
        if (s.getPointsObtained() != null) {
            int total = s.getPointsTotal() != null ? s.getPointsTotal()
                    : (s.getAssignment() != null && s.getAssignment().getTotalMarks() != null
                            ? s.getAssignment().getTotalMarks() : 0);
            return total > 0 ? s.getPointsObtained() + "/" + total : String.valueOf(s.getPointsObtained());
        }
        return "—";
    }

    /** Derive a UI status (ACTIVE / GRADING / CLOSED) for an assignment row. */
    private String deriveAssignmentStatus(Assignment a) {
        long ungraded = submissionRepository.countByAssignment_IdAndStatus(a.getId(), AssignmentStatus.SUBMITTED)
                + submissionRepository.countByAssignment_IdAndStatus(a.getId(), AssignmentStatus.LATE);
        if (ungraded > 0) {
            return "GRADING";
        }
        if (a.getDueDate() != null && a.getDueDate().isBefore(LocalDate.now())) {
            return "CLOSED";
        }
        return "ACTIVE";
    }

    private static String attendanceRemark(TeacherAttendanceEntry e) {
        if (e.getLateNote() != null && !e.getLateNote().isBlank()) {
            return e.getLateNote();
        }
        return switch (e.getStatus()) {
            case PRESENT -> "On Time";
            case LATE -> "Late Arrival";
            case HALF_DAY -> "Half Day";
            case LEAVE -> "On Leave";
        };
    }

    private static final String[] DAY_NAMES = {
            "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"
    };

    private static String dayName(Integer dayOfWeek) {
        if (dayOfWeek == null || dayOfWeek < 1 || dayOfWeek > 7) {
            return "—";
        }
        return DAY_NAMES[dayOfWeek - 1];
    }

    private static String resolveDesignation(FacultyProfile p, Faculty f) {
        if (p.getDesignation() != null && !p.getDesignation().isBlank()) {
            return p.getDesignation();
        }
        return f.getExperience() != null && f.getExperience() >= 8 ? "Senior Teacher" : "Teacher";
    }

    private Faculty requireFaculty(Long schoolId, Long facultyId) {
        return facultyRepository.findByIdAndSchool_IdWithDetails(facultyId, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found"));
    }

    private FacultyProfile requireProfile(Faculty faculty) {
        return facultyProfileRepository.findByFaculty_Id(faculty.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Teacher profile not found"));
    }

    private void validateDocumentFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is required");
        }
        if (file.getSize() > MAX_DOC_BYTES) {
            throw new IllegalArgumentException("File must not exceed 10MB");
        }
        String name = file.getOriginalFilename();
        int i = name != null ? name.lastIndexOf('.') : -1;
        String ext = i >= 0 ? name.substring(i + 1).toLowerCase() : "";
        if (!ALLOWED_EXT.contains(ext)) {
            throw new IllegalArgumentException("Only PDF, JPG, and PNG files are allowed");
        }
    }
}
