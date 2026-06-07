ALTER TABLE certificates
    ADD COLUMN IF NOT EXISTS file_name VARCHAR(255) NULL,
    ADD COLUMN IF NOT EXISTS file_size_bytes BIGINT NULL,
    ADD COLUMN IF NOT EXISTS uploaded_by VARCHAR(100) NULL;

CREATE INDEX IF NOT EXISTS idx_cert_category ON certificates(category);
