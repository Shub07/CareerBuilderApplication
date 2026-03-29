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
            @Param("examType") com.org.careerbuilder.models.enums.ExamType examType
    );

    // 🔥 Average score
    @Query("""
        SELECT AVG(er.obtainedMarks * 1.0 / er.totalMarks * 100)
        FROM ExamResult er
        WHERE er.student.id = :studentId
    """)
    Double getAverageScore(@Param("studentId") Long studentId);

    // 🔥 Detailed results with exam type filter
    @Query("""
        SELECT er
        FROM ExamResult er
        WHERE er.student.id = :studentId
        AND (:examType IS NULL OR er.exam.examType = :examType)
        ORDER BY er.exam.examDate DESC
    """)
    List<ExamResult> findDetailedResults(
            @Param("studentId") Long studentId,
            @Param("examType") com.org.careerbuilder.models.enums.ExamType examType
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

    // ✅ UPCOMING EXAMS (For Upcoming Tab)
    @Query("""
        SELECT er FROM ExamResult er
        WHERE er.student.id = :studentId
        AND er.exam.examDate >= CURRENT_DATE
        ORDER BY er.exam.examDate ASC
    """)
    List<ExamResult> findUpcomingExams(@Param("studentId") Long studentId);

    // ✅ PERFORMANCE COMPARISON (Class Average, Highest Score, Rank)
    @Query("""
        SELECT 
            CAST(AVG(er.obtainedMarks * 1.0 / er.totalMarks * 100) AS Double) as classAvg,
            MAX(er.obtainedMarks * 1.0 / er.totalMarks * 100) as highestScore
        FROM ExamResult er
        WHERE er.exam.id = :examId
    """)
    Object[] getExamPerformanceStats(@Param("examId") Long examId);

    // ✅ Student rank for an exam
    @Query("""
        SELECT COUNT(*) 
        FROM ExamResult er
        WHERE er.exam.id = :examId
        AND (er.obtainedMarks * 1.0 / er.totalMarks * 100) > 
            (SELECT er2.obtainedMarks * 1.0 / er2.totalMarks * 100
             FROM ExamResult er2
             WHERE er2.id = :examResultId)
    """)
    Long getStudentRank(@Param("examResultId") Long examResultId, @Param("examId") Long examId);
}