CREATE TABLE IF NOT EXISTS admin_student_table_preferences (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    school_id BIGINT NOT NULL,
    admin_user_id BIGINT NULL,
    admin_email VARCHAR(150) NULL,
    preference_key VARCHAR(50) NOT NULL DEFAULT 'students_table',
    visible_columns TEXT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL,
    CONSTRAINT fk_astp_school FOREIGN KEY (school_id) REFERENCES schools(id) ON DELETE CASCADE
);

CREATE INDEX idx_astp_school_user ON admin_student_table_preferences (school_id, admin_user_id, preference_key);
CREATE INDEX idx_astp_school_email ON admin_student_table_preferences (school_id, admin_email, preference_key);
