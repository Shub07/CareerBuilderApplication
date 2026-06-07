package com.org.careerbuilder.repository;

import com.org.careerbuilder.models.Exam;
import com.org.careerbuilder.models.enums.ExamStatus;
import com.org.careerbuilder.models.enums.ExamType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ExamRepository extends JpaRepository<Exam, Long> {

    Optional<Exam> findByIdAndSchoolIdAndDeletedFalse(Long id, Long schoolId);

    long countBySchoolIdAndDeletedFalse(Long schoolId);

    long countBySchoolIdAndDeletedFalseAndStatus(Long schoolId, ExamStatus status);

    @Query("""
            SELECT DISTINCT e.className FROM Exam e
            WHERE e.schoolId = :schoolId AND e.deleted = false AND e.className IS NOT NULL
            ORDER BY e.className
            """)
    List<String> findDistinctClassNames(@Param("schoolId") Long schoolId);

    @Query("""
            SELECT DISTINCT e.academicYear FROM Exam e
            WHERE e.schoolId = :schoolId AND e.deleted = false AND e.academicYear IS NOT NULL
            ORDER BY e.academicYear DESC
            """)
    List<String> findDistinctAcademicYears(@Param("schoolId") Long schoolId);

    @Query("""
            SELECT e FROM Exam e
            WHERE e.schoolId = :schoolId
              AND e.deleted = false
              AND (:q IS NULL OR :q = '' OR LOWER(e.name) LIKE LOWER(CONCAT('%', :q, '%')))
              AND (:className IS NULL OR :className = '' OR e.className = :className)
              AND (:examType IS NULL OR e.examType = :examType)
              AND (:status IS NULL OR e.status = :status)
              AND (:academicYear IS NULL OR :academicYear = '' OR e.academicYear = :academicYear)
            ORDER BY e.examDate DESC, e.name ASC
            """)
    Page<Exam> search(
            @Param("schoolId") Long schoolId,
            @Param("q") String q,
            @Param("className") String className,
            @Param("examType") ExamType examType,
            @Param("status") ExamStatus status,
            @Param("academicYear") String academicYear,
            Pageable pageable);

    @Query("""
            SELECT DISTINCT e FROM Exam e
            WHERE e.id IN (
                SELECT er.exam.id FROM ExamResult er
                JOIN er.student st
                WHERE st.school.id = :schoolId
                AND st.className = :className
                AND st.section = :section
            )
            ORDER BY e.examDate DESC
            """)
    List<Exam> findDistinctForClass(
            @Param("schoolId") Long schoolId,
            @Param("className") String className,
            @Param("section") String section);
}
