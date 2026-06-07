package com.org.careerbuilder.repository;

import com.org.careerbuilder.models.ExamVenue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ExamVenueRepository extends JpaRepository<ExamVenue, Long> {

    List<ExamVenue> findBySchoolIdAndActiveTrueOrderByNameAsc(Long schoolId);

    Optional<ExamVenue> findByIdAndSchoolId(Long id, Long schoolId);
}
