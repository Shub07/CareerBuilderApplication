package com.org.careerbuilder.models;

import com.org.careerbuilder.models.enums.FeeFrequency;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * A reusable fee template ("Create Fee Structure") that defines how much a given
 * fee type costs for a target class/section, its cadence and due schedule.
 * Concrete per-student obligations are materialised as {@link Fee} rows.
 */
@Entity
@Table(name = "fee_structures", indexes = {
        @Index(name = "idx_fee_structure_school", columnList = "school_id"),
        @Index(name = "idx_fee_structure_class", columnList = "target_class_name"),
        @Index(name = "idx_fee_structure_year", columnList = "academic_year")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeeStructure {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    @Column(name = "fee_type", nullable = false, length = 60)
    private String feeType;

    /** Display name e.g. "Annual Tuition Fee". Falls back to feeType when blank. */
    @Column(name = "fee_name", length = 120)
    private String feeName;

    /** Target grade label e.g. "Grade 10", or "ALL" for all classes. */
    @Column(name = "target_class_name", nullable = false, length = 50)
    private String targetClassName;

    /** Optional section; "ALL" (or null) means every section of the class. */
    @Column(name = "target_section", length = 10)
    private String targetSection;

    @Column(name = "academic_year", length = 20)
    private String academicYear;

    @Column(name = "financial_year", length = 20)
    private String financialYear;

    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "frequency", nullable = false, length = 20)
    private FeeFrequency frequency;

    /** Fixed due date (one-time / yearly / quarterly templates). */
    @Column(name = "due_date")
    private LocalDate dueDate;

    /** Day-of-month for recurring monthly templates (e.g. 5 → "Every 5th"). */
    @Column(name = "due_day_of_month")
    private Integer dueDayOfMonth;

    @Column(name = "late_fee_enabled", nullable = false)
    private boolean lateFeeEnabled;

    @Column(name = "notes", length = 1000)
    private String notes;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "deleted", nullable = false)
    private boolean deleted;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
