package com.org.careerbuilder.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * 📌 Payment Initiation Request DTO
 */
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentInitiateRequest {

    @NotNull(message = "Fee ID is required")
    @JsonProperty("fee_id")
    private Long feeId;

    @NotNull(message = "Student ID is required")
    @JsonProperty("student_id")
    private Long studentId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @JsonProperty("amount")
    private BigDecimal amount;

    @NotNull(message = "Payment method is required")
    @JsonProperty("payment_method")
    private String paymentMethod;  // CREDIT_CARD, DEBIT_CARD, UPI, NET_BANKING, etc.

    @NotNull(message = "Idempotency key is required for idempotent requests")
    @Size(min = 5, max = 100, message = "Idempotency key must be between 5 and 100 characters")
    @JsonProperty("idempotency_key")
    private String idempotencyKey;

    @JsonProperty("description")
    private String description;

    // Payment gateway specific fields
    @JsonProperty("card_number")
    private String cardNumber;

    @JsonProperty("card_expiry")
    private String cardExpiry;

    @JsonProperty("card_cvv")
    private String cardCVV;

    @JsonProperty("upi_id")
    private String upiId;

    @JsonProperty("bank_account")
    private String bankAccount;

    @JsonProperty("return_url")
    private String returnUrl;
}

