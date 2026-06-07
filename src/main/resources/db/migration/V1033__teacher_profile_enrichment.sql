-- ─────────────────────────────────────────────────────────────────────────────
-- Teacher Profile enrichment: allocation role/date, salary & contract fields
-- Supports: Class & Subject Allocation roles, Employment salary/contract panel
-- ─────────────────────────────────────────────────────────────────────────────

-- Class & Subject Allocation: role (Class Teacher / Subject Teacher) + assigned date
ALTER TABLE class_subject_teachers
    ADD COLUMN IF NOT EXISTS role VARCHAR(30) NOT NULL DEFAULT 'SUBJECT_TEACHER';

ALTER TABLE class_subject_teachers
    ADD COLUMN IF NOT EXISTS assigned_date DATE;

UPDATE class_subject_teachers
SET assigned_date = CURRENT_DATE
WHERE assigned_date IS NULL;

-- Employment Details: salary & contract panel
ALTER TABLE faculty_profiles
    ADD COLUMN IF NOT EXISTS basic_salary NUMERIC(12, 2);

ALTER TABLE faculty_profiles
    ADD COLUMN IF NOT EXISTS contract_type VARCHAR(40);

ALTER TABLE faculty_profiles
    ADD COLUMN IF NOT EXISTS contract_path VARCHAR(500);
