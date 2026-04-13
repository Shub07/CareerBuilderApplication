package com.org.careerbuilder.repository;

import com.org.careerbuilder.models.CareerResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CareerResultRepository extends JpaRepository<CareerResult, Long> {

    Optional<CareerResult> findByStudent_IdAndAssessment_Id(Long studentId, Long assessmentId);

    Optional<CareerResult> findByIdAndStudent_Id(Long id, Long studentId);

    List<CareerResult> findByStudent_IdOrderByCreatedAtDesc(Long studentId);

    long countByStudent_Id(Long studentId);
}
