package com.org.careerbuilder.repository;

import com.org.careerbuilder.models.ExamMarksSubmission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ExamMarksSubmissionRepository extends JpaRepository<ExamMarksSubmission, Long> {

    List<ExamMarksSubmission> findByExam_IdAndSchoolId(Long examId, Long schoolId);

    Optional<ExamMarksSubmission> findByExam_IdAndSubject_IdAndSchoolId(Long examId, Long subjectId, Long schoolId);
}
