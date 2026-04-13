package com.org.careerbuilder.service;

import com.org.careerbuilder.dto.request.PaymentInitiateRequest;
import com.org.careerbuilder.models.PaymentTransaction;

/**
 * 📌 Payment Gateway Service Interface - Abstraction for payment gateway integration
 * Implement this with actual gateway providers like Razorpay, PayPal, Stripe, etc.
 */
public interface PaymentGatewayService {

    /**
     * Initiate payment with gateway
     */
    String initiatePayment(PaymentTransaction transaction, PaymentInitiateRequest request);

    /**
     * Verify transaction status with gateway
     */
    boolean verifyTransaction(PaymentTransaction transaction);

    /**
     * Refund a payment
     */
    String refundPayment(PaymentTransaction transaction);

    /**
     * Retry payment
     */
    String retryPayment(PaymentTransaction transaction);

    /**
     * Get transaction status from gateway
     */
    String getTransactionStatus(String transactionReferenceId);

    /**
     * Validate payment signature from callback
     */
    boolean validateCallbackSignature(String data, String signature);
}

