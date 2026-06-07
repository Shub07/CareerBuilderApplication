package com.org.careerbuilder.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Request payloads for the Admin Student Fees module.
 */
public final class AdminFeeRequests {

    private AdminFeeRequests() {
    }

    /** "Create Fee Structure" modal. */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateFeeStructureRequest {
        /** Set server-side from JWT; clients must not supply a different tenant. */
        private Long schoolId;
        @NotBlank
        private String feeType;
        private String feeName;
        /** Grade label e.g. "Grade 10" or "ALL". */
        @NotBlank
        private String targetClassName;
        private String targetSection;
        private String academicYear;
        private String financialYear;
        @NotNull
        @DecimalMin(value = "0.0", inclusive = false)
        private BigDecimal amount;
        /** ONE_TIME | MONTHLY | QUARTERLY | HALF_YEARLY | YEARLY */
        @NotBlank
        private String frequency;
        private LocalDate dueDate;
        private Integer dueDayOfMonth;
        private boolean lateFeeEnabled;
        private String notes;
        /** When true, materialises fee obligations for students in the target class. */
        @Builder.Default
        private boolean generateForStudents = true;
        private String performedBy;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateFeeStructureRequest {
        /** Set server-side from JWT. */
        private Long schoolId;
        private String feeName;
        private BigDecimal amount;
        private String frequency;
        private LocalDate dueDate;
        private Integer dueDayOfMonth;
        private Boolean lateFeeEnabled;
        private Boolean active;
        private String notes;
        private String performedBy;
    }

    /** "Collect Fee" modal. */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CollectFeeRequest {
        /** Set server-side from JWT. */
        private Long schoolId;
        @NotNull
        private Long studentId;
        /** Optional: settle a specific fee obligation. */
        private Long feeId;
        @NotBlank
        private String feeType;
        @NotNull
        @DecimalMin(value = "0.0", inclusive = false)
        private BigDecimal amount;
        /** CASH | UPI | CARD | BANK_TRANSFER | CHEQUE | ONLINE */
        @NotBlank
        private String paymentMethod;
        private LocalDate paymentDate;
        private String referenceNumber;
        private String notes;
        private String performedBy;
    }

    /** "Refund Fee" modal. */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RefundFeeRequest {
        /** Set server-side from JWT. */
        private Long schoolId;
        @NotNull
        private Long studentId;
        /** Original collection transaction being refunded (from "Paid Fees List"). */
        private Long sourceTransactionId;
        private Long feeId;
        @NotNull
        @DecimalMin(value = "0.0", inclusive = false)
        private BigDecimal refundAmount;
        @NotBlank
        private String refundMethod;
        @NotBlank
        private String refundReason;
        private String notes;
        private String performedBy;
    }

    /** "Export Fee Reports" modal. */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ExportFeeReportRequest {
        /** Set server-side from JWT. */
        private Long schoolId;
        /** At least one of the report-type flags. */
        private List<String> reportTypes;
        private String className;
        private String section;
        private String feeCategory;
        private List<String> paymentStatuses;
        private LocalDate startDate;
        private LocalDate endDate;
        private boolean includePenalty;
        private boolean includeRefunds;
        private boolean includePartialPayments;
        /** EXCEL | CSV | PDF */
        @Builder.Default
        private String format = "EXCEL";
    }
}
