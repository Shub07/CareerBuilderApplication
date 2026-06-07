package com.org.careerbuilder.repository;

import com.org.careerbuilder.models.ExamResult;
import com.org.careerbuilder.models.enums.ExamType;
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

    @Query("""
            SELECT er FROM ExamResult er
            JOIN FETCH er.exam e
            JOIN FETCH er.student st
            JOIN FETCH e.subject sub
            WHERE er.exam.id = :examId
            ORDER BY st.rollNo ASC
            """)
    List<ExamResult> findWithContextByExamId(@Param("examId") Long examId);

    @Query("""
            SELECT er FROM ExamResult er
            JOIN FETCH er.exam e
            JOIN FETCH er.student st
            JOIN FETCH e.subject sub
            WHERE st.school.id = :schoolId
            AND e.subject.id = :subjectId
            AND (:className IS NULL OR st.className = :className)
            AND (:section IS NULL OR st.section = :section)
            AND (:examType IS NULL OR e.examType = :examType)
            """)
    List<ExamResult> findTeacherExamResults(
            @Param("schoolId") Long schoolId,
            @Param("subjectId") Long subjectId,
            @Param("className") String className,
            @Param("section") String section,
            @Param("examType") ExamType examType);

    @Query("""
            SELECT er FROM ExamResult er
            JOIN FETCH er.exam e
            JOIN FETCH er.student st
            JOIN FETCH e.subject sub
            WHERE st.school.id = :schoolId
            AND e.subject.id = :subjectId
            AND er.marksLocked = true
            AND (:className IS NULL OR st.className = :className)
            AND (:section IS NULL OR st.section = :section)
            """)
    List<ExamResult> findLockedTeacherExamResults(
            @Param("schoolId") Long schoolId,
            @Param("subjectId") Long subjectId,
            @Param("className") String className,
            @Param("section") String section);

    @Query("""
            SELECT er FROM ExamResult er
            JOIN FETCH er.exam e
            JOIN FETCH er.student st
            JOIN FETCH er.subject sub
            WHERE st.school.id = :schoolId
            AND st.className = :className
            AND st.section = :section
            AND (:subjectId IS NULL OR sub.id = :subjectId)
            AND (:examId IS NULL OR e.id = :examId)
            """)
    List<ExamResult> findCohortResults(
            @Param("schoolId") Long schoolId,
            @Param("className") String className,
            @Param("section") String section,
            @Param("subjectId") Long subjectId,
            @Param("examId") Long examId);

    @Query("""
            SELECT er FROM ExamResult er
            JOIN FETCH er.exam e
            JOIN FETCH er.student st
            JOIN FETCH er.subject sub
            WHERE st.id = :studentId
            AND (:subjectId IS NULL OR sub.id = :subjectId)
            AND (:examId IS NULL OR e.id = :examId)
            ORDER BY e.examDate DESC
            """)
    List<ExamResult> findStudentResultsFiltered(
            @Param("studentId") Long studentId,
            @Param("subjectId") Long subjectId,
            @Param("examId") Long examId);

    @Query("""
            SELECT er FROM ExamResult er
            JOIN FETCH er.student st
            JOIN FETCH er.subject sub
            WHERE er.exam.id = :examId AND er.subject.id = :subjectId
            ORDER BY st.rollNo ASC
            """)
    List<ExamResult> findByExamIdAndSubjectId(
            @Param("examId") Long examId,
            @Param("subjectId") Long subjectId);

    long countByExam_IdAndSubject_Id(Long examId, Long subjectId);

    long countByExam_IdAndSubject_IdAndObtainedMarksIsNotNull(Long examId, Long subjectId);

    long countByExam_IdAndSubject_IdAndMarksLockedTrue(Long examId, Long subjectId);
}