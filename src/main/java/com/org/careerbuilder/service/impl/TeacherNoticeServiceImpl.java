package com.org.careerbuilder.service.impl;

import com.org.careerbuilder.dto.request.TeacherClassAnnouncementRequest;
import com.org.careerbuilder.dto.request.TeacherStudentAlertRequest;
import com.org.careerbuilder.dto.response.TeacherNoticeDtos;
import com.org.careerbuilder.exceptions.ResourceNotFoundException;
import com.org.careerbuilder.models.*;
import com.org.careerbuilder.models.enums.NoticeCategory;
import com.org.careerbuilder.models.enums.TeacherNoticeStatus;
import com.org.careerbuilder.models.enums.TeacherNoticeType;
import com.org.careerbuilder.repository.*;
import com.org.careerbuilder.service.TeacherNoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeacherNoticeServiceImpl implements TeacherNoticeService {

    private final FacultyRepository facultyRepository;
    private final ClassSubjectTeacherRepository classSubjectTeacherRepository;
    private final StudentRepository studentRepository;
    private final TeacherNoticeRepository teacherNoticeRepository;
    private final TeacherNoticeClassTargetRepository teacherNoticeClassTargetRepository;
    private final TeacherNoticeStudentTargetRepository teacherNoticeStudentTargetRepository;
    private final NoticeRepository noticeRepository;
    private final NoticeAudienceStudentRepository noticeAudienceStudentRepository;

    private static final String NOTICE_UPLOAD_DIR = "uploads/notices";

    private Faculty loadFaculty(Long facultyId) {
        return facultyRepository.findById(facultyId)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty not found"));
    }

    private TeacherNotice loadOwnedTeacherNotice(Long facultyId, Long teacherNoticeId) {
        TeacherNotice notice = teacherNoticeRepository.findById(teacherNoticeId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher notice not found"));
        if (!notice.getFaculty().getId().equals(facultyId)) {
            throw new IllegalArgumentException("Notice belongs to another teacher");
        }
        return notice;
    }

    @Override
    @Transactional(readOnly = true)
    public TeacherNoticeDtos.TeacherNoticeFiltersResponse getFilters(Long facultyId, String studentQuery) {
        Faculty faculty = loadFaculty(facultyId);
        Long schoolId = faculty.getSchool().getId();

        List<ClassSubjectTeacher> assignments = classSubjectTeacherRepository.findByFaculty_IdAndActiveTrue(facultyId);
        Map<String, TeacherNoticeDtos.ClassOption> uniqueClasses = new LinkedHashMap<>();
        for (ClassSubjectTeacher cst : assignments) {
            String key = cst.getClassName() + "::" + cst.getSection();
            uniqueClasses.putIfAbsent(key, new TeacherNoticeDtos.ClassOption(
                    cst.getClassName(),
                    cst.getSection(),
                    "Grade " + cst.getClassName() + " " + cst.getSection()
            ));
        }

        List<Student> students = resolveStudentsForAssignedClasses(schoolId, assignments);
        String q = studentQuery != null ? studentQuery.trim().toLowerCase(Locale.ROOT) : null;
        List<TeacherNoticeDtos.StudentOption> studentOptions = students.stream()
                .filter(s -> q == null || q.isBlank()
                        || (s.getFirstName() + " " + s.getLastName()).toLowerCase(Locale.ROOT).contains(q)
                        || String.valueOf(s.getRollNo()).contains(q))
                .sorted(Comparator.comparing(Student::getClassName)
                        .thenComparing(Student::getSection)
                        .thenComparing(Student::getRollNo))
                .map(s -> new TeacherNoticeDtos.StudentOption(
                        s.getId(),
                        (s.getFirstName() + " " + s.getLastName()).trim(),
                        s.getRollNo(),
                        s.getClassName(),
                        s.getSection(),
                        s.getFirstName() + " " + s.getLastName() + " (" + s.getClassName() + " " + s.getSection() + " / " + s.getRollNo() + ")"
                ))
                .toList();

        return new TeacherNoticeDtos.TeacherNoticeFiltersResponse(new ArrayList<>(uniqueClasses.values()), studentOptions);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeacherNoticeDtos.TeacherNoticeResponse> listNotices(Long facultyId, String tab, String query) {
        loadFaculty(facultyId);
        String normalizedTab = tab == null ? "ALL" : tab.trim().toUpperCase(Locale.ROOT);
        List<TeacherNotice> base = switch (normalizedTab) {
            case "CLASS", "CLASS_ANNOUNCEMENT", "CLASS_ANNOUNCEMENTS" ->
                    teacherNoticeRepository.findByFaculty_IdAndNoticeTypeOrderByCreatedAtDesc(facultyId, TeacherNoticeType.CLASS_ANNOUNCEMENT);
            case "STUDENT", "STUDENT_ALERT", "STUDENT_ALERTS" ->
                    teacherNoticeRepository.findByFaculty_IdAndNoticeTypeOrderByCreatedAtDesc(facultyId, TeacherNoticeType.STUDENT_ALERT);
            default -> teacherNoticeRepository.findByFaculty_IdOrderByCreatedAtDesc(facultyId);
        };

        String q = query != null ? query.trim().toLowerCase(Locale.ROOT) : null;
        return base.stream()
                .filter(n -> q == null || q.isBlank()
                        || n.getTitle().toLowerCase(Locale.ROOT).contains(q)
                        || n.getDescription().toLowerCase(Locale.ROOT).contains(q))
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TeacherNoticeDtos.TeacherNoticeResponse getNotice(Long facultyId, Long teacherNoticeId) {
        return toResponse(loadOwnedTeacherNotice(facultyId, teacherNoticeId));
    }

    @Override
    public TeacherNoticeDtos.AttachmentUploadResponse uploadAttachment(Long facultyId, MultipartFile file) {
        loadFaculty(facultyId);
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Attachment file is required");
        }
        String originalName = Objects.requireNonNullElse(file.getOriginalFilename(), "attachment.bin");
        String safeOriginal = Paths.get(originalName).getFileName().toString().replaceAll("[^a-zA-Z0-9._-]", "_");
        String storedName = System.currentTimeMillis() + "_" + safeOriginal;
        Path dir = Paths.get(NOTICE_UPLOAD_DIR);
        Path path = dir.resolve(storedName);
        try {
            Files.createDirectories(dir);
            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to store attachment");
        }
        return new TeacherNoticeDtos.AttachmentUploadResponse(
                path.toString(),
                originalName,
                file.getContentType(),
                file.getSize()
        );
    }

    @Override
    @Transactional
    public TeacherNoticeDtos.TeacherNoticeResponse createClassAnnouncement(Long facultyId, TeacherClassAnnouncementRequest request) {
        Faculty faculty = loadFaculty(facultyId);
        Long schoolId = faculty.getSchool().getId();

        List<ClassSubjectTeacher> assignments = classSubjectTeacherRepository.findByFaculty_IdAndActiveTrue(facultyId);
        Set<String> allowed = assignments.stream()
                .map(c -> c.getClassName().toLowerCase(Locale.ROOT) + "::" + c.getSection().toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());

        TeacherNotice notice = TeacherNotice.builder()
                .faculty(faculty)
                .schoolId(schoolId)
                .noticeType(TeacherNoticeType.CLASS_ANNOUNCEMENT)
                .status(request.isDraft() ? TeacherNoticeStatus.DRAFT : TeacherNoticeStatus.PUBLISHED)
                .title(request.getTitle().trim())
                .description(request.getDescription().trim())
                .attachmentUrl(request.getAttachmentUrl())
                .attachmentFileName(request.getAttachmentFileName())
                .attachmentFileType(request.getAttachmentFileType())
                .attachmentFileSize(request.getAttachmentFileSize())
                .build();
        TeacherNotice saved = teacherNoticeRepository.save(notice);

        for (TeacherClassAnnouncementRequest.ClassTarget target : request.getClasses()) {
            String className = target.getClassName().trim();
            String section = target.getSection().trim();
            String key = className.toLowerCase(Locale.ROOT) + "::" + section.toLowerCase(Locale.ROOT);
            if (!allowed.contains(key)) {
                throw new IllegalArgumentException("Teacher is not assigned to class " + className + " " + section);
            }
            teacherNoticeClassTargetRepository.save(TeacherNoticeClassTarget.builder()
                    .teacherNotice(saved)
                    .className(className)
                    .section(section)
                    .build());
        }

        if (saved.getStatus() == TeacherNoticeStatus.PUBLISHED) {
            publishInternal(saved);
        }

        return toResponse(saved);
    }

    @Override
    @Transactional
    public TeacherNoticeDtos.TeacherNoticeResponse createStudentAlert(Long facultyId, TeacherStudentAlertRequest request) {
        Faculty faculty = loadFaculty(facultyId);
        Long schoolId = faculty.getSchool().getId();
        List<ClassSubjectTeacher> assignments = classSubjectTeacherRepository.findByFaculty_IdAndActiveTrue(facultyId);
        Set<String> allowedClasses = assignments.stream()
                .map(c -> c.getClassName().toLowerCase(Locale.ROOT) + "::" + c.getSection().toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());

        List<Student> students = studentRepository.findAllById(request.getStudentIds());
        if (students.size() != new HashSet<>(request.getStudentIds()).size()) {
            throw new IllegalArgumentException("One or more students not found");
        }
        for (Student student : students) {
            if (!student.getSchool().getId().equals(schoolId)) {
                throw new IllegalArgumentException("Student does not belong to teacher school");
            }
            String key = student.getClassName().toLowerCase(Locale.ROOT) + "::" + student.getSection().toLowerCase(Locale.ROOT);
            if (!allowedClasses.contains(key)) {
                throw new IllegalArgumentException("Teacher is not assigned to student class for studentId " + student.getId());
            }
        }

        TeacherNotice notice = TeacherNotice.builder()
                .faculty(faculty)
                .schoolId(schoolId)
                .noticeType(TeacherNoticeType.STUDENT_ALERT)
                .status(request.isDraft() ? TeacherNoticeStatus.DRAFT : TeacherNoticeStatus.PUBLISHED)
                .title(request.getTitle().trim())
                .description(request.getDescription().trim())
                .attachmentUrl(request.getAttachmentUrl())
                .attachmentFileName(request.getAttachmentFileName())
                .attachmentFileType(request.getAttachmentFileType())
                .attachmentFileSize(request.getAttachmentFileSize())
                .build();
        TeacherNotice saved = teacherNoticeRepository.save(notice);
        for (Student student : students) {
            teacherNoticeStudentTargetRepository.save(TeacherNoticeStudentTarget.builder()
                    .teacherNotice(saved)
                    .student(student)
                    .build());
        }

        if (saved.getStatus() == TeacherNoticeStatus.PUBLISHED) {
            publishInternal(saved);
        }
        return toResponse(saved);
    }

    @Override
    @Transactional
    public TeacherNoticeDtos.TeacherNoticeResponse updateClassAnnouncement(Long facultyId, Long teacherNoticeId, TeacherClassAnnouncementRequest request) {
        TeacherNotice notice = loadOwnedTeacherNotice(facultyId, teacherNoticeId);
        if (notice.getNoticeType() != TeacherNoticeType.CLASS_ANNOUNCEMENT) {
            throw new IllegalArgumentException("Notice type is not class announcement");
        }
        Long schoolId = notice.getSchoolId();
        List<ClassSubjectTeacher> assignments = classSubjectTeacherRepository.findByFaculty_IdAndActiveTrue(facultyId);
        Set<String> allowed = assignments.stream()
                .map(c -> c.getClassName().toLowerCase(Locale.ROOT) + "::" + c.getSection().toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());

        teacherNoticeClassTargetRepository.deleteByTeacherNotice_Id(notice.getId());
        teacherNoticeStudentTargetRepository.deleteByTeacherNotice_Id(notice.getId());

        for (TeacherClassAnnouncementRequest.ClassTarget target : request.getClasses()) {
            String className = target.getClassName().trim();
            String section = target.getSection().trim();
            String key = className.toLowerCase(Locale.ROOT) + "::" + section.toLowerCase(Locale.ROOT);
            if (!allowed.contains(key)) {
                throw new IllegalArgumentException("Teacher is not assigned to class " + className + " " + section);
            }
            teacherNoticeClassTargetRepository.save(TeacherNoticeClassTarget.builder()
                    .teacherNotice(notice)
                    .className(className)
                    .section(section)
                    .build());
        }

        notice.setTitle(request.getTitle().trim());
        notice.setDescription(request.getDescription().trim());
        notice.setAttachmentUrl(request.getAttachmentUrl());
        notice.setAttachmentFileName(request.getAttachmentFileName());
        notice.setAttachmentFileType(request.getAttachmentFileType());
        notice.setAttachmentFileSize(request.getAttachmentFileSize());
        if (notice.getStatus() == TeacherNoticeStatus.DRAFT) {
            notice.setStatus(request.isDraft() ? TeacherNoticeStatus.DRAFT : TeacherNoticeStatus.PUBLISHED);
        }
        teacherNoticeRepository.save(notice);

        if (notice.getStatus() == TeacherNoticeStatus.PUBLISHED) {
            if (notice.getPublicNotice() == null) {
                publishInternal(notice);
            } else {
                syncPublishedNoticeAudienceAndContent(notice, schoolId);
            }
        }
        return toResponse(notice);
    }

    @Override
    @Transactional
    public TeacherNoticeDtos.TeacherNoticeResponse updateStudentAlert(Long facultyId, Long teacherNoticeId, TeacherStudentAlertRequest request) {
        TeacherNotice notice = loadOwnedTeacherNotice(facultyId, teacherNoticeId);
        if (notice.getNoticeType() != TeacherNoticeType.STUDENT_ALERT) {
            throw new IllegalArgumentException("Notice type is not student alert");
        }
        Long schoolId = notice.getSchoolId();
        List<ClassSubjectTeacher> assignments = classSubjectTeacherRepository.findByFaculty_IdAndActiveTrue(facultyId);
        Set<String> allowedClasses = assignments.stream()
                .map(c -> c.getClassName().toLowerCase(Locale.ROOT) + "::" + c.getSection().toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());

        List<Student> students = studentRepository.findAllById(request.getStudentIds());
        if (students.size() != new HashSet<>(request.getStudentIds()).size()) {
            throw new IllegalArgumentException("One or more students not found");
        }
        for (Student student : students) {
            if (!student.getSchool().getId().equals(schoolId)) {
                throw new IllegalArgumentException("Student does not belong to teacher school");
            }
            String key = student.getClassName().toLowerCase(Locale.ROOT) + "::" + student.getSection().toLowerCase(Locale.ROOT);
            if (!allowedClasses.contains(key)) {
                throw new IllegalArgumentException("Teacher is not assigned to student class for studentId " + student.getId());
            }
        }

        teacherNoticeStudentTargetRepository.deleteByTeacherNotice_Id(notice.getId());
        for (Student student : students) {
            teacherNoticeStudentTargetRepository.save(TeacherNoticeStudentTarget.builder()
                    .teacherNotice(notice)
                    .student(student)
                    .build());
        }

        notice.setTitle(request.getTitle().trim());
        notice.setDescription(request.getDescription().trim());
        notice.setAttachmentUrl(request.getAttachmentUrl());
        notice.setAttachmentFileName(request.getAttachmentFileName());
        notice.setAttachmentFileType(request.getAttachmentFileType());
        notice.setAttachmentFileSize(request.getAttachmentFileSize());
        if (notice.getStatus() == TeacherNoticeStatus.DRAFT) {
            notice.setStatus(request.isDraft() ? TeacherNoticeStatus.DRAFT : TeacherNoticeStatus.PUBLISHED);
        }
        teacherNoticeRepository.save(notice);

        if (notice.getStatus() == TeacherNoticeStatus.PUBLISHED) {
            if (notice.getPublicNotice() == null) {
                publishInternal(notice);
            } else {
                syncPublishedNoticeAudienceAndContent(notice, schoolId);
            }
        }
        return toResponse(notice);
    }

    @Override
    @Transactional
    public TeacherNoticeDtos.TeacherNoticeResponse publish(Long facultyId, Long teacherNoticeId) {
        TeacherNotice notice = loadOwnedTeacherNotice(facultyId, teacherNoticeId);
        if (notice.getStatus() == TeacherNoticeStatus.PUBLISHED) {
            return toResponse(notice);
        }
        notice.setStatus(TeacherNoticeStatus.PUBLISHED);
        publishInternal(notice);
        return toResponse(notice);
    }

    @Override
    @Transactional
    public void deleteNotice(Long facultyId, Long teacherNoticeId) {
        TeacherNotice notice = loadOwnedTeacherNotice(facultyId, teacherNoticeId);
        teacherNoticeClassTargetRepository.deleteByTeacherNotice_Id(teacherNoticeId);
        teacherNoticeStudentTargetRepository.deleteByTeacherNotice_Id(teacherNoticeId);
        if (notice.getPublicNotice() != null) {
            noticeAudienceStudentRepository.deleteByNotice_Id(notice.getPublicNotice().getId());
            noticeRepository.delete(notice.getPublicNotice());
        }
        teacherNoticeRepository.delete(notice);
    }

    @Override
    @Transactional(readOnly = true)
    public Resource downloadAttachment(Long facultyId, Long teacherNoticeId) {
        TeacherNotice notice = loadOwnedTeacherNotice(facultyId, teacherNoticeId);
        if (notice.getAttachmentUrl() == null || notice.getAttachmentUrl().isBlank()) {
            throw new ResourceNotFoundException("Attachment not found");
        }
        Resource resource = new PathResource(Paths.get(notice.getAttachmentUrl()));
        if (!resource.exists()) {
            throw new ResourceNotFoundException("Attachment file does not exist");
        }
        return resource;
    }

    private List<Student> resolveStudentsForAssignedClasses(Long schoolId, List<ClassSubjectTeacher> assignments) {
        Set<String> classSectionKeys = assignments.stream()
                .map(c -> c.getClassName().toLowerCase(Locale.ROOT) + "::" + c.getSection().toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());
        return studentRepository.findAll().stream()
                .filter(s -> s.getSchool().getId().equals(schoolId))
                .filter(s -> classSectionKeys.contains(s.getClassName().toLowerCase(Locale.ROOT) + "::" + s.getSection().toLowerCase(Locale.ROOT)))
                .toList();
    }

    private void publishInternal(TeacherNotice notice) {
        if (notice.getPublicNotice() != null) {
            return;
        }
        String source = (notice.getFaculty().getFirstName() + " " + notice.getFaculty().getLastName()).trim();
        Notice publicNotice = Notice.builder()
                .schoolId(notice.getSchoolId())
                .title(notice.getTitle())
                .description(notice.getDescription())
                .body(notice.getDescription())
                .source(source)
                .category(NoticeCategory.GENERAL)
                .isPinned(false)
                .createdAt(LocalDateTime.now())
                .build();
        Notice savedPublic = noticeRepository.save(publicNotice);
        notice.setPublicNotice(savedPublic);
        notice.setPublishedAt(LocalDateTime.now());
        teacherNoticeRepository.save(notice);

        List<Student> recipients;
        if (notice.getNoticeType() == TeacherNoticeType.CLASS_ANNOUNCEMENT) {
            List<TeacherNoticeClassTarget> classTargets = teacherNoticeClassTargetRepository.findByTeacherNotice_Id(notice.getId());
            Set<String> allowed = classTargets.stream()
                    .map(t -> t.getClassName().toLowerCase(Locale.ROOT) + "::" + t.getSection().toLowerCase(Locale.ROOT))
                    .collect(Collectors.toSet());
            recipients = studentRepository.findAll().stream()
                    .filter(s -> s.getSchool().getId().equals(notice.getSchoolId()))
                    .filter(s -> allowed.contains(s.getClassName().toLowerCase(Locale.ROOT) + "::" + s.getSection().toLowerCase(Locale.ROOT)))
                    .toList();
            for (Student student : recipients) {
                teacherNoticeStudentTargetRepository.save(TeacherNoticeStudentTarget.builder()
                        .teacherNotice(notice)
                        .student(student)
                        .build());
            }
        } else {
            recipients = teacherNoticeStudentTargetRepository.findByTeacherNotice_Id(notice.getId())
                    .stream()
                    .map(TeacherNoticeStudentTarget::getStudent)
                    .toList();
        }

        for (Student recipient : recipients) {
            noticeAudienceStudentRepository.save(NoticeAudienceStudent.builder()
                    .notice(savedPublic)
                    .student(recipient)
                    .build());
        }
    }

    private void syncPublishedNoticeAudienceAndContent(TeacherNotice notice, Long schoolId) {
        Notice publicNotice = notice.getPublicNotice();
        publicNotice.setTitle(notice.getTitle());
        publicNotice.setDescription(notice.getDescription());
        publicNotice.setBody(notice.getDescription());
        noticeRepository.save(publicNotice);

        noticeAudienceStudentRepository.deleteByNotice_Id(publicNotice.getId());
        List<Student> recipients;
        if (notice.getNoticeType() == TeacherNoticeType.CLASS_ANNOUNCEMENT) {
            List<TeacherNoticeClassTarget> classTargets = teacherNoticeClassTargetRepository.findByTeacherNotice_Id(notice.getId());
            Set<String> allowed = classTargets.stream()
                    .map(t -> t.getClassName().toLowerCase(Locale.ROOT) + "::" + t.getSection().toLowerCase(Locale.ROOT))
                    .collect(Collectors.toSet());
            recipients = studentRepository.findAll().stream()
                    .filter(s -> s.getSchool().getId().equals(schoolId))
                    .filter(s -> allowed.contains(s.getClassName().toLowerCase(Locale.ROOT) + "::" + s.getSection().toLowerCase(Locale.ROOT)))
                    .toList();
            teacherNoticeStudentTargetRepository.deleteByTeacherNotice_Id(notice.getId());
            for (Student student : recipients) {
                teacherNoticeStudentTargetRepository.save(TeacherNoticeStudentTarget.builder()
                        .teacherNotice(notice)
                        .student(student)
                        .build());
            }
        } else {
            recipients = teacherNoticeStudentTargetRepository.findByTeacherNotice_Id(notice.getId())
                    .stream()
                    .map(TeacherNoticeStudentTarget::getStudent)
                    .toList();
        }
        for (Student student : recipients) {
            noticeAudienceStudentRepository.save(NoticeAudienceStudent.builder()
                    .notice(publicNotice)
                    .student(student)
                    .build());
        }
    }

    private TeacherNoticeDtos.TeacherNoticeResponse toResponse(TeacherNotice notice) {
        List<TeacherNoticeClassTarget> classTargets = teacherNoticeClassTargetRepository.findByTeacherNotice_Id(notice.getId());
        List<TeacherNoticeStudentTarget> studentTargets = teacherNoticeStudentTargetRepository.findByTeacherNotice_Id(notice.getId());

        String sentTo = notice.getNoticeType() == TeacherNoticeType.CLASS_ANNOUNCEMENT
                ? classTargets.stream()
                        .map(t -> "Grade " + t.getClassName() + " " + t.getSection())
                        .distinct()
                        .collect(Collectors.joining(", "))
                : studentTargets.size() + " Students";

        List<TeacherNoticeDtos.ClassTargetRef> classRefs = classTargets.stream()
                .map(t -> new TeacherNoticeDtos.ClassTargetRef(t.getClassName(), t.getSection()))
                .distinct()
                .toList();
        List<Long> studentIds = studentTargets.stream()
                .map(t -> t.getStudent().getId())
                .toList();

        return new TeacherNoticeDtos.TeacherNoticeResponse(
                notice.getId(),
                notice.getNoticeType().name(),
                notice.getStatus().name(),
                notice.getTitle(),
                notice.getDescription(),
                sentTo,
                studentTargets.isEmpty() && notice.getNoticeType() == TeacherNoticeType.STUDENT_ALERT
                        ? 0
                        : (notice.getNoticeType() == TeacherNoticeType.CLASS_ANNOUNCEMENT
                                ? classTargets.size()
                                : studentTargets.size()),
                classRefs,
                studentIds,
                notice.getAttachmentUrl(),
                notice.getAttachmentFileName(),
                notice.getAttachmentFileType(),
                notice.getAttachmentFileSize(),
                notice.getPublicNotice() != null ? notice.getPublicNotice().getId() : null,
                notice.getPublishedAt(),
                notice.getCreatedAt(),
                notice.getUpdatedAt()
        );
    }
}
