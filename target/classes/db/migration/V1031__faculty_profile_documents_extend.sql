ALTER TABLE faculty_profiles
    ADD COLUMN IF NOT EXISTS certificates_path VARCHAR(500) NULL,
    ADD COLUMN IF NOT EXISTS experience_letters_path VARCHAR(500) NULL;
