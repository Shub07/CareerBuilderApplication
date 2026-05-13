-- Teacher Daily Activity (Students) review state
-- Uses existing daily_life_activities as source of activity logs.

CREATE TABLE IF NOT EXISTS teacher_daily_activity_reviews (
    id BIGSERIAL PRIMARY KEY,
    faculty_id BIGINT NOT NULL REFERENCES faculty(faculty_pk) ON DELETE CASCADE,
    student_id BIGINT NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    activity_date DATE NOT NULL,
    status VARCHAR(40) NOT NULL,
    note VARCHAR(1000),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    CONSTRAINT uk_tdar_faculty_student_date UNIQUE (faculty_id, student_id, activity_date)
);

CREATE INDEX IF NOT EXISTS idx_tdar_faculty_date
    ON teacher_daily_activity_reviews (faculty_id, activity_date);

CREATE INDEX IF NOT EXISTS idx_tdar_student_date
    ON teacher_daily_activity_reviews (student_id, activity_date);
