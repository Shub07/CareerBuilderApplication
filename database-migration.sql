-- ============================================================================
-- DATABASE MIGRATION SCRIPT FOR CAREER BUILDER
-- ============================================================================
-- This script updates the database schema to support the new entity relationships.
--
-- Changes:
-- 1. Updates 'faculty' table: school_id from varchar to bigint
-- 2. Updates 'notices' table: school_id from varchar to bigint
-- 3. Updates 'parents' table: school_id and student_id from varchar to bigint
-- 4. Updates 'students' table: removes school_id/school_name, adds school_id as bigint FK
-- 5. Creates 'my_classes' table for student class enrollments
--
-- IMPORTANT: Backup your database before running this script!
-- ============================================================================

-- Step 1: Drop existing foreign key constraints (if they exist)
ALTER TABLE IF EXISTS faculty
  DROP CONSTRAINT IF EXISTS FKe0ksmnf4tg4h5i8s3viksmp45;

ALTER TABLE IF EXISTS parents
  DROP CONSTRAINT IF EXISTS FKatq0lg3m5wavlfe3p7kfkvwpj,
  DROP CONSTRAINT IF EXISTS FKivc7hrl007b55xq6endc0bb58;

ALTER TABLE IF EXISTS students
  DROP CONSTRAINT IF EXISTS fk_students_school;

-- Step 2: Update faculty table
ALTER TABLE faculty
  ALTER COLUMN school_id TYPE bigint USING school_id::bigint;

-- Step 3: Update notices table
ALTER TABLE notices
  ALTER COLUMN school_id TYPE bigint USING school_id::bigint;

-- Step 4: Update parents table - school_id
ALTER TABLE parents
  ALTER COLUMN school_id TYPE bigint USING school_id::bigint;

-- Step 5: Update parents table - student_id
ALTER TABLE parents
  ALTER COLUMN student_id TYPE bigint USING student_id::bigint;

-- Step 6: Update students table - remove old school columns and add new FK
ALTER TABLE students
  DROP COLUMN IF EXISTS school_name,
  ALTER COLUMN school_id TYPE bigint USING school_id::bigint;

-- Step 7: Recreate foreign key constraints
ALTER TABLE faculty
  ADD CONSTRAINT FKe0ksmnf4tg4h5i8s3viksmp45
  FOREIGN KEY (school_id) REFERENCES schools(id);

ALTER TABLE parents
  ADD CONSTRAINT FKatq0lg3m5wavlfe3p7kfkvwpj
  FOREIGN KEY (school_id) REFERENCES schools(id);

ALTER TABLE parents
  ADD CONSTRAINT FKivc7hrl007b55xq6endc0bb58
  FOREIGN KEY (student_id) REFERENCES students(id);

ALTER TABLE students
  ADD CONSTRAINT fk_students_school
  FOREIGN KEY (school_id) REFERENCES schools(id);

-- Step 8: Create my_classes table
CREATE TABLE IF NOT EXISTS my_classes (
    id SERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL,
    subject_id BIGINT NOT NULL,
    class_name VARCHAR(50) NOT NULL,
    section VARCHAR(10) NOT NULL,
    CONSTRAINT fk_myclass_student FOREIGN KEY (student_id) REFERENCES students(id),
    CONSTRAINT fk_myclass_subject FOREIGN KEY (subject_id) REFERENCES subjects(subject_id),
    CONSTRAINT uk_myclass_student_subject UNIQUE (student_id, subject_id)
);

-- Verify the changes
\d faculty
\d notices
\d parents
\d students
\d my_classes

-- ============================================================================
-- Migration complete! The schema is now consistent with the JPA entities.
-- ============================================================================
