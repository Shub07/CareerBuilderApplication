package com.org.careerbuilder.repository;

import com.org.careerbuilder.models.ExamSection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExamSectionRepository extends JpaRepository<ExamSection, Long> {
    
    @Query("""
        SELECT es FROM ExamSection es
        WHERE es.exam.id = :examId
        ORDER BY es.id
    """)
    List<ExamSection> findByExamId(@Param("examId") Long examId);
}

