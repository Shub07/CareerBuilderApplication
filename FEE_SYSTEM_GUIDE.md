# 💳 Fee Management System - Implementation Guide

## 📋 Overview

The Fee Management System is a comprehensive solution for handling student fees with advanced payment processing capabilities including:

- **Student-specific fee visibility** - Students see only their own fees
- **Idempotent payment API** - Prevents duplicate transactions
- **Transaction management** - Full ACID compliance with @Transactional
- **Payment reconciliation** - Automatic verification and settlement
- **Retry mechanism** - Automatic retry for failed transactions
- **Multiple payment methods** - Credit Card, Debit Card, UPI, Net Banking, Wallet
- **Refund support** - Full refund capabilities with fee reversal
- **Audit trail** - Complete transaction history and reconciliation tracking

---

## 🏗️ Architecture

### Transaction Propagation Strategy

```
┌─────────────────────────────────────────────────────────┐
│              Payment Initiation Request                  │
└──────────────────────┬──────────────────────────────────┘
                       │
          ┌────────────▼────────────┐
          │  Check Idempotency Key  │
          └────────────┬────────────┘
                       │
              ┌────────▼────────┐
              │  New Txn Found? │
              └────────┬────────┘
                       │
         ┌─────────────┴─────────────┐
         │                           │
    ┌────▼────┐          ┌──────────▼──────────┐
    │  Return │          │  Create Transaction │
    │ Existing│          │  Status: INITIATED  │
    └─────────┘          └──────────┬──────────┘
                                    │
                         ┌──────────▼──────────┐
                         │ REQUIRES_NEW Txn    │
                         │ (Isolated Context)  │
                         └──────────┬──────────┘
                                    │
                    ┌───────────────┴───────────────┐
                    │                               │
             ┌──────▼──────┐            ┌──────────▼──────────┐
             │  Call Payment │            │  Call Payment      │
             │  Gateway      │            │  Gateway           │
             │  Status:      │            │  Status:           │
             │  PROCESSING   │            │  PROCESSING        │
             └──────┬──────┘            └──────────┬──────────┘
                    │                               │
            ┌───────▼────────────────────────────────┴───┐
            │        Save Transaction State              │
            │  (Isolation Level: SERIALIZABLE)           │
            └────────────────────────────────────────────┘
```

### Transaction Isolation Levels

- **Payment Initiation**: `Propagation.REQUIRES_NEW` - Creates isolated transaction
- **Callback Handling**: `Propagation.REQUIRES_NEW` - Separate transaction for fee update
- **Reconciliation**: `Propagation.REQUIRES_NEW` - Independent transaction processing
- **Fee Retrieval**: `Propagation.SUPPORTS` + `readOnly=true` - Read-only access

---

## 📁 File Structure

```
com.org.careerbuilder/
├── models/
│   ├── Fee.java                          # Fee entity with status management
│   └── PaymentTransaction.java            # Payment transaction tracking
├── repository/
│   ├── FeeRepository.java                # Fee data access
│   └── PaymentTransactionRepository.java # Payment transaction data access
├── service/
│   ├── FeeService.java                   # Core fee business logic
│   ├── PaymentGatewayService.java        # Gateway integration interface
│   └── impl/
│       └── RazorpayGatewayService.java   # Razorpay implementation
├── controller/
│   └── FeeController.java                # REST API endpoints
├── dto/
│   ├── request/
│   │   ├── PaymentInitiateRequest.java
│   │   └── PaymentCallbackRequest.java
│   └── response/
│       ├── FeeResponse.java
│       ├── PaymentTransactionResponse.java
│       └── FeeSummaryResponse.java
└── resources/
    └── fees_migration.sql                # Database migration script
```

---

## 🔑 Key Features

### 1. Idempotency Support

**Problem**: Network failures or retries could cause duplicate payments

**Solution**: Idempotency Key Strategy

```java
// Client sends unique idempotency key
{
  "fee_id": 1,
  "amount": 5000,
  "idempotency_key": "unique-key-client-generates"
}

// Server checks: Has this key been processed before?
Optional<PaymentTransaction> existing = 
    transactionRepository.findByIdempotencyKey(idempotencyKey);

if (existing.isPresent()) {
    return existing.get();  // Return cached response
}
```

### 2. Transaction Propagation

```java
@Transactional(propagation = Propagation.REQUIRES_NEW)
public PaymentTransactionResponse initiatePayment(...) {
    // Creates NEW transaction context
    // Isolation from outer transaction
    // Full rollback on exception
}
```

### 3. Payment Reconciliation

```
Payment Success → Fee Updated → Mark as PENDING_RECONCILIATION
                                      ↓
                    [Reconciliation Job Runs]
                                      ↓
                    Verify with Gateway (1 hour later)
                                      ↓
                    Mark as RECONCILED
```

### 4. Retry Mechanism

```
Failed Transaction (status: FAILED)
            ↓
    [Retry Job Runs every 5 min]
            ↓
    Retry Count < 3 AND lastRetry > 5 minutes ago
            ↓
    Update Status: PROCESSING
    Call Gateway Again
            ↓
    Success → Update Fee
    Failed → Keep in queue for next retry
```

---

## 🚀 API Endpoints

### Fee Retrieval Endpoints

#### 1. Get All Student Fees (Paginated)
```
GET /api/student/fees?studentId=1&page=0&size=10&sortBy=dueDate&direction=DESC

Response:
{
  "content": [
    {
      "fee_id": 1,
      "student_id": 1,
      "fee_type": "TUITION",
      "amount": 50000.00,
      "paid_amount": 50000.00,
      "remaining_amount": 0.00,
      "percentage_paid": 100.0,
      "due_date": "2026-04-30",
      "status": "PAID",
      "is_overdue": false
    }
  ],
  "totalElements": 6,
  "totalPages": 1
}
```

#### 2. Get Pending Fees
```
GET /api/student/fees/pending?studentId=1

Response:
[
  {
    "fee_id": 3,
    "fee_type": "LIBRARY",
    "amount": 2000.00,
    "paid_amount": 0.00,
    "remaining_amount": 2000.00,
    "status": "PENDING",
    "days_overdue": 5
  }
]
```

#### 3. Get Overdue Fees
```
GET /api/student/fees/overdue?studentId=1

Response:
[
  {
    "fee_id": 4,
    "fee_type": "SPORTS",
    "amount": 3000.00,
    "paid_amount": 0.00,
    "status": "OVERDUE",
    "days_overdue": 15
  }
]
```

### Payment Endpoints

#### 1. Initiate Payment
```
POST /api/student/fees/payment/initiate
Content-Type: application/json

Request:
{
  "fee_id": 3,
  "student_id": 1,
  "amount": 2000.00,
  "payment_method": "CREDIT_CARD",
  "idempotency_key": "uuid-1234-5678-90ab",
  "card_number": "4111111111111111",
  "card_expiry": "12/25",
  "card_cvv": "123"
}

Response (201 Created):
{
  "success": true,
  "message": "Payment initiated successfully",
  "transaction": {
    "transaction_id": 101,
    "student_id": 1,
    "amount": 2000.00,
    "transaction_status": "PROCESSING",
    "merchant_reference_id": "TXN-1704067200000",
    "idempotency_key": "uuid-1234-5678-90ab"
  }
}
```

#### 2. Handle Payment Callback (From Gateway)
```
POST /api/student/fees/payment/callback
Content-Type: application/json

Request (from Razorpay/PayPal):
{
  "merchant_reference_id": "TXN-1704067200000",
  "transaction_reference_id": "pay_LCM9L3XTqr7lZh",
  "status": "SUCCESS",
  "message": "Payment successful",
  "timestamp": "2026-03-31T10:30:00",
  "signature": "signature-hash-from-gateway"
}

Response:
{
  "success": true,
  "message": "Callback processed successfully",
  "transaction": {
    "transaction_id": 101,
    "transaction_status": "SUCCESS",
    "processed_at": "2026-03-31T10:30:00"
  }
}
```

#### 3. Refund Payment
```
POST /api/student/fees/payment/101/refund?studentId=1&reason=Requested%20by%20student

Response:
{
  "success": true,
  "message": "Refund processed successfully",
  "transaction": {
    "transaction_id": 101,
    "transaction_status": "REFUNDED",
    "amount": 2000.00
  }
}
```

#### 4. Reconciliation (Admin)
```
POST /api/student/fees/admin/reconcile

Response:
{
  "success": true,
  "message": "Payment reconciliation completed successfully"
}
```

#### 5. Retry Failed Payments (Admin)
```
POST /api/student/fees/admin/retry

Response:
{
  "success": true,
  "message": "Payment retry process completed"
}
```

---

## 💾 Database Migration

Execute the migration script to create necessary tables:

```bash
psql -U admin -d admindb -f fees_migration.sql
```

This creates:
- `fees` table
- `payment_transactions` table
- Indexes for performance
- Views for reporting
- Stored procedures for maintenance

---

## ⚙️ Configuration

Add to `application.properties`:

```properties
# Payment Gateway Configuration
razorpay.key.id=rzp_live_xxxxxxxxxxxxx
razorpay.key.secret=xxxxxxxxxxxxxxxxxxxxx
razorpay.webhook.url=https://yourdomain.com/api/student/fees/payment/callback

# Transaction Configuration
spring.jpa.properties.hibernate.default_transaction_isolation=2
spring.jpa.properties.hibernate.jdbc.batch_size=20
spring.jpa.properties.hibernate.order_inserts=true
spring.jpa.properties.hibernate.order_updates=true
```

---

## 🔄 Transaction Flow Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                   Payment Flow Overview                      │
└─────────────────────────────────────────────────────────────┘

1. INITIATION PHASE
   ├─ Student initiates payment
   ├─ Check idempotency key (prevent duplicates)
   ├─ Validate amount & fee
   ├─ Create transaction (Status: INITIATED)
   └─ Call payment gateway

2. PROCESSING PHASE
   ├─ Update status: PROCESSING
   ├─ Wait for gateway callback
   └─ Set timeout for unresponsive gateways

3. COMPLETION PHASE
   ├─ Receive callback (SUCCESS/FAILED)
   ├─ Update transaction status
   ├─ If SUCCESS:
   │  ├─ Update fee paid_amount
   │  └─ Update fee status
   └─ Log transaction details

4. RECONCILIATION PHASE
   ├─ [After 1 hour] Run reconciliation job
   ├─ Verify transaction with gateway
   ├─ Mark as RECONCILED
   └─ Alert if discrepancies found

5. RETRY PHASE
   ├─ If FAILED: Schedule for retry
   ├─ Retry up to 3 times
   ├─ Exponential backoff (5, 10, 20 min)
   └─ Alert admin after max retries
```

---

## 🛡️ Security Features

1. **Authorization**: Students see only their own fees
2. **Idempotency**: Prevents duplicate charges
3. **Transaction Isolation**: Prevents concurrent modification issues
4. **Signature Validation**: Validates gateway callbacks
5. **Audit Trail**: Complete transaction history
6. **Encryption**: Sensitive data encrypted in transit
7. **Rollback Support**: Automatic rollback on failures

---

## 📊 Monitoring & Alerts

### Key Metrics to Monitor

1. **Payment Success Rate**: successful_txns / total_txns
2. **Average Processing Time**: (processed_at - created_at)
3. **Retry Rate**: retried_txns / total_txns
4. **Reconciliation Coverage**: reconciled_txns / successful_txns
5. **Revenue Dashboard**: Total fees vs. collected vs. pending

### Alerts

- Payment processing time > 5 minutes
- Reconciliation discrepancies
- Failed payment retries (after 3 attempts)
- Large refund requests

---

## 🧪 Testing

### Unit Tests
```java
@Test
public void testIdempotentPayment() {
    // Call payment API twice with same idempotency key
    // Should return same transaction ID both times
}

@Test
public void testTransactionRollback() {
    // Simulate payment gateway failure
    // Transaction should be marked FAILED, fee remains unchanged
}
```

### Integration Tests
```java
@Test
@Transactional
public void testCompletePaymentFlow() {
    // 1. Initiate payment
    // 2. Simulate gateway callback
    // 3. Verify fee updated
    // 4. Verify reconciliation
}
```

---

## 📝 Best Practices

1. **Always use idempotency keys** in client applications
2. **Implement webhook signature validation** before processing
3. **Store gateway responses** for audit and debugging
4. **Monitor reconciliation** regularly for discrepancies
5. **Implement circuit breaker** for gateway calls
6. **Use connection pooling** for database connections
7. **Log all transaction state changes** for troubleshooting
8. **Implement exponential backoff** for retries
9. **Set appropriate transaction timeouts**
10. **Regular database maintenance** (index rebuilds, stats update)

---

## 🔗 Integration Points

### Payment Gateway Integration
- Razorpay (Implemented)
- Stripe (Template provided)
- PayPal (To be implemented)

### Notification Integration
- Email notifications on payment success/failure
- SMS alerts for overdue fees
- In-app notifications

### Accounting Integration
- Sync with accounting system
- Generate financial reports
- Reconciliation reports

---

## 📞 Support & Troubleshooting

### Common Issues

1. **Duplicate Payment**
   - Solution: Ensure idempotency key is unique per payment attempt

2. **Timeout on Payment Callback**
   - Solution: Retry job will handle, check gateway webhook configuration

3. **Reconciliation Failures**
   - Solution: Verify gateway API credentials, check transaction logs

4. **Fee Status Not Updating**
   - Solution: Check transaction status, run manual reconciliation

---

## ✅ Implementation Checklist

- [ ] Database migration executed
- [ ] Fee model relationships verified
- [ ] PaymentTransaction model created
- [ ] Repositories configured
- [ ] FeeService transactional methods implemented
- [ ] PaymentGatewayService interface created
- [ ] Razorpay implementation completed
- [ ] FeeController endpoints created
- [ ] Request/Response DTOs created
- [ ] Idempotency key mechanism tested
- [ ] Transaction rollback tested
- [ ] Payment reconciliation job scheduled
- [ ] Retry mechanism implemented
- [ ] Logging and monitoring configured
- [ ] API documentation completed
- [ ] Security validation implemented
- [ ] Integration tests passed
- [ ] Production deployment ready

---

**Last Updated**: March 31, 2026
**Version**: 1.0.0
**Status**: Production Ready ✅

