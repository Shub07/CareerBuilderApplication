-- Link admin accounts to a tenant school for JWT-scoped authorization.
ALTER TABLE app_users ADD COLUMN IF NOT EXISTS school_id BIGINT REFERENCES schools (id);

CREATE INDEX IF NOT EXISTS idx_app_users_school ON app_users (school_id);
