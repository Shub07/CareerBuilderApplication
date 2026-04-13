package com.org.careerbuilder.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 📌 Payment Transaction Model - Track all payment transactions with idempotency support
 */
@Entity
@Table(name = "payment_transactions", indexes = {
        @Index(name = "idx_transaction_student", columnList = "student_id"),
        @Index(name = "idx_transaction_fee", columnList = "fee_id"),
        @Index(name = "idx_transaction_status", columnList = "transaction_status"),
        @Index(name = "idx_transaction_reference", columnList = "transaction_reference_id"),
        @Index(name = "idx_transaction_idempotency", columnList = "idempotency_key")
})
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_id", nullable = false, updatable = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fee_id", nullable = false)
    private Fee fee;

    @NotNull
    @DecimalMin(value = "0.01")
    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @NotNull
    @Column(name = "payment_method", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;  // CREDIT_CARD, DEBIT_CARD, UPI, NET_BANKING, WALLET

    @NotNull
    @Column(name = "transaction_status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private TransactionStatus transactionStatus;  // INITIATED, PROCESSING, SUCCESS, FAILED, REFUNDED

    @NotNull
    @Column(name = "transaction_reference_id", nullable = false, unique = true, length = 100)
    private String transactionReferenceId;  // Gateway transaction ID

    @NotNull
    @Column(name = "idempotency_key", nullable = false, unique = true, length = 100)
    private String idempotencyKey;  // For idempotency support

    @Column(name = "merchant_reference_id", length = 100)
    private String merchantReferenceId;  // Our internal reference

    @Column(name = "gateway_response", columnDefinition = "TEXT")
    private String gatewayResponse;  // JSON response from payment gateway

    @Column(name = "error_message", length = 500)
    private String errorMessage;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "reconciliation_at")
    private LocalDateTime reconciliationAt;

    @Column(name = "is_reconciled")
    private Boolean isReconciled;

    @Column(name = "retry_count")
    private Integer retryCount;

    @Column(name = "last_retry_at")
    private LocalDateTime lastRetryAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (idempotencyKey == null) {
            idempotencyKey = UUID.randomUUID().toString();
        }
        if (merchantReferenceId == null) {
            merchantReferenceId = "TXN-" + System.currentTimeMillis();
        }
        if (retryCount == null) {
            retryCount = 0;
        }
        if (isReconciled == null) {
            isReconciled = false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum PaymentMethod {
        CREDIT_CARD("Credit Card"),
        DEBIT_CARD("Debit Card"),
        UPI("UPI"),
        NET_BANKING("Net Banking"),
        WALLET("Digital Wallet"),
        CASH("Cash"),
        CHEQUE("Cheque");

        private final String label;

        PaymentMethod(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    public enum TransactionStatus {
        INITIATED("Initiated"),
        PROCESSING("Processing"),
        SUCCESS("Success"),
        FAILED("Failed"),
        REFUNDED("Refunded"),
        CANCELLED("Cancelled"),
        PENDING_RECONCILIATION("Pending Reconciliation");

        private final String label;

        TransactionStatus(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    public Boolean isSuccessful() {
        return transactionStatus == TransactionStatus.SUCCESS;
    }

    public Boolean isFailed() {
        return transactionStatus == TransactionStatus.FAILED;
    }

    public Boolean isPending() {
        return transactionStatus == TransactionStatus.INITIATED || 
               transactionStatus == TransactionStatus.PROCESSING;
    }
}

