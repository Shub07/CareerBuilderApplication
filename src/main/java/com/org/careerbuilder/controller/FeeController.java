package com.org.careerbuilder.controller;

import com.org.careerbuilder.dto.request.PaymentCallbackRequest;
import com.org.careerbuilder.dto.request.PaymentInitiateRequest;
import com.org.careerbuilder.dto.response.FeeResponse;
import com.org.careerbuilder.dto.response.PaymentTransactionResponse;
import com.org.careerbuilder.service.FeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 📌 Fee Controller - Handles student fee and payment operations
 */
@Slf4j
@RestController
@RequestMapping("/api/student/fees")
@RequiredArgsConstructor
public class FeeController {

    private final FeeService feeService;

    // ===== FEE RETRIEVAL ENDPOINTS =====

    /**
     * Get all fees for authenticated student (with pagination)
     * GET /api/student/fees?page=0&size=10&sort=dueDate,desc
     */
    @GetMapping
    public ResponseEntity<Page<FeeResponse>> getStudentFees(
            @RequestParam Long studentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "dueDate") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction) {
        
        log.info("Fetching fees for student: {} (page: {}, size: {})", studentId, page, size);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<FeeResponse> fees = feeService.getStudentFees(studentId, pageable);
        
        return ResponseEntity.ok(fees);
    }

    /**
     * Get all fees for student (without pagination)
     * GET /api/student/fees/all
     */
    @GetMapping("/all")
    public ResponseEntity<List<FeeResponse>> getAllFees(@RequestParam Long studentId) {
        log.info("Fetching all fees for student: {}", studentId);
        
        List<FeeResponse> fees = feeService.getAllStudentFees(studentId);
        
        return ResponseEntity.ok(fees);
    }

    /**
     * Get pending fees for student
     * GET /api/student/fees/pending
     */
    @GetMapping("/pending")
    public ResponseEntity<List<FeeResponse>> getPendingFees(@RequestParam Long studentId) {
        log.info("Fetching pending fees for student: {}", studentId);
        
        List<FeeResponse> fees = feeService.getPendingFees(studentId);
        
        return ResponseEntity.ok(fees);
    }

    /**
     * Get overdue fees for student
     * GET /api/student/fees/overdue
     */
    @GetMapping("/overdue")
    public ResponseEntity<List<FeeResponse>> getOverdueFees(@RequestParam Long studentId) {
        log.info("Fetching overdue fees for student: {}", studentId);
        
        List<FeeResponse> fees = feeService.getOverdueFees(studentId);
        
        return ResponseEntity.ok(fees);
    }

    /**
     * Get specific fee details
     * GET /api/student/fees/{feeId}
     */
    @GetMapping("/{feeId}")
    public ResponseEntity<FeeResponse> getFeeById(
            @PathVariable Long feeId,
            @RequestParam Long studentId) {
        
        log.info("Fetching fee: {} for student: {}", feeId, studentId);
        
        FeeResponse fee = feeService.getFeeById(feeId, studentId);
        
        return ResponseEntity.ok(fee);
    }

    // ===== PAYMENT ENDPOINTS =====

    /**
     * Initiate payment with idempotency support
     * POST /api/student/fees/payment/initiate
     * 
     * Request body:
     * {
     *   "fee_id": 1,
     *   "student_id": 1,
     *   "amount": 5000.00,
     *   "payment_method": "CREDIT_CARD",
     *   "idempotency_key": "unique-key-123",
     *   "card_number": "4111111111111111",
     *   "card_expiry": "12/25",
     *   "card_cvv": "123"
     * }
     */
    @PostMapping("/payment/initiate")
    public ResponseEntity<Map<String, Object>> initiatePayment(
            @Valid @RequestBody PaymentInitiateRequest request) {
        
        log.info("Payment initiation requested for fee: {} with idempotency key: {}", 
                request.getFeeId(), request.getIdempotencyKey());
        
        try {
            PaymentTransactionResponse response = feeService.initiatePayment(request);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Payment initiated successfully");
            result.put("transaction", response);
            result.put("status_code", 201);
            
            log.info("Payment initiated successfully with transaction ID: {}", response.getTransactionId());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
            
        } catch (Exception e) {
            log.error("Error initiating payment: {}", e.getMessage());
            
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("message", "Payment initiation failed: " + e.getMessage());
            errorResult.put("error_code", "PAYMENT_INIT_ERROR");
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResult);
        }
    }

    /**
     * Handle payment gateway callback
     * POST /api/student/fees/payment/callback
     * This endpoint is typically called by the payment gateway asynchronously
     * 
     * Request body:
     * {
     *   "merchant_reference_id": "TXN-1234567890",
     *   "transaction_reference_id": "GATEWAY-TXN-ID",
     *   "status": "SUCCESS",
     *   "message": "Payment successful",
     *   "timestamp": "2026-03-31T10:30:00",
     *   "signature": "signature-hash"
     * }
     */
    @PostMapping("/payment/callback")
    public ResponseEntity<Map<String, Object>> handlePaymentCallback(
            @Valid @RequestBody PaymentCallbackRequest request) {
        
        log.info("Payment callback received for transaction reference: {}", 
                request.getTransactionReferenceId());
        
        try {
            PaymentTransactionResponse response = feeService.handlePaymentCallback(request);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Callback processed successfully");
            result.put("transaction", response);
            
            log.info("Payment callback processed successfully");
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("Error processing payment callback: {}", e.getMessage());
            
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("message", "Callback processing failed: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResult);
        }
    }

    /**
     * Refund a payment
     * POST /api/student/fees/payment/{transactionId}/refund
     */
    @PostMapping("/payment/{transactionId}/refund")
    public ResponseEntity<Map<String, Object>> refundPayment(
            @PathVariable Long transactionId,
            @RequestParam Long studentId,
            @RequestParam(required = false, defaultValue = "Requested by student") String reason) {
        
        log.info("Refund requested for transaction: {} by student: {}", transactionId, studentId);
        
        try {
            PaymentTransactionResponse response = feeService.refundPayment(transactionId, studentId, reason);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Refund processed successfully");
            result.put("transaction", response);
            
            log.info("Refund processed successfully for transaction: {}", transactionId);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("Error processing refund: {}", e.getMessage());
            
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("message", "Refund processing failed: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResult);
        }
    }

    // ===== ADMIN ENDPOINTS (for reconciliation and retries) =====

    /**
     * Reconcile payments with gateway
     * POST /api/student/fees/admin/reconcile
     * Note: This should be secured with admin role
     */
    @PostMapping("/admin/reconcile")
    public ResponseEntity<Map<String, Object>> reconcilePayments() {
        log.info("Payment reconciliation initiated");
        
        try {
            feeService.reconcilePayments();
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Payment reconciliation completed successfully");
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("Error during reconciliation: {}", e.getMessage());
            
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("message", "Reconciliation failed: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResult);
        }
    }

    /**
     * Retry failed payments
     * POST /api/student/fees/admin/retry
     * Note: This should be secured with admin role
     */
    @PostMapping("/admin/retry")
    public ResponseEntity<Map<String, Object>> retryFailedPayments() {
        log.info("Payment retry process initiated");
        
        try {
            feeService.retryFailedPayments();
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Payment retry process completed");
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("Error during retry process: {}", e.getMessage());
            
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("message", "Retry process failed: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResult);
        }
    }
}

