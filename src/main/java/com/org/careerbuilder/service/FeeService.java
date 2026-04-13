package com.org.careerbuilder.service;

import com.org.careerbuilder.dto.request.PaymentCallbackRequest;
import com.org.careerbuilder.dto.request.PaymentInitiateRequest;
import com.org.careerbuilder.dto.response.FeeResponse;
import com.org.careerbuilder.dto.response.PaymentTransactionResponse;
import com.org.careerbuilder.models.Fee;
import com.org.careerbuilder.models.PaymentTransaction;
import com.org.careerbuilder.models.Student;
import com.org.careerbuilder.repository.FeeRepository;
import com.org.careerbuilder.repository.PaymentTransactionRepository;
import com.org.careerbuilder.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 📌 Fee Service - Manages fee operations with transaction support
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FeeService {

    private final FeeRepository feeRepository;
    private final PaymentTransactionRepository transactionRepository;
    private final StudentRepository studentRepository;
    private final PaymentGatewayService paymentGatewayService;  // For integration with payment gateway

    // ===== FEE RETRIEVAL OPERATIONS =====

    /**
     * Get all fees for a student with pagination
     */
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public Page<FeeResponse> getStudentFees(Long studentId, Pageable pageable) {
        log.info("Fetching fees for student: {}", studentId);
        
        // Verify student exists and has access
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found: " + studentId));
        
        Page<Fee> fees = feeRepository.findByStudentId(studentId, pageable);
        return fees.map(this::mapToFeeResponse);
    }

    /**
     * Get all fees for a student
     */
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public List<FeeResponse> getAllStudentFees(Long studentId) {
        log.info("Fetching all fees for student: {}", studentId);
        
        List<Fee> fees = feeRepository.findAllByStudentId(studentId);
        return fees.stream()
                .map(this::mapToFeeResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get pending fees for a student
     */
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public List<FeeResponse> getPendingFees(Long studentId) {
        log.info("Fetching pending fees for student: {}", studentId);
        
        List<Fee> fees = feeRepository.findPendingFeesByStudentId(studentId);
        return fees.stream()
                .map(this::mapToFeeResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get overdue fees for a student
     */
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public List<FeeResponse> getOverdueFees(Long studentId) {
        log.info("Fetching overdue fees for student: {}", studentId);
        
        List<Fee> fees = feeRepository.findOverdueFeesForStudent(studentId);
        return fees.stream()
                .map(this::mapToFeeResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get fee by ID with authorization check
     */
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public FeeResponse getFeeById(Long feeId, Long studentId) {
        log.info("Fetching fee: {} for student: {}", feeId, studentId);
        
        Fee fee = feeRepository.findById(feeId)
                .orElseThrow(() -> new RuntimeException("Fee not found: " + feeId));
        
        // Authorization check: ensure student owns this fee
        if (!fee.getStudent().getId().equals(studentId)) {
            throw new RuntimeException("Unauthorized access to fee: " + feeId);
        }
        
        return mapToFeeResponse(fee);
    }

    // ===== PAYMENT OPERATIONS WITH TRANSACTION MANAGEMENT =====

    /**
     * Initiate payment with idempotency support
     * Uses REQUIRES_NEW to create new transaction context
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public PaymentTransactionResponse initiatePayment(PaymentInitiateRequest request) {
        log.info("Initiating payment for fee: {} with idempotency key: {}", 
                request.getFeeId(), request.getIdempotencyKey());
        
        try {
            // Step 1: Check for duplicate payment using idempotency key
            Optional<PaymentTransaction> existingTxn = transactionRepository
                    .findByIdempotencyKey(request.getIdempotencyKey());
            
            if (existingTxn.isPresent()) {
                log.warn("Duplicate payment attempt detected with idempotency key: {}", 
                        request.getIdempotencyKey());
                return mapToPaymentResponse(existingTxn.get());
            }

            // Step 2: Verify fee exists and belongs to student
            Fee fee = feeRepository.findById(request.getFeeId())
                    .orElseThrow(() -> new RuntimeException("Fee not found"));
            
            if (!fee.getStudent().getId().equals(request.getStudentId())) {
                throw new RuntimeException("Unauthorized fee access");
            }

            // Step 3: Validate payment amount
            if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new RuntimeException("Invalid payment amount");
            }

            if (request.getAmount().compareTo(fee.getRemainingAmount()) > 0) {
                throw new RuntimeException("Payment amount exceeds remaining balance");
            }

            // Step 4: Create payment transaction record with status INITIATED
            PaymentTransaction transaction = PaymentTransaction.builder()
                    .student(fee.getStudent())
                    .fee(fee)
                    .amount(request.getAmount())
                    .paymentMethod(PaymentTransaction.PaymentMethod.valueOf(request.getPaymentMethod()))
                    .transactionStatus(PaymentTransaction.TransactionStatus.INITIATED)
                    .idempotencyKey(request.getIdempotencyKey())
                    .build();

            transaction = transactionRepository.save(transaction);
            log.info("Payment transaction created with ID: {}", transaction.getId());

            // Step 5: Call payment gateway
            String gatewayResponse = paymentGatewayService.initiatePayment(transaction, request);
            transaction.setGatewayResponse(gatewayResponse);
            transaction.setTransactionStatus(PaymentTransaction.TransactionStatus.PROCESSING);

            transaction = transactionRepository.save(transaction);
            
            log.info("Payment processing initiated for transaction ID: {}", transaction.getId());
            return mapToPaymentResponse(transaction);

        } catch (Exception e) {
            log.error("Error initiating payment: {}", e.getMessage(), e);
            throw new RuntimeException("Payment initiation failed: " + e.getMessage(), e);
        }
    }

    /**
     * Handle payment gateway callback
     * Uses REQUIRES_NEW for transaction isolation
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public PaymentTransactionResponse handlePaymentCallback(PaymentCallbackRequest request) {
        log.info("Processing payment callback for transaction reference: {}", 
                request.getTransactionReferenceId());
        
        try {
            // Step 1: Find transaction by merchant reference ID
            PaymentTransaction transaction = transactionRepository
                    .findByMerchantReferenceId(request.getMerchantReferenceId())
                    .orElseThrow(() -> new RuntimeException("Transaction not found"));

            // Step 2: Update transaction status based on gateway response
            if ("SUCCESS".equalsIgnoreCase(request.getStatus())) {
                transaction.setTransactionStatus(PaymentTransaction.TransactionStatus.SUCCESS);
                transaction.setProcessedAt(LocalDateTime.now());

                // Step 3: Update fee with payment
                Fee fee = transaction.getFee();
                BigDecimal newPaidAmount = fee.getPaidAmount().add(transaction.getAmount());
                fee.setPaidAmount(newPaidAmount);

                // Update fee status
                if (newPaidAmount.compareTo(fee.getAmount()) >= 0) {
                    fee.setStatus(Fee.FeeStatus.PAID);
                } else {
                    fee.setStatus(Fee.FeeStatus.PARTIAL);
                }

                fee = feeRepository.save(fee);
                log.info("Fee updated after successful payment. Fee ID: {}, New Status: {}", 
                        fee.getId(), fee.getStatus());

            } else if ("FAILED".equalsIgnoreCase(request.getStatus())) {
                transaction.setTransactionStatus(PaymentTransaction.TransactionStatus.FAILED);
                transaction.setErrorMessage(request.getMessage());
                log.warn("Payment failed for transaction ID: {} - {}", 
                        transaction.getId(), request.getMessage());

            } else if ("CANCELLED".equalsIgnoreCase(request.getStatus())) {
                transaction.setTransactionStatus(PaymentTransaction.TransactionStatus.CANCELLED);
                log.info("Payment cancelled for transaction ID: {}", transaction.getId());
            }

            transaction.setTransactionReferenceId(request.getTransactionReferenceId());
            transaction = transactionRepository.save(transaction);

            log.info("Payment callback processed successfully for transaction ID: {}", transaction.getId());
            return mapToPaymentResponse(transaction);

        } catch (Exception e) {
            log.error("Error processing payment callback: {}", e.getMessage(), e);
            throw new RuntimeException("Callback processing failed: " + e.getMessage(), e);
        }
    }

    /**
     * Refund a successful payment transaction
     * Uses REQUIRES_NEW for transaction isolation
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public PaymentTransactionResponse refundPayment(Long transactionId, Long studentId, String reason) {
        log.info("Processing refund for transaction: {}", transactionId);
        
        try {
            // Step 1: Find and validate transaction
            PaymentTransaction transaction = transactionRepository.findById(transactionId)
                    .orElseThrow(() -> new RuntimeException("Transaction not found"));

            if (!transaction.getStudent().getId().equals(studentId)) {
                throw new RuntimeException("Unauthorized refund attempt");
            }

            if (!transaction.isSuccessful()) {
                throw new RuntimeException("Cannot refund non-successful transaction");
            }

            // Step 2: Call payment gateway for refund
            String refundResponse = paymentGatewayService.refundPayment(transaction);
            
            // Step 3: Update transaction status
            transaction.setTransactionStatus(PaymentTransaction.TransactionStatus.REFUNDED);
            transaction.setGatewayResponse(refundResponse);
            transaction = transactionRepository.save(transaction);

            // Step 4: Revert fee payment
            Fee fee = transaction.getFee();
            BigDecimal newPaidAmount = fee.getPaidAmount().subtract(transaction.getAmount());
            fee.setPaidAmount(newPaidAmount.max(BigDecimal.ZERO));
            fee.setStatus(newPaidAmount.compareTo(BigDecimal.ZERO) > 0 ? 
                    Fee.FeeStatus.PARTIAL : Fee.FeeStatus.PENDING);
            feeRepository.save(fee);

            log.info("Refund processed successfully for transaction ID: {}", transactionId);
            return mapToPaymentResponse(transaction);

        } catch (Exception e) {
            log.error("Error processing refund: {}", e.getMessage(), e);
            throw new RuntimeException("Refund processing failed: " + e.getMessage(), e);
        }
    }

    // ===== RECONCILIATION OPERATIONS =====

    /**
     * Reconcile payments - mark transactions as reconciled
     * Uses REQUIRES_NEW to isolate transaction processing
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void reconcilePayments() {
        log.info("Starting payment reconciliation process");
        
        try {
            // Get unreconciled successful transactions older than 1 hour
            LocalDateTime cutoffTime = LocalDateTime.now().minusHours(1);
            List<PaymentTransaction> unreconciledTransactions = 
                    transactionRepository.findUnreconciledTransactionsOlderThan(cutoffTime);

            log.info("Found {} transactions for reconciliation", unreconciledTransactions.size());

            for (PaymentTransaction transaction : unreconciledTransactions) {
                try {
                    // Verify with payment gateway
                    boolean isVerified = paymentGatewayService.verifyTransaction(transaction);
                    
                    if (isVerified) {
                        transaction.setIsReconciled(true);
                        transaction.setReconciliationAt(LocalDateTime.now());
                        transactionRepository.save(transaction);
                        log.info("Transaction {} reconciled successfully", transaction.getId());
                    } else {
                        log.warn("Transaction {} failed verification", transaction.getId());
                    }
                } catch (Exception e) {
                    log.error("Error reconciling transaction {}: {}", transaction.getId(), e.getMessage());
                }
            }

            log.info("Payment reconciliation process completed");

        } catch (Exception e) {
            log.error("Error in reconciliation process: {}", e.getMessage(), e);
            throw new RuntimeException("Reconciliation failed: " + e.getMessage(), e);
        }
    }

    // ===== RETRY MECHANISM =====

    /**
     * Retry failed payment transactions
     * Uses REQUIRES_NEW for transaction isolation
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void retryFailedPayments() {
        log.info("Starting retry process for failed payments");
        
        try {
            int maxRetries = 3;
            LocalDateTime retryAfter = LocalDateTime.now().minusMinutes(5);
            
            List<PaymentTransaction> eligibleTransactions = 
                    transactionRepository.findTransactionsEligibleForRetry(maxRetries, retryAfter);

            log.info("Found {} transactions eligible for retry", eligibleTransactions.size());

            for (PaymentTransaction transaction : eligibleTransactions) {
                try {
                    log.info("Retrying payment transaction: {}", transaction.getId());
                    
                    transaction.setRetryCount(transaction.getRetryCount() + 1);
                    transaction.setLastRetryAt(LocalDateTime.now());
                    transaction.setTransactionStatus(PaymentTransaction.TransactionStatus.PROCESSING);
                    
                    transaction = transactionRepository.save(transaction);
                    
                    // Retry payment gateway call
                    String gatewayResponse = paymentGatewayService.retryPayment(transaction);
                    transaction.setGatewayResponse(gatewayResponse);
                    
                    transactionRepository.save(transaction);
                    log.info("Retry initiated for transaction: {}", transaction.getId());
                    
                } catch (Exception e) {
                    log.error("Error retrying transaction {}: {}", transaction.getId(), e.getMessage());
                }
            }

            log.info("Retry process completed");

        } catch (Exception e) {
            log.error("Error in retry process: {}", e.getMessage(), e);
            throw new RuntimeException("Retry process failed: " + e.getMessage(), e);
        }
    }

    // ===== HELPER METHODS =====

    private FeeResponse mapToFeeResponse(Fee fee) {
        BigDecimal remainingAmount = fee.getRemainingAmount();
        Double percentagePaid = fee.getAmount().compareTo(BigDecimal.ZERO) > 0 ?
                (fee.getPaidAmount().doubleValue() / fee.getAmount().doubleValue()) * 100 : 0;

        Long daysOverdue = 0L;
        if (fee.isOverdue()) {
            daysOverdue = LocalDate.now().toEpochDay() - fee.getDueDate().toEpochDay();
        }

        return FeeResponse.builder()
                .feeId(fee.getId())
                .studentId(fee.getStudent().getId())
                .feeType(fee.getFeeType())
                .amount(fee.getAmount())
                .paidAmount(fee.getPaidAmount())
                .remainingAmount(remainingAmount)
                .percentagePaid(percentagePaid)
                .dueDate(fee.getDueDate())
                .status(fee.getStatus().name())
                .statusLabel(fee.getStatus().getLabel())
                .academicYear(fee.getAcademicYear())
                .term(fee.getTerm())
                .description(fee.getDescription())
                .isOverdue(fee.isOverdue())
                .daysOverdue(daysOverdue)
                .createdAt(fee.getCreatedAt())
                .updatedAt(fee.getUpdatedAt())
                .dueReminderSent(fee.getDueReminderSent())
                .overdueReminderSent(fee.getOverdueReminderSent())
                .build();
    }

    private PaymentTransactionResponse mapToPaymentResponse(PaymentTransaction transaction) {
        return PaymentTransactionResponse.builder()
                .transactionId(transaction.getId())
                .studentId(transaction.getStudent().getId())
                .feeId(transaction.getFee().getId())
                .amount(transaction.getAmount())
                .paymentMethod(transaction.getPaymentMethod().name())
                .transactionStatus(transaction.getTransactionStatus().name())
                .transactionReferenceId(transaction.getTransactionReferenceId())
                .merchantReferenceId(transaction.getMerchantReferenceId())
                .errorMessage(transaction.getErrorMessage())
                .isReconciled(transaction.getIsReconciled())
                .retryCount(transaction.getRetryCount())
                .createdAt(transaction.getCreatedAt())
                .updatedAt(transaction.getUpdatedAt())
                .processedAt(transaction.getProcessedAt())
                .reconciliationAt(transaction.getReconciliationAt())
                .build();
    }
}

