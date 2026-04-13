-- ============================================================
-- ATTENDANCE MANAGEMENT SYSTEM - DATABASE MIGRATION
-- ============================================================

-- 1. ATTENDANCE STATUS ENUM (Already exists, but ensuring values)
-- Values: PRESENT, ABSENT, LEAVE

-- 2. ENHANCE ATTENDANCE_RECORDS TABLE
ALTER TABLE attendance_records
ADD COLUMN IF NOT EXISTS class_name VARCHAR(50) NOT NULL DEFAULT 'Unknown',
ADD COLUMN IF NOT EXISTS section VARCHAR(10) NOT NULL DEFAULT 'NA',
ADD COLUMN IF NOT EXISTS subject_id BIGINT,
ADD COLUMN IF NOT EXISTS marked_by_id BIGINT,
ADD COLUMN IF NOT EXISTS remarks VARCHAR(500),
ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
ADD CONSTRAINT fk_attendance_subject FOREIGN KEY (subject_id) REFERENCES subjects(id) ON DELETE SET NULL,
ADD CONSTRAINT fk_attendance_marked_by FOREIGN KEY (marked_by_id) REFERENCES app_users(id) ON DELETE SET NULL;

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_attendance_class_date ON attendance_records(class_name, section, att_date);
CREATE INDEX IF NOT EXISTS idx_attendance_subject ON attendance_records(subject_id);
CREATE INDEX IF NOT EXISTS idx_attendance_created_at ON attendance_records(created_at);
CREATE INDEX IF NOT EXISTS idx_attendance_status_date ON attendance_records(status, att_date);

-- 3. CREATE ATTENDANCE_SETTINGS TABLE (for school-wise configurations)
CREATE TABLE IF NOT EXISTS attendance_settings (
    setting_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    school_id BIGINT NOT NULL UNIQUE,
    working_days_per_week INT DEFAULT 5,
    min_attendance_percentage INT DEFAULT 75,
    allow_bulk_marking BOOLEAN DEFAULT TRUE,
    allow_retroactive_marking BOOLEAN DEFAULT TRUE,
    max_retroactive_days INT DEFAULT 7,
    notify_low_attendance BOOLEAN DEFAULT TRUE,
    low_attendance_threshold INT DEFAULT 75,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_settings_school FOREIGN KEY (school_id) REFERENCES schools(school_id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_attendance_settings_school ON attendance_settings(school_id);

-- 4. CREATE ATTENDANCE_BATCH_UPLOAD TABLE (for tracking bulk uploads)
CREATE TABLE IF NOT EXISTS attendance_batch_uploads (
    batch_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    school_id BIGINT NOT NULL,
    class_name VARCHAR(50) NOT NULL,
    section VARCHAR(10) NOT NULL,
    upload_date DATE NOT NULL,
    uploaded_by_id BIGINT NOT NULL,
    total_records INT DEFAULT 0,
    successful_records INT DEFAULT 0,
    failed_records INT DEFAULT 0,
    upload_status VARCHAR(50) DEFAULT 'PENDING',
    remarks VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_batch_school FOREIGN KEY (school_id) REFERENCES schools(school_id) ON DELETE CASCADE,
    CONSTRAINT fk_batch_uploaded_by FOREIGN KEY (uploaded_by_id) REFERENCES app_users(id) ON DELETE RESTRICT,
    UNIQUE KEY uk_batch_upload UNIQUE (school_id, class_name, section, upload_date)
);

CREATE INDEX IF NOT EXISTS idx_batch_school_date ON attendance_batch_uploads(school_id, upload_date);
CREATE INDEX IF NOT EXISTS idx_batch_status ON attendance_batch_uploads(upload_status);

-- 5. CREATE ATTENDANCE_EXCEPTIONS TABLE (for handling special cases)
CREATE TABLE IF NOT EXISTS attendance_exceptions (
    exception_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    student_id BIGINT NOT NULL,
    exception_date DATE NOT NULL,
    exception_type VARCHAR(50) NOT NULL,
    status VARCHAR(50) DEFAULT 'PENDING',
    requested_by_id BIGINT NOT NULL,
    approved_by_id BIGINT,
    reason VARCHAR(500) NOT NULL,
    attachment_url VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    approved_at TIMESTAMP,
    CONSTRAINT fk_exception_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    CONSTRAINT fk_exception_requested_by FOREIGN KEY (requested_by_id) REFERENCES app_users(id) ON DELETE RESTRICT,
    CONSTRAINT fk_exception_approved_by FOREIGN KEY (approved_by_id) REFERENCES app_users(id) ON DELETE SET NULL,
    UNIQUE KEY uk_exception_date UNIQUE (student_id, exception_date)
);

CREATE INDEX IF NOT EXISTS idx_exception_student ON attendance_exceptions(student_id);
CREATE INDEX IF NOT EXISTS idx_exception_status ON attendance_exceptions(status);
CREATE INDEX IF NOT EXISTS idx_exception_date ON attendance_exceptions(exception_date);

-- 6. CREATE ATTENDANCE_NOTIFICATIONS TABLE (for tracking notifications sent)
CREATE TABLE IF NOT EXISTS attendance_notifications (
    notification_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    student_id BIGINT NOT NULL,
    parent_id BIGINT,
    notification_type VARCHAR(50) NOT NULL,
    message VARCHAR(500) NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    read_at TIMESTAMP,
    CONSTRAINT fk_notif_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    CONSTRAINT fk_notif_parent FOREIGN KEY (parent_id) REFERENCES parents(id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_notification_student ON attendance_notifications(student_id);
CREATE INDEX IF NOT EXISTS idx_notification_unread ON attendance_notifications(is_read, created_at);

-- 7. CREATE ATTENDANCE_REPORT_CACHE TABLE (for performance optimization)
CREATE TABLE IF NOT EXISTS attendance_report_cache (
    cache_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    student_id BIGINT NOT NULL,
    report_period VARCHAR(20) NOT NULL,
    attendance_percentage INT,
    total_days_expected INT,
    total_days_present INT,
    total_days_absent INT,
    total_days_leave INT,
    cached_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP,
    CONSTRAINT fk_cache_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    UNIQUE KEY uk_cache_period UNIQUE (student_id, report_period, cached_at)
);

CREATE INDEX IF NOT EXISTS idx_cache_expires_at ON attendance_report_cache(expires_at);

-- 8. CREATE ATTENDANCE_AUDIT_LOG TABLE (for compliance and tracking)
CREATE TABLE IF NOT EXISTS attendance_audit_log (
    audit_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    attendance_id BIGINT,
    action VARCHAR(50) NOT NULL,
    old_status VARCHAR(20),
    new_status VARCHAR(20),
    changed_by_id BIGINT,
    change_reason VARCHAR(500),
    changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_audit_attendance FOREIGN KEY (attendance_id) REFERENCES attendance_records(attendance_id) ON DELETE SET NULL,
    CONSTRAINT fk_audit_changed_by FOREIGN KEY (changed_by_id) REFERENCES app_users(id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_audit_attendance ON attendance_audit_log(attendance_id);
CREATE INDEX IF NOT EXISTS idx_audit_changed_at ON attendance_audit_log(changed_at);
CREATE INDEX IF NOT EXISTS idx_audit_action ON attendance_audit_log(action);

-- ============================================================
-- INSERT DEFAULT ATTENDANCE SETTINGS FOR EXISTING SCHOOLS
-- ============================================================

INSERT IGNORE INTO attendance_settings (school_id, working_days_per_week, min_attendance_percentage)
SELECT school_id, 5, 75 FROM schools;

-- ============================================================
-- FINAL VERIFICATION
-- ============================================================
SELECT 'Attendance migration completed successfully' as status;

