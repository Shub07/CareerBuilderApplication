-- Critical correctness fixes for the Student Fees module.

-- Point 1: optimistic-lock version on fee obligations
ALTER TABLE fees ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;

-- Point 2: atomic, race-free receipt numbering (per calendar year)
CREATE TABLE IF NOT EXISTS receipt_counters (
    year       INT PRIMARY KEY,
    last_value BIGINT NOT NULL DEFAULT 0
);

-- Point 3: idempotency for fee collection
ALTER TABLE fee_transactions ADD COLUMN IF NOT EXISTS idempotency_key VARCHAR(80);
CREATE UNIQUE INDEX IF NOT EXISTS ux_fee_txn_idem
    ON fee_transactions (school_id, idempotency_key)
    WHERE idempotency_key IS NOT NULL;
