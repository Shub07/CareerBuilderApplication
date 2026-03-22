package com.org.careerbuilder.repository;

import com.org.careerbuilder.models.AssignmentSubmission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AssignmentSubmissionRepository
        extends JpaRepository<AssignmentSubmission, Long> {

    List<AssignmentSubmission> findByStudent_Id(Long studentId);

    Optional<AssignmentSubmission> findByAssignment_IdAndStudent_Id(
            Long assignmentId, Long studentId);
}