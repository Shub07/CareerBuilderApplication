-- Teacher notices module for class announcements and student alerts.
-- Integrates with: faculty, students, class_subject_teachers, notices.

CREATE TABLE IF NOT EXISTS teacher_notices (
    id BIGSERIAL PRIMARY KEY,
    faculty_id BIGINT NOT NULL REFERENCES faculty(faculty_pk) ON DELETE CASCADE,
    school_id BIGINT NOT NULL REFERENCES schools(id) ON DELETE CASCADE,
    notice_type VARCHAR(40) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    title VARCHAR(200) NOT NULL,
    description VARCHAR(1000) NOT NULL,
    attachment_url VARCHAR(500),
    attachment_file_name VARCHAR(250),
    attachment_file_type VARCHAR(120),
    attachment_file_size BIGINT,
    public_notice_id BIGINT REFERENCES notices(notice_id) ON DELETE SET NULL,
    published_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_teacher_notices_faculty
    ON teacher_notices(faculty_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_teacher_notices_school
    ON teacher_notices(school_id);

CREATE INDEX IF NOT EXISTS idx_teacher_notices_type_status
    ON teacher_notices(notice_type, status);

CREATE TABLE IF NOT EXISTS teacher_notice_class_targets (
    id BIGSERIAL PRIMARY KEY,
    teacher_notice_id BIGINT NOT NULL REFERENCES teacher_notices(id) ON DELETE CASCADE,
    class_name VARCHAR(30) NOT NULL,
    section VARCHAR(10) NOT NULL,
    CONSTRAINT uk_tnct_notice_class_section UNIQUE (teacher_notice_id, class_name, section)
);

CREATE INDEX IF NOT EXISTS idx_tnct_notice
    ON teacher_notice_class_targets(teacher_notice_id);

CREATE TABLE IF NOT EXISTS teacher_notice_student_targets (
    id BIGSERIAL PRIMARY KEY,
    teacher_notice_id BIGINT NOT NULL REFERENCES teacher_notices(id) ON DELETE CASCADE,
    student_id BIGINT NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    CONSTRAINT uk_tnst_notice_student UNIQUE (teacher_notice_id, student_id)
);

CREATE INDEX IF NOT EXISTS idx_tnst_notice
    ON teacher_notice_student_targets(teacher_notice_id);

CREATE INDEX IF NOT EXISTS idx_tnst_student
    ON teacher_notice_student_targets(student_id);

-- Targeted audience for the existing `notices` feed:
-- if a notice has no rows here, it is visible to all students in the school.
-- if it has rows, only mapped students should see it.
CREATE TABLE IF NOT EXISTS notice_audience_students (
    id BIGSERIAL PRIMARY KEY,
    notice_id BIGINT NOT NULL REFERENCES notices(notice_id) ON DELETE CASCADE,
    student_id BIGINT NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    CONSTRAINT uk_notice_audience_student UNIQUE (notice_id, student_id)
);

CREATE INDEX IF NOT EXISTS idx_nas_notice
    ON notice_audience_students(notice_id);

CREATE INDEX IF NOT EXISTS idx_nas_student
    ON notice_audience_students(student_id);
