package com.org.careerbuilder.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 📌 Fee Details Response DTO
 */
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeeResponse {

    @JsonProperty("fee_id")
    private Long feeId;

    @JsonProperty("student_id")
    private Long studentId;

    @JsonProperty("fee_type")
    private String feeType;

    @JsonProperty("amount")
    private BigDecimal amount;

    @JsonProperty("paid_amount")
    private BigDecimal paidAmount;

    @JsonProperty("remaining_amount")
    private BigDecimal remainingAmount;

    @JsonProperty("percentage_paid")
    private Double percentagePaid;

    @JsonProperty("due_date")
    private LocalDate dueDate;

    @JsonProperty("status")
    private String status;

    @JsonProperty("status_label")
    private String statusLabel;

    @JsonProperty("academic_year")
    private String academicYear;

    @JsonProperty("term")
    private String term;

    @JsonProperty("description")
    private String description;

    @JsonProperty("is_overdue")
    private Boolean isOverdue;

    @JsonProperty("days_overdue")
    private Long daysOverdue;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    @JsonProperty("due_reminder_sent")
    private Boolean dueReminderSent;

    @JsonProperty("overdue_reminder_sent")
    private Boolean overdueReminderSent;
}

