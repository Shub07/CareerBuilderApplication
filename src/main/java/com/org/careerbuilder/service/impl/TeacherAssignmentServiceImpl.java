package com.org.careerbuilder.service.impl;

import com.org.careerbuilder.dto.request.*;
import com.org.careerbuilder.dto.response.TeacherAssignmentDtos;
import com.org.careerbuilder.exceptions.ResourceNotFoundException;
import com.org.careerbuilder.models.*;
import com.org.careerbuilder.models.enums.AssignmentPublishStatus;
import com.org.careerbuilder.models.enums.AssignmentStatus;
import com.org.careerbuilder.repository.*;
import com.org.careerbuilder.service.TeacherAssignmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@RequiredArgsConstructor
public class TeacherAssignmentServiceImpl implements TeacherAssignmentService {

    private final FacultyRepository facultyRepository;
    private final SubjectRepository subjectRepository;
    private final ClassSubjectTeacherRepository classSubjectTeacherRepository;
    private final StudentRepository studentRepository;
    private final AssignmentRepository assignmentRepository;
    private final AssignmentSubmissionRepository submissionRepository;

    @Override
    @Transactional(readOnly = true)
    public TeacherAssignmentDtos.AssignmentFiltersResponse getFilters(Long facultyId) {
        loadFaculty(facultyId);
        List<ClassSubjectTeacher> csts = classSubjectTeacherRepository.findByFaculty_IdAndActiveTrue(facultyId);
        Map<String, TeacherAssignmentDtos.ClassSectionOption> classes = new LinkedHashMap<>();
        Map<Long, TeacherAssignmentDtos.SubjectOption> subjects = new LinkedHashMap<>();
        for (ClassSubjectTeacher cst : csts) {
            String key = cst.getClassName().toLowerCase(Locale.ROOT) + "|" + cst.getSection().toLowerCase(Locale.ROOT);
            classes.putIfAbsent(key, new TeacherAssignmentDtos.ClassSectionOption(
                    cst.getClassName(), cst.getSection(), "Grade " + cst.getClassName() + " " + cst.getSection()));
            if (cst.getSubject() != null) {
                subjects.putIfAbsent(cst.getSubject().getId(),
                        new TeacherAssignmentDtos.SubjectOption(cst.getSubject().getId(), cst.getSubject().getName()));
            }
        }
        return new TeacherAssignmentDtos.AssignmentFiltersResponse(
                new ArrayList<>(classes.values()),
                new ArrayList<>(subjects.values())
        );
    }

    @Override
    @Transactional(readOnly = true)
    public TeacherAssignmentDtos.AssignmentListPage listAssignments(
            Long facultyId,
            String view,
            String className,
            String section,
            Long subjectId,
            LocalDate dueFrom,
            LocalDate dueTo,
            Integer month,
            String search,
            String displayStatus,
            Pageable pageable) {
        loadFaculty(facultyId);
        Specification<Assignment> spec = Specification.where(teacherEq(facultyId));
        String v = view == null || view.isBlank() ? "current" : view.trim().toLowerCase(Locale.ROOT);
        if ("history".equals(v)) {
            spec = spec.and(historicalSpec());
        } else {
            spec = spec.and(currentSpec());
        }
        if (className != null && !className.isBlank()) {
            spec = spec.and((r, q, cb) -> cb.equal(cb.lower(r.get("className")), className.trim().toLowerCase(Locale.ROOT)));
        }
        if (section != null && !section.isBlank()) {
            spec = spec.and((r, q, cb) -> cb.equal(cb.lower(r.get("section")), section.trim().toLowerCase(Locale.ROOT)));
        }
        if (subjectId != null) {
            spec = spec.and((r, q, cb) -> cb.equal(r.get("subject").get("id"), subjectId));
        }
        if (dueFrom != null) {
            spec = spec.and((r, q, cb) -> cb.greaterThanOrEqualTo(r.get("dueDate"), dueFrom));
        }
        if (dueTo != null) {
            spec = spec.and((r, q, cb) -> cb.lessThanOrEqualTo(r.get("dueDate"), dueTo));
        }
        if (month != null && month >= 1 && month <= 12) {
            int y = LocalDate.now().getYear();
            YearMonth ym = YearMonth.of(y, month);
            spec = spec.and((r, q, cb) -> cb.between(r.get("dueDate"), ym.atDay(1), ym.atEndOfMonth()));
        }
        if (search != null && !search.isBlank()) {
            String p = "%" + search.trim().toLowerCase(Locale.ROOT) + "%";
            spec = spec.and((r, q, cb) -> cb.like(cb.lower(r.get("title")), p));
        }
        if (displayStatus != null && !displayStatus.isBlank()) {
            String ds = displayStatus.trim().toUpperCase(Locale.ROOT);
            spec = spec.and(displayStatusSpec(ds));
        }

        Page<Assignment> page = assignmentRepository.findAll(spec, pageable);
        List<TeacherAssignmentDtos.AssignmentListRow> rows = new ArrayList<>();
        for (Assignment a : page.getContent()) {
            int total = totalStudents(a);
            int submitted = (int) submissionRepository.countSubmittedWithFile(a.getId());
            String display = computeDisplayStatusInst(a, submitted, total);
            rows.add(new TeacherAssignmentDtos.AssignmentListRow(
                    a.getId(),
                    a.getTitle(),
                    safeSubjectName(a),
                    classLabel(a),
                    a.getDueDate(),
                    a.getDueTime(),
                    submitted,
                    total,
                    display,
                    a.getPublishStatus().name()
            ));
        }
        return new TeacherAssignmentDtos.AssignmentListPage(rows, page.getTotalPages(), page.getTotalElements());
    }

    private static Specification<Assignment> displayStatusSpec(String ds) {
        LocalDate today = LocalDate.now();
        return switch (ds) {
            case "DRAFT" -> (r, q, cb) -> cb.equal(r.get("publishStatus"), AssignmentPublishStatus.DRAFT);
            case "PUBLISHED" -> (r, q, cb) -> cb.and(
                    cb.equal(r.get("publishStatus"), AssignmentPublishStatus.PUBLISHED),
                    cb.notEqual(r.get("dueDate"), today)
            );
            case "DUE_TODAY" -> (r, q, cb) -> cb.and(
                    cb.equal(r.get("publishStatus"), AssignmentPublishStatus.PUBLISHED),
                    cb.equal(r.get("dueDate"), today)
            );
            case "CLOSED" -> (r, q, cb) -> cb.equal(r.get("publishStatus"), AssignmentPublishStatus.CLOSED);
            case "EVALUATED" -> (r, q, cb) -> cb.and(
                    cb.equal(r.get("publishStatus"), AssignmentPublishStatus.PUBLISHED),
                    cb.lessThan(r.get("dueDate"), today)
            );
            default -> (r, q, cb) -> cb.conjunction();
        };
    }

    @Override
    @Transactional(readOnly = true)
    public TeacherAssignmentDtos.AssignmentDetailResponse getDetail(Long facultyId, Long assignmentId) {
        Assignment a = loadAssignmentForFaculty(facultyId, assignmentId);
        int total = totalStudents(a);
        int submitted = (int) submissionRepository.countSubmittedWithFile(a.getId());
        int graded = (int) submissionRepository.countByAssignment_IdAndStatus(a.getId(), AssignmentStatus.GRADED);
        int late = (int) submissionRepository.countByAssignment_IdAndStatus(a.getId(), AssignmentStatus.LATE);
        int pending = Math.max(0, total - submitted);
        String display = computeDisplayStatusInst(a, submitted, total);
        return new TeacherAssignmentDtos.AssignmentDetailResponse(
                a.getId(),
                a.getTitle(),
                a.getDescription(),
                safeSubjectName(a),
                a.getSubject().getId(),
                a.getClassName(),
                a.getSection(),
                classLabel(a),
                a.getDueDate(),
                a.getDueTime(),
                a.getPublishStatus().name(),
                display,
                a.getTotalMarks(),
                a.isAllowLateSubmission(),
                a.isAllowResubmission(),
                a.getGivenDate() != null ? a.getGivenDate() : a.getCreatedAt().toLocalDate(),
                a.getAttachmentPath(),
                total,
                submitted,
                pending,
                late,
                graded
        );
    }

    @Override
    @Transactional(readOnly = true)
    public TeacherAssignmentDtos.SubmissionsPage listSubmissions(
            Long facultyId, Long assignmentId, String statusTab, Pageable pageable) {
        Assignment a = loadAssignmentForFaculty(facultyId, assignmentId);
        if (a.getSchoolId() == null || a.getClassName() == null || a.getSection() == null) {
            return new TeacherAssignmentDtos.SubmissionsPage(List.of(), 0, 0);
        }
        List<Student> students = studentRepository.findBySchool_IdAndClassNameAndSection(
                a.getSchoolId(), a.getClassName(), a.getSection());
        students.sort(Comparator.comparing(Student::getRollNo));
        Map<Long, AssignmentSubmission> byStudent = submissionRepository.findByAssignment_Id(assignmentId).stream()
                .collect(Collectors.toMap(s -> s.getStudent().getId(), s -> s, (x, y) -> x));

        String tab = statusTab == null || statusTab.isBlank() ? "ALL" : statusTab.trim().toUpperCase(Locale.ROOT);
        List<TeacherAssignmentDtos.SubmissionRow> all = new ArrayList<>();
        for (Student st : students) {
            AssignmentSubmission sub = byStudent.get(st.getId());
            String rowStatus = rowStatus(sub);
            if (!matchesTab(tab, rowStatus)) {
                continue;
            }
            all.add(toSubmissionRow(st, sub, a));
        }
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), all.size());
        List<TeacherAssignmentDtos.SubmissionRow> slice = start >= all.size() ? List.of() : all.subList(start, end);
        return new TeacherAssignmentDtos.SubmissionsPage(slice,
                (int) Math.ceil(all.size() / (double) pageable.getPageSize()),
                all.size());
    }

    private static boolean matchesTab(String tab, String rowStatus) {
        if ("ALL".equals(tab)) {
            return true;
        }
        if ("SUBMITTED".equals(tab)) {
            return "SUBMITTED".equals(rowStatus) || "LATE".equals(rowStatus) || "CHECKED".equals(rowStatus) || "RETURNED".equals(rowStatus);
        }
        return tab.equals(rowStatus);
    }

    private static String rowStatus(AssignmentSubmission sub) {
        if (sub == null || sub.getFilePath() == null || sub.getFilePath().isBlank()) {
            return "PENDING";
        }
        if (sub.getStatus() == AssignmentStatus.GRADED) {
            return "CHECKED";
        }
        if (sub.getStatus() == AssignmentStatus.RETURNED) {
            return "RETURNED";
        }
        if (sub.getStatus() == AssignmentStatus.LATE) {
            return "LATE";
        }
        return "SUBMITTED";
    }

    private TeacherAssignmentDtos.SubmissionRow toSubmissionRow(Student st, AssignmentSubmission sub, Assignment a) {
        String marks = "--";
        Integer po = null;
        Integer pt = a.getTotalMarks();
        if (sub != null && sub.getPointsObtained() != null) {
            po = sub.getPointsObtained();
            marks = po + " / " + (sub.getPointsTotal() != null ? sub.getPointsTotal() : (pt != null ? pt : "?"));
        }
        String fn = st.getFirstName() == null ? "" : st.getFirstName();
        String ln = st.getLastName() == null ? "" : st.getLastName();
        String initials = ((fn.isEmpty() ? "?" : fn.substring(0, 1)) + (ln.isEmpty() ? "" : ln.substring(0, 1))).toUpperCase(Locale.ROOT);
        return new TeacherAssignmentDtos.SubmissionRow(
                sub != null ? sub.getId() : null,
                st.getId(),
                (fn + " " + ln).trim(),
                st.getRollNo(),
                initials,
                sub != null ? sub.getSubmittedAt() : null,
                rowStatus(sub),
                marks,
                po,
                pt
        );
    }

    @Override
    @Transactional(readOnly = true)
    public TeacherAssignmentDtos.SubmissionReviewResponse getSubmissionReview(Long facultyId, Long assignmentId, Long submissionId) {
        Assignment a = loadAssignmentForFaculty(facultyId, assignmentId);
        AssignmentSubmission s = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found"));
        if (!Objects.equals(s.getAssignment().getId(), assignmentId)) {
            throw new IllegalArgumentException("Submission does not belong to this assignment");
        }
        String name = (s.getStudent().getFirstName() + " " + s.getStudent().getLastName()).trim();
        String hint = s.getFilePath() == null ? null : Paths.get(s.getFilePath()).getFileName().toString();
        return new TeacherAssignmentDtos.SubmissionReviewResponse(
                s.getId(),
                a.getId(),
                s.getStudent().getId(),
                name,
                s.getSubmittedAt(),
                s.getFilePath(),
                hint,
                s.getPointsTotal() != null ? s.getPointsTotal() : a.getTotalMarks(),
                s.getPointsObtained(),
                s.getTeacherRemarks(),
                s.getStatus() == null ? "PENDING" : (s.getStatus() == AssignmentStatus.GRADED ? "CHECKED" : s.getStatus().name())
        );
    }

    @Override
    @Transactional
    public Long createAssignment(Long facultyId, TeacherAssignmentCreateRequest request, MultipartFile attachment) {
        Faculty faculty = loadFaculty(facultyId);
        assertTeaches(facultyId, faculty.getSchool().getId(), request.getClassName(), request.getSection(), request.getSubjectId());
        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found"));
        String path = storeTeacherAttachment(facultyId, attachment);
        AssignmentPublishStatus ps = request.getPublishStatus() != null ? request.getPublishStatus() : AssignmentPublishStatus.PUBLISHED;
        Assignment a = Assignment.builder()
                .teacher(faculty)
                .subject(subject)
                .schoolId(faculty.getSchool().getId())
                .className(request.getClassName().trim())
                .section(request.getSection().trim())
                .title(request.getTitle().trim())
                .description(request.getDescription())
                .dueDate(request.getDueDate())
                .dueTime(request.getDueTime())
                .givenDate(request.getGivenDate() != null ? request.getGivenDate() : LocalDate.now())
                .totalMarks(request.getTotalMarks())
                .allowLateSubmission(Boolean.TRUE.equals(request.getAllowLateSubmission()))
                .allowResubmission(Boolean.TRUE.equals(request.getAllowResubmission()))
                .publishStatus(ps)
                .attachmentPath(path)
                .build();
        return assignmentRepository.save(a).getId();
    }

    @Override
    @Transactional
    public void updateAssignment(Long facultyId, Long assignmentId, TeacherAssignmentUpdateRequest request, MultipartFile attachment) {
        Assignment a = loadAssignmentForFaculty(facultyId, assignmentId);
        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            a.setTitle(request.getTitle().trim());
        }
        if (request.getDescription() != null) {
            a.setDescription(request.getDescription());
        }
        if (request.getSubjectId() != null) {
            Subject sub = subjectRepository.findById(request.getSubjectId())
                    .orElseThrow(() -> new ResourceNotFoundException("Subject not found"));
            a.setSubject(sub);
        }
        if (request.getClassName() != null && !request.getClassName().isBlank()) {
            a.setClassName(request.getClassName().trim());
        }
        if (request.getSection() != null && !request.getSection().isBlank()) {
            a.setSection(request.getSection().trim());
        }
        if (request.getDueDate() != null) {
            a.setDueDate(request.getDueDate());
        }
        if (request.getDueTime() != null) {
            a.setDueTime(request.getDueTime());
        }
        if (request.getTotalMarks() != null) {
            a.setTotalMarks(request.getTotalMarks());
        }
        if (request.getAllowLateSubmission() != null) {
            a.setAllowLateSubmission(request.getAllowLateSubmission());
        }
        if (request.getAllowResubmission() != null) {
            a.setAllowResubmission(request.getAllowResubmission());
        }
        if (request.getPublishStatus() != null) {
            a.setPublishStatus(request.getPublishStatus());
        }
        if (request.getGivenDate() != null) {
            a.setGivenDate(request.getGivenDate());
        }
        if (attachment != null && !attachment.isEmpty()) {
            a.setAttachmentPath(storeTeacherAttachment(facultyId, attachment));
        }
        if (a.getClassName() != null && a.getSection() != null && a.getSubject() != null) {
            assertTeaches(facultyId, a.getSchoolId() != null ? a.getSchoolId() : a.getTeacher().getSchool().getId(),
                    a.getClassName(), a.getSection(), a.getSubject().getId());
        }
        assignmentRepository.save(a);
    }

    @Override
    @Transactional
    public void deleteAssignment(Long facultyId, Long assignmentId) {
        loadAssignmentForFaculty(facultyId, assignmentId);
        submissionRepository.deleteByAssignment_Id(assignmentId);
        assignmentRepository.deleteById(assignmentId);
    }

    @Override
    @Transactional
    public TeacherAssignmentDtos.DuplicateAssignmentResponse duplicateAssignment(Long facultyId, Long assignmentId) {
        Assignment src = loadAssignmentForFaculty(facultyId, assignmentId);
        String newTitle = truncate(src.getTitle() + " (Copy)", 200);
        String newPath = null;
        if (src.getAttachmentPath() != null && !src.getAttachmentPath().isBlank()) {
            try {
                Path p = Paths.get(src.getAttachmentPath());
                if (Files.exists(p)) {
                    Path dir = Paths.get("uploads", "teacher-assignments", String.valueOf(facultyId));
                    Files.createDirectories(dir);
                    Path target = dir.resolve("dup_" + UUID.randomUUID() + "_" + p.getFileName());
                    Files.copy(p, target, StandardCopyOption.REPLACE_EXISTING);
                    newPath = target.toString().replace("\\", "/");
                }
            } catch (Exception ignored) {
                newPath = src.getAttachmentPath();
            }
        }
        Assignment copy = Assignment.builder()
                .teacher(src.getTeacher())
                .subject(src.getSubject())
                .schoolId(src.getSchoolId())
                .className(src.getClassName())
                .section(src.getSection())
                .title(newTitle)
                .description(src.getDescription())
                .dueDate(src.getDueDate())
                .dueTime(src.getDueTime())
                .givenDate(LocalDate.now())
                .totalMarks(src.getTotalMarks())
                .allowLateSubmission(src.isAllowLateSubmission())
                .allowResubmission(src.isAllowResubmission())
                .publishStatus(AssignmentPublishStatus.DRAFT)
                .attachmentPath(newPath)
                .build();
        Assignment saved = assignmentRepository.save(copy);
        return new TeacherAssignmentDtos.DuplicateAssignmentResponse(saved.getId());
    }

    @Override
    @Transactional
    public void gradeSubmission(Long facultyId, Long assignmentId, Long submissionId, TeacherGradeSubmissionRequest request) {
        Assignment a = loadAssignmentForFaculty(facultyId, assignmentId);
        AssignmentSubmission s = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found"));
        if (!Objects.equals(s.getAssignment().getId(), assignmentId)) {
            throw new IllegalArgumentException("Submission does not belong to this assignment");
        }
        int max = a.getTotalMarks() != null ? a.getTotalMarks() : Integer.MAX_VALUE;
        if (request.getPointsObtained() < 0 || request.getPointsObtained() > max) {
            throw new IllegalArgumentException("pointsObtained must be between 0 and " + max);
        }
        s.setPointsObtained(request.getPointsObtained());
        s.setPointsTotal(a.getTotalMarks());
        s.setTeacherRemarks(request.getTeacherRemarks());
        s.setStatus(AssignmentStatus.GRADED);
        submissionRepository.save(s);
    }

    @Override
    @Transactional
    public void returnSubmission(Long facultyId, Long assignmentId, Long submissionId, TeacherReturnSubmissionRequest request) {
        loadAssignmentForFaculty(facultyId, assignmentId);
        AssignmentSubmission s = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found"));
        if (!Objects.equals(s.getAssignment().getId(), assignmentId)) {
            throw new IllegalArgumentException("Submission does not belong to this assignment");
        }
        s.setStatus(AssignmentStatus.RETURNED);
        s.setTeacherRemarks(request.getTeacherRemarks().trim());
        submissionRepository.save(s);
    }

    @Override
    @Transactional(readOnly = true)
    public Resource downloadSubmissionFile(Long facultyId, Long assignmentId, Long studentId) {
        loadAssignmentForFaculty(facultyId, assignmentId);
        AssignmentSubmission s = submissionRepository.findByAssignment_IdAndStudent_Id(assignmentId, studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found"));
        if (s.getFilePath() == null || s.getFilePath().isBlank()) {
            throw new ResourceNotFoundException("No file for this submission");
        }
        Path path = Paths.get(s.getFilePath());
        Resource resource = new PathResource(path);
        if (!resource.exists()) {
            throw new ResourceNotFoundException("File missing on server");
        }
        return resource;
    }

    @Override
    @Transactional(readOnly = true)
    public Resource exportAllSubmissionsZip(Long facultyId, Long assignmentId) {
        loadAssignmentForFaculty(facultyId, assignmentId);
        List<AssignmentSubmission> subs = submissionRepository.findByAssignment_Id(assignmentId);
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream(); ZipOutputStream zos = new ZipOutputStream(bos)) {
            for (AssignmentSubmission s : subs) {
                if (s.getFilePath() == null || s.getFilePath().isBlank()) {
                    continue;
                }
                Path p = Paths.get(s.getFilePath());
                if (!Files.exists(p)) {
                    continue;
                }
                String entry = "roll_" + s.getStudent().getRollNo() + "_" + p.getFileName();
                zos.putNextEntry(new ZipEntry(entry));
                zos.write(Files.readAllBytes(p));
                zos.closeEntry();
            }
            zos.finish();
            return new ByteArrayResource(bos.toByteArray());
        } catch (Exception e) {
            throw new IllegalStateException("Could not build zip export", e);
        }
    }

    // --- specs ---

    private static Specification<Assignment> teacherEq(Long facultyId) {
        return (r, q, cb) -> cb.equal(r.get("teacher").get("id"), facultyId);
    }

    private static Specification<Assignment> historicalSpec() {
        LocalDate today = LocalDate.now();
        return (r, q, cb) -> cb.or(
                cb.equal(r.get("publishStatus"), AssignmentPublishStatus.CLOSED),
                cb.lessThan(r.get("dueDate"), today)
        );
    }

    private static Specification<Assignment> currentSpec() {
        LocalDate today = LocalDate.now();
        return (r, q, cb) -> cb.and(
                cb.notEqual(r.get("publishStatus"), AssignmentPublishStatus.CLOSED),
                cb.or(
                        cb.equal(r.get("publishStatus"), AssignmentPublishStatus.DRAFT),
                        cb.greaterThanOrEqualTo(r.get("dueDate"), today)
                )
        );
    }

    private String computeDisplayStatusInst(Assignment a, int submitted, int total) {
        if (a.getPublishStatus() == AssignmentPublishStatus.DRAFT) {
            return "DRAFT";
        }
        if (a.getPublishStatus() == AssignmentPublishStatus.CLOSED) {
            return "CLOSED";
        }
        LocalDate today = LocalDate.now();
        if (a.getDueDate().equals(today)) {
            return "DUE_TODAY";
        }
        if (submitted > 0) {
            int graded = (int) submissionRepository.countByAssignment_IdAndStatus(a.getId(), AssignmentStatus.GRADED);
            if (graded >= submitted) {
                return "EVALUATED";
            }
        }
        return "PUBLISHED";
    }

    private Faculty loadFaculty(Long facultyId) {
        return facultyRepository.findById(facultyId)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty not found"));
    }

    private Assignment loadAssignmentForFaculty(Long facultyId, Long assignmentId) {
        Assignment a = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found"));
        if (!a.getTeacher().getId().equals(facultyId)) {
            throw new IllegalArgumentException("Assignment belongs to another teacher");
        }
        return a;
    }

    private void assertTeaches(Long facultyId, Long schoolId, String className, String section, Long subjectId) {
        List<ClassSubjectTeacher> list = classSubjectTeacherRepository.findBySchoolIdAndClassNameAndSectionAndSubject_IdAndActiveTrue(
                schoolId, className, section, subjectId);
        boolean ok = list.stream().anyMatch(c -> c.getFaculty().getId().equals(facultyId));
        if (!ok) {
            throw new IllegalArgumentException("Teacher is not assigned to this class and subject");
        }
    }

    private int totalStudents(Assignment a) {
        if (a.getSchoolId() == null || a.getClassName() == null || a.getSection() == null) {
            return 0;
        }
        return (int) studentRepository.countBySchool_IdAndClassNameAndSection(a.getSchoolId(), a.getClassName(), a.getSection());
    }

    private String safeSubjectName(Assignment a) {
        try {
            return a.getSubject() != null && a.getSubject().getName() != null ? a.getSubject().getName() : "—";
        } catch (Exception e) {
            return "—";
        }
    }

    private String classLabel(Assignment a) {
        if (a.getClassName() == null || a.getSection() == null) {
            return "—";
        }
        return "Grade " + a.getClassName() + " " + a.getSection();
    }

    private String storeTeacherAttachment(Long facultyId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        try {
            Path dir = Paths.get("uploads", "teacher-assignments", String.valueOf(facultyId));
            Files.createDirectories(dir);
            String orig = file.getOriginalFilename() != null ? file.getOriginalFilename() : "attachment";
            String safe = orig.replaceAll("[^a-zA-Z0-9._-]", "_");
            Path target = dir.resolve(UUID.randomUUID() + "_" + safe);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            return target.toString().replace("\\", "/");
        } catch (Exception e) {
            throw new IllegalStateException("Failed to store attachment", e);
        }
    }

    private static String truncate(String s, int max) {
        if (s == null) {
            return null;
        }
        return s.length() <= max ? s : s.substring(0, max - 3) + "...";
    }
}
