package com.org.careerbuilder.repository;

import com.org.careerbuilder.models.ExamResultBreakdown;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExamResultBreakdownRepository extends JpaRepository<ExamResultBreakdown, Long> {
    
    @Query("""
        SELECT erb FROM ExamResultBreakdown erb
        WHERE erb.examResult.id = :examResultId
        ORDER BY erb.sectionName
    """)
    List<ExamResultBreakdown> findByExamResultId(@Param("examResultId") Long examResultId);
}

