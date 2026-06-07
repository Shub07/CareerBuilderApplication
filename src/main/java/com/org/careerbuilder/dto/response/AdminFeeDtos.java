package com.org.careerbuilder.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Response payloads for the Admin Student Fees module.
 */
public final class AdminFeeDtos {

    private AdminFeeDtos() {
    }

    // ─── List page header ──────────────────────────────────────────────────────

    public record FeeStatsResponse(
            BigDecimal totalCollected,
            BigDecimal totalPending,
            BigDecimal totalOverdue,
            BigDecimal collectionThisMonth,
            String totalCollectedLabel,
            String totalPendingLabel,
            String totalOverdueLabel,
            String collectionThisMonthLabel
    ) {
    }

    public record StudentFeeListResponse(
            List<StudentFeeRow> students,
            int page,
            int size,
            long totalElements,
            int totalPages,
            String showingLabel
    ) {
    }

    public record StudentFeeRow(
            Long studentId,
            String studentName,
            String classLabel,
            BigDecimal totalFees,
            BigDecimal paid,
            BigDecimal pending,
            String status,
            String statusLabel,
            boolean canCollect,
            boolean canRefund,
            boolean overdue
    ) {
    }

    public record FeeFilterOptionsResponse(
            List<String> classes,
            List<FeeOption> statuses,
            List<FeeOption> feeTypes,
            List<FeeOption> paymentModes
    ) {
    }

    public record FeeOption(String value, String label) {
    }

    // ─── Fee structures ──────────────────────────────────────────────────────

    public record FeeStructureStatsResponse(
            long activeStructures,
            BigDecimal avgFeePerStructure,
            long totalStudentsAssigned,
            String avgFeeLabel
    ) {
    }

    public record FeeStructureListResponse(
            FeeStructureStatsResponse stats,
            List<FeeStructureRow> structures
    ) {
    }

    public record FeeStructureRow(
            Long id,
            String feeName,
            String feeType,
            String targetClass,
            BigDecimal amount,
            String amountLabel,
            String frequency,
            String frequencyLabel,
            String dueDateSchedule,
            String academicYear,
            boolean active
    ) {
    }

    // ─── Collect / Refund + receipt ─────────────────────────────────────────────

    public record CollectFeeResponse(
            boolean success,
            String message,
            ReceiptResponse receipt
    ) {
    }

    public record RefundResponse(
            boolean success,
            String message,
            String receiptNumber
    ) {
    }

    public record ReceiptResponse(
            String receiptNumber,
            String schoolName,
            String studentName,
            String classLabel,
            String admissionNumber,
            String paymentMode,
            LocalDate transactionDate,
            String referenceNumber,
            List<ReceiptItem> items,
            BigDecimal totalPaid,
            String totalPaidLabel
    ) {
    }

    public record ReceiptItem(String description, BigDecimal amount) {
    }

    public record StudentSearchOption(
            Long studentId,
            String name,
            String admissionNumber,
            String classLabel
    ) {
    }

    public record PaidFeeOption(
            Long transactionId,
            Long feeId,
            String label,
            String feeType,
            BigDecimal amount,
            LocalDate paidOn
    ) {
    }

    // ─── Student detail ─────────────────────────────────────────────────────

    public record StudentFeeDetailResponse(
            Long studentId,
            String studentName,
            String classLabel,
            String admissionNumber,
            BigDecimal totalFees,
            BigDecimal paidAmount,
            BigDecimal pendingAmount,
            BigDecimal overdueAmount,
            List<FeeBreakdownRow> breakdown,
            List<PaymentHistoryRow> paymentHistory
    ) {
    }

    public record FeeBreakdownRow(
            Long feeId,
            String feeType,
            BigDecimal totalAmount,
            BigDecimal paid,
            BigDecimal pending,
            LocalDate dueDate,
            String status,
            String statusLabel
    ) {
    }

    public record PaymentHistoryRow(
            Long transactionId,
            LocalDate date,
            String feeType,
            BigDecimal amount,
            String mode,
            String modeLabel,
            String referenceId,
            String collectedBy,
            String receiptNumber,
            String type
    ) {
    }

    // ─── Generic ──────────────────────────────────────────────────────────────

    public record ActionResponse(boolean success, String message) {
    }

    // ─── Async jobs (obligation generation, exports) ─────────────────────────────

    public record JobStatusResponse(
            Long jobId,
            String jobType,
            String status,
            Integer totalItems,
            Integer processedItems,
            String message,
            boolean downloadReady
    ) {
    }
}
