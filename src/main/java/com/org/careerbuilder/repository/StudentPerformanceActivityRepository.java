package com.org.careerbuilder.repository;

import com.org.careerbuilder.models.StudentPerformanceActivity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface StudentPerformanceActivityRepository extends JpaRepository<StudentPerformanceActivity, Long> {

    List<StudentPerformanceActivity> findTop50ByStudent_IdOrderByActivityDateDescCreatedAtDesc(Long studentId);

    List<StudentPerformanceActivity> findByStudent_IdAndActivityDateBetweenOrderByActivityDateDescCreatedAtDesc(
            Long studentId, LocalDate from, LocalDate to);
}
