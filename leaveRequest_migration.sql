-- Migration script for Leave Request System
-- This script creates the leave_requests table and related structures

-- Create leave_requests table if not exists
CREATE TABLE IF NOT EXISTS leave_requests (
    leave_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    student_id BIGINT NOT NULL,
    from_date DATE NOT NULL,
    to_date DATE NOT NULL,
    leave_type VARCHAR(50),
    reason VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'APPLIED',
    is_half_day BOOLEAN DEFAULT FALSE,
    half_day_period VARCHAR(20),
    approved_on DATETIME,
    approved_by VARCHAR(100),
    rejection_reason VARCHAR(500),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Foreign Key constraint
    CONSTRAINT fk_leave_student FOREIGN KEY (student_id) 
        REFERENCES students(student_id) ON DELETE CASCADE,
    
    -- Indexes for better query performance
    INDEX idx_leave_student_from (student_id, from_date),
    INDEX idx_leave_student_status (student_id, status),
    INDEX idx_leave_from_date (from_date),
    INDEX idx_leave_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Add column if not exists (for schema migrations)
ALTER TABLE leave_requests 
ADD COLUMN IF NOT EXISTS leave_type VARCHAR(50),
ADD COLUMN IF NOT EXISTS reason VARCHAR(500),
ADD COLUMN IF NOT EXISTS is_half_day BOOLEAN DEFAULT FALSE,
ADD COLUMN IF NOT EXISTS half_day_period VARCHAR(20),
ADD COLUMN IF NOT EXISTS approved_on DATETIME,
ADD COLUMN IF NOT EXISTS approved_by VARCHAR(100),
ADD COLUMN IF NOT EXISTS rejection_reason VARCHAR(500);

-- Verify the table structure
SELECT 
    COLUMN_NAME, 
    COLUMN_TYPE, 
    IS_NULLABLE, 
    COLUMN_DEFAULT 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME = 'leave_requests' 
ORDER BY ORDINAL_POSITION;
