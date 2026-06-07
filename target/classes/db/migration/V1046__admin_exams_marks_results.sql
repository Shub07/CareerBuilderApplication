-- Admin marks approval workflow + per-student verification.

CREATE TABLE IF NOT EXISTS exam_marks_submissions (
    submission_id   BIGSERIAL PRIMARY KEY,
    exam_id         BIGINT       NOT NULL REFERENCES exams (exam_id) ON DELETE CASCADE,
    school_id       BIGINT       NOT NULL REFERENCES schools (id),
    subject_id      BIGINT       NOT NULL REFERENCES subjects (subject_id),
    teacher_id      BIGINT       REFERENCES faculty (faculty_pk),
    status          VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    max_marks       INT          NOT NULL DEFAULT 100,
    submitted_at    TIMESTAMP,
    approved_at     TIMESTAMP,
    approved_by     VARCHAR(200),
    rejected_at     TIMESTAMP,
    rejection_reason TEXT,
    created_at      TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP,
    CONSTRAINT uk_exam_marks_submission UNIQUE (exam_id, subject_id)
);

CREATE INDEX IF NOT EXISTS idx_exam_marks_sub_exam ON exam_marks_submissions (exam_id);

ALTER TABLE exam_results
    ADD COLUMN IF NOT EXISTS admin_verified BOOLEAN NOT NULL DEFAULT FALSE;
