-- ============================================================================
-- 📢 NOTICES MODULE - DATABASE MIGRATION
-- Adds enhanced notice management functionality
-- ============================================================================

-- Create or update notices table with new columns
ALTER TABLE notices ADD COLUMN IF NOT EXISTS description VARCHAR(500);
ALTER TABLE notices ADD COLUMN IF NOT EXISTS category VARCHAR(50) DEFAULT 'OTHER';
ALTER TABLE notices ADD COLUMN IF NOT EXISTS source VARCHAR(150);
ALTER TABLE notices ADD COLUMN IF NOT EXISTS is_pinned BOOLEAN DEFAULT false;
ALTER TABLE notices ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP;

-- Add indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_notice_category ON notices(category);
CREATE INDEX IF NOT EXISTS idx_notice_pinned ON notices(is_pinned);
CREATE INDEX IF NOT EXISTS idx_notice_school_created ON notices(school_id, created_at);
CREATE INDEX IF NOT EXISTS idx_notice_search ON notices(school_id, title, body);

-- Ensure student_notice_reads table exists with proper structure
CREATE TABLE IF NOT EXISTS student_notice_reads (
    read_id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL,
    notice_id BIGINT NOT NULL,
    read_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(student_id, notice_id),
    FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE,
    FOREIGN KEY (notice_id) REFERENCES notices(notice_id) ON DELETE CASCADE
);

-- Create index on student_notice_reads for faster queries
CREATE INDEX IF NOT EXISTS idx_student_notice_read_student ON student_notice_reads(student_id);
CREATE INDEX IF NOT EXISTS idx_student_notice_read_notice ON student_notice_reads(notice_id);

-- ============================================================================
-- Sample data for testing (optional - comment out if not needed)
-- ============================================================================

-- Insert sample notices if table is empty
INSERT INTO notices (school_id, title, description, body, category, source, is_pinned, created_at, updated_at)
SELECT 
    1, 
    'Annual Examination Schedule 2025', 
    'The annual examinations for all classes will commence from December 15, 2025.',
    'The annual examinations for all classes will commence from December 15, 2025. Students are advised to check their individual timetables on the student portal.',
    'EXAMS',
    'Examination Department',
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM notices WHERE title = 'Annual Examination Schedule 2025' AND school_id = 1)
ON CONFLICT DO NOTHING;

INSERT INTO notices (school_id, title, description, body, category, source, is_pinned, created_at, updated_at)
SELECT 
    1,
    'Winter Break Holiday Notice',
    'School will be closed for winter break from December 20, 2025 to January 5, 2026.',
    'School will be closed for winter break from December 20, 2025 to January 5, 2026. Classes will resume on January 6, 2026. We wish all students and staff a wonderful holiday season.',
    'HOLIDAYS',
    'Administration Office',
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM notices WHERE title = 'Winter Break Holiday Notice' AND school_id = 1)
ON CONFLICT DO NOTHING;

INSERT INTO notices (school_id, title, description, body, category, source, is_pinned, created_at, updated_at)
SELECT 
    1,
    'Science Fair Participation Invitation',
    'Students from grades 8-12 are invited to participate in the Annual Inter-School Science Fair.',
    'Students from grades 8-12 are invited to participate in the Annual Inter-School Science Fair. This is a great opportunity to showcase your scientific knowledge and innovation. Registration deadline is December 10, 2025.',
    'EVENTS',
    'Examination Department',
    false,
    CURRENT_TIMESTAMP - INTERVAL '1 hour',
    CURRENT_TIMESTAMP - INTERVAL '1 hour'
WHERE NOT EXISTS (SELECT 1 FROM notices WHERE title = 'Science Fair Participation Invitation' AND school_id = 1)
ON CONFLICT DO NOTHING;

INSERT INTO notices (school_id, title, description, body, category, source, is_pinned, created_at, updated_at)
SELECT 
    1,
    'Library Hours Extended During Exam Period',
    'The school library will extend its operating hours during the examination period.',
    'The school library will extend its operating hours during the examination period. From December 10-20, the library will be open from 7:00 AM to 6:00 PM (including weekends) to support student preparation.',
    'ACADEMIC',
    'Administration Office',
    false,
    CURRENT_TIMESTAMP - INTERVAL '2 hours',
    CURRENT_TIMESTAMP - INTERVAL '2 hours'
WHERE NOT EXISTS (SELECT 1 FROM notices WHERE title = 'Library Hours Extended During Exam Period' AND school_id = 1)
ON CONFLICT DO NOTHING;

INSERT INTO notices (school_id, title, description, body, category, source, is_pinned, created_at, updated_at)
SELECT 
    1,
    'Fee Payment Deadline Reminder',
    'Please submit your fee payment before December 31, 2025.',
    'This is a reminder that fee payment for the current semester is due by December 31, 2025. Late submissions will incur a penalty. Please process payments through the student portal or contact the accounts office.',
    'GENERAL',
    'Administration Office',
    false,
    CURRENT_TIMESTAMP - INTERVAL '3 hours',
    CURRENT_TIMESTAMP - INTERVAL '3 hours'
WHERE NOT EXISTS (SELECT 1 FROM notices WHERE title = 'Fee Payment Deadline Reminder' AND school_id = 1)
ON CONFLICT DO NOTHING;

-- ============================================================================
-- END OF MIGRATION
-- ============================================================================
