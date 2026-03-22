package com.org.careerbuilder.repository;

import com.org.careerbuilder.models.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

    List<Assignment> findBySubjectId(Long subjectId);
}