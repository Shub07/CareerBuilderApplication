-- ─────────────────────────────────────────────────────────────────────────────
-- Admin Student Fees module
-- Fee structure templates + collect/refund ledger.
-- (Per-student obligations continue to live in the existing `fees` table.)
-- ─────────────────────────────────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS fee_structures (
    id                BIGSERIAL PRIMARY KEY,
    school_id         BIGINT        NOT NULL REFERENCES schools (id),
    fee_type          VARCHAR(60)   NOT NULL,
    fee_name          VARCHAR(120),
    target_class_name VARCHAR(50)   NOT NULL,
    target_section    VARCHAR(10),
    academic_year     VARCHAR(20),
    financial_year    VARCHAR(20),
    amount            NUMERIC(12,2) NOT NULL,
    frequency         VARCHAR(20)   NOT NULL DEFAULT 'YEARLY',
    due_date          DATE,
    due_day_of_month  INT,
    late_fee_enabled  BOOLEAN       NOT NULL DEFAULT FALSE,
    notes             VARCHAR(1000),
    active            BOOLEAN       NOT NULL DEFAULT TRUE,
    deleted           BOOLEAN       NOT NULL DEFAULT FALSE,
    created_by        VARCHAR(100),
    created_at        TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_fee_structure_school  ON fee_structures (school_id);
CREATE INDEX IF NOT EXISTS idx_fee_structure_class   ON fee_structures (target_class_name);
CREATE INDEX IF NOT EXISTS idx_fee_structure_year    ON fee_structures (academic_year);
CREATE INDEX IF NOT EXISTS idx_fee_structure_deleted ON fee_structures (deleted);

CREATE TABLE IF NOT EXISTS fee_transactions (
    id               BIGSERIAL PRIMARY KEY,
    school_id        BIGINT        NOT NULL REFERENCES schools (id),
    student_id       BIGINT        NOT NULL REFERENCES students (id),
    fee_id           BIGINT        REFERENCES fees (id),
    fee_type         VARCHAR(60)   NOT NULL,
    type             VARCHAR(20)   NOT NULL,
    amount           NUMERIC(12,2) NOT NULL,
    mode             VARCHAR(20)   NOT NULL,
    transaction_date DATE          NOT NULL,
    reference_number VARCHAR(100),
    refund_reason    VARCHAR(200),
    notes            VARCHAR(1000),
    receipt_number   VARCHAR(60)   NOT NULL UNIQUE,
    collected_by     VARCHAR(100),
    created_at       TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_fee_txn_school  ON fee_transactions (school_id);
CREATE INDEX IF NOT EXISTS idx_fee_txn_student ON fee_transactions (student_id);
CREATE INDEX IF NOT EXISTS idx_fee_txn_date    ON fee_transactions (transaction_date);
CREATE INDEX IF NOT EXISTS idx_fee_txn_type    ON fee_transactions (type);
