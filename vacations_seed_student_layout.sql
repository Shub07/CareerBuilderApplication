-- Idempotent vacation seed for Student Layout testing
-- Run with: psql -h localhost -p 5432 -U admin -d admindb -f vacations_seed_student_layout.sql
-- Assumes school_id=1 already exists.

INSERT INTO vacations (school_id, vacation_name, vacation_type, start_date, end_date, description, is_active, created_by)
SELECT 1, 'Founders Day Holiday', 'HOLIDAY', '2026-04-12', '2026-04-12', 'School foundation day holiday', TRUE, 'ADMIN'
WHERE NOT EXISTS (
  SELECT 1 FROM vacations
  WHERE school_id = 1 AND vacation_name = 'Founders Day Holiday' AND start_date = '2026-04-12' AND end_date = '2026-04-12'
);

INSERT INTO vacations (school_id, vacation_name, vacation_type, start_date, end_date, description, is_active, created_by)
SELECT 1, 'Summer Vacation 2026', 'SUMMER', '2026-05-20', '2026-06-30', 'Summer break for all grades', TRUE, 'ADMIN'
WHERE NOT EXISTS (
  SELECT 1 FROM vacations
  WHERE school_id = 1 AND vacation_name = 'Summer Vacation 2026' AND start_date = '2026-05-20' AND end_date = '2026-06-30'
);

INSERT INTO vacations (school_id, vacation_name, vacation_type, start_date, end_date, description, is_active, created_by)
SELECT 1, 'Monsoon Break', 'MONSOON', '2026-08-01', '2026-08-05', 'Short monsoon recess', TRUE, 'ADMIN'
WHERE NOT EXISTS (
  SELECT 1 FROM vacations
  WHERE school_id = 1 AND vacation_name = 'Monsoon Break' AND start_date = '2026-08-01' AND end_date = '2026-08-05'
);

INSERT INTO vacations (school_id, vacation_name, vacation_type, start_date, end_date, description, is_active, created_by)
SELECT 1, 'Winter Break 2026', 'WINTER', '2026-12-20', '2027-01-05', 'Year-end winter break', TRUE, 'ADMIN'
WHERE NOT EXISTS (
  SELECT 1 FROM vacations
  WHERE school_id = 1 AND vacation_name = 'Winter Break 2026' AND start_date = '2026-12-20' AND end_date = '2027-01-05'
);

INSERT INTO vacations (school_id, vacation_name, vacation_type, start_date, end_date, description, is_active, created_by)
SELECT 1, 'Republic Day Holiday 2026', 'HOLIDAY', '2026-01-26', '2026-01-26', 'National holiday', TRUE, 'ADMIN'
WHERE NOT EXISTS (
  SELECT 1 FROM vacations
  WHERE school_id = 1 AND vacation_name = 'Republic Day Holiday 2026' AND start_date = '2026-01-26' AND end_date = '2026-01-26'
);

INSERT INTO vacations (school_id, vacation_name, vacation_type, start_date, end_date, description, is_active, created_by)
SELECT 1, 'Exam Break Term 1', 'EXAM_BREAK', '2026-03-10', '2026-03-20', 'Preparation break for term exams', TRUE, 'ADMIN'
WHERE NOT EXISTS (
  SELECT 1 FROM vacations
  WHERE school_id = 1 AND vacation_name = 'Exam Break Term 1' AND start_date = '2026-03-10' AND end_date = '2026-03-20'
);
