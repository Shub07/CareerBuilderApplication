ALTER TABLE student_profiles
    ADD COLUMN IF NOT EXISTS account_status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE';

CREATE INDEX IF NOT EXISTS idx_student_profile_account_status ON student_profiles(account_status);
