package com.org.careerbuilder.models;

import com.org.careerbuilder.models.enums.FeePaymentMode;
import com.org.careerbuilder.models.enums.FeeTransactionType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Immutable ledger entry recorded whenever the admin desk collects or refunds a
 * fee. Drives the per-student "Payment History" and the printable receipt.
 */
@Entity
@Table(name = "fee_transactions", indexes = {
        @Index(name = "idx_fee_txn_school", columnList = "school_id"),
        @Index(name = "idx_fee_txn_student", columnList = "student_id"),
        @Index(name = "idx_fee_txn_date", columnList = "transaction_date"),
        @Index(name = "idx_fee_txn_type", columnList = "type")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeeTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    /** The fee obligation this entry settled (nullable for ad-hoc collections). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fee_id")
    private Fee fee;

    @Column(name = "fee_type", nullable = false, length = 60)
    private String feeType;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private FeeTransactionType type;

    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "mode", nullable = false, length = 20)
    private FeePaymentMode mode;

    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;

    @Column(name = "reference_number", length = 100)
    private String referenceNumber;

    @Column(name = "refund_reason", length = 200)
    private String refundReason;

    @Column(name = "notes", length = 1000)
    private String notes;

    @Column(name = "receipt_number", nullable = false, unique = true, length = 60)
    private String receiptNumber;

    @Column(name = "collected_by", length = 100)
    private String collectedBy;

    /** Client-supplied key that makes fee collection safe to retry (no double-charge). */
    @Column(name = "idempotency_key", length = 80)
    private String idempotencyKey;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (transactionDate == null) {
            transactionDate = LocalDate.now();
        }
    }
}
