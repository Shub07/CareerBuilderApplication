-- Extended admin exams: venues, student registrations, publish results, rich schedules.

ALTER TABLE exams
    ADD COLUMN IF NOT EXISTS results_published BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS results_published_at TIMESTAMP;

CREATE INDEX IF NOT EXISTS idx_exam_results_published ON exams (school_id, results_published);

CREATE TABLE IF NOT EXISTS exam_venues (
    venue_id    BIGSERIAL PRIMARY KEY,
    school_id   BIGINT       NOT NULL REFERENCES schools (id),
    name        VARCHAR(120) NOT NULL,
    capacity    INT          NOT NULL DEFAULT 100,
    active      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP    NOT NULL DEFAULT now(),
    CONSTRAINT uk_exam_venue_school_name UNIQUE (school_id, name)
);

CREATE INDEX IF NOT EXISTS idx_exam_venue_school ON exam_venues (school_id);

CREATE TABLE IF NOT EXISTS exam_registrations (
    registration_id    BIGSERIAL PRIMARY KEY,
    exam_id            BIGINT       NOT NULL REFERENCES exams (exam_id) ON DELETE CASCADE,
    school_id          BIGINT       NOT NULL REFERENCES schools (id),
    student_id         BIGINT       NOT NULL REFERENCES students (id) ON DELETE CASCADE,
    hall_ticket_number VARCHAR(40)  NOT NULL,
    primary_venue      VARCHAR(200),
    status             VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    created_at         TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at         TIMESTAMP,
    CONSTRAINT uk_exam_registration UNIQUE (exam_id, student_id),
    CONSTRAINT uk_exam_hall_ticket UNIQUE (school_id, hall_ticket_number)
);

CREATE INDEX IF NOT EXISTS idx_exam_reg_exam ON exam_registrations (exam_id);
CREATE INDEX IF NOT EXISTS idx_exam_reg_student ON exam_registrations (student_id);
CREATE INDEX IF NOT EXISTS idx_exam_reg_school_status ON exam_registrations (school_id, status);

ALTER TABLE exam_schedules
    ADD COLUMN IF NOT EXISTS venue_id BIGINT REFERENCES exam_venues (venue_id),
    ADD COLUMN IF NOT EXISTS invigilator_id BIGINT REFERENCES faculty (faculty_pk),
    ADD COLUMN IF NOT EXISTS assistant_invigilator_id BIGINT REFERENCES faculty (faculty_pk),
    ADD COLUMN IF NOT EXISTS duration_minutes INT,
    ADD COLUMN IF NOT EXISTS reporting_time TIME,
    ADD COLUMN IF NOT EXISTS buffer_minutes INT,
    ADD COLUMN IF NOT EXISTS instructions TEXT;

CREATE INDEX IF NOT EXISTS idx_exam_schedule_venue ON exam_schedules (venue_id, scheduled_date);
