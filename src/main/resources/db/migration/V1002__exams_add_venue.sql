-- =============================================================================
-- PostgreSQL: add venue to exams (JPA: com.org.careerbuilder.models.Exam.venue)
-- Target DB: same as spring.datasource.url in application.properties
--   e.g. jdbc:postgresql://localhost:5432/admindb
--
-- Apply manually, for example:
--   psql -h localhost -U admin -d admindb -f src/main/resources/db/migration/V1002__exams_add_venue.sql
-- Or rely on spring.jpa.hibernate.ddl-auto=update in dev to sync the column.
-- =============================================================================

ALTER TABLE exams
    ADD COLUMN IF NOT EXISTS venue VARCHAR(200);

COMMENT ON COLUMN exams.venue IS 'Room, hall, or lab where the exam is held.';
