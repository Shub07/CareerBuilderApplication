-- Teacher self attendance and leave module

CREATE TABLE IF NOT EXISTS teacher_attendance_entries (
    id BIGSERIAL PRIMARY KEY,
    faculty_id BIGINT NOT NULL REFERENCES faculty(faculty_pk) ON DELETE CASCADE,
    school_id BIGINT NOT NULL REFERENCES schools(id) ON DELETE CASCADE,
    work_date DATE NOT NULL,
    check_in_time TIME,
    check_out_time TIME,
    worked_minutes INTEGER,
    status VARCHAR(20) NOT NULL,
    late_note VARCHAR(500),
    work_summary VARCHAR(1000),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    CONSTRAINT uk_teacher_attendance_faculty_date UNIQUE (faculty_id, work_date)
);

CREATE INDEX IF NOT EXISTS idx_tae_faculty_date
    ON teacher_attendance_entries (faculty_id, work_date);

CREATE INDEX IF NOT EXISTS idx_tae_school_date
    ON teacher_attendance_entries (school_id, work_date);

CREATE TABLE IF NOT EXISTS teacher_leave_requests (
    id BIGSERIAL PRIMARY KEY,
    faculty_id BIGINT NOT NULL REFERENCES faculty(faculty_pk) ON DELETE CASCADE,
    school_id BIGINT NOT NULL REFERENCES schools(id) ON DELETE CASCADE,
    leave_type VARCHAR(20) NOT NULL,
    from_date DATE NOT NULL,
    to_date DATE NOT NULL,
    reason VARCHAR(1000),
    rejection_reason VARCHAR(1000),
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    CONSTRAINT chk_teacher_leave_dates CHECK (to_date >= from_date)
);

CREATE INDEX IF NOT EXISTS idx_tlr_faculty_from
    ON teacher_leave_requests (faculty_id, from_date);

CREATE INDEX IF NOT EXISTS idx_tlr_faculty_status
    ON teacher_leave_requests (faculty_id, status);

CREATE TABLE IF NOT EXISTS teacher_leave_balances (
    id BIGSERIAL PRIMARY KEY,
    faculty_id BIGINT NOT NULL REFERENCES faculty(faculty_pk) ON DELETE CASCADE,
    casual_total INTEGER NOT NULL DEFAULT 12,
    medical_total INTEGER NOT NULL DEFAULT 10,
    half_day_total INTEGER NOT NULL DEFAULT 6,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    CONSTRAINT uk_tlb_faculty UNIQUE (faculty_id)
);
