package com.org.careerbuilder.repository;

import com.org.careerbuilder.models.QuizSubmission;
import com.org.careerbuilder.models.enums.QuizSubmissionGradeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface QuizSubmissionRepository extends JpaRepository<QuizSubmission, Long> {

    void deleteByQuiz_Id(Long quizId);

    List<QuizSubmission> findByQuiz_Id(Long quizId);

    Optional<QuizSubmission> findByQuiz_IdAndStudent_Id(Long quizId, Long studentId);

    long countByQuiz_IdAndSubmittedAtIsNotNull(Long quizId);

    long countByQuiz_IdAndSubmittedAtIsNotNullAndGradeStatus(Long quizId, QuizSubmissionGradeStatus gradeStatus);

    @Query("""
            select avg(s.totalScore) from QuizSubmission s
            where s.quiz.id = :quizId
            and s.gradeStatus = :gs
            and s.totalScore is not null
            """)
    Optional<Double> averageTotalScore(@Param("quizId") Long quizId, @Param("gs") QuizSubmissionGradeStatus gs);
}
