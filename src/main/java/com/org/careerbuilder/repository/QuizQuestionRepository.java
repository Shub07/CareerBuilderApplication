package com.org.careerbuilder.repository;

import com.org.careerbuilder.models.QuizQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizQuestionRepository extends JpaRepository<QuizQuestion, Long> {

    long countByQuiz_Id(Long quizId);
}
