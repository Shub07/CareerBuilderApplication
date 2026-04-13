-- Add parent portal credential for student ID 34
-- Safe to run multiple times.
-- Password: parent@123
-- BCrypt hash is generated via pgcrypto at execution time.

CREATE EXTENSION IF NOT EXISTS pgcrypto;

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

-- Verify
SELECT parent_id, email, student_id, is_active, created_at
FROM parent_credentials
WHERE parent_id = 'P034' OR student_id = '34';
