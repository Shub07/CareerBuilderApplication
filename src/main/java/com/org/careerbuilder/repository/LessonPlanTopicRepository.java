package com.org.careerbuilder.repository;

import com.org.careerbuilder.models.LessonPlanTopic;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LessonPlanTopicRepository extends JpaRepository<LessonPlanTopic, Long> {

    List<LessonPlanTopic> findBySchoolIdAndClassNameAndSectionAndSubject_IdOrderBySortOrderAscIdAsc(
            Long schoolId, String className, String section, Long subjectId);

    List<LessonPlanTopic> findByTeacher_IdAndSchoolIdAndClassNameAndSectionAndSubject_IdOrderBySortOrderAscIdAsc(
            Long teacherId, Long schoolId, String className, String section, Long subjectId);
}
