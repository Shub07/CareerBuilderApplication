package com.org.careerbuilder.repository;

import com.org.careerbuilder.models.Certificate;
import com.org.careerbuilder.models.Certificate.CertificateCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CertificateRepository extends JpaRepository<Certificate, Long> {

    @Query("""
            SELECT c FROM Certificate c
            WHERE c.student.id = :studentId
              AND (:academicYear IS NULL OR c.academicYear = :academicYear)
              AND (:search IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%')))
            ORDER BY c.category, c.name
            """)
    List<Certificate> findByStudentFiltered(
            @Param("studentId") Long studentId,
            @Param("academicYear") String academicYear,
            @Param("search") String search
    );

    @Query("""
            SELECT c FROM Certificate c
            WHERE c.student.id = :studentId
              AND c.category = :category
              AND (:academicYear IS NULL OR c.academicYear = :academicYear)
              AND (:search IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%')))
            ORDER BY c.name
            """)
    List<Certificate> findByStudentAndCategory(
            @Param("studentId") Long studentId,
            @Param("category") CertificateCategory category,
            @Param("academicYear") String academicYear,
            @Param("search") String search
    );

    Optional<Certificate> findByIdAndStudentId(Long id, Long studentId);

    @Query("SELECT DISTINCT c.academicYear FROM Certificate c WHERE c.student.id = :studentId AND c.academicYear IS NOT NULL ORDER BY c.academicYear DESC")
    List<String> findDistinctAcademicYearsByStudentId(@Param("studentId") Long studentId);
}
