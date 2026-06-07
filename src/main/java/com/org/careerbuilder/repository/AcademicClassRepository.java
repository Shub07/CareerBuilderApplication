package com.org.careerbuilder.repository;

import com.org.careerbuilder.models.AcademicClass;
import com.org.careerbuilder.models.enums.ClassStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AcademicClassRepository extends JpaRepository<AcademicClass, Long> {

    @Query("""
            SELECT c FROM AcademicClass c
            LEFT JOIN FETCH c.classTeacher t
            LEFT JOIN FETCH t.subject
            WHERE c.id = :id AND c.school.id = :schoolId AND c.deleted = false
            """)
    Optional<AcademicClass> findActiveById(@Param("id") Long id, @Param("schoolId") Long schoolId);

    boolean existsBySchool_IdAndNameIgnoreCaseAndAcademicYearAndDeletedFalse(
            Long schoolId, String name, String academicYear);

    @Query("""
            SELECT c FROM AcademicClass c
            LEFT JOIN FETCH c.classTeacher t
            LEFT JOIN FETCH t.subject
            WHERE c.school.id = :schoolId
              AND c.deleted = false
              AND (:academicYear IS NULL OR :academicYear = '' OR c.academicYear = :academicYear)
              AND (:unassignedOnly = false OR c.classTeacher IS NULL)
              AND (:status IS NULL OR c.status = :status)
              AND (:q IS NULL OR :q = ''
                   OR LOWER(c.name) LIKE LOWER(CONCAT('%', :q, '%'))
                   OR LOWER(CONCAT(t.firstName, ' ', t.lastName)) LIKE LOWER(CONCAT('%', :q, '%')))
            ORDER BY c.name
            """)
    Page<AcademicClass> search(
            @Param("schoolId") Long schoolId,
            @Param("q") String q,
            @Param("academicYear") String academicYear,
            @Param("status") ClassStatus status,
            @Param("unassignedOnly") boolean unassignedOnly,
            Pageable pageable);

    long countBySchool_IdAndDeletedFalse(Long schoolId);

    long countBySchool_IdAndDeletedFalseAndClassTeacherIsNull(Long schoolId);

    @Query("""
            SELECT DISTINCT c.academicYear FROM AcademicClass c
            WHERE c.school.id = :schoolId AND c.deleted = false
            ORDER BY c.academicYear DESC
            """)
    List<String> findDistinctAcademicYears(@Param("schoolId") Long schoolId);

    List<AcademicClass> findBySchool_IdAndDeletedFalse(Long schoolId);
}
