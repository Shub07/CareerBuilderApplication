package com.org.careerbuilder.repository;

import com.org.careerbuilder.models.DailyLifeActivity;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DailyLifeActivityRepository extends JpaRepository<DailyLifeActivity, Long> {

    List<DailyLifeActivity> findByStudent_IdAndActivityDateOrderByStartTimeAsc(Long studentId, LocalDate activityDate);

    long countByStudent_IdAndActivityDate(Long studentId, LocalDate activityDate);

    long countByStudent_IdAndActivityDateAndCompletedTrue(Long studentId, LocalDate activityDate);

    List<DailyLifeActivity> findByStudent_IdInAndActivityDateOrderByStudent_IdAscStartTimeAsc(List<Long> studentIds, LocalDate activityDate);

    List<DailyLifeActivity> findByStudent_IdAndActivityDateBetweenOrderByActivityDateAscStartTimeAsc(
            Long studentId, LocalDate from, LocalDate to);
}