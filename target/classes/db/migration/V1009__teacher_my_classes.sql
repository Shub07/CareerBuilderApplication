-- My Classes: per-session lesson notes + optional substitution label on teacher entries + assignment attachment path

CREATE TABLE IF NOT EXISTS teacher_session_notes (
    id                    BIGSERIAL PRIMARY KEY,
    faculty_id            BIGINT       NOT NULL REFERENCES faculty(faculty_pk) ON DELETE CASCADE,
    school_id             BIGINT       NOT NULL REFERENCES schools(id) ON DELETE CASCADE,
    session_date          DATE         NOT NULL,
    session_kind          VARCHAR(30)  NOT NULL,
    ref_id                BIGINT       NOT NULL,
    class_name            VARCHAR(50)  NOT NULL,
    section               VARCHAR(10)  NOT NULL,
    subject_id            BIGINT       REFERENCES subjects(subject_id) ON DELETE SET NULL,
    session_start_time    TIME         NOT NULL,
    session_end_time      TIME         NOT NULL,
    topic_covered         VARCHAR(500),
    description_notes   TEXT,
    homework              VARCHAR(500),
    created_at            TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMP,
    CONSTRAINT uk_teacher_session_note UNIQUE (faculty_id, session_kind, ref_id, session_date)
);

CREATE INDEX IF NOT EXISTS idx_tsn_faculty_date ON teacher_session_notes(faculty_id, session_date DESC);
CREATE INDEX IF NOT EXISTS idx_tsn_class_subject ON teacher_session_notes(class_name, section, subject_id);

ALTER TABLE teacher_schedule_entries
    ADD COLUMN IF NOT EXISTS substitute_for_name VARCHAR(120);

ALTER TABLE assignments
    ADD COLUMN IF NOT EXISTS attachment_path VARCHAR(500);

COMMENT ON TABLE teacher_session_notes IS 'Lesson plan / session notes for a concrete timetable slot or extra session instance.';
COMMENT ON COLUMN teacher_schedule_entries.substitute_for_name IS 'Optional label e.g. Handling for John (substitution).';
