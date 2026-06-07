-- Leave proof documents + substitute teacher reference for teacher self-attendance.

ALTER TABLE teacher_leave_requests
    ADD COLUMN IF NOT EXISTS document_path VARCHAR(500),
    ADD COLUMN IF NOT EXISTS document_original_name VARCHAR(255),
    ADD COLUMN IF NOT EXISTS document_content_type VARCHAR(120),
    ADD COLUMN IF NOT EXISTS substitute_faculty_id BIGINT;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints
        WHERE constraint_name = 'fk_tlr_substitute_faculty'
    ) THEN
        ALTER TABLE teacher_leave_requests
            ADD CONSTRAINT fk_tlr_substitute_faculty
                FOREIGN KEY (substitute_faculty_id) REFERENCES faculty(faculty_pk);
    END IF;
END $$;
