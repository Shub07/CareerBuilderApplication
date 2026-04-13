package com.org.careerbuilder.repository;

import com.org.careerbuilder.models.Fee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface FeeRepository extends JpaRepository<Fee, Long> {

    // ===== Pagination Queries =====
    @Query("""
        SELECT f FROM Fee f
        WHERE f.student.id = :studentId
        ORDER BY f.dueDate DESC
    """)
    Page<Fee> findByStudentId(@Param("studentId") Long studentId, Pageable pageable);

    @Query("""
        SELECT f FROM Fee f
        WHERE f.student.id = :studentId
        ORDER BY f.dueDate DESC
    """)
    List<Fee> findAllByStudentId(@Param("studentId") Long studentId);

    // ===== Status-based Queries =====
    @Query("""
        SELECT f FROM Fee f
        WHERE f.student.id = :studentId AND f.status = :status
        ORDER BY f.dueDate DESC
    """)
    List<Fee> findByStudentIdAndStatus(@Param("studentId") Long studentId, @Param("status") Fee.FeeStatus status);

    @Query("""
        SELECT f FROM Fee f
        WHERE f.student.id = :studentId 
        AND (f.status = 'PENDING' OR f.status = 'PARTIAL' OR f.status = 'OVERDUE')
        ORDER BY f.dueDate ASC
    """)
    List<Fee> findPendingFeesByStudentId(@Param("studentId") Long studentId);

    // ===== Academic Year Queries =====
    @Query("""
        SELECT f FROM Fee f
        WHERE f.student.id = :studentId AND f.academicYear = :academicYear
        ORDER BY f.dueDate DESC
    """)
    List<Fee> findByStudentIdAndAcademicYear(@Param("studentId") Long studentId, @Param("academicYear") String academicYear);

    // ===== Overdue Queries =====
    @Query("""
        SELECT f FROM Fee f
        WHERE f.student.id = :studentId AND f.status = 'OVERDUE'
        ORDER BY f.dueDate ASC
    """)
    List<Fee> findOverdueFeesForStudent(@Param("studentId") Long studentId);

    @Query("""
        SELECT f FROM Fee f
        WHERE f.student.id = :studentId AND f.dueDate <= :dueDate
        AND f.status IN ('PENDING', 'PARTIAL', 'OVERDUE')
    """)
    List<Fee> findFeesDueBeforeDate(@Param("studentId") Long studentId, @Param("dueDate") LocalDate dueDate);

    // ===== Fee Type Queries =====
    @Query("""
        SELECT f FROM Fee f
        WHERE f.student.id = :studentId AND f.feeType = :feeType
        ORDER BY f.dueDate DESC
    """)
    List<Fee> findByStudentIdAndFeeType(@Param("studentId") Long studentId, @Param("feeType") String feeType);

    // ===== Financial Calculation Queries =====
    @Query("""
        SELECT COALESCE(SUM(f.amount), 0) 
        FROM Fee f
        WHERE f.student.id = :studentId
    """)
    BigDecimal calculateTotalFees(@Param("studentId") Long studentId);

    @Query("""
        SELECT COALESCE(SUM(f.paidAmount), 0)
        FROM Fee f
        WHERE f.student.id = :studentId
    """)
    BigDecimal calculateTotalPaidAmount(@Param("studentId") Long studentId);

    @Query("""
        SELECT COALESCE(SUM(f.amount - f.paidAmount), 0)
        FROM Fee f
        WHERE f.student.id = :studentId AND f.status != 'PAID'
    """)
    BigDecimal calculateTotalPendingAmount(@Param("studentId") Long studentId);

    // ===== Existence Checks =====
    Optional<Fee> findByStudentIdAndFeeTypeAndAcademicYearAndTerm(
            Long studentId, String feeType, String academicYear, String term);
}
