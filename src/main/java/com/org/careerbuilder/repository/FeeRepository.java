package com.org.careerbuilder.repository;

import com.org.careerbuilder.models.Fee;
import com.org.careerbuilder.repository.projection.StudentFeeAggregate;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
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

    Optional<Fee> findTopByStudent_IdOrderByDueDateDesc(Long studentId);

    @Query("""
            SELECT COALESCE(SUM(
                CASE WHEN f.amount IS NOT NULL THEN f.amount ELSE CAST(f.totalAmount AS bigdecimal) END
                - COALESCE(f.paidAmount, 0)
            ), 0)
            FROM Fee f
            WHERE f.student.school.id = :schoolId
            AND f.status IN ('PENDING', 'PARTIAL', 'OVERDUE')
            """)
    BigDecimal sumPendingAmountBySchool(@Param("schoolId") Long schoolId);

    // ===== Admin Student Fees module (school-scoped aggregates) =====

    @Query("""
            SELECT COALESCE(SUM(COALESCE(f.paidAmount, 0)), 0)
            FROM Fee f
            WHERE f.student.school.id = :schoolId
            """)
    BigDecimal sumCollectedBySchool(@Param("schoolId") Long schoolId);

    @Query("""
            SELECT COALESCE(SUM(
                CASE WHEN f.amount IS NOT NULL THEN f.amount ELSE CAST(f.totalAmount AS bigdecimal) END
                - COALESCE(f.paidAmount, 0)
            ), 0)
            FROM Fee f
            WHERE f.student.school.id = :schoolId
              AND f.status <> 'PAID'
              AND f.dueDate < :today
            """)
    BigDecimal sumOverdueBySchool(@Param("schoolId") Long schoolId, @Param("today") LocalDate today);

    /** Fee obligations for a student+type that still owe money, earliest due first. */
    @Query("""
            SELECT f FROM Fee f
            WHERE f.student.id = :studentId
              AND LOWER(f.feeType) = LOWER(:feeType)
              AND f.status <> 'PAID'
            ORDER BY f.dueDate ASC
            """)
    List<Fee> findCollectibleForStudent(@Param("studentId") Long studentId, @Param("feeType") String feeType);

    /**
     * Same as {@link #findCollectibleForStudent} but takes a write lock so concurrent
     * collections against the same obligation are serialized (prevents lost updates).
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT f FROM Fee f
            WHERE f.student.id = :studentId
              AND LOWER(f.feeType) = LOWER(:feeType)
              AND f.status <> 'PAID'
            ORDER BY f.dueDate ASC
            """)
    List<Fee> lockCollectibleForStudent(@Param("studentId") Long studentId, @Param("feeType") String feeType);

    /** Loads a single fee with a write lock so refund adjustments are serialized. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT f FROM Fee f WHERE f.id = :id")
    Optional<Fee> lockById(@Param("id") Long id);

    @Query("""
            SELECT f FROM Fee f
            WHERE f.student.id = :studentId AND f.status = 'PAID'
            ORDER BY f.dueDate DESC
            """)
    List<Fee> findPaidFeesForStudent(@Param("studentId") Long studentId);

    @Query("""
            SELECT s.id AS studentId, s.firstName AS firstName, s.lastName AS lastName,
                   s.className AS className, s.section AS section,
                   SUM(COALESCE(f.amount, 0)) AS totalFees,
                   SUM(COALESCE(f.paidAmount, 0)) AS paidFees,
                   SUM(CASE WHEN f.status <> 'PAID' AND f.dueDate < :today
                            THEN (COALESCE(f.amount, 0) - COALESCE(f.paidAmount, 0)) ELSE 0 END) AS overdueFees
            FROM Fee f JOIN f.student s
            WHERE s.school.id = :schoolId
              AND (:q IS NULL OR :q = '' OR LOWER(CONCAT(s.firstName, ' ', s.lastName)) LIKE LOWER(CONCAT('%', :q, '%')))
              AND (:className IS NULL OR :className = '' OR s.className = :className)
            GROUP BY s.id, s.firstName, s.lastName, s.className, s.section
            HAVING (:status IS NULL OR :status = '' OR :status = 'ALL'
                OR (:status = 'OVERDUE' AND SUM(CASE WHEN f.status <> 'PAID' AND f.dueDate < :today THEN (COALESCE(f.amount,0) - COALESCE(f.paidAmount,0)) ELSE 0 END) > 0)
                OR (:status = 'PAID' AND SUM(COALESCE(f.amount,0)) > 0 AND SUM(COALESCE(f.paidAmount,0)) >= SUM(COALESCE(f.amount,0)))
                OR (:status = 'PARTIAL' AND SUM(COALESCE(f.paidAmount,0)) > 0 AND SUM(COALESCE(f.paidAmount,0)) < SUM(COALESCE(f.amount,0))
                      AND SUM(CASE WHEN f.status <> 'PAID' AND f.dueDate < :today THEN 1 ELSE 0 END) = 0)
                OR (:status = 'PENDING' AND SUM(COALESCE(f.paidAmount,0)) = 0
                      AND SUM(CASE WHEN f.dueDate < :today THEN 1 ELSE 0 END) = 0))
            ORDER BY s.firstName, s.lastName
            """)
    List<StudentFeeAggregate> aggregateStudentFees(@Param("schoolId") Long schoolId,
                                                    @Param("q") String q,
                                                    @Param("className") String className,
                                                    @Param("status") String status,
                                                    @Param("today") LocalDate today,
                                                    Pageable pageable);

    @Query("""
            SELECT COUNT(s.id) FROM Student s
            WHERE s.school.id = :schoolId
              AND (:q IS NULL OR :q = '' OR LOWER(CONCAT(s.firstName, ' ', s.lastName)) LIKE LOWER(CONCAT('%', :q, '%')))
              AND (:className IS NULL OR :className = '' OR s.className = :className)
              AND s.id IN (
                  SELECT f.student.id FROM Fee f
                  WHERE f.student.school.id = :schoolId
                  GROUP BY f.student.id
                  HAVING (:status IS NULL OR :status = '' OR :status = 'ALL'
                      OR (:status = 'OVERDUE' AND SUM(CASE WHEN f.status <> 'PAID' AND f.dueDate < :today THEN (COALESCE(f.amount,0) - COALESCE(f.paidAmount,0)) ELSE 0 END) > 0)
                      OR (:status = 'PAID' AND SUM(COALESCE(f.amount,0)) > 0 AND SUM(COALESCE(f.paidAmount,0)) >= SUM(COALESCE(f.amount,0)))
                      OR (:status = 'PARTIAL' AND SUM(COALESCE(f.paidAmount,0)) > 0 AND SUM(COALESCE(f.paidAmount,0)) < SUM(COALESCE(f.amount,0))
                            AND SUM(CASE WHEN f.status <> 'PAID' AND f.dueDate < :today THEN 1 ELSE 0 END) = 0)
                      OR (:status = 'PENDING' AND SUM(COALESCE(f.paidAmount,0)) = 0
                            AND SUM(CASE WHEN f.dueDate < :today THEN 1 ELSE 0 END) = 0))
              )
            """)
    long countStudentFees(@Param("schoolId") Long schoolId,
                          @Param("q") String q,
                          @Param("className") String className,
                          @Param("status") String status,
                          @Param("today") LocalDate today);

    @Query("SELECT DISTINCT f.student.className FROM Fee f WHERE f.student.school.id = :schoolId ORDER BY f.student.className")
    List<String> findDistinctClassNames(@Param("schoolId") Long schoolId);
}
