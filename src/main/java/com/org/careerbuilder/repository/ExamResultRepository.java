package com.org.careerbuilder.repository;

import com.org.careerbuilder.models.ExamResult;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ExamResultRepository extends JpaRepository<ExamResult, Long> {

    // 🔥 Subject aggregation
    @Query("""
        SELECT er.subject.name,
               SUM(er.obtainedMarks),
               SUM(er.totalMarks)
        FROM ExamResult er
        WHERE er.student.id = :studentId
        AND (:examType IS NULL OR er.exam.examType = :examType)
        GROUP BY er.subject.name
    """)
    List<Object[]> getSubjectPerformance(
            @Param("studentId") Long studentId,
            @Param("examType") String examType
    );

    // 🔥 Average score
    @Query("""
        SELECT AVG(er.obtainedMarks * 1.0 / er.totalMarks * 100)
        FROM ExamResult er
        WHERE er.student.id = :studentId
    """)
    Double getAverageScore(@Param("studentId") Long studentId);

    // 🔥 Detailed results (FIXED)
    @Query("""
        SELECT er
        FROM ExamResult er
        WHERE er.student.id = :studentId
        AND (:examType IS NULL OR er.exam.examType = :examType)
    """)
    List<ExamResult> findDetailedResults(
            @Param("studentId") Long studentId,
            @Param("examType") String examType
    );

    @Query("""
    SELECT er
    FROM ExamResult er
    WHERE er.student.id = :studentId
    ORDER BY er.exam.examDate DESC
""")
    List<ExamResult> findRecentResults(Long studentId, Pageable pageable);

    // ✅ Completed Exams (UI: Completed Tab)
    @Query("""
        SELECT er FROM ExamResult er
        WHERE er.student.id = :studentId
        ORDER BY er.exam.examDate DESC
    """)
    List<ExamResult> findCompletedExams(@Param("studentId") Long studentId);

    // ✅ View Result Button
    Optional<ExamResult> findByExam_IdAndStudent_Id(Long examId, Long studentId);


}