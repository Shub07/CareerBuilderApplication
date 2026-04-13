CREATE TABLE IF NOT EXISTS daily_life_activities (
    activity_id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL,
    activity_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    activity_type VARCHAR(40) NOT NULL,
    note VARCHAR(500),
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    duration_minutes INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_dla_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_dla_student_date ON daily_life_activities(student_id, activity_date);
CREATE INDEX IF NOT EXISTS idx_dla_student_type ON daily_life_activities(student_id, activity_type);
CREATE INDEX IF NOT EXISTS idx_dla_date_completed ON daily_life_activities(activity_date, completed);

CREATE TABLE IF NOT EXISTS daily_life_quick_checks (
    quick_check_id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL,
    check_date DATE NOT NULL,
    check_key VARCHAR(80) NOT NULL,
    label VARCHAR(150) NOT NULL,
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_dquick_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    CONSTRAINT uk_dquick_student_date_key UNIQUE (student_id, check_date, check_key)
);

CREATE INDEX IF NOT EXISTS idx_dquick_student_date ON daily_life_quick_checks(student_id, check_date);
CREATE INDEX IF NOT EXISTS idx_dquick_completed ON daily_life_quick_checks(completed);