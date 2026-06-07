-- Teacher personal / class timetable (InVitto weekly schedule UI)
-- Apply to DB from application.properties (PostgreSQL)

CREATE TABLE IF NOT EXISTS teacher_schedule_entries (
    id               BIGSERIAL PRIMARY KEY,
    faculty_id       BIGINT       NOT NULL REFERENCES faculty(faculty_pk) ON DELETE CASCADE,
    school_id        BIGINT       NOT NULL REFERENCES schools(id) ON DELETE CASCADE,
    title            VARCHAR(200) NOT NULL,
    activity_type    VARCHAR(30)  NOT NULL,
    recurring        BOOLEAN      NOT NULL DEFAULT TRUE,
    day_of_week      INTEGER,
    specific_date    DATE,
    start_time       TIME         NOT NULL,
    end_time         TIME         NOT NULL,
    class_name       VARCHAR(50),
    section          VARCHAR(10),
    section_label    VARCHAR(120),
    venue            VARCHAR(200),
    notes            TEXT,
    subject_id       BIGINT       REFERENCES subjects(subject_id) ON DELETE SET NULL,
    created_at       TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP,
    CONSTRAINT chk_teacher_schedule_day CHECK (
        (recurring = TRUE AND day_of_week IS NOT NULL AND day_of_week BETWEEN 1 AND 7)
        OR (recurring = FALSE AND specific_date IS NOT NULL)
    ),
    CONSTRAINT chk_teacher_schedule_time CHECK (start_time < end_time)
);

CREATE INDEX IF NOT EXISTS idx_tse_faculty ON teacher_schedule_entries(faculty_id);
CREATE INDEX IF NOT EXISTS idx_tse_school ON teacher_schedule_entries(school_id);
CREATE INDEX IF NOT EXISTS idx_tse_specific_date ON teacher_schedule_entries(specific_date);
CREATE INDEX IF NOT EXISTS idx_tse_day_recurring ON teacher_schedule_entries(faculty_id, recurring, day_of_week);

COMMENT ON TABLE teacher_schedule_entries IS 'Teacher timetable: recurring weekly rows and one-off activities (meetings, prep).';
