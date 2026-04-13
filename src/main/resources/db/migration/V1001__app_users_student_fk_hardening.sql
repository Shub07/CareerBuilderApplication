-- ============================================================
--  Auth FK Hardening: app_users -> students
--  V1001__app_users_student_fk_hardening.sql
-- ============================================================

ALTER TABLE app_users
  ADD COLUMN IF NOT EXISTS student_id BIGINT;

DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM information_schema.table_constraints
    WHERE constraint_name = 'fk_app_users_student'
      AND table_name = 'app_users'
  ) THEN
    ALTER TABLE app_users
      ADD CONSTRAINT fk_app_users_student
      FOREIGN KEY (student_id) REFERENCES students(id)
      ON DELETE SET NULL;
  END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_app_users_student_id
  ON app_users(student_id);

-- Enforce one login account per linked student profile
CREATE UNIQUE INDEX IF NOT EXISTS uk_app_users_student_id_not_null
  ON app_users(student_id)
  WHERE student_id IS NOT NULL;
