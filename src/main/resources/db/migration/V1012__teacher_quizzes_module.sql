-- Teacher Portal: Quizzes & Assessments (metadata, questions, student attempts, per-answer marks)

CREATE TABLE IF NOT EXISTS teacher_quizzes (
    quiz_id             BIGSERIAL PRIMARY KEY,
    school_id           BIGINT       NOT NULL REFERENCES schools(id) ON DELETE CASCADE,
    teacher_id          BIGINT       NOT NULL REFERENCES faculty(faculty_pk) ON DELETE CASCADE,
    subject_id          BIGINT       NOT NULL REFERENCES subjects(subject_id) ON DELETE RESTRICT,
    title               VARCHAR(300) NOT NULL,
    instructions        TEXT,
    class_name          VARCHAR(50)  NOT NULL,
    section             VARCHAR(10)  NOT NULL,
    time_limit_minutes  INT          NOT NULL DEFAULT 30,
    total_marks         INT          NOT NULL,
    scheduled_at        TIMESTAMP,
    shuffle_questions   BOOLEAN      NOT NULL DEFAULT FALSE,
    lifecycle_status    VARCHAR(20)  NOT NULL DEFAULT 'DRAFT',
    conducted_on        DATE,
    created_at          TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_teacher_quizzes_teacher ON teacher_quizzes(teacher_id);
CREATE INDEX IF NOT EXISTS idx_teacher_quizzes_school_class ON teacher_quizzes(school_id, class_name, section);
CREATE INDEX IF NOT EXISTS idx_teacher_quizzes_lifecycle ON teacher_quizzes(lifecycle_status);
CREATE INDEX IF NOT EXISTS idx_teacher_quizzes_conducted ON teacher_quizzes(conducted_on);

CREATE TABLE IF NOT EXISTS quiz_questions (
    question_id   BIGSERIAL PRIMARY KEY,
    quiz_id       BIGINT       NOT NULL REFERENCES teacher_quizzes(quiz_id) ON DELETE CASCADE,
    sort_order     INT          NOT NULL DEFAULT 0,
    question_type  VARCHAR(20)  NOT NULL,
    question_text  TEXT         NOT NULL,
    max_marks      DECIMAL(10, 2) NOT NULL,
    answer_key     TEXT
);

CREATE INDEX IF NOT EXISTS idx_quiz_questions_quiz ON quiz_questions(quiz_id);

CREATE TABLE IF NOT EXISTS quiz_question_options (
    option_id    BIGSERIAL PRIMARY KEY,
    question_id  BIGINT       NOT NULL REFERENCES quiz_questions(question_id) ON DELETE CASCADE,
    sort_order    INT          NOT NULL DEFAULT 0,
    option_text   VARCHAR(500) NOT NULL,
    correct       BOOLEAN      NOT NULL DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_quiz_question_options_q ON quiz_question_options(question_id);

CREATE TABLE IF NOT EXISTS quiz_submissions (
    submission_id   BIGSERIAL PRIMARY KEY,
    quiz_id         BIGINT       NOT NULL REFERENCES teacher_quizzes(quiz_id) ON DELETE CASCADE,
    student_id       BIGINT       NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    total_score      DECIMAL(10, 2),
    teacher_remark   TEXT,
    grade_status     VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    submitted_at     TIMESTAMP,
    created_at       TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_quiz_submission_student UNIQUE (quiz_id, student_id)
);

CREATE INDEX IF NOT EXISTS idx_quiz_submissions_quiz ON quiz_submissions(quiz_id);
CREATE INDEX IF NOT EXISTS idx_quiz_submissions_grade ON quiz_submissions(quiz_id, grade_status);

CREATE TABLE IF NOT EXISTS quiz_submission_answers (
    answer_id           BIGSERIAL PRIMARY KEY,
    submission_id       BIGINT       NOT NULL REFERENCES quiz_submissions(submission_id) ON DELETE CASCADE,
    question_id         BIGINT       NOT NULL REFERENCES quiz_questions(question_id) ON DELETE CASCADE,
    answer_text         TEXT,
    selected_option_id  BIGINT       REFERENCES quiz_question_options(option_id) ON DELETE SET NULL,
    marks_awarded       DECIMAL(10, 2),
    CONSTRAINT uk_submission_question UNIQUE (submission_id, question_id)
);

CREATE INDEX IF NOT EXISTS idx_quiz_submission_answers_sub ON quiz_submission_answers(submission_id);
