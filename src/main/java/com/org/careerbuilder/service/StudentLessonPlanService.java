package com.org.careerbuilder.service;

import com.org.careerbuilder.dto.response.LessonPlanDtos;
import org.springframework.core.io.Resource;

import java.util.List;

public interface StudentLessonPlanService {

    List<LessonPlanDtos.StudentTopicRow> listTopics(Long studentId, Long subjectId);

    LessonPlanDtos.TopicDetailResponse getStudentTopicDetail(Long studentId, Long topicId);

    List<LessonPlanDtos.SessionLogRow> listStudentSessionLogs(Long studentId, Long topicId);

    List<LessonPlanDtos.StudentMaterialRow> listMaterials(Long studentId, Long subjectId, Long topicId, String search);

    Resource downloadMaterial(Long studentId, Long materialId);
}
