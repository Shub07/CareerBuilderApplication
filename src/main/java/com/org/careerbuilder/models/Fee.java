package com.org.careerbuilder.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 📌 Fee Model - Student Fee Management with Transaction Support
 */
@Entity
@Table(name = "fees", indexes = {
        @Index(name = "idx_fee_student", columnList = "student_id"),
        @Index(name = "idx_fee_status", columnList = "status"),
        @Index(name = "idx_fee_due_date", columnList = "due_date")
})
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Fee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @NotNull
    @Column(name = "fee_type", nullable = false, length = 50)
    private String feeType;  // TUITION, EXAMINATION, LIBRARY, SPORTS, HOSTEL, etc.

    @Column(name = "amount", precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "total_amount")  // Keep for backward compatibility
    private Double totalAmount;

    @NotNull
    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @NotNull
    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private FeeStatus status;  // PENDING, PARTIAL, PAID, OVERDUE, CANCELLED

    @Column(name = "paid_amount", precision = 10, scale = 2)
    private BigDecimal paidAmount;

    @Column(name = "academic_year", length = 20)
    private String academicYear;  // 2025-2026

    @Column(name = "term", length = 20)
    private String term;  // For termly fees

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "due_reminder_sent")
    private Boolean dueReminderSent;

    @Column(name = "overdue_reminder_sent")
    private Boolean overdueReminderSent;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        
        // Ensure amount and totalAmount are consistent
        if (amount == null && totalAmount != null) {
            amount = BigDecimal.valueOf(totalAmount);
        } else if (amount != null && totalAmount == null) {
            totalAmount = amount.doubleValue();
        }
        
        if (paidAmount == null) {
            paidAmount = BigDecimal.ZERO;
        }
        if (dueReminderSent == null) {
            dueReminderSent = false;
        }
        if (overdueReminderSent == null) {
            overdueReminderSent = false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        
        // Sync amount and totalAmount
        if (amount != null) {
            totalAmount = amount.doubleValue();
        }
        
        // Update status based on paid amount
        if (paidAmount != null) {
            if (paidAmount.compareTo(getAmountAsDecimal()) >= 0) {
                status = FeeStatus.PAID;
            } else if (paidAmount.compareTo(BigDecimal.ZERO) > 0) {
                status = FeeStatus.PARTIAL;
            } else if (LocalDate.now().isAfter(dueDate) && status != FeeStatus.PAID) {
                status = FeeStatus.OVERDUE;
            }
        }
    }

    public enum FeeStatus {
        PENDING("Pending"),
        PARTIAL("Partially Paid"),
        PAID("Paid"),
        OVERDUE("Overdue"),
        CANCELLED("Cancelled"),
        REFUNDED("Refunded");

        private final String label;

        FeeStatus(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    // ===== HELPER METHODS =====

    public BigDecimal getAmountAsDecimal() {
        if (amount != null) {
            return amount;
        }
        return totalAmount != null ? BigDecimal.valueOf(totalAmount) : BigDecimal.ZERO;
    }

    public BigDecimal getRemainingAmount() {
        BigDecimal feeAmount = getAmountAsDecimal();
        BigDecimal paid = paidAmount != null ? paidAmount : BigDecimal.ZERO;
        return feeAmount.subtract(paid);
    }

    public Boolean isPaid() {
        return status == FeeStatus.PAID;
    }

    public Boolean isOverdue() {
        return LocalDate.now().isAfter(dueDate) && status != FeeStatus.PAID;
    }
}

