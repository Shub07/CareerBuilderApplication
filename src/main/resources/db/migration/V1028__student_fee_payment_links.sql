CREATE TABLE IF NOT EXISTS student_fee_payment_links (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    school_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    token VARCHAR(64) NOT NULL UNIQUE,
    fee_ids VARCHAR(500) NOT NULL,
    amount DECIMAL(12, 2) NOT NULL,
    expires_at DATETIME NOT NULL,
    created_by VARCHAR(100),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_sfpl_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE
);

CREATE INDEX idx_sfpl_token ON student_fee_payment_links(token);
