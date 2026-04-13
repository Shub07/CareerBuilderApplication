package com.org.careerbuilder.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * 📌 Payment Callback Request DTO - Received from payment gateway
 */
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentCallbackRequest {

    @NotNull(message = "Merchant reference ID is required")
    @JsonProperty("merchant_reference_id")
    private String merchantReferenceId;

    @NotNull(message = "Transaction reference ID is required")
    @JsonProperty("transaction_reference_id")
    private String transactionReferenceId;

    @NotNull(message = "Status is required")
    @JsonProperty("status")
    private String status;  // SUCCESS, FAILED, CANCELLED

    @JsonProperty("message")
    private String message;

    @JsonProperty("error_code")
    private String errorCode;

    @JsonProperty("gateway_response")
    private String gatewayResponse;

    @JsonProperty("timestamp")
    private String timestamp;

    @JsonProperty("signature")
    private String signature;  // For validating callback authenticity
}

