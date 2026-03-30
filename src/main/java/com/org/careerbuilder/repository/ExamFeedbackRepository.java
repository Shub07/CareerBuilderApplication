package com.org.careerbuilder.repository;

import com.org.careerbuilder.models.ExamFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ExamFeedbackRepository extends JpaRepository<ExamFeedback, Long> {
    
    Optional<ExamFeedback> findByExamResult_Id(Long examResultId);
}

