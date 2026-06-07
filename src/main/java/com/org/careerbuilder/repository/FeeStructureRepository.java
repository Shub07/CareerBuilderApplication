package com.org.careerbuilder.repository;

import com.org.careerbuilder.models.FeeStructure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface FeeStructureRepository extends JpaRepository<FeeStructure, Long> {

    @Query("""
            SELECT fs FROM FeeStructure fs
            WHERE fs.school.id = :schoolId AND fs.deleted = false
            ORDER BY fs.createdAt DESC
            """)
    List<FeeStructure> findActiveBySchool(@Param("schoolId") Long schoolId);

    Optional<FeeStructure> findByIdAndSchool_IdAndDeletedFalse(Long id, Long schoolId);

    @Query("""
            SELECT COUNT(fs) FROM FeeStructure fs
            WHERE fs.school.id = :schoolId AND fs.deleted = false AND fs.active = true
            """)
    long countActiveBySchool(@Param("schoolId") Long schoolId);

    @Query("""
            SELECT COALESCE(AVG(fs.amount), 0) FROM FeeStructure fs
            WHERE fs.school.id = :schoolId AND fs.deleted = false AND fs.active = true
            """)
    BigDecimal averageAmountBySchool(@Param("schoolId") Long schoolId);

    @Query("""
            SELECT fs FROM FeeStructure fs
            WHERE fs.school.id = :schoolId AND fs.deleted = false AND fs.active = true
              AND fs.academicYear = :academicYear
              AND (fs.targetClassName = :className OR fs.targetClassName = 'ALL')
              AND (fs.targetSection IS NULL OR fs.targetSection = 'ALL' OR fs.targetSection = :section)
            """)
    List<FeeStructure> findApplicable(@Param("schoolId") Long schoolId,
                                      @Param("academicYear") String academicYear,
                                      @Param("className") String className,
                                      @Param("section") String section);
}
