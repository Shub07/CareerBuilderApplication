-- Demo seed for Admin Exams module (matches Figma list + timetable)
DO $$
DECLARE
    v_school_id   BIGINT := 19;
    v_exam_active BIGINT;
    v_exam_draft  BIGINT;
    v_exam_done   BIGINT;
    v_exam_upcoming BIGINT;
    v_sub_math    BIGINT := 1;
    v_sub_english BIGINT := 2;
BEGIN
    -- First Quarter Assessment — ACTIVE
    INSERT INTO exams (
        exam_name, exam_date, end_date, exam_type, school_id, class_name, section,
        academic_year, status, deleted, description
    )
    SELECT 'First Quarter Assessment', DATE '2026-03-10', DATE '2026-03-15',
           'ASSESSMENT', v_school_id, 'Grade 10', 'A', '2025-2026', 'ACTIVE', FALSE,
           'Quarterly internal assessment for Grade 10'
    WHERE NOT EXISTS (
        SELECT 1 FROM exams e
        WHERE e.school_id = v_school_id AND e.exam_name = 'First Quarter Assessment' AND e.deleted = FALSE
    );

    -- Unit Test 1 — COMPLETED
    INSERT INTO exams (
        exam_name, exam_date, end_date, exam_type, school_id, class_name,
        academic_year, status, deleted
    )
    SELECT 'Unit Test 1', DATE '2026-01-20', DATE '2026-01-22',
           'UNIT_TEST', v_school_id, 'Grade 9', '2025-2026', 'COMPLETED', FALSE
    WHERE NOT EXISTS (
        SELECT 1 FROM exams e
        WHERE e.school_id = v_school_id AND e.exam_name = 'Unit Test 1' AND e.deleted = FALSE
    );

    -- Mid Term Examination 2026 — SCHEDULED
    INSERT INTO exams (
        exam_name, exam_date, end_date, exam_type, school_id, class_name,
        academic_year, status, deleted, instructions
    )
    SELECT 'Mid Term Examination 2026', DATE '2026-06-01', DATE '2026-06-15',
           'MID_TERM', v_school_id, 'Grade 11', '2025-2026', 'SCHEDULED', FALSE,
           'Bring hall ticket and school ID card.'
    WHERE NOT EXISTS (
        SELECT 1 FROM exams e
        WHERE e.school_id = v_school_id AND e.exam_name = 'Mid Term Examination 2026' AND e.deleted = FALSE
    );

    -- Final Mock Test — DRAFT
    INSERT INTO exams (
        exam_name, exam_date, end_date, exam_type, school_id, class_name,
        academic_year, status, deleted
    )
    SELECT 'Final Mock Test', DATE '2026-08-01', DATE '2026-08-10',
           'WEEKLY', v_school_id, 'Grade 10', '2025-2026', 'DRAFT', FALSE
    WHERE NOT EXISTS (
        SELECT 1 FROM exams e
        WHERE e.school_id = v_school_id AND e.exam_name = 'Final Mock Test' AND e.deleted = FALSE
    );

    SELECT exam_id INTO v_exam_active FROM exams
    WHERE school_id = v_school_id AND exam_name = 'First Quarter Assessment' AND deleted = FALSE LIMIT 1;

    SELECT exam_id INTO v_exam_done FROM exams
    WHERE school_id = v_school_id AND exam_name = 'Mid Term Examination 2026' AND deleted = FALSE LIMIT 1;

    -- Timetable for Mid Term (Figma detail screen)
    IF v_exam_done IS NOT NULL THEN
        INSERT INTO exam_schedules (exam_id, school_id, subject_id, scheduled_date, start_time, end_time, venue)
        SELECT v_exam_done, v_school_id, v_sub_math, DATE '2026-03-10', TIME '09:00', TIME '12:00', 'Examination Hall A'
        WHERE NOT EXISTS (
            SELECT 1 FROM exam_schedules s WHERE s.exam_id = v_exam_done AND s.subject_id = v_sub_math
        );

        INSERT INTO exam_schedules (exam_id, school_id, subject_id, scheduled_date, start_time, end_time, venue)
        SELECT v_exam_done, v_school_id, v_sub_english, DATE '2026-03-11', TIME '09:00', TIME '11:30', 'Room 102'
        WHERE NOT EXISTS (
            SELECT 1 FROM exam_schedules s WHERE s.exam_id = v_exam_done AND s.subject_id = v_sub_english
        );
    END IF;

    RAISE NOTICE 'Admin Exams seed complete for school %', v_school_id;
END $$;
