package com.org.careerbuilder.repository;

import com.org.careerbuilder.models.ExamVenueAllocation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ExamVenueAllocationRepository extends JpaRepository<ExamVenueAllocation, Long> {

    List<ExamVenueAllocation> findByExam_IdAndSchoolIdOrderByVenue_NameAsc(Long examId, Long schoolId);

    Optional<ExamVenueAllocation> findByIdAndSchoolId(Long id, Long schoolId);

    long countByExam_IdAndSchoolId(Long examId, Long schoolId);

    boolean existsByExam_IdAndVenue_Id(Long examId, Long venueId);
}
