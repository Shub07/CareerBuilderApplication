-- ============================================================
-- ATTENDANCE MANAGEMENT SYSTEM - PostgreSQL MIGRATION
-- ============================================================

-- Enable UUID extension if needed
-- CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 1. ENHANCE ATTENDANCE_RECORDS TABLE (if not already done)
-- This table should already exist, but we'll add missing columns

DO $$
BEGIN
    -- Add columns if they don't exist
    IF NOT EXISTS(SELECT 1 FROM information_schema.columns
                  WHERE table_name='attendance_records' AND column_name='class_name') THEN
        ALTER TABLE attendance_records ADD COLUMN class_name VARCHAR(50);
    END IF;

    IF NOT EXISTS(SELECT 1 FROM information_schema.columns
                  WHERE table_name='attendance_records' AND column_name='section') THEN
        ALTER TABLE attendance_records ADD COLUMN section VARCHAR(10);
    END IF;

    IF NOT EXISTS(SELECT 1 FROM information_schema.columns
                  WHERE table_name='attendance_records' AND column_name='subject_id') THEN
        ALTER TABLE attendance_records ADD COLUMN subject_id BIGINT;
    END IF;

    IF NOT EXISTS(SELECT 1 FROM information_schema.columns
                  WHERE table_name='attendance_records' AND column_name='marked_by_id') THEN
        ALTER TABLE attendance_records ADD COLUMN marked_by_id BIGINT;
    END IF;

    IF NOT EXISTS(SELECT 1 FROM information_schema.columns
                  WHERE table_name='attendance_records' AND column_name='remarks') THEN
        ALTER TABLE attendance_records ADD COLUMN remarks VARCHAR(500);
    END IF;

    IF NOT EXISTS(SELECT 1 FROM information_schema.columns
                  WHERE table_name='attendance_records' AND column_name='created_at') THEN
        ALTER TABLE attendance_records ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
    END IF;

    IF NOT EXISTS(SELECT 1 FROM information_schema.columns
                  WHERE table_name='attendance_records' AND column_name='updated_at') THEN
        ALTER TABLE attendance_records ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
    END IF;
END$$;

-- 2. CREATE ATTENDANCE_SETTINGS TABLE
CREATE TABLE IF NOT EXISTS attendance_settings (
    setting_id BIGSERIAL PRIMARY KEY,
    school_id BIGINT NOT NULL UNIQUE,
    working_days_per_week INTEGER DEFAULT 5,
    min_attendance_percentage INTEGER DEFAULT 75,
    allow_bulk_marking BOOLEAN DEFAULT TRUE,
    allow_retroactive_marking BOOLEAN DEFAULT TRUE,
    max_retroactive_days INTEGER DEFAULT 7,
    notify_low_attendance BOOLEAN DEFAULT TRUE,
    low_attendance_threshold INTEGER DEFAULT 75,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_attendance_settings_school ON attendance_settings(school_id);

-- 3. CREATE ATTENDANCE_BATCH_UPLOADS TABLE
CREATE TABLE IF NOT EXISTS attendance_batch_uploads (
    batch_id BIGSERIAL PRIMARY KEY,
    school_id BIGINT NOT NULL,
    class_name VARCHAR(50) NOT NULL,
    section VARCHAR(10) NOT NULL,
    upload_date DATE NOT NULL,
    uploaded_by_id BIGINT NOT NULL,
    total_records INTEGER DEFAULT 0,
    successful_records INTEGER DEFAULT 0,
    failed_records INTEGER DEFAULT 0,
    upload_status VARCHAR(50) DEFAULT 'PENDING',
    remarks VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (school_id, class_name, section, upload_date)
);

CREATE INDEX IF NOT EXISTS idx_batch_school_date ON attendance_batch_uploads(school_id, upload_date);
CREATE INDEX IF NOT EXISTS idx_batch_status ON attendance_batch_uploads(upload_status);

-- 4. CREATE ATTENDANCE_EXCEPTIONS TABLE
CREATE TABLE IF NOT EXISTS attendance_exceptions (
    exception_id BIGSERIAL PRIMARY KEY,
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
    UNIQUE (student_id, exception_date)
);

CREATE INDEX IF NOT EXISTS idx_exception_student ON attendance_exceptions(student_id);
CREATE INDEX IF NOT EXISTS idx_exception_status ON attendance_exceptions(status);
CREATE INDEX IF NOT EXISTS idx_exception_date ON attendance_exceptions(exception_date);

-- 5. CREATE ATTENDANCE_NOTIFICATIONS TABLE
CREATE TABLE IF NOT EXISTS attendance_notifications (
    notification_id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL,
    parent_id BIGINT,
    notification_type VARCHAR(50) NOT NULL,
    message VARCHAR(500) NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    read_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_notification_student ON attendance_notifications(student_id);
CREATE INDEX IF NOT EXISTS idx_notification_unread ON attendance_notifications(is_read, created_at);

-- 6. CREATE ATTENDANCE_AUDIT_LOG TABLE
CREATE TABLE IF NOT EXISTS attendance_audit_log (
    audit_id BIGSERIAL PRIMARY KEY,
    attendance_id BIGINT,
    action VARCHAR(50) NOT NULL,
    old_status VARCHAR(20),
    new_status VARCHAR(20),
    changed_by_id BIGINT,
    change_reason VARCHAR(500),
    changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_audit_attendance ON attendance_audit_log(attendance_id);
CREATE INDEX IF NOT EXISTS idx_audit_changed_at ON attendance_audit_log(changed_at);
CREATE INDEX IF NOT EXISTS idx_audit_action ON attendance_audit_log(action);

-- 7. CREATE ATTENDANCE_REPORT_CACHE TABLE
CREATE TABLE IF NOT EXISTS attendance_report_cache (
    cache_id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL,
    report_period VARCHAR(20) NOT NULL,
    attendance_percentage INTEGER,
    total_days_expected INTEGER,
    total_days_present INTEGER,
    total_days_absent INTEGER,
    total_days_leave INTEGER,
    cached_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP,
    UNIQUE (student_id, report_period, cached_at)
);

CREATE INDEX IF NOT EXISTS idx_cache_expires_at ON attendance_report_cache(expires_at);

-- 8. Add foreign key constraints to attendance_settings if schools table exists
DO $$
BEGIN
    IF EXISTS(SELECT 1 FROM information_schema.tables WHERE table_name='schools') THEN
        IF NOT EXISTS(SELECT 1 FROM information_schema.table_constraints
                      WHERE table_name='attendance_settings' AND constraint_name='fk_settings_school') THEN
            -- Use the actual primary key column name from schools table (usually 'id' not 'school_id')
            ALTER TABLE attendance_settings
            ADD CONSTRAINT fk_settings_school FOREIGN KEY (school_id)
            REFERENCES schools(id) ON DELETE CASCADE;
        END IF;
    END IF;
END$$;

-- 9. Add foreign keys to other tables if they don't exist
DO $$
BEGIN
    -- Add FK for attendance_batch_uploads
    IF EXISTS(SELECT 1 FROM information_schema.tables WHERE table_name='schools') THEN
        IF NOT EXISTS(SELECT 1 FROM information_schema.table_constraints
                      WHERE table_name='attendance_batch_uploads' AND constraint_name='fk_batch_school') THEN
            ALTER TABLE attendance_batch_uploads
            ADD CONSTRAINT fk_batch_school FOREIGN KEY (school_id)
            REFERENCES schools(id) ON DELETE CASCADE;
        END IF;
    END IF;

    -- Add FK for attendance_exceptions
    IF EXISTS(SELECT 1 FROM information_schema.tables WHERE table_name='students') THEN
        IF NOT EXISTS(SELECT 1 FROM information_schema.table_constraints
                      WHERE table_name='attendance_exceptions' AND constraint_name='fk_exception_student') THEN
            ALTER TABLE attendance_exceptions
            ADD CONSTRAINT fk_exception_student FOREIGN KEY (student_id)
            REFERENCES students(id) ON DELETE CASCADE;
        END IF;
    END IF;

    -- Add FK for attendance_notifications
    IF EXISTS(SELECT 1 FROM information_schema.tables WHERE table_name='students') THEN
        IF NOT EXISTS(SELECT 1 FROM information_schema.table_constraints
                      WHERE table_name='attendance_notifications' AND constraint_name='fk_notif_student') THEN
            ALTER TABLE attendance_notifications
            ADD CONSTRAINT fk_notif_student FOREIGN KEY (student_id)
            REFERENCES students(id) ON DELETE CASCADE;
        END IF;
    END IF;

    -- Add FK for attendance_report_cache
    IF EXISTS(SELECT 1 FROM information_schema.tables WHERE table_name='students') THEN
        IF NOT EXISTS(SELECT 1 FROM information_schema.table_constraints
                      WHERE table_name='attendance_report_cache' AND constraint_name='fk_cache_student') THEN
            ALTER TABLE attendance_report_cache
            ADD CONSTRAINT fk_cache_student FOREIGN KEY (student_id)
            REFERENCES students(id) ON DELETE CASCADE;
        END IF;
    END IF;
END$$;

-- ============================================================
-- VERIFICATION
-- ============================================================
SELECT 'Attendance migration completed successfully' as status;

-- Show created tables
SELECT table_name FROM information_schema.tables
WHERE table_schema = 'public' AND table_name LIKE 'attendance_%'
ORDER BY table_name;

