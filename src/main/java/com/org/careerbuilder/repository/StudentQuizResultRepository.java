package com.org.careerbuilder.repository;

import com.org.careerbuilder.models.StudentQuizResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudentQuizResultRepository extends JpaRepository<StudentQuizResult, Long> {

    List<StudentQuizResult> findByStudent_IdOrderByQuizDateDesc(Long studentId);

    List<StudentQuizResult> findByStudent_IdAndSubject_NameContainingIgnoreCaseOrderByQuizDateDesc(
            Long studentId, String subjectQuery);
}
