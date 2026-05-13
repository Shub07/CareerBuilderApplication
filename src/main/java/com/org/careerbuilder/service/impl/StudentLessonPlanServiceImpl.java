package com.org.careerbuilder.service.impl;

import com.org.careerbuilder.dto.response.LessonPlanDtos;
import com.org.careerbuilder.exceptions.ResourceNotFoundException;
import com.org.careerbuilder.models.LearningMaterial;
import com.org.careerbuilder.models.LessonPlanSessionLog;
import com.org.careerbuilder.models.LessonPlanTopic;
import com.org.careerbuilder.models.Student;
import com.org.careerbuilder.repository.LearningMaterialRepository;
import com.org.careerbuilder.repository.LessonPlanSessionLogRepository;
import com.org.careerbuilder.repository.LessonPlanTopicRepository;
import com.org.careerbuilder.service.StudentLessonPlanService;
import com.org.careerbuilder.service.support.LessonPlanSharedHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class StudentLessonPlanServiceImpl implements StudentLessonPlanService {

    private final LessonPlanSharedHelper helper;
    private final LessonPlanTopicRepository topicRepository;
    private final LessonPlanSessionLogRepository sessionLogRepository;
    private final LearningMaterialRepository materialRepository;

    @Override
    @Transactional(readOnly = true)
    public List<LessonPlanDtos.StudentTopicRow> listTopics(Long studentId, Long subjectId) {
        if (subjectId == null) {
            throw new IllegalArgumentException("subjectId is required");
        }
        Student st = helper.loadStudent(studentId);
        List<LessonPlanTopic> topics = topicRepository.findBySchoolIdAndClassNameAndSectionAndSubject_IdOrderBySortOrderAscIdAsc(
                st.getSchool().getId(), st.getClassName(), st.getSection(), subjectId);
        return topics.stream().map(t -> toStudentTopicRow(t)).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public LessonPlanDtos.TopicDetailResponse getStudentTopicDetail(Long studentId, Long topicId) {
        Student st = helper.loadStudent(studentId);
        LessonPlanTopic t = topicRepository.findById(topicId)
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found"));
        assertStudentCanAccessTopic(st, t);
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

    @Override
    @Transactional(readOnly = true)
    public List<LessonPlanDtos.SessionLogRow> listStudentSessionLogs(Long studentId, Long topicId) {
        Student st = helper.loadStudent(studentId);
        LessonPlanTopic t = topicRepository.findById(topicId)
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found"));
        assertStudentCanAccessTopic(st, t);
        return sessionLogRepository.findByTopic_IdOrderBySessionDateDescIdDesc(topicId).stream()
                .map(this::toSessionLogRow)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LessonPlanDtos.StudentMaterialRow> listMaterials(Long studentId, Long subjectId, Long topicId, String search) {
        Student st = helper.loadStudent(studentId);
        Long schoolId = st.getSchool().getId();
        String cn = st.getClassName();
        String sec = st.getSection();

        Specification<LearningMaterial> spec = (r, q, cb) -> cb.and(
                cb.equal(r.get("schoolId"), schoolId),
                cb.equal(cb.lower(r.get("className")), cn.toLowerCase(Locale.ROOT)),
                cb.equal(cb.lower(r.get("section")), sec.toLowerCase(Locale.ROOT)),
                cb.isTrue(r.get("visibleToStudents"))
        );
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
                .map(this::toStudentMaterialRow)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Resource downloadMaterial(Long studentId, Long materialId) {
        Student st = helper.loadStudent(studentId);
        LearningMaterial m = materialRepository.findById(materialId)
                .orElseThrow(() -> new ResourceNotFoundException("Material not found"));
        if (!m.isVisibleToStudents()) {
            throw new ResourceNotFoundException("Material not found");
        }
        if (!helper.studentMatchesClass(st, m.getSchoolId(), m.getClassName(), m.getSection())) {
            throw new ResourceNotFoundException("Material not found");
        }
        Path path = Paths.get(m.getStoredPath());
        PathResource resource = new PathResource(path);
        if (!resource.exists()) {
            throw new ResourceNotFoundException("File not found on server");
        }
        return resource;
    }

    private void assertStudentCanAccessTopic(Student st, LessonPlanTopic t) {
        if (!helper.studentMatchesClass(st, t.getSchoolId(), t.getClassName(), t.getSection())) {
            throw new ResourceNotFoundException("Topic not found");
        }
    }

    private LessonPlanDtos.StudentTopicRow toStudentTopicRow(LessonPlanTopic t) {
        int sessions = (int) sessionLogRepository.countByTopic_Id(t.getId());
        String teacherName = teacherDisplayName(t);
        return new LessonPlanDtos.StudentTopicRow(
                t.getId(),
                t.getSortOrder(),
                t.getTitle(),
                t.getDescription(),
                t.getStatus().name(),
                t.getLastTaughtDate(),
                sessions,
                t.getSubject().getName(),
                teacherName
        );
    }

    private static String teacherDisplayName(LessonPlanTopic t) {
        try {
            var f = t.getTeacher();
            if (f == null) {
                return "—";
            }
            String fn = f.getFirstName() != null ? f.getFirstName() : "";
            String ln = f.getLastName() != null ? f.getLastName() : "";
            String full = (fn + " " + ln).trim();
            return full.isEmpty() ? "—" : full;
        } catch (Exception e) {
            return "—";
        }
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

    private LessonPlanDtos.StudentMaterialRow toStudentMaterialRow(LearningMaterial m) {
        String topicTitle = m.getTopic() != null ? m.getTopic().getTitle() : null;
        return new LessonPlanDtos.StudentMaterialRow(
                m.getId(),
                m.getTitle(),
                m.getOriginalFilename(),
                m.getFileSizeBytes(),
                m.getMimeType(),
                m.getCreatedAt(),
                m.getSubject().getName(),
                topicTitle,
                m.getDueDate()
        );
    }
}
