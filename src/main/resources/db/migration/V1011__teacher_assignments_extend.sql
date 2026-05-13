-- Teacher portal assignments: class targeting, publish lifecycle, marks, flags, submission feedback

ALTER TABLE assignments
    ADD COLUMN IF NOT EXISTS school_id BIGINT REFERENCES schools(id) ON DELETE SET NULL;

ALTER TABLE assignments
    ADD COLUMN IF NOT EXISTS class_name VARCHAR(50);

ALTER TABLE assignments
    ADD COLUMN IF NOT EXISTS section VARCHAR(10);

ALTER TABLE assignments
    ADD COLUMN IF NOT EXISTS due_time TIME;

ALTER TABLE assignments
    ADD COLUMN IF NOT EXISTS given_date DATE;

ALTER TABLE assignments
    ADD COLUMN IF NOT EXISTS total_marks INTEGER;

ALTER TABLE assignments
    ADD COLUMN IF NOT EXISTS allow_late_submission BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE assignments
    ADD COLUMN IF NOT EXISTS allow_resubmission BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE assignments
    ADD COLUMN IF NOT EXISTS publish_status VARCHAR(20);

UPDATE assignments SET publish_status = 'PUBLISHED' WHERE publish_status IS NULL;

ALTER TABLE assignments
    ALTER COLUMN publish_status SET DEFAULT 'PUBLISHED';

ALTER TABLE assignments
    ALTER COLUMN publish_status SET NOT NULL;

CREATE INDEX IF NOT EXISTS idx_assignments_teacher_status
    ON assignments(teacher_id, publish_status);

CREATE INDEX IF NOT EXISTS idx_assignments_class_due
    ON assignments(class_name, section, due_date);

ALTER TABLE assignment_submissions
    ADD COLUMN IF NOT EXISTS teacher_remarks TEXT;

COMMENT ON COLUMN assignments.publish_status IS 'DRAFT = teacher only; PUBLISHED = visible to class; CLOSED = no new submissions.';
COMMENT ON COLUMN assignment_submissions.teacher_remarks IS 'Feedback from teacher when grading or returning work.';
