package com.org.careerbuilder.repository;

import com.org.careerbuilder.models.CareerAssessment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CareerAssessmentRepository extends JpaRepository<CareerAssessment, Long> {

    List<CareerAssessment> findBySchoolIdAndIsActiveTrueOrderByOrderIndexAsc(Long schoolId);

    long countBySchoolIdAndIsActiveTrue(Long schoolId);
}
