# 🚀 Fee System - Quick Start Guide

## ⚡ Get Started in 5 Minutes

### 1. **Verify Backend is Running**
```bash
curl http://localhost:9091/test
# Response: Services are up ..
```

### 2. **Get Student Fees**
```bash
curl -X GET "http://localhost:9091/api/student/fees?studentId=1&page=0&size=10"
```

**Response:**
```json
{
  "content": [
    {
      "fee_id": 1,
      "fee_type": "TUITION",
      "amount": 50000.00,
      "paid_amount": 50000.00,
      "remaining_amount": 0.00,
      "status": "PAID",
      "percentage_paid": 100.0
    },
    {
      "fee_id": 3,
      "fee_type": "LIBRARY",
      "amount": 2000.00,
      "paid_amount": 0.00,
      "remaining_amount": 2000.00,
      "status": "PENDING",
      "percentage_paid": 0.0
    }
  ],
  "totalElements": 6,
  "totalPages": 1
}
```

### 3. **Get Pending Fees**
```bash
curl -X GET "http://localhost:9091/api/student/fees/pending?studentId=1"
```

### 4. **Initiate Payment**
```bash
curl -X POST "http://localhost:9091/api/student/fees/payment/initiate" \
  -H "Content-Type: application/json" \
  -d '{
    "fee_id": 3,
    "student_id": 1,
    "amount": 2000.00,
    "payment_method": "CREDIT_CARD",
    "idempotency_key": "unique-key-123-'$(uuidgen)'",
    "card_number": "4111111111111111",
    "card_expiry": "12/25",
    "card_cvv": "123"
  }'
```

**Response:**
```json
{
  "success": true,
  "message": "Payment initiated successfully",
  "transaction": {
    "transaction_id": 101,
    "amount": 2000.00,
    "transaction_status": "PROCESSING",
    "merchant_reference_id": "TXN-1704067200000"
  }
}
```

### 5. **Handle Payment Callback** (From Payment Gateway)
```bash
curl -X POST "http://localhost:9091/api/student/fees/payment/callback" \
  -H "Content-Type: application/json" \
  -d '{
    "merchant_reference_id": "TXN-1704067200000",
    "transaction_reference_id": "pay_LCM9L3XTqr7lZh",
    "status": "SUCCESS"
  }'
```

---

## 📊 Fee Summary

After payment, fees are automatically updated:
- ✅ Fee status updated to PARTIAL or PAID
- ✅ Paid amount tracked
- ✅ Reconciliation scheduled
- ✅ Transaction logged for audit

---

## 🔄 Payment Flow

```
1. Student initiates payment
   ↓
2. System checks idempotency (prevents duplicates)
   ↓
3. Transaction created with status PROCESSING
   ↓
4. Payment gateway called
   ↓
5. Gateway returns SUCCESS/FAILED
   ↓
6. If SUCCESS:
   - Fee updated
   - Transaction marked SUCCESS
   - Reconciliation scheduled
   ↓
7. If FAILED:
   - Transaction marked FAILED
   - Scheduled for retry
```

---

## 🛠️ Troubleshooting

### Payment Not Going Through?
1. Check transaction status: `GET /api/student/fees/payment/{txn_id}`
2. Verify idempotency key is unique
3. Check amount doesn't exceed pending balance

### Fee Not Updating After Payment?
1. Wait for webhook callback from gateway
2. Manually trigger: `POST /api/student/fees/admin/reconcile`
3. Check transaction logs

### Duplicate Charges?
✅ **Won't happen!** Idempotency key prevents duplicates
- Same key = Same response (cached)

---

## 📚 Complete Documentation

See detailed guides:
- `FEE_SYSTEM_GUIDE.md` - Complete technical guide (500+ lines)
- `FEE_SYSTEM_IMPLEMENTATION_COMPLETE.md` - Implementation summary

---

## ✅ Production Checklist

- [x] Backend running on port 9091
- [x] Database tables created
- [x] All 8 API endpoints working
- [x] Idempotency implemented
- [x] Error handling with rollback
- [x] Reconciliation job ready
- [x] Retry mechanism active
- [x] Documentation complete

**Status: READY FOR PRODUCTION** 🚀

