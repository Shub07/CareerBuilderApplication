ALTER TABLE student_profiles
    ADD COLUMN IF NOT EXISTS transport_route VARCHAR(200) NULL,
    ADD COLUMN IF NOT EXISTS nationality VARCHAR(50) NULL DEFAULT 'Indian',
    ADD COLUMN IF NOT EXISTS flagged BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS flag_reason VARCHAR(1000) NULL,
    ADD COLUMN IF NOT EXISTS flagged_at DATETIME NULL,
    ADD COLUMN IF NOT EXISTS flagged_by VARCHAR(100) NULL;

ALTER TABLE student_documents
    ADD COLUMN IF NOT EXISTS uploaded_by VARCHAR(100) NULL;

CREATE TABLE IF NOT EXISTS admin_student_messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    school_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    message_body TEXT NOT NULL,
    sent_by VARCHAR(100),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_asm_school FOREIGN KEY (school_id) REFERENCES schools(id),
    CONSTRAINT fk_asm_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE
);

CREATE INDEX idx_asm_student_created ON admin_student_messages(student_id, created_at DESC);
