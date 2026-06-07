-- Hall tickets (document hash, generation flag) + exam-level venue allocations.

ALTER TABLE exam_registrations
    ADD COLUMN IF NOT EXISTS venue_id BIGINT REFERENCES exam_venues (venue_id),
    ADD COLUMN IF NOT EXISTS document_hash VARCHAR(40),
    ADD COLUMN IF NOT EXISTS hall_ticket_generated BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS portal_notified BOOLEAN NOT NULL DEFAULT FALSE;

CREATE INDEX IF NOT EXISTS idx_exam_reg_venue ON exam_registrations (exam_id, venue_id);

CREATE TABLE IF NOT EXISTS exam_venue_allocations (
    allocation_id            BIGSERIAL PRIMARY KEY,
    exam_id                  BIGINT       NOT NULL REFERENCES exams (exam_id) ON DELETE CASCADE,
    school_id                BIGINT       NOT NULL REFERENCES schools (id),
    venue_id                 BIGINT       NOT NULL REFERENCES exam_venues (venue_id),
    capacity_limit           INT,
    administrator_id         BIGINT REFERENCES faculty (faculty_pk),
    primary_invigilator_id   BIGINT REFERENCES faculty (faculty_pk),
    assistant_invigilator_id BIGINT REFERENCES faculty (faculty_pk),
    created_at               TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at               TIMESTAMP,
    CONSTRAINT uk_exam_venue_allocation UNIQUE (exam_id, venue_id)
);

CREATE INDEX IF NOT EXISTS idx_exam_venue_alloc_exam ON exam_venue_allocations (exam_id);
