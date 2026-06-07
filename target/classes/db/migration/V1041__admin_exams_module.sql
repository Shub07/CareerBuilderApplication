-- Admin Exams module: extended exam metadata + per-subject timetable rows.

ALTER TABLE exams ADD COLUMN IF NOT EXISTS section VARCHAR(10);
ALTER TABLE exams ADD COLUMN IF NOT EXISTS academic_year VARCHAR(20);
ALTER TABLE exams ADD COLUMN IF NOT EXISTS description TEXT;
ALTER TABLE exams ADD COLUMN IF NOT EXISTS instructions TEXT;
ALTER TABLE exams ADD COLUMN IF NOT EXISTS coordinator_id BIGINT REFERENCES faculty (faculty_pk);
ALTER TABLE exams ADD COLUMN IF NOT EXISTS status VARCHAR(20) NOT NULL DEFAULT 'DRAFT';
ALTER TABLE exams ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE exams ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT now();
ALTER TABLE exams ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP;

CREATE INDEX IF NOT EXISTS idx_exam_school ON exams (school_id);
CREATE INDEX IF NOT EXISTS idx_exam_school_status ON exams (school_id, status);
CREATE INDEX IF NOT EXISTS idx_exam_school_class ON exams (school_id, class_name);
CREATE INDEX IF NOT EXISTS idx_exam_academic_year ON exams (academic_year);

CREATE TABLE IF NOT EXISTS exam_schedules (
    schedule_id   BIGSERIAL PRIMARY KEY,
    exam_id       BIGINT       NOT NULL REFERENCES exams (exam_id) ON DELETE CASCADE,
    school_id     BIGINT       NOT NULL REFERENCES schools (id),
    subject_id    BIGINT       NOT NULL REFERENCES subjects (subject_id),
    scheduled_date DATE        NOT NULL,
    start_time    TIME         NOT NULL,
    end_time      TIME         NOT NULL,
    venue         VARCHAR(200),
    created_at    TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_exam_schedule_exam ON exam_schedules (exam_id);
CREATE INDEX IF NOT EXISTS idx_exam_schedule_school ON exam_schedules (school_id);
