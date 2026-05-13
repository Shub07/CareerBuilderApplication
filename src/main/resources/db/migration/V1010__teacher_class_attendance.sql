-- Teacher "Class Attendance" (mark + history + lock + edit audit) — PostgreSQL
-- One session per faculty + class + section + subject + calendar date.

CREATE TABLE IF NOT EXISTS class_attendance_sessions (
    id                     BIGSERIAL PRIMARY KEY,
    faculty_id             BIGINT       NOT NULL REFERENCES faculty(faculty_pk) ON DELETE CASCADE,
    school_id              BIGINT       NOT NULL REFERENCES schools(id) ON DELETE CASCADE,
    class_name             VARCHAR(50)  NOT NULL,
    section                VARCHAR(10)  NOT NULL,
    subject_id             BIGINT       NOT NULL REFERENCES subjects(subject_id) ON DELETE RESTRICT,
    session_date           DATE         NOT NULL,
    locked                 BOOLEAN      NOT NULL DEFAULT FALSE,
    locked_at              TIMESTAMP,
    proof_attachment_path  VARCHAR(500),
    created_at             TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at             TIMESTAMP,
    CONSTRAINT uk_class_attendance_session UNIQUE (faculty_id, class_name, section, subject_id, session_date)
);

CREATE INDEX IF NOT EXISTS idx_cas_faculty_date ON class_attendance_sessions(faculty_id, session_date DESC);
CREATE INDEX IF NOT EXISTS idx_cas_school_date ON class_attendance_sessions(school_id, session_date DESC);

CREATE TABLE IF NOT EXISTS class_attendance_lines (
    id          BIGSERIAL PRIMARY KEY,
    session_id  BIGINT       NOT NULL REFERENCES class_attendance_sessions(id) ON DELETE CASCADE,
    student_id  BIGINT       NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    status      VARCHAR(20)  NOT NULL,
    remarks     TEXT,
    updated_at  TIMESTAMP,
    CONSTRAINT uk_class_attendance_line UNIQUE (session_id, student_id)
);

CREATE INDEX IF NOT EXISTS idx_cal_session ON class_attendance_lines(session_id);

CREATE TABLE IF NOT EXISTS class_attendance_edit_audit (
    id          BIGSERIAL PRIMARY KEY,
    session_id  BIGINT       NOT NULL REFERENCES class_attendance_sessions(id) ON DELETE CASCADE,
    faculty_id  BIGINT       NOT NULL REFERENCES faculty(faculty_pk) ON DELETE CASCADE,
    reason_text TEXT         NOT NULL,
    edited_at   TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_caea_session ON class_attendance_edit_audit(session_id, edited_at DESC);

COMMENT ON TABLE class_attendance_sessions IS 'Teacher class attendance: one locked submission per class/subject/day.';
COMMENT ON TABLE class_attendance_lines IS 'Per-student status within a class attendance session.';
COMMENT ON TABLE class_attendance_edit_audit IS 'Justification when editing a locked class attendance session.';
