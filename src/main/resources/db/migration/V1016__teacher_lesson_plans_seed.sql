-- Demo lesson plan topics, session logs, and learning materials for faculty 1
-- across two classes: Grade 8 A (Mathematics) and Grade 9 B (English).

DO $$
DECLARE
    v_teacher_id   BIGINT := 1;
    v_school_id    BIGINT;
    v_class8       VARCHAR(50) := '8';
    v_section8     VARCHAR(10) := 'A';
    v_sub_math     BIGINT := 1;
    v_class9       VARCHAR(50) := '9';
    v_section9     VARCHAR(10) := 'B';
    v_sub_english  BIGINT := 2;
    v_t8_1         BIGINT;
    v_t8_2         BIGINT;
    v_t8_3         BIGINT;
    v_t9_1         BIGINT;
    v_t9_2         BIGINT;
    v_t9_3         BIGINT;
BEGIN
    SELECT school_id INTO v_school_id FROM faculty WHERE faculty_pk = v_teacher_id;
    IF v_school_id IS NULL THEN
        RETURN;
    END IF;

    INSERT INTO class_subject_teachers (id, is_active, class_name, school_id, section, faculty_id, subject_id)
    SELECT COALESCE(MAX(id), 0) + 1, TRUE, v_class9, v_school_id, v_section9, v_teacher_id, v_sub_english
    FROM class_subject_teachers
    WHERE NOT EXISTS (
        SELECT 1 FROM class_subject_teachers c
        WHERE c.faculty_id = v_teacher_id
          AND c.school_id = v_school_id
          AND c.class_name = v_class9
          AND c.section = v_section9
          AND c.subject_id = v_sub_english
    );

    DELETE FROM lesson_plan_session_logs
    WHERE topic_id IN (
        SELECT topic_id FROM lesson_plan_topics WHERE title LIKE 'Teacher Portal Demo:%'
    );
    DELETE FROM learning_materials WHERE title LIKE 'Teacher Portal Demo:%';
    DELETE FROM lesson_plan_topics WHERE title LIKE 'Teacher Portal Demo:%';

    v_t8_1 := (SELECT COALESCE(MAX(topic_id), 0) + 1 FROM lesson_plan_topics);
    v_t8_2 := v_t8_1 + 1;
    v_t8_3 := v_t8_1 + 2;
    v_t9_1 := v_t8_1 + 3;
    v_t9_2 := v_t8_1 + 4;
    v_t9_3 := v_t8_1 + 5;

    INSERT INTO lesson_plan_topics (
        topic_id, school_id, teacher_id, subject_id, class_name, section,
        title, description, sort_order, status, last_taught_date, created_at, updated_at
    )
    VALUES
        (v_t8_1, v_school_id, v_teacher_id, v_sub_math, v_class8, v_section8,
         'Teacher Portal Demo: Introduction to Algebra',
         'Variables, constants, and simple expressions.',
         1, 'COMPLETED', CURRENT_DATE - 5, NOW() - INTERVAL '10 days', NOW()),
        (v_t8_2, v_school_id, v_teacher_id, v_sub_math, v_class8, v_section8,
         'Teacher Portal Demo: Linear Equations',
         'Solving one-variable linear equations.',
         2, 'IN_PROGRESS', CURRENT_DATE - 2, NOW() - INTERVAL '8 days', NOW()),
        (v_t8_3, v_school_id, v_teacher_id, v_sub_math, v_class8, v_section8,
         'Teacher Portal Demo: Quadratic Expressions',
         'Factoring and expanding quadratic expressions.',
         3, 'PENDING', NULL, NOW() - INTERVAL '5 days', NOW()),
        (v_t9_1, v_school_id, v_teacher_id, v_sub_english, v_class9, v_section9,
         'Teacher Portal Demo: Essay Writing Basics',
         'Structure of introduction, body, and conclusion.',
         1, 'COMPLETED', CURRENT_DATE - 4, NOW() - INTERVAL '9 days', NOW()),
        (v_t9_2, v_school_id, v_teacher_id, v_sub_english, v_class9, v_section9,
         'Teacher Portal Demo: Grammar & Punctuation',
         'Commas, semicolons, and subject-verb agreement.',
         2, 'IN_PROGRESS', CURRENT_DATE - 1, NOW() - INTERVAL '6 days', NOW()),
        (v_t9_3, v_school_id, v_teacher_id, v_sub_english, v_class9, v_section9,
         'Teacher Portal Demo: Poetry Appreciation',
         'Rhyme, meter, and figurative language.',
         3, 'PENDING', NULL, NOW() - INTERVAL '4 days', NOW());

    INSERT INTO lesson_plan_session_logs (
        log_id, topic_id, session_date, session_time, notes, material_path, mark_topic_completed, created_at
    )
    VALUES
        ((SELECT COALESCE(MAX(log_id), 0) + 1 FROM lesson_plan_session_logs), v_t8_1, CURRENT_DATE - 5, TIME '09:30',
         'Covered variables and simple expressions with board examples.', NULL, TRUE, NOW() - INTERVAL '5 days'),
        ((SELECT COALESCE(MAX(log_id), 0) + 2 FROM lesson_plan_session_logs), v_t8_1, CURRENT_DATE - 3, TIME '10:00',
         'Practice worksheet on evaluating expressions.', 'uploads/demo/lesson8a-algebra-notes.pdf', FALSE, NOW() - INTERVAL '3 days'),
        ((SELECT COALESCE(MAX(log_id), 0) + 3 FROM lesson_plan_session_logs), v_t8_2, CURRENT_DATE - 2, TIME '11:15',
         'Introduced balance method for linear equations.', NULL, FALSE, NOW() - INTERVAL '2 days'),
        ((SELECT COALESCE(MAX(log_id), 0) + 4 FROM lesson_plan_session_logs), v_t9_1, CURRENT_DATE - 4, TIME '09:00',
         'Modeled five-paragraph essay structure.', NULL, TRUE, NOW() - INTERVAL '4 days'),
        ((SELECT COALESCE(MAX(log_id), 0) + 5 FROM lesson_plan_session_logs), v_t9_2, CURRENT_DATE - 1, TIME '10:30',
         'Comma rules and peer editing activity.', NULL, FALSE, NOW() - INTERVAL '1 day');

    INSERT INTO learning_materials (
        material_id, school_id, teacher_id, subject_id, class_name, section, topic_id,
        title, original_filename, stored_path, file_size_bytes, mime_type,
        visible_to_students, due_date, created_at, updated_at
    )
    VALUES
        ((SELECT COALESCE(MAX(material_id), 0) + 1 FROM learning_materials), v_school_id, v_teacher_id, v_sub_math, v_class8, v_section8, v_t8_2,
         'Teacher Portal Demo: Linear Equations Worksheet', 'linear-equations-8a.pdf',
         'uploads/demo/lesson-linear-equations-8a.pdf', 245760, 'application/pdf', TRUE, CURRENT_DATE + 7, NOW() - INTERVAL '2 days', NOW()),
        ((SELECT COALESCE(MAX(material_id), 0) + 2 FROM learning_materials), v_school_id, v_teacher_id, v_sub_math, v_class8, v_section8, v_t8_3,
         'Teacher Portal Demo: Quadratic Intro Slides', 'quadratic-intro-8a.pptx',
         'uploads/demo/lesson-quadratic-intro-8a.pptx', 512000, 'application/vnd.openxmlformats-officedocument.presentationml.presentation', TRUE, NULL, NOW() - INTERVAL '1 day', NOW()),
        ((SELECT COALESCE(MAX(material_id), 0) + 3 FROM learning_materials), v_school_id, v_teacher_id, v_sub_english, v_class9, v_section9, v_t9_1,
         'Teacher Portal Demo: Essay Writing Guide', 'essay-guide-9b.pdf',
         'uploads/demo/lesson-essay-guide-9b.pdf', 198400, 'application/pdf', TRUE, CURRENT_DATE + 5, NOW() - INTERVAL '3 days', NOW()),
        ((SELECT COALESCE(MAX(material_id), 0) + 4 FROM learning_materials), v_school_id, v_teacher_id, v_sub_english, v_class9, v_section9, v_t9_2,
         'Teacher Portal Demo: Grammar Cheat Sheet', 'grammar-cheatsheet-9b.pdf',
         'uploads/demo/lesson-grammar-9b.pdf', 156000, 'application/pdf', TRUE, NULL, NOW() - INTERVAL '1 day', NOW());
END $$;
