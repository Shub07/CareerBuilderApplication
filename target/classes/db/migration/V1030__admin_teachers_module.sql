-- Admin Teachers module: extended profile for faculty management UI

CREATE TABLE IF NOT EXISTS faculty_profiles (
    id              BIGSERIAL PRIMARY KEY,
    faculty_id      BIGINT NOT NULL,
    account_status  VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    photo_url       VARCHAR(500) NULL,
    date_of_birth   DATE NULL,
    joining_date    DATE NULL,
    employment_type VARCHAR(30) NULL,
    department      VARCHAR(100) NULL,
    skills          VARCHAR(500) NULL,
    certifications  VARCHAR(1000) NULL,
    resume_path     VARCHAR(500) NULL,
    id_proof_path   VARCHAR(500) NULL,
    deleted         BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NULL,
    CONSTRAINT uk_faculty_profile_faculty UNIQUE (faculty_id),
    CONSTRAINT fk_faculty_profile_faculty FOREIGN KEY (faculty_id) REFERENCES faculty(faculty_pk)
);

CREATE INDEX IF NOT EXISTS idx_faculty_profile_status ON faculty_profiles(account_status);
CREATE INDEX IF NOT EXISTS idx_faculty_profile_deleted ON faculty_profiles(deleted);

-- Backfill profiles for existing faculty
INSERT INTO faculty_profiles (faculty_id, account_status, joining_date, deleted)
SELECT f.faculty_pk, 'ACTIVE', CURRENT_DATE, FALSE
FROM faculty f
WHERE NOT EXISTS (
    SELECT 1 FROM faculty_profiles fp WHERE fp.faculty_id = f.faculty_pk
);
