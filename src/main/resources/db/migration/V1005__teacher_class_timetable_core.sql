-- Core tables used by teacher timetable APIs:
-- 1) class_subject_teachers: which teacher is assigned to class/section/subject
-- 2) class_schedule_slots: class-wise weekly timetable slots

CREATE TABLE IF NOT EXISTS class_subject_teachers (
    id BIGSERIAL PRIMARY KEY,
    school_id BIGINT NOT NULL REFERENCES schools(id) ON DELETE CASCADE,
    class_name VARCHAR(20) NOT NULL,
    section VARCHAR(5) NOT NULL,
    subject_id BIGINT NOT NULL REFERENCES subjects(subject_id) ON DELETE CASCADE,
    faculty_id BIGINT NOT NULL REFERENCES faculty(faculty_pk) ON DELETE CASCADE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_cst_school_class_section_subject UNIQUE (school_id, class_name, section, subject_id)
);

CREATE INDEX IF NOT EXISTS idx_cst_faculty_active
    ON class_subject_teachers (faculty_id, is_active);

CREATE INDEX IF NOT EXISTS idx_cst_school_class_section
    ON class_subject_teachers (school_id, class_name, section);

CREATE TABLE IF NOT EXISTS class_schedule_slots (
    id BIGSERIAL PRIMARY KEY,
    school_id BIGINT NOT NULL REFERENCES schools(id) ON DELETE CASCADE,
    class_name VARCHAR(20) NOT NULL,
    section VARCHAR(5) NOT NULL,
    subject_id BIGINT NOT NULL REFERENCES subjects(subject_id) ON DELETE CASCADE,
    day_of_week INTEGER NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    slot_type VARCHAR(20) NOT NULL,
    title VARCHAR(120),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT chk_css_day_of_week CHECK (day_of_week BETWEEN 1 AND 7),
    CONSTRAINT chk_css_time_window CHECK (start_time < end_time)
);

CREATE INDEX IF NOT EXISTS idx_css_schedule_lookup
    ON class_schedule_slots (school_id, class_name, section, day_of_week, start_time);

CREATE INDEX IF NOT EXISTS idx_css_subject_day
    ON class_schedule_slots (school_id, class_name, section, subject_id, day_of_week, is_active);
