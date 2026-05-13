package com.org.careerbuilder.service.impl;

import com.org.careerbuilder.dto.request.*;
import com.org.careerbuilder.dto.response.LessonPlanDtos;
import com.org.careerbuilder.exceptions.ResourceNotFoundException;
import com.org.careerbuilder.models.*;
import com.org.careerbuilder.models.enums.LessonPlanTopicStatus;
import com.org.careerbuilder.repository.*;
import com.org.careerbuilder.service.TeacherLessonPlanService;
import com.org.careerbuilder.service.support.LessonPlanSharedHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class TeacherLessonPlanServiceImpl implements TeacherLessonPlanService {

    private final LessonPlanSharedHelper helper;
    private final ClassSubjectTeacherRepository classSubjectTeacherRepository;
    private final LessonPlanTopicRepository topicRepository;
    private final LessonPlanSessionLogRepository sessionLogRepository;
    private final LearningMaterialRepository materialRepository;
    private final SubjectRepository subjectRepository;

    @Override
    @Transactional(readOnly = true)
    public LessonPlanDtos.LessonPlanFiltersResponse getFilters(Long facultyId) {
        helper.loadFaculty(facultyId);
        List<ClassSubjectTeacher> csts = classSubjectTeacherRepository.findByFaculty_IdAndActiveTrue(facultyId);
        Map<String, LessonPlanDtos.ClassSectionOption> classes = new LinkedHashMap<>();
        Map<Long, LessonPlanDtos.SubjectOption> subjects = new LinkedHashMap<>();
        for (ClassSubjectTeacher cst : csts) {
            String key = cst.getClassName().toLowerCase(Locale.ROOT) + "|" + cst.getSection().toLowerCase(Locale.ROOT);
            classes.putIfAbsent(key, new LessonPlanDtos.ClassSectionOption(
                    cst.getClassName(), cst.getSection(),
                    helper.classLabel(cst.getClassName(), cst.getSection())));
            if (cst.getSubject() != null) {
                subjects.putIfAbsent(cst.getSubject().getId(),
                        new LessonPlanDtos.SubjectOption(cst.getSubject().getId(), cst.getSubject().getName()));
            }
        }
        return new LessonPlanDtos.LessonPlanFiltersResponse(new ArrayList<>(classes.values()), new ArrayList<>(subjects.values()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<LessonPlanDtos.TopicRow> listTopics(Long facultyId, String className, String section, Long subjectId) {
        Faculty f = helper.loadFaculty(facultyId);
        if (className == null || className.isBlank() || section == null || section.isBlank() || subjectId == null) {
            throw new IllegalArgumentException("className, section, and subjectId are required");
        }
        helper.assertTeaches(facultyId, f.getSchool().getId(), className.trim(), section.trim(), subjectId);
        List<LessonPlanTopic> list = topicRepository.findByTeacher_IdAndSchoolIdAndClassNameAndSectionAndSubject_IdOrderBySortOrderAscIdAsc(
                facultyId, f.getSchool().getId(), className.trim(), section.trim(), subjectId);
        return list.stream().map(this::toTopicRow).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public LessonPlanDtos.TopicDetailResponse getTopicDetail(Long facultyId, Long topicId) {
        LessonPlanTopic t = loadTopicForTeacher(facultyId, topicId);
        return toTopicDetail(t);
    }

    @Override
    @Transactional
    public Long createTopic(Long facultyId, LessonPlanTopicUpsertRequest request) {
        Faculty f = helper.loadFaculty(facultyId);
        helper.assertTeaches(facultyId, f.getSchool().getId(), request.getClassName(), request.getSection(), request.getSubjectId());
        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found"));
        LessonPlanTopicStatus st = request.getStatus() != null ? request.getStatus() : LessonPlanTopicStatus.PENDING;
        LessonPlanTopic topic = LessonPlanTopic.builder()
                .schoolId(f.getSchool().getId())
                .teacher(f)
                .subject(subject)
                .className(request.getClassName().trim())
                .section(request.getSection().trim())
                .title(request.getTitle().trim())
                .description(request.getDescription())
                .sortOrder(request.getSortOrder())
                .status(st)
                .lastTaughtDate(request.getLastTaughtDate())
                .build();
        return topicRepository.save(topic).getId();
    }

    @Override
    @Transactional
    public void updateTopic(Long facultyId, Long topicId, LessonPlanTopicUpsertRequest request) {
        LessonPlanTopic t = loadTopicForTeacher(facultyId, topicId);
        helper.assertTeaches(facultyId, t.getSchoolId(), request.getClassName(), request.getSection(), request.getSubjectId());
        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found"));
        t.setTitle(request.getTitle().trim());
        t.setDescription(request.getDescription());
        t.setSortOrder(request.getSortOrder());
        t.setClassName(request.getClassName().trim());
        t.setSection(request.getSection().trim());
        t.setSubject(subject);
        if (request.getStatus() != null) {
            t.setStatus(request.getStatus());
        }
        if (request.getLastTaughtDate() != null) {
            t.setLastTaughtDate(request.getLastTaughtDate());
        }
        topicRepository.save(t);
    }

    @Override
    @Transactional
    public void deleteTopic(Long facultyId, Long topicId) {
        LessonPlanTopic t = loadTopicForTeacher(facultyId, topicId);
        List<LessonPlanSessionLog> logs = sessionLogRepository.findByTopic_IdOrderBySessionDateDescIdDesc(topicId);
        for (LessonPlanSessionLog log : logs) {
            helper.deleteStoredFile(log.getMaterialPath());
        }
        materialRepository.findAll((root, q, cb) -> cb.equal(root.get("topic").get("id"), topicId))
                .forEach(m -> {
                    helper.deleteStoredFile(m.getStoredPath());
                    materialRepository.delete(m);
                });
        topicRepository.delete(t);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LessonPlanDtos.SessionLogRow> listSessionLogs(Long facultyId, Long topicId) {
        loadTopicForTeacher(facultyId, topicId);
        return sessionLogRepository.findByTopic_IdOrderBySessionDateDescIdDesc(topicId).stream()
                .map(this::toSessionLogRow)
                .toList();
    }

    @Override
    @Transactional
    public Long addSessionLog(Long facultyId, Long topicId, LessonPlanSessionLogRequest request, MultipartFile attachment) {
        LessonPlanTopic topic = loadTopicForTeacher(facultyId, topicId);
        String path = helper.storeUploadedFile(facultyId, "lesson-session-materials", attachment);
        LessonPlanSessionLog log = LessonPlanSessionLog.builder()
                .topic(topic)
                .sessionDate(request.getSessionDate())
                .sessionTime(request.getSessionTime())
                .notes(request.getNotes())
                .materialPath(path)
                .markTopicCompleted(request.isMarkTopicCompleted())
                .build();
        sessionLogRepository.save(log);
        applyMarkCompleted(topic, request.isMarkTopicCompleted(), request.getSessionDate());
        recomputeLastTaught(topic);
        topicRepository.save(topic);
        return log.getId();
    }

    @Override
    @Transactional
    public void updateSessionLog(Long facultyId, Long topicId, Long logId, LessonPlanSessionLogRequest request, MultipartFile attachment) {
        LessonPlanTopic topic = loadTopicForTeacher(facultyId, topicId);
        LessonPlanSessionLog log = sessionLogRepository.findById(logId)
                .orElseThrow(() -> new ResourceNotFoundException("Session log not found"));
        if (!log.getTopic().getId().equals(topic.getId())) {
            throw new IllegalArgumentException("Log does not belong to this topic");
        }
        if (attachment != null && !attachment.isEmpty()) {
            helper.deleteStoredFile(log.getMaterialPath());
            log.setMaterialPath(helper.storeUploadedFile(facultyId, "lesson-session-materials", attachment));
        }
        log.setSessionDate(request.getSessionDate());
        log.setSessionTime(request.getSessionTime());
        log.setNotes(request.getNotes());
        log.setMarkTopicCompleted(request.isMarkTopicCompleted());
        sessionLogRepository.save(log);
        applyMarkCompleted(topic, request.isMarkTopicCompleted(), request.getSessionDate());
        recomputeLastTaught(topic);
        topicRepository.save(topic);
    }

    @Override
    @Transactional
    public void deleteSessionLog(Long facultyId, Long topicId, Long logId) {
        LessonPlanTopic topic = loadTopicForTeacher(facultyId, topicId);
        LessonPlanSessionLog log = sessionLogRepository.findById(logId)
                .orElseThrow(() -> new ResourceNotFoundException("Session log not found"));
        if (!log.getTopic().getId().equals(topic.getId())) {
            throw new IllegalArgumentException("Log does not belong to this topic");
        }
        helper.deleteStoredFile(log.getMaterialPath());
        sessionLogRepository.delete(log);
        recomputeLastTaught(topic);
        topicRepository.save(topic);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LessonPlanDtos.LearningMaterialRow> listMaterials(
            Long facultyId, String className, String section, Long subjectId, Long topicId, String search) {
        helper.loadFaculty(facultyId);
        Specification<LearningMaterial> spec = (r, q, cb) -> cb.equal(r.get("teacher").get("id"), facultyId);
        if (className != null && !className.isBlank()) {
            spec = spec.and((r, q, cb) -> cb.equal(cb.lower(r.get("className")), className.trim().toLowerCase(Locale.ROOT)));
        }
        if (section != null && !section.isBlank()) {
            spec = spec.and((r, q, cb) -> cb.equal(cb.lower(r.get("section")), section.trim().toLowerCase(Locale.ROOT)));
        }
        if (subjectId != null) {
            spec = spec.and((r, q, cb) -> cb.equal(r.get("subject").get("id"), subjectId));
        }
        if (topicId != null) {
            spec = spec.and((r, q, cb) -> cb.equal(r.get("topic").get("id"), topicId));
        }
        if (search != null && !search.isBlank()) {
            String p = "%" + search.trim().toLowerCase(Locale.ROOT) + "%";
            spec = spec.and((r, q, cb) -> cb.or(
                    cb.like(cb.lower(r.get("title")), p),
                    cb.like(cb.lower(r.get("originalFilename")), p)
            ));
        }
        return materialRepository.findAll(spec).stream()
                .sorted(Comparator.comparing(LearningMaterial::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .map(this::toMaterialRow)
                .toList();
    }

    @Override
    @Transactional
    public Long uploadMaterial(Long facultyId, LearningMaterialMetadataRequest meta, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is required");
        }
        Faculty f = helper.loadFaculty(facultyId);
        helper.assertTeaches(facultyId, f.getSchool().getId(), meta.getClassName(), meta.getSection(), meta.getSubjectId());
        Subject subject = subjectRepository.findById(meta.getSubjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found"));
        LessonPlanTopic linkedTopic = null;
        if (meta.getTopicId() != null) {
            linkedTopic = loadTopicForTeacher(facultyId, meta.getTopicId());
            if (!linkedTopic.getSubject().getId().equals(meta.getSubjectId())
                    || !linkedTopic.getClassName().equalsIgnoreCase(meta.getClassName().trim())
                    || !linkedTopic.getSection().equalsIgnoreCase(meta.getSection().trim())) {
                throw new IllegalArgumentException("Topic does not match class, section, or subject");
            }
        }
        String stored = helper.storeUploadedFile(facultyId, "learning-materials", file);
        boolean vis = meta.getVisibleToStudents() == null || Boolean.TRUE.equals(meta.getVisibleToStudents());
        LearningMaterial m = LearningMaterial.builder()
                .schoolId(f.getSchool().getId())
                .teacher(f)
                .subject(subject)
                .className(meta.getClassName().trim())
                .section(meta.getSection().trim())
                .topic(linkedTopic)
                .title(meta.getTitle().trim())
                .originalFilename(file.getOriginalFilename())
                .storedPath(stored)
                .fileSizeBytes(file.getSize())
                .mimeType(file.getContentType())
                .visibleToStudents(vis)
                .dueDate(meta.getDueDate())
                .build();
        return materialRepository.save(m).getId();
    }

    @Override
    @Transactional
    public void deleteMaterial(Long facultyId, Long materialId) {
        LearningMaterial m = loadMaterialForTeacher(facultyId, materialId);
        helper.deleteStoredFile(m.getStoredPath());
        materialRepository.delete(m);
    }

    @Override
    @Transactional
    public void setMaterialVisibility(Long facultyId, Long materialId, LearningMaterialVisibilityRequest request) {
        LearningMaterial m = loadMaterialForTeacher(facultyId, materialId);
        m.setVisibleToStudents(Boolean.TRUE.equals(request.visibleToStudents()));
        materialRepository.save(m);
    }

    @Override
    @Transactional(readOnly = true)
    public Resource downloadMaterialTeacher(Long facultyId, Long materialId) {
        LearningMaterial m = loadMaterialForTeacher(facultyId, materialId);
        return toFileResource(m.getStoredPath());
    }

    private Resource toFileResource(String storedPath) {
        Path path = Paths.get(storedPath);
        PathResource resource = new PathResource(path);
        if (!resource.exists()) {
            throw new ResourceNotFoundException("File not found on server");
        }
        return resource;
    }

    private LearningMaterial loadMaterialForTeacher(Long facultyId, Long materialId) {
        LearningMaterial m = materialRepository.findById(materialId)
                .orElseThrow(() -> new ResourceNotFoundException("Material not found"));
        if (!m.getTeacher().getId().equals(facultyId)) {
            throw new ResourceNotFoundException("Material not found");
        }
        return m;
    }

    private LessonPlanTopic loadTopicForTeacher(Long facultyId, Long topicId) {
        LessonPlanTopic t = topicRepository.findById(topicId)
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found"));
        if (!t.getTeacher().getId().equals(facultyId)) {
            throw new ResourceNotFoundException("Topic not found");
        }
        return t;
    }

    private void applyMarkCompleted(LessonPlanTopic topic, boolean mark, LocalDate sessionDate) {
        if (mark) {
            topic.setStatus(LessonPlanTopicStatus.COMPLETED);
            if (sessionDate != null) {
                if (topic.getLastTaughtDate() == null || sessionDate.isAfter(topic.getLastTaughtDate())) {
                    topic.setLastTaughtDate(sessionDate);
                }
            }
        }
    }

    private void recomputeLastTaught(LessonPlanTopic topic) {
        List<LessonPlanSessionLog> logs = sessionLogRepository.findByTopic_IdOrderBySessionDateDescIdDesc(topic.getId());
        Optional<LocalDate> max = logs.stream().map(LessonPlanSessionLog::getSessionDate).max(Comparator.naturalOrder());
        topic.setLastTaughtDate(max.orElse(null));
    }

    private LessonPlanDtos.TopicRow toTopicRow(LessonPlanTopic t) {
        int sessions = (int) sessionLogRepository.countByTopic_Id(t.getId());
        return new LessonPlanDtos.TopicRow(
                t.getId(),
                t.getSortOrder(),
                t.getTitle(),
                t.getDescription(),
                t.getStatus().name(),
                t.getLastTaughtDate(),
                sessions
        );
    }

    private LessonPlanDtos.TopicDetailResponse toTopicDetail(LessonPlanTopic t) {
        int sessions = (int) sessionLogRepository.countByTopic_Id(t.getId());
        return new LessonPlanDtos.TopicDetailResponse(
                t.getId(),
                t.getTitle(),
                t.getDescription(),
                t.getSortOrder(),
                t.getStatus().name(),
                t.getLastTaughtDate(),
                sessions,
                t.getClassName(),
                t.getSection(),
                helper.classLabel(t.getClassName(), t.getSection()),
                t.getSubject().getId(),
                t.getSubject().getName()
        );
    }

    private LessonPlanDtos.SessionLogRow toSessionLogRow(LessonPlanSessionLog log) {
        String hint = null;
        if (log.getMaterialPath() != null && !log.getMaterialPath().isBlank()) {
            String p = log.getMaterialPath();
            int slash = Math.max(p.lastIndexOf('/'), p.lastIndexOf('\\'));
            hint = slash >= 0 ? p.substring(slash + 1) : p;
        }
        return new LessonPlanDtos.SessionLogRow(
                log.getId(),
                log.getSessionDate(),
                helper.formatSessionTime(log.getSessionTime()),
                log.getNotes(),
                log.getMaterialPath() != null && !log.getMaterialPath().isBlank(),
                hint,
                log.isMarkTopicCompleted()
        );
    }

    private LessonPlanDtos.LearningMaterialRow toMaterialRow(LearningMaterial m) {
        String topicTitle = null;
        Long topicId = null;
        if (m.getTopic() != null) {
            topicId = m.getTopic().getId();
            topicTitle = m.getTopic().getTitle();
        }
        return new LessonPlanDtos.LearningMaterialRow(
                m.getId(),
                m.getTitle(),
                m.getOriginalFilename(),
                m.getFileSizeBytes(),
                m.getMimeType(),
                m.getCreatedAt(),
                helper.classLabel(m.getClassName(), m.getSection()),
                m.getSubject().getId(),
                m.getSubject().getName(),
                topicId,
                topicTitle,
                m.isVisibleToStudents(),
                m.getDueDate()
        );
    }
}
