CREATE TABLE IF NOT EXISTS faculty_documents (
    id              BIGSERIAL PRIMARY KEY,
    faculty_id      BIGINT NOT NULL,
    document_name   VARCHAR(200) NOT NULL,
    document_type   VARCHAR(50) NOT NULL,
    file_name       VARCHAR(255) NOT NULL,
    file_path       VARCHAR(500) NOT NULL,
    file_size_bytes BIGINT NULL,
    uploaded_by     VARCHAR(100) NULL,
    uploaded_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_faculty_doc_faculty FOREIGN KEY (faculty_id) REFERENCES faculty(faculty_pk)
);

CREATE INDEX IF NOT EXISTS idx_faculty_doc_faculty ON faculty_documents(faculty_id);

ALTER TABLE faculty_profiles
    ADD COLUMN IF NOT EXISTS designation VARCHAR(100) NULL;
