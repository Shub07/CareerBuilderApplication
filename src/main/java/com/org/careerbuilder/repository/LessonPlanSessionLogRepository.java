package com.org.careerbuilder.repository;

import com.org.careerbuilder.models.LessonPlanSessionLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LessonPlanSessionLogRepository extends JpaRepository<LessonPlanSessionLog, Long> {

    List<LessonPlanSessionLog> findByTopic_IdOrderBySessionDateDescIdDesc(Long topicId);

    long countByTopic_Id(Long topicId);
}
