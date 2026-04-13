package com.org.careerbuilder.repository;

import com.org.careerbuilder.models.CareerStudentProgress;
import com.org.careerbuilder.models.enums.CareerAssessmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CareerStudentProgressRepository extends JpaRepository<CareerStudentProgress, Long> {

    Optional<CareerStudentProgress> findByStudent_IdAndAssessment_Id(Long studentId, Long assessmentId);

    List<CareerStudentProgress> findByStudent_IdAndStatus(Long studentId, CareerAssessmentStatus status);

    List<CareerStudentProgress> findByStudent_Id(Long studentId);

    long countByStudent_IdAndStatus(Long studentId, CareerAssessmentStatus status);
}
