package com.org.careerbuilder.repository;

import com.org.careerbuilder.models.StudentExamAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentExamAnswerRepository extends JpaRepository<StudentExamAnswer, Long> {
    
    @Query("""
        SELECT DISTINCT sea FROM StudentExamAnswer sea
        JOIN FETCH sea.question q
        JOIN FETCH q.section s
        WHERE sea.examResult.id = :examResultId
        ORDER BY q.questionNumber
    """)
    List<StudentExamAnswer> findByExamResultId(@Param("examResultId") Long examResultId);
}

