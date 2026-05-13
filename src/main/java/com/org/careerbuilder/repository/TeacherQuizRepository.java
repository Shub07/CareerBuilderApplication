package com.org.careerbuilder.repository;

import com.org.careerbuilder.models.TeacherQuiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TeacherQuizRepository extends JpaRepository<TeacherQuiz, Long>, JpaSpecificationExecutor<TeacherQuiz> {

    @Query("""
            select distinct q from TeacherQuiz q
            left join fetch q.questions questions
            where q.id = :id
            """)
    Optional<TeacherQuiz> findByIdWithQuestions(@Param("id") Long id);
}
