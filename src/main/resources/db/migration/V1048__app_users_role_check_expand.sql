-- Allow SCHOOL_ADMIN and PARENT roles in app_users (legacy check only had STUDENT, TEACHER, ADMIN).
ALTER TABLE app_users DROP CONSTRAINT IF EXISTS app_users_role_check;

ALTER TABLE app_users
    ADD CONSTRAINT app_users_role_check
        CHECK (role IN ('STUDENT', 'PARENT', 'TEACHER', 'SCHOOL_ADMIN', 'ADMIN'));
