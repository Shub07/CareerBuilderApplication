-- Lesson Plan Tracker (syllabus topics + teaching session logs) and Learning Material repository
-- Linked to students via school_id + class_name + section (+ subject_id)

CREATE TABLE IF NOT EXISTS lesson_plan_topics (
    topic_id         BIGSERIAL PRIMARY KEY,
    school_id        BIGINT       NOT NULL REFERENCES schools(id) ON DELETE CASCADE,
    teacher_id       BIGINT       NOT NULL REFERENCES faculty(faculty_pk) ON DELETE CASCADE,
    subject_id       BIGINT       NOT NULL REFERENCES subjects(subject_id) ON DELETE RESTRICT,
    class_name       VARCHAR(50)  NOT NULL,
    section          VARCHAR(10)  NOT NULL,
    title            VARCHAR(300) NOT NULL,
    description      TEXT,
    sort_order       INT          NOT NULL DEFAULT 0,
    status           VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    last_taught_date DATE,
    created_at       TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_lesson_topics_class_subject
    ON lesson_plan_topics(school_id, class_name, section, subject_id);
CREATE INDEX IF NOT EXISTS idx_lesson_topics_teacher ON lesson_plan_topics(teacher_id);

CREATE TABLE IF NOT EXISTS lesson_plan_session_logs (
    log_id                 BIGSERIAL PRIMARY KEY,
    topic_id               BIGINT       NOT NULL REFERENCES lesson_plan_topics(topic_id) ON DELETE CASCADE,
    session_date           DATE         NOT NULL,
    session_time           TIME,
    notes                  TEXT,
    material_path          VARCHAR(500),
    mark_topic_completed   BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at             TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_lesson_session_logs_topic ON lesson_plan_session_logs(topic_id);

CREATE TABLE IF NOT EXISTS learning_materials (
    material_id          BIGSERIAL PRIMARY KEY,
    school_id             BIGINT       NOT NULL REFERENCES schools(id) ON DELETE CASCADE,
    teacher_id            BIGINT       NOT NULL REFERENCES faculty(faculty_pk) ON DELETE CASCADE,
    subject_id            BIGINT       NOT NULL REFERENCES subjects(subject_id) ON DELETE RESTRICT,
    class_name            VARCHAR(50)  NOT NULL,
    section               VARCHAR(10)  NOT NULL,
    topic_id              BIGINT       REFERENCES lesson_plan_topics(topic_id) ON DELETE SET NULL,
    title                 VARCHAR(300) NOT NULL,
    original_filename     VARCHAR(500),
    stored_path           VARCHAR(500) NOT NULL,
    file_size_bytes       BIGINT,
    mime_type             VARCHAR(100),
    visible_to_students   BOOLEAN      NOT NULL DEFAULT TRUE,
    due_date              DATE,
    created_at            TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_learning_materials_class
    ON learning_materials(school_id, class_name, section, subject_id);
CREATE INDEX IF NOT EXISTS idx_learning_materials_teacher ON learning_materials(teacher_id);
CREATE INDEX IF NOT EXISTS idx_learning_materials_visible ON learning_materials(visible_to_students);
