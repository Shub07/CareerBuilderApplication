package com.org.careerbuilder.service;

import com.org.careerbuilder.dto.request.*;
import com.org.careerbuilder.dto.response.LessonPlanDtos;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface TeacherLessonPlanService {

    LessonPlanDtos.LessonPlanFiltersResponse getFilters(Long facultyId);

    List<LessonPlanDtos.TopicRow> listTopics(Long facultyId, String className, String section, Long subjectId);

    LessonPlanDtos.TopicDetailResponse getTopicDetail(Long facultyId, Long topicId);

    Long createTopic(Long facultyId, LessonPlanTopicUpsertRequest request);

    void updateTopic(Long facultyId, Long topicId, LessonPlanTopicUpsertRequest request);

    void deleteTopic(Long facultyId, Long topicId);

    List<LessonPlanDtos.SessionLogRow> listSessionLogs(Long facultyId, Long topicId);

    Long addSessionLog(Long facultyId, Long topicId, LessonPlanSessionLogRequest request, MultipartFile attachment);

    void updateSessionLog(Long facultyId, Long topicId, Long logId, LessonPlanSessionLogRequest request, MultipartFile attachment);

    void deleteSessionLog(Long facultyId, Long topicId, Long logId);

    List<LessonPlanDtos.LearningMaterialRow> listMaterials(
            Long facultyId, String className, String section, Long subjectId, Long topicId, String search);

    Long uploadMaterial(Long facultyId, LearningMaterialMetadataRequest meta, MultipartFile file);

    void deleteMaterial(Long facultyId, Long materialId);

    void setMaterialVisibility(Long facultyId, Long materialId, LearningMaterialVisibilityRequest request);

    Resource downloadMaterialTeacher(Long facultyId, Long materialId);
}
