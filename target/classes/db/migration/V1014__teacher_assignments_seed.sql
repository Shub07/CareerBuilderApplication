-- Demo assignments + submissions for teacher portal (faculty_pk = 1 by default in frontend).

DO $$
DECLARE
    v_faculty_id   BIGINT;
    v_school_id    BIGINT;
    v_class_name   VARCHAR(50);
    v_section      VARCHAR(10);
    v_subject_id   BIGINT;
    v_today        DATE := CURRENT_DATE;
    v_a1           BIGINT;
    v_a2           BIGINT;
    v_a3           BIGINT;
    v_a4           BIGINT;
    v_stu          RECORD;
    v_i            INT := 0;
BEGIN
    IF EXISTS (SELECT 1 FROM faculty WHERE faculty_pk = 1) THEN
        v_faculty_id := 1;
    ELSE
        SELECT cst.faculty_id
        INTO v_faculty_id
        FROM class_subject_teachers cst
        WHERE cst.is_active = TRUE
        ORDER BY cst.faculty_id
        LIMIT 1;
    END IF;

    IF v_faculty_id IS NULL THEN
        RETURN;
    END IF;

    SELECT school_id INTO v_school_id FROM faculty WHERE faculty_pk = v_faculty_id;
    IF v_school_id IS NULL THEN
        RETURN;
    END IF;

    SELECT cst.class_name, cst.section, cst.subject_id
    INTO v_class_name, v_section, v_subject_id
    FROM class_subject_teachers cst
    WHERE cst.faculty_id = v_faculty_id AND cst.is_active = TRUE
    ORDER BY cst.id
    LIMIT 1;

    IF v_class_name IS NULL THEN
        SELECT s.class_name, s.section
        INTO v_class_name, v_section
        FROM students s
        WHERE s.school_id = v_school_id
        GROUP BY s.class_name, s.section
        ORDER BY COUNT(*) DESC
        LIMIT 1;

        v_subject_id := COALESCE(
            (SELECT subject_id FROM subjects ORDER BY subject_id LIMIT 1),
            1
        );

        IF v_class_name IS NOT NULL THEN
            INSERT INTO class_subject_teachers (id, is_active, class_name, school_id, section, faculty_id, subject_id)
            SELECT COALESCE(MAX(id), 0) + 1, TRUE, v_class_name, v_school_id, v_section, v_faculty_id, v_subject_id
            FROM class_subject_teachers
            WHERE NOT EXISTS (
                SELECT 1 FROM class_subject_teachers c
                WHERE c.faculty_id = v_faculty_id
                  AND c.school_id = v_school_id
                  AND c.class_name = v_class_name
                  AND c.section = v_section
                  AND c.subject_id = v_subject_id
            );
        END IF;
    END IF;

    IF v_class_name IS NULL THEN
        RETURN;
    END IF;

    DELETE FROM assignment_submissions
    WHERE assignment_id IN (
        SELECT assignment_id FROM assignments WHERE title LIKE 'Teacher Portal Demo:%'
    );

    DELETE FROM assignments WHERE title LIKE 'Teacher Portal Demo:%';

    v_a1 := (SELECT COALESCE(MAX(assignment_id), 0) + 1 FROM assignments);
    v_a2 := v_a1 + 1;
    v_a3 := v_a1 + 2;
    v_a4 := v_a1 + 3;

    INSERT INTO assignments (
        assignment_id, title, description, subject_id, teacher_id, due_date, due_time,
        school_id, class_name, section, given_date, total_marks,
        allow_late_submission, allow_resubmission, publish_status, created_at, updated_at
    )
    VALUES
        (v_a1, 'Teacher Portal Demo: Algebra Problem Set',
         'Complete exercises 1–15 from chapter 4. Show all working.',
         v_subject_id, v_faculty_id, v_today + 7, TIME '23:59',
         v_school_id, v_class_name, v_section, v_today - 2, 50,
         TRUE, FALSE, 'PUBLISHED', NOW() - INTERVAL '2 days', NOW()),
        (v_a2, 'Teacher Portal Demo: Essay Draft',
         'Write a 500-word essay on climate change impacts in your region.',
         v_subject_id, v_faculty_id, v_today, TIME '17:00',
         v_school_id, v_class_name, v_section, v_today - 1, 50,
         FALSE, TRUE, 'PUBLISHED', NOW() - INTERVAL '1 day', NOW()),
        (v_a3, 'Teacher Portal Demo: Lab Report (Draft)',
         'Draft section only — not visible to students yet.',
         v_subject_id, v_faculty_id, v_today + 14, TIME '23:59',
         v_school_id, v_class_name, v_section, v_today, 40,
         TRUE, FALSE, 'DRAFT', NOW(), NOW()),
        (v_a4, 'Teacher Portal Demo: History Worksheet',
         'Closed assignment from last term for history tab.',
         v_subject_id, v_faculty_id, v_today - 14, TIME '23:59',
         v_school_id, v_class_name, v_section, v_today - 21, 30,
         FALSE, FALSE, 'CLOSED', NOW() - INTERVAL '21 days', NOW() - INTERVAL '14 days');

    FOR v_stu IN
        SELECT s.id
        FROM students s
        WHERE s.school_id = v_school_id
          AND s.class_name = v_class_name
          AND s.section = v_section
        ORDER BY s.roll_no NULLS LAST, s.id
        LIMIT 8
    LOOP
        v_i := v_i + 1;

        IF v_i <= 3 THEN
            INSERT INTO assignment_submissions (
                assignment_id, student_id, file_path, status, submitted_at,
                points_obtained, points_total, teacher_remarks
            )
            VALUES (
                v_a1, v_stu.id,
                'uploads/demo/submission_' || v_stu.id || '_algebra.pdf',
                CASE WHEN v_i = 1 THEN 'GRADED' ELSE 'SUBMITTED' END,
                NOW() - (v_i || ' days')::INTERVAL,
                CASE WHEN v_i = 1 THEN 42 ELSE NULL END,
                50,
                CASE WHEN v_i = 1 THEN 'Good work on ratios.' ELSE NULL END
            );
        END IF;

        IF v_i <= 2 THEN
            INSERT INTO assignment_submissions (
                assignment_id, student_id, file_path, status, submitted_at,
                points_obtained, points_total
            )
            VALUES (
                v_a2, v_stu.id,
                'uploads/demo/submission_' || v_stu.id || '_essay.pdf',
                CASE WHEN v_i = 2 THEN 'LATE' ELSE 'SUBMITTED' END,
                NOW() - INTERVAL '3 hours',
                NULL, 50
            );
        END IF;
    END LOOP;
END $$;
