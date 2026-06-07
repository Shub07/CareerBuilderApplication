-- Background job tracking for the Student Fees module:
-- async obligation generation (Create Structure) and async report exports.
CREATE TABLE IF NOT EXISTS fee_jobs (
    id                  BIGSERIAL PRIMARY KEY,
    school_id           BIGINT NOT NULL,
    job_type            VARCHAR(40) NOT NULL,           -- OBLIGATION_GENERATION | EXPORT
    status              VARCHAR(20) NOT NULL DEFAULT 'QUEUED', -- QUEUED|RUNNING|DONE|FAILED
    total_items         INT,
    processed_items     INT NOT NULL DEFAULT 0,
    message             VARCHAR(500),
    result_filename     VARCHAR(200),
    result_content_type VARCHAR(120),
    result_data         BYTEA,
    created_by          VARCHAR(100),
    created_at          TIMESTAMP NOT NULL DEFAULT now(),
    updated_at          TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_fee_jobs_school_status ON fee_jobs (school_id, status);
