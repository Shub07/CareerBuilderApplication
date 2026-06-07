-- =============================================================================
-- Teacher "Student Performance" module — PostgreSQL
-- DB: same as spring.datasource.url (e.g. admindb)
-- Apply: psql ... -f src/main/resources/db/migration/V1003__teacher_student_performance.sql
-- =============================================================================

-- Quiz results (tab: Quiz) — separate from formal exams
CREATE TABLE IF NOT EXISTS student_quiz_results (
    id              BIGSERIAL PRIMARY KEY,
    student_id      BIGINT       NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    subject_id      BIGINT       NOT NULL REFERENCES subjects(subject_id) ON DELETE RESTRICT,
    faculty_id      BIGINT       REFERENCES faculty(faculty_pk) ON DELETE SET NULL,
    title           VARCHAR(200) NOT NULL,
    quiz_date       DATE         NOT NULL,
    obtained_marks  INTEGER      NOT NULL,
    total_marks     INTEGER      NOT NULL,
    passed          BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_quiz_student ON student_quiz_results(student_id);
CREATE INDEX IF NOT EXISTS idx_quiz_subject ON student_quiz_results(subject_id);
CREATE INDEX IF NOT EXISTS idx_quiz_date ON student_quiz_results(quiz_date DESC);

-- Activity feed (Overview tab: submissions, grades, absences)
CREATE TABLE IF NOT EXISTS student_performance_activities (
    id                   BIGSERIAL PRIMARY KEY,
    student_id           BIGINT       NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    activity_type        VARCHAR(40)  NOT NULL,
    title                VARCHAR(300) NOT NULL,
    description          VARCHAR(500),
    activity_date        DATE         NOT NULL,
    severity             VARCHAR(20)  NOT NULL DEFAULT 'INFO',
    related_entity_type  VARCHAR(40),
    related_entity_id    BIGINT,
    created_at           TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_spa_student_date ON student_performance_activities(student_id, activity_date DESC);

-- General teacher remark for student (modal "Save Remark" / parents)
CREATE TABLE IF NOT EXISTS teacher_student_remarks (
    id           BIGSERIAL PRIMARY KEY,
    student_id   BIGINT       NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    faculty_id   BIGINT       NOT NULL REFERENCES faculty(faculty_pk) ON DELETE CASCADE,
    remark_text  TEXT         NOT NULL,
    created_at   TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_tsr_student_faculty ON teacher_student_remarks(student_id, faculty_id);

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'uk_tsr_student_faculty'
    ) THEN
        ALTER TABLE teacher_student_remarks
            ADD CONSTRAINT uk_tsr_student_faculty UNIQUE (student_id, faculty_id);
    END IF;
END $$;

-- Assignment grading (Assignments tab: Grade column)
ALTER TABLE assignment_submissions
    ADD COLUMN IF NOT EXISTS letter_grade VARCHAR(5);
ALTER TABLE assignment_submissions
    ADD COLUMN IF NOT EXISTS points_obtained INTEGER;
ALTER TABLE assignment_submissions
    ADD COLUMN IF NOT EXISTS points_total INTEGER;

-- Attendance detail (check-in time on tab Attendance)
ALTER TABLE attendance_records
    ADD COLUMN IF NOT EXISTS check_in_time TIME;

COMMENT ON COLUMN attendance_records.check_in_time IS 'Optional; null when absent.';
