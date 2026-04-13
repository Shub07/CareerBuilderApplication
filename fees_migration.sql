-- ============================================================================
-- FEE MANAGEMENT SYSTEM - DATABASE MIGRATION SCRIPT
-- ============================================================================

-- ============================================================================
-- 1. CREATE FEES TABLE
-- ============================================================================
CREATE TABLE IF NOT EXISTS fees (
    fee_id SERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL,
    fee_type VARCHAR(50) NOT NULL,
    amount DECIMAL(10, 2) NOT NULL CHECK (amount > 0),
    due_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    paid_amount DECIMAL(10, 2) DEFAULT 0.00 CHECK (paid_amount >= 0),
    academic_year VARCHAR(20),
    term VARCHAR(20),
    description VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    due_reminder_sent BOOLEAN DEFAULT FALSE,
    overdue_reminder_sent BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    CONSTRAINT fk_fee_status CHECK (status IN ('PENDING', 'PARTIAL', 'PAID', 'OVERDUE', 'CANCELLED', 'REFUNDED'))
);

-- Create indexes for fees table
CREATE INDEX idx_fee_student ON fees(student_id);
CREATE INDEX idx_fee_status ON fees(status);
CREATE INDEX idx_fee_due_date ON fees(due_date);
CREATE INDEX idx_fee_academic_year ON fees(academic_year);

-- ============================================================================
-- 2. CREATE PAYMENT TRANSACTIONS TABLE
-- ============================================================================
CREATE TABLE IF NOT EXISTS payment_transactions (
    transaction_id SERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL,
    fee_id BIGINT NOT NULL,
    amount DECIMAL(10, 2) NOT NULL CHECK (amount > 0),
    payment_method VARCHAR(50) NOT NULL,
    transaction_status VARCHAR(50) NOT NULL DEFAULT 'INITIATED',
    transaction_reference_id VARCHAR(100) NOT NULL UNIQUE,
    idempotency_key VARCHAR(100) NOT NULL UNIQUE,
    merchant_reference_id VARCHAR(100),
    gateway_response TEXT,
    error_message VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP,
    reconciliation_at TIMESTAMP,
    is_reconciled BOOLEAN DEFAULT FALSE,
    retry_count INTEGER DEFAULT 0,
    last_retry_at TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    FOREIGN KEY (fee_id) REFERENCES fees(fee_id) ON DELETE RESTRICT,
    CONSTRAINT fk_payment_method CHECK (payment_method IN ('CREDIT_CARD', 'DEBIT_CARD', 'UPI', 'NET_BANKING', 'WALLET', 'CASH', 'CHEQUE')),
    CONSTRAINT fk_transaction_status CHECK (transaction_status IN ('INITIATED', 'PROCESSING', 'SUCCESS', 'FAILED', 'REFUNDED', 'CANCELLED', 'PENDING_RECONCILIATION'))
);

-- Create indexes for payment transactions table
CREATE INDEX idx_transaction_student ON payment_transactions(student_id);
CREATE INDEX idx_transaction_fee ON payment_transactions(fee_id);
CREATE INDEX idx_transaction_status ON payment_transactions(transaction_status);
CREATE INDEX idx_transaction_reference ON payment_transactions(transaction_reference_id);
CREATE INDEX idx_transaction_idempotency ON payment_transactions(idempotency_key);
CREATE INDEX idx_transaction_created_at ON payment_transactions(created_at);

-- ============================================================================
-- 3. INSERT SAMPLE FEE DATA
-- ============================================================================
-- Sample fees for student ID 1 (Academic Year 2025-2026)
INSERT INTO fees (student_id, fee_type, amount, due_date, status, paid_amount, academic_year, term, description)
VALUES
    (1, 'TUITION', 50000.00, '2026-04-30', 'PAID', 50000.00, '2025-2026', 'Term 1', 'Tuition Fee - Term 1'),
    (1, 'EXAMINATION', 5000.00, '2026-05-31', 'PARTIAL', 2500.00, '2025-2026', 'Term 1', 'Examination Fee'),
    (1, 'LIBRARY', 2000.00, '2026-06-30', 'PENDING', 0.00, '2025-2026', 'Term 1', 'Library Fee'),
    (1, 'SPORTS', 3000.00, '2026-07-31', 'PENDING', 0.00, '2025-2026', 'Term 1', 'Sports Activity Fee'),
    (1, 'HOSTEL', 30000.00, '2026-04-15', 'PENDING', 0.00, '2025-2026', 'Term 1', 'Hostel Accommodation'),
    (1, 'TUITION', 50000.00, '2026-09-30', 'PENDING', 0.00, '2025-2026', 'Term 2', 'Tuition Fee - Term 2');

-- ============================================================================
-- 4. CREATE VIEWS FOR REPORTING
-- ============================================================================

-- View: Student Fee Summary
CREATE OR REPLACE VIEW v_student_fee_summary AS
SELECT
    f.student_id,
    s.first_name,
    s.last_name,
    s.email,
    COALESCE(SUM(f.amount), 0) as total_fees,
    COALESCE(SUM(f.paid_amount), 0) as total_paid,
    COALESCE(SUM(f.amount) - SUM(f.paid_amount), 0) as total_pending,
    ROUND(COALESCE((SUM(f.paid_amount) / SUM(f.amount)) * 100, 0), 2) as percentage_paid,
    COUNT(CASE WHEN f.status = 'PAID' THEN 1 END) as paid_fees_count,
    COUNT(CASE WHEN f.status IN ('PENDING', 'PARTIAL') THEN 1 END) as pending_fees_count,
    COUNT(CASE WHEN f.status = 'OVERDUE' THEN 1 END) as overdue_fees_count
FROM fees f
JOIN students s ON f.student_id = s.id
GROUP BY f.student_id, s.id, s.first_name, s.last_name, s.email;

-- View: Transaction Reconciliation Status
CREATE OR REPLACE VIEW v_transaction_reconciliation_status AS
SELECT
    pt.student_id,
    COUNT(*) as total_transactions,
    COUNT(CASE WHEN pt.transaction_status = 'SUCCESS' THEN 1 END) as successful_transactions,
    COUNT(CASE WHEN pt.transaction_status = 'FAILED' THEN 1 END) as failed_transactions,
    COUNT(CASE WHEN pt.is_reconciled = FALSE AND pt.transaction_status = 'SUCCESS' THEN 1 END) as unreconciled_transactions,
    COALESCE(SUM(CASE WHEN pt.transaction_status = 'SUCCESS' THEN pt.amount ELSE 0 END), 0) as total_successful_amount,
    COALESCE(SUM(CASE WHEN pt.transaction_status = 'FAILED' THEN pt.amount ELSE 0 END), 0) as total_failed_amount,
    MAX(pt.updated_at) as last_transaction_date
FROM payment_transactions pt
GROUP BY pt.student_id;

-- ============================================================================
-- 5. CREATE STORED PROCEDURES
-- ============================================================================

-- Procedure: Mark Overdue Fees
CREATE OR REPLACE FUNCTION update_overdue_fees()
RETURNS void AS $$
BEGIN
    UPDATE fees
    SET status = 'OVERDUE'
    WHERE status IN ('PENDING', 'PARTIAL')
    AND due_date < CURRENT_DATE
    AND paid_amount < amount;

    RAISE NOTICE 'Updated overdue fees';
END;
$$ LANGUAGE plpgsql;

-- Procedure: Calculate Student Fee Summary
CREATE OR REPLACE FUNCTION get_student_fee_summary(p_student_id BIGINT)
RETURNS TABLE(
    total_fees DECIMAL,
    total_paid DECIMAL,
    total_pending DECIMAL,
    percentage_paid NUMERIC,
    paid_count INTEGER,
    pending_count INTEGER,
    overdue_count INTEGER
) AS $$
BEGIN
    RETURN QUERY
    SELECT
        COALESCE(SUM(f.amount), 0)::DECIMAL,
        COALESCE(SUM(f.paid_amount), 0)::DECIMAL,
        COALESCE(SUM(f.amount - f.paid_amount), 0)::DECIMAL,
        ROUND(COALESCE((SUM(f.paid_amount) / SUM(f.amount)) * 100, 0), 2)::NUMERIC,
        COUNT(CASE WHEN f.status = 'PAID' THEN 1 END)::INTEGER,
        COUNT(CASE WHEN f.status IN ('PENDING', 'PARTIAL') THEN 1 END)::INTEGER,
        COUNT(CASE WHEN f.status = 'OVERDUE' THEN 1 END)::INTEGER
    FROM fees f
    WHERE f.student_id = p_student_id;
END;
$$ LANGUAGE plpgsql;

-- ============================================================================
-- 6. ADD GRANT STATEMENTS
-- ============================================================================
GRANT SELECT, INSERT, UPDATE ON fees TO postgres;
GRANT SELECT, INSERT, UPDATE ON payment_transactions TO postgres;
GRANT SELECT ON v_student_fee_summary TO postgres;
GRANT SELECT ON v_transaction_reconciliation_status TO postgres;

-- ============================================================================
-- MIGRATION COMPLETE
-- ============================================================================
SELECT 'Fee Management System - Database Migration Complete!' as status;

