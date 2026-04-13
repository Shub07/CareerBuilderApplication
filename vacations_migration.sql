-- ============================================================================
-- VACATION MANAGEMENT SYSTEM - DATABASE MIGRATION
-- ============================================================================

-- ============================================================================
-- 0. ENSURE SCHOOLS EXIST (Required for Foreign Key)
-- ============================================================================
-- Insert schools if they don't already exist
-- This ensures the foreign key constraint in vacations table can be satisfied

INSERT INTO schools (id, school_name, school_code, address, city, state, country, phone, email, principal_name, established_year)
VALUES
    (1, 'Delhi Public School', 'DPS001', '123 School Lane, Delhi', 'Delhi', 'Delhi', 'India', '9876543200', 'principal@dps.edu.in', 'Dr. Sharma', 2010),
    (2, 'Mumbai Academy', 'MA001', '456 Education Ave, Mumbai', 'Mumbai', 'Maharashtra', 'India', '9876543201', 'principal@mumbaiaca.edu.in', 'Mrs. Patel', 2015)
ON CONFLICT (id) DO NOTHING;

-- ============================================================================
-- 1. CREATE VACATIONS TABLE
-- ============================================================================
CREATE TABLE IF NOT EXISTS vacations (
    vacation_id SERIAL PRIMARY KEY,
    school_id BIGINT NOT NULL,
    vacation_name VARCHAR(100) NOT NULL,
    vacation_type VARCHAR(50) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    description VARCHAR(500),
    is_active BOOLEAN DEFAULT TRUE,
    notice_sent BOOLEAN DEFAULT FALSE,
    notice_sent_date TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (school_id) REFERENCES schools(id) ON DELETE CASCADE,
    CONSTRAINT fk_vacation_type CHECK (vacation_type IN ('HOLIDAY', 'EXAM_BREAK', 'SUMMER', 'WINTER', 'SPRING', 'AUTUMN', 'MONSOON', 'SPECIAL', 'EMERGENCY'))
);

-- Create indexes for performance
CREATE INDEX idx_vacation_start_date ON vacations(start_date);
CREATE INDEX idx_vacation_end_date ON vacations(end_date);
CREATE INDEX idx_vacation_school_id ON vacations(school_id);
CREATE INDEX idx_vacation_type ON vacations(vacation_type);
CREATE INDEX idx_vacation_active ON vacations(is_active);

-- ============================================================================
-- 2. INSERT SAMPLE VACATION DATA
-- ============================================================================

-- Spring Vacation 2026
-- Using ON CONFLICT DO NOTHING to prevent duplicates if data already exists
-- Data for School 1 (Delhi Public School)
INSERT INTO vacations (school_id, vacation_name, vacation_type, start_date, end_date, description, is_active, created_by)
VALUES
    (1, 'Spring Vacation 2026', 'SPRING', '2026-04-01', '2026-04-14', 'Spring break for all students', TRUE, 'ADMIN'),
    (1, 'Summer Vacation 2026', 'SUMMER', '2026-05-15', '2026-07-15', 'Extended summer vacation - 2 months', TRUE, 'ADMIN'),
    (1, 'Independence Day', 'HOLIDAY', '2026-08-15', '2026-08-15', 'National holiday', TRUE, 'ADMIN'),
    (1, 'Autumn Vacation', 'AUTUMN', '2026-09-01', '2026-09-07', 'Autumn break', TRUE, 'ADMIN'),
    (1, 'Winter Vacation', 'WINTER', '2026-12-20', '2027-01-10', 'Christmas and New Year vacation', TRUE, 'ADMIN'),
    (1, 'Exam Break - Term 1', 'EXAM_BREAK', '2026-03-01', '2026-03-31', 'No classes - Students focus on exams', FALSE, 'ADMIN')
ON CONFLICT DO NOTHING;

-- Data for School 2 (Mumbai Academy)
INSERT INTO vacations (school_id, vacation_name, vacation_type, start_date, end_date, description, is_active, created_by)
VALUES
    (2, 'Spring Vacation 2026 S2', 'SPRING', '2026-03-15', '2026-03-28', 'Spring break for all students', TRUE, 'ADMIN'),
    (2, 'Summer Vacation 2026 S2', 'SUMMER', '2026-06-01', '2026-07-31', 'Extended summer vacation', TRUE, 'ADMIN'),
    (2, 'Monsoon Break', 'MONSOON', '2026-07-15', '2026-08-31', 'Monsoon season break', TRUE, 'ADMIN'),
    (2, 'Autumn Vacation S2', 'AUTUMN', '2026-10-01', '2026-10-07', 'Autumn break', TRUE, 'ADMIN'),
    (2, 'Winter Vacation S2', 'WINTER', '2026-12-15', '2027-01-05', 'Christmas and New Year vacation', TRUE, 'ADMIN')
ON CONFLICT DO NOTHING;

-- ============================================================================
-- 3. CREATE VIEWS FOR REPORTING
-- ============================================================================

-- View: Current/Ongoing Vacations
CREATE OR REPLACE VIEW v_ongoing_vacations AS
SELECT
    v.vacation_id,
    v.school_id,
    v.vacation_name,
    v.vacation_type,
    v.start_date,
    v.end_date,
    EXTRACT(DAY FROM v.end_date - CURRENT_DATE) as days_remaining,
    s.school_name
FROM vacations v
JOIN schools s ON v.school_id = s.id
WHERE CURRENT_DATE BETWEEN v.start_date AND v.end_date
AND v.is_active = TRUE;

-- View: Upcoming Vacations
CREATE OR REPLACE VIEW v_upcoming_vacations AS
SELECT
    v.vacation_id,
    v.school_id,
    v.vacation_name,
    v.vacation_type,
    v.start_date,
    v.end_date,
    EXTRACT(DAY FROM v.start_date - CURRENT_DATE) as days_until_start,
    s.school_name
FROM vacations v
JOIN schools s ON v.school_id = s.id
WHERE v.start_date > CURRENT_DATE
AND v.is_active = TRUE
ORDER BY v.start_date ASC;

-- View: Vacation Schedule Summary
CREATE OR REPLACE VIEW v_vacation_schedule_summary AS
SELECT
    s.id as school_id,
    s.school_name,
    COUNT(CASE WHEN CURRENT_DATE BETWEEN v.start_date AND v.end_date AND v.is_active = TRUE THEN 1 END) as ongoing_count,
    COUNT(CASE WHEN v.start_date > CURRENT_DATE AND v.is_active = TRUE THEN 1 END) as upcoming_count,
    COUNT(CASE WHEN v.end_date < CURRENT_DATE THEN 1 END) as completed_count,
    MAX(CASE WHEN CURRENT_DATE BETWEEN v.start_date AND v.end_date THEN v.vacation_name END) as current_vacation
FROM schools s
LEFT JOIN vacations v ON s.id = v.school_id
GROUP BY s.id, s.school_name;

-- ============================================================================
-- 4. GRANT PERMISSIONS
-- ============================================================================
GRANT SELECT, INSERT, UPDATE, DELETE ON vacations TO postgres;
GRANT SELECT ON v_ongoing_vacations TO postgres;
GRANT SELECT ON v_upcoming_vacations TO postgres;
GRANT SELECT ON v_vacation_schedule_summary TO postgres;

-- ============================================================================
-- VACATION MIGRATION COMPLETE
-- ============================================================================
SELECT 'Vacation Management System - Database Migration Complete!' as status;

