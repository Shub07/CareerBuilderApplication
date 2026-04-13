package com.org.careerbuilder.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 📌 Payment Transaction Response DTO
 */
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentTransactionResponse {

    @JsonProperty("transaction_id")
    private Long transactionId;

    @JsonProperty("student_id")
    private Long studentId;

    @JsonProperty("fee_id")
    private Long feeId;

    @JsonProperty("amount")
    private BigDecimal amount;

    @JsonProperty("payment_method")
    private String paymentMethod;

    @JsonProperty("transaction_status")
    private String transactionStatus;

    @JsonProperty("transaction_reference_id")
    private String transactionReferenceId;

    @JsonProperty("merchant_reference_id")
    private String merchantReferenceId;

    @JsonProperty("error_message")
    private String errorMessage;

    @JsonProperty("is_reconciled")
    private Boolean isReconciled;

    @JsonProperty("retry_count")
    private Integer retryCount;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    @JsonProperty("processed_at")
    private LocalDateTime processedAt;

    @JsonProperty("reconciliation_at")
    private LocalDateTime reconciliationAt;
}

