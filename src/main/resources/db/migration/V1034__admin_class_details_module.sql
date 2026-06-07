-- ─────────────────────────────────────────────────────────────────────────────
-- Admin Class Details module
-- First-class academic class / section / subject entities (previously string-only)
-- ─────────────────────────────────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS academic_classes (
    id               BIGSERIAL PRIMARY KEY,
    school_id        BIGINT       NOT NULL REFERENCES schools (id),
    name             VARCHAR(50)  NOT NULL,
    academic_year    VARCHAR(20)  NOT NULL,
    max_capacity     INT,
    status           VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    class_teacher_id BIGINT       REFERENCES faculty (faculty_pk),
    deleted          BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP,
    CONSTRAINT uk_academic_class_school_name_year UNIQUE (school_id, name, academic_year)
);

CREATE INDEX IF NOT EXISTS idx_academic_class_school  ON academic_classes (school_id);
CREATE INDEX IF NOT EXISTS idx_academic_class_status  ON academic_classes (status);
CREATE INDEX IF NOT EXISTS idx_academic_class_deleted ON academic_classes (deleted);

CREATE TABLE IF NOT EXISTS class_sections (
    id                 BIGSERIAL PRIMARY KEY,
    class_id           BIGINT      NOT NULL REFERENCES academic_classes (id) ON DELETE CASCADE,
    school_id          BIGINT      NOT NULL,
    name               VARCHAR(20) NOT NULL,
    section_teacher_id BIGINT      REFERENCES faculty (faculty_pk),
    created_at         TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMP,
    CONSTRAINT uk_class_section_class_name UNIQUE (class_id, name)
);

CREATE INDEX IF NOT EXISTS idx_class_section_class  ON class_sections (class_id);
CREATE INDEX IF NOT EXISTS idx_class_section_school ON class_sections (school_id);

CREATE TABLE IF NOT EXISTS class_subjects (
    id                  BIGSERIAL PRIMARY KEY,
    class_id            BIGINT      NOT NULL REFERENCES academic_classes (id) ON DELETE CASCADE,
    school_id           BIGINT      NOT NULL,
    subject_id          BIGINT      NOT NULL REFERENCES subjects (subject_id),
    subject_code        VARCHAR(30),
    assigned_teacher_id BIGINT      REFERENCES faculty (faculty_pk),
    created_at          TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP,
    CONSTRAINT uk_class_subject_class_subject UNIQUE (class_id, subject_id)
);

CREATE INDEX IF NOT EXISTS idx_class_subject_class  ON class_subjects (class_id);
CREATE INDEX IF NOT EXISTS idx_class_subject_school ON class_subjects (school_id);
