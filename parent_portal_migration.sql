-- ============================================================================
-- PARENT PORTAL DATABASE MIGRATION
-- Career Builder Backend - Parent Authentication Table
-- Date: April 9, 2026
-- ============================================================================

-- Create parent_credentials table
CREATE TABLE IF NOT EXISTS parent_credentials (
    id BIGSERIAL PRIMARY KEY,
    parent_id VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    student_id VARCHAR(50) NOT NULL,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    last_login TIMESTAMP,
    role VARCHAR(20) DEFAULT 'PARENT'
);

-- Create indexes for performance
CREATE INDEX idx_parent_email ON parent_credentials(email);
CREATE INDEX idx_parent_id ON parent_credentials(parent_id);
CREATE INDEX idx_student_id ON parent_credentials(student_id);

-- Enable pgcrypto for BCrypt hash generation in SQL
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- ============================================================================
-- INSERT TEST CREDENTIALS
-- Password for all: parent@123
-- BCrypt hashes are generated at insert time via pgcrypto crypt(..., gen_salt('bf', 10)).
-- ============================================================================

INSERT INTO parent_credentials (parent_id, email, password_hash, student_id, is_active, created_at)
VALUES
  ('P001', 'rajesh.patel@email.com', crypt('parent@123', gen_salt('bf', 10)), '1', true, NOW()),
  ('P002', 'priya.verma@email.com', crypt('parent@123', gen_salt('bf', 10)), '2', true, NOW()),
  ('P003', 'amit.gupta@email.com', crypt('parent@123', gen_salt('bf', 10)), '3', true, NOW());

-- Add parent credential for existing student ID 34 (idempotent)
INSERT INTO parent_credentials (parent_id, email, password_hash, student_id, is_active, created_at)
SELECT
  'P034',
  'parent.shubham34@gmail.com',
  crypt('parent@123', gen_salt('bf', 10)),
  '34',
  true,
  NOW()
WHERE NOT EXISTS (
  SELECT 1
  FROM parent_credentials
  WHERE parent_id = 'P034' OR email = 'parent.shubham34@gmail.com'
);

-- ============================================================================
-- VERIFICATION QUERIES
-- ============================================================================

-- Check if table exists
SELECT * FROM parent_credentials;

-- Count records
SELECT COUNT(*) as total_parents FROM parent_credentials;

-- View all parent credentials
SELECT parent_id, email, student_id, is_active, created_at FROM parent_credentials;

-- Test parent login query
SELECT * FROM parent_credentials WHERE email = 'rajesh.patel@email.com' AND is_active = true;

-- ============================================================================
-- TEST CREDENTIALS
-- ============================================================================
-- Parent 1:
--   Email: rajesh.patel@email.com
--   Password: parent@123
--   Child: Aarav Patel (Student ID: 1)
--
-- Parent 2:
--   Email: priya.verma@email.com
--   Password: parent@123
--   Child: Ananya Verma (Student ID: 2)
--
-- Parent 3:
--   Email: amit.gupta@email.com
--   Password: parent@123
--   Child: Arjun Gupta (Student ID: 3)
-- ============================================================================

