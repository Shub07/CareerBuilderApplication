package com.org.careerbuilder.repository;

import com.org.careerbuilder.models.PaymentTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 📌 Payment Transaction Repository
 */
@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {

    // ===== Idempotency Queries =====
    /**
     * Check if transaction with same idempotency key exists
     */
    Optional<PaymentTransaction> findByIdempotencyKey(String idempotencyKey);

    /**
     * Find transaction by merchant reference ID
     */
    Optional<PaymentTransaction> findByMerchantReferenceId(String merchantReferenceId);

    /**
     * Find transaction by gateway transaction ID
     */
    Optional<PaymentTransaction> findByTransactionReferenceId(String transactionReferenceId);

    // ===== Student Transaction Queries =====
    @Query("""
        SELECT pt FROM PaymentTransaction pt
        WHERE pt.student.id = :studentId
        ORDER BY pt.createdAt DESC
    """)
    Page<PaymentTransaction> findByStudentId(@Param("studentId") Long studentId, Pageable pageable);

    @Query("""
        SELECT pt FROM PaymentTransaction pt
        WHERE pt.student.id = :studentId
        ORDER BY pt.createdAt DESC
    """)
    List<PaymentTransaction> findAllByStudentId(@Param("studentId") Long studentId);

    // ===== Status-based Queries =====
    @Query("""
        SELECT pt FROM PaymentTransaction pt
        WHERE pt.student.id = :studentId AND pt.transactionStatus = :status
        ORDER BY pt.createdAt DESC
    """)
    List<PaymentTransaction> findByStudentIdAndStatus(
            @Param("studentId") Long studentId,
            @Param("status") PaymentTransaction.TransactionStatus status);

    @Query("""
        SELECT pt FROM PaymentTransaction pt
        WHERE pt.student.id = :studentId AND pt.transactionStatus IN ('INITIATED', 'PROCESSING')
        ORDER BY pt.createdAt DESC
    """)
    List<PaymentTransaction> findPendingTransactionsForStudent(@Param("studentId") Long studentId);

    // ===== Fee-related Queries =====
    @Query("""
        SELECT pt FROM PaymentTransaction pt
        WHERE pt.fee.id = :feeId
        ORDER BY pt.createdAt DESC
    """)
    List<PaymentTransaction> findByFeeId(@Param("feeId") Long feeId);

    @Query("""
        SELECT pt FROM PaymentTransaction pt
        WHERE pt.fee.id = :feeId AND pt.transactionStatus = 'SUCCESS'
    """)
    List<PaymentTransaction> findSuccessfulTransactionsByFeeId(@Param("feeId") Long feeId);

    // ===== Reconciliation Queries =====
    @Query("""
        SELECT pt FROM PaymentTransaction pt
        WHERE pt.isReconciled = false AND pt.transactionStatus = 'SUCCESS'
        ORDER BY pt.createdAt ASC
    """)
    List<PaymentTransaction> findUnreconciledSuccessfulTransactions();

    @Query("""
        SELECT pt FROM PaymentTransaction pt
        WHERE pt.isReconciled = false 
        AND pt.transactionStatus = 'SUCCESS'
        AND pt.createdAt <= :cutoffTime
        ORDER BY pt.createdAt ASC
    """)
    List<PaymentTransaction> findUnreconciledTransactionsOlderThan(@Param("cutoffTime") LocalDateTime cutoffTime);

    // ===== Retry Queries =====
    @Query("""
        SELECT pt FROM PaymentTransaction pt
        WHERE pt.transactionStatus IN ('INITIATED', 'PROCESSING', 'FAILED')
        AND pt.retryCount < :maxRetries
        AND (pt.lastRetryAt IS NULL OR pt.lastRetryAt <= :retryAfter)
        ORDER BY pt.createdAt ASC
    """)
    List<PaymentTransaction> findTransactionsEligibleForRetry(
            @Param("maxRetries") Integer maxRetries,
            @Param("retryAfter") LocalDateTime retryAfter);

    // ===== Financial Queries =====
    @Query("""
        SELECT COALESCE(SUM(pt.amount), 0)
        FROM PaymentTransaction pt
        WHERE pt.student.id = :studentId AND pt.transactionStatus = 'SUCCESS'
    """)
    Double calculateTotalSuccessfulPayments(@Param("studentId") Long studentId);

    @Query("""
        SELECT COALESCE(SUM(pt.amount), 0)
        FROM PaymentTransaction pt
        WHERE pt.student.id = :studentId AND pt.transactionStatus = 'FAILED'
    """)
    Double calculateTotalFailedPaymentAttempts(@Param("studentId") Long studentId);

    // ===== Time-based Queries =====
    @Query("""
        SELECT pt FROM PaymentTransaction pt
        WHERE pt.student.id = :studentId
        AND pt.createdAt BETWEEN :startDate AND :endDate
        ORDER BY pt.createdAt DESC
    """)
    List<PaymentTransaction> findTransactionsByStudentIdAndDateRange(
            @Param("studentId") Long studentId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // ===== Payment Method Queries =====
    @Query("""
        SELECT pt FROM PaymentTransaction pt
        WHERE pt.student.id = :studentId AND pt.paymentMethod = :paymentMethod
        AND pt.transactionStatus = 'SUCCESS'
        ORDER BY pt.createdAt DESC
    """)
    List<PaymentTransaction> findSuccessfulTransactionsByPaymentMethod(
            @Param("studentId") Long studentId,
            @Param("paymentMethod") PaymentTransaction.PaymentMethod paymentMethod);
}

