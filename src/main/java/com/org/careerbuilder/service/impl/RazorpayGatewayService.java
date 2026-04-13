package com.org.careerbuilder.service.impl;

import com.org.careerbuilder.dto.request.PaymentInitiateRequest;
import com.org.careerbuilder.models.PaymentTransaction;
import com.org.careerbuilder.service.PaymentGatewayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.UUID;

/**
 * 📌 Razorpay Payment Gateway Implementation
 * You can replace this with actual Razorpay SDK
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RazorpayGatewayService implements PaymentGatewayService {

    @Value("${razorpay.key.id:#{null}}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret:#{null}}")
    private String razorpayKeySecret;

    @Value("${razorpay.webhook.url:http://localhost:9091/api/student/fees/payment/callback}")
    private String webhookUrl;

    @Override
    public String initiatePayment(PaymentTransaction transaction, PaymentInitiateRequest request) {
        log.info("Initiating Razorpay payment for transaction: {}", transaction.getId());
        
        try {
            // Build Razorpay order creation request
            String orderId = "order_" + UUID.randomUUID().toString();
            
            // In production, use Razorpay Java SDK:
            // RazorpayClient client = new RazorpayClient(razorpayKeyId, razorpayKeySecret);
            // JSONObject orderRequest = new JSONObject();
            // orderRequest.put("amount", transaction.getAmount().longValue() * 100); // in paise
            // orderRequest.put("currency", "INR");
            // orderRequest.put("receipt", transaction.getMerchantReferenceId());
            // orderRequest.put("payment_capture", 1);
            // Order order = client.Orders.create(orderRequest);
            
            // For now, simulate gateway response
            String gatewayResponse = simulateGatewayOrder(transaction, orderId);
            
            log.info("Razorpay order created: {}", orderId);
            return gatewayResponse;
            
        } catch (Exception e) {
            log.error("Error initiating Razorpay payment: {}", e.getMessage(), e);
            throw new RuntimeException("Razorpay payment initiation failed", e);
        }
    }

    @Override
    public boolean verifyTransaction(PaymentTransaction transaction) {
        log.info("Verifying transaction with Razorpay: {}", transaction.getId());
        
        try {
            // Call Razorpay API to fetch payment details
            // In production:
            // RazorpayClient client = new RazorpayClient(razorpayKeyId, razorpayKeySecret);
            // Payment payment = client.Payments.fetch(transaction.getTransactionReferenceId());
            // return "captured".equals(payment.get("status"));
            
            // For simulation
            return transaction.isSuccessful();
            
        } catch (Exception e) {
            log.error("Error verifying transaction: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public String refundPayment(PaymentTransaction transaction) {
        log.info("Initiating refund for Razorpay payment: {}", transaction.getId());
        
        try {
            // In production:
            // RazorpayClient client = new RazorpayClient(razorpayKeyId, razorpayKeySecret);
            // JSONObject refundRequest = new JSONObject();
            // refundRequest.put("amount", transaction.getAmount().longValue() * 100);
            // Refund refund = client.Payments.refund(transaction.getTransactionReferenceId(), refundRequest);
            
            String refundId = "refund_" + UUID.randomUUID().toString();
            log.info("Refund initiated: {}", refundId);
            return "{\"refund_id\": \"" + refundId + "\", \"status\": \"success\"}";
            
        } catch (Exception e) {
            log.error("Error refunding payment: {}", e.getMessage(), e);
            throw new RuntimeException("Razorpay refund failed", e);
        }
    }

    @Override
    public String retryPayment(PaymentTransaction transaction) {
        log.info("Retrying payment: {}", transaction.getId());
        return initiatePayment(transaction, null);
    }

    @Override
    public String getTransactionStatus(String transactionReferenceId) {
        log.info("Fetching transaction status from Razorpay: {}", transactionReferenceId);
        
        try {
            // In production:
            // RazorpayClient client = new RazorpayClient(razorpayKeyId, razorpayKeySecret);
            // Payment payment = client.Payments.fetch(transactionReferenceId);
            // return payment.get("status").toString();
            
            return "captured";
            
        } catch (Exception e) {
            log.error("Error fetching transaction status: {}", e.getMessage(), e);
            return "unknown";
        }
    }

    @Override
    public boolean validateCallbackSignature(String data, String signature) {
        log.info("Validating Razorpay callback signature");
        
        try {
            String computedSignature = computeHmacSha256(data, razorpayKeySecret);
            return computedSignature.equals(signature);
            
        } catch (Exception e) {
            log.error("Error validating signature: {}", e.getMessage());
            return false;
        }
    }

    // ===== HELPER METHODS =====

    /**
     * Compute HMAC SHA256 for signature validation
     */
    private String computeHmacSha256(String data, String secret) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
        mac.init(keySpec);
        byte[] hmacBytes = mac.doFinal(data.getBytes());
        return Base64.getEncoder().encodeToString(hmacBytes);
    }

    /**
     * Simulate Razorpay order creation for testing
     */
    private String simulateGatewayOrder(PaymentTransaction transaction, String orderId) {
        return "{" +
                "\"order_id\": \"" + orderId + "\"," +
                "\"amount\": " + transaction.getAmount().longValue() * 100 + "," +
                "\"currency\": \"INR\"," +
                "\"status\": \"created\"," +
                "\"created_at\": \"" + System.currentTimeMillis() / 1000 + "\"" +
                "}";
    }
}

