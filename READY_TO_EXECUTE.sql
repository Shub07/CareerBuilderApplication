-- ========================================
-- COMPLETE DATA INSERT SCRIPT
-- Run this in PostgreSQL (pgAdmin or psql)
-- ========================================

-- Step 1: INSERT SCHOOLS (No dependencies)
INSERT INTO schools (id, school_name, school_code, address, city, state, country, phone, email, principal_name, established_year)
VALUES
  (1, 'Delhi Public School', 'DPS001', '123 School Lane, Delhi', 'Delhi', 'Delhi', 'India', '9876543200', 'principal@dps.edu.in', 'Dr. Sharma', 2010),
  (2, 'Mumbai Academy', 'MA001', '456 Education Ave, Mumbai', 'Mumbai', 'Maharashtra', 'India', '9876543201', 'principal@mumbaiaca.edu.in', 'Mrs. Patel', 2015);

-- Step 2: INSERT SUBJECTS (No dependencies)
INSERT INTO subjects (subject_id, subject_name)
VALUES
  (1, 'Mathematics'),
  (2, 'English'),
  (3, 'Physics'),
  (4, 'Chemistry'),
  (5, 'Biology'),
  (6, 'History'),
  (7, 'Geography');

-- Step 3: INSERT STUDENTS (Depends on schools - school_id must exist)
INSERT INTO students (id, first_name, last_name, age, class_name, section, roll_no, parent_name, phone, email, address, school_id)
VALUES
  (1, 'John', 'Doe', 16, '11th Standard', 'A', 1, 'Mr. Doe', '9876543210', 'john.doe@example.com', '123 Main St, Delhi', 1),
  (2, 'Jane', 'Smith', 15, '10th Standard', 'B', 2, 'Ms. Smith', '9876543211', 'jane.smith@example.com', '456 Oak Ave, Mumbai', 2),
  (3, 'Bob', 'Wilson', 17, '12th Standard', 'A', 3, 'Mr. Wilson', '9876543212', 'bob.wilson@example.com', '789 Elm Rd, Delhi', 1);

-- Step 4: INSERT MYCLASS (Depends on students and subjects - both IDs must exist)
INSERT INTO my_classes (student_id, subject_id, class_name, section)
VALUES
  (1, 1, 'Mathematics Class', 'A'),
  (1, 2, 'English Class', 'A'),
  (1, 3, 'Physics Class', 'A'),
  (2, 1, 'Mathematics Class', 'B'),
  (2, 4, 'Chemistry Class', 'B'),
  (3, 3, 'Physics Class', 'A'),
  (3, 4, 'Chemistry Class', 'A'),
  (3, 5, 'Biology Class', 'A');

-- ========================================
-- VERIFICATION QUERIES
-- ========================================

-- Check Schools
SELECT 'SCHOOLS' as Table_Name, COUNT(*) as Record_Count FROM schools;
SELECT * FROM schools;

-- Check Subjects
SELECT 'SUBJECTS' as Table_Name, COUNT(*) as Record_Count FROM subjects;
SELECT * FROM subjects;

-- Check Students
SELECT 'STUDENTS' as Table_Name, COUNT(*) as Record_Count FROM students;
SELECT * FROM students;

-- Check MyClasses
SELECT 'MYCLASS' as Table_Name, COUNT(*) as Record_Count FROM my_classes;
SELECT * FROM my_classes;

-- Detailed Join Query - See student-subject relationships
SELECT
  s.first_name || ' ' || s.last_name AS Student_Name,
  sub.subject_name AS Subject,
  mc.class_name AS Class_Name,
  mc.section AS Section
FROM my_classes mc
JOIN students s ON mc.student_id = s.id
JOIN subjects sub ON mc.subject_id = sub.subject_id
ORDER BY s.first_name, sub.subject_name;

