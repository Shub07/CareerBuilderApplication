package com.org.careerbuilder.repository;

import com.org.careerbuilder.models.FeeJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FeeJobRepository extends JpaRepository<FeeJob, Long> {

    Optional<FeeJob> findByIdAndSchoolId(Long id, Long schoolId);
}
