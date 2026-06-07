-- Demo quizzes for teacher portal (faculty id = 1, Grade 8 A, Mathematics).

DO $$
DECLARE
    v_teacher_id   BIGINT := 1;
    v_school_id    BIGINT;
    v_class_name   VARCHAR(50) := '8';
    v_section      VARCHAR(10) := 'A';
    v_subject_id   BIGINT := 1;
    v_today        DATE := CURRENT_DATE;
    v_qz_pub       BIGINT;
    v_qz_cond      BIGINT;
    v_qz_draft     BIGINT;
    v_qz_hist      BIGINT;
    v_qq1          BIGINT;
    v_qq2          BIGINT;
    v_qq3          BIGINT;
    v_qq4          BIGINT;
    v_opt1         BIGINT;
    v_sub1         BIGINT;
    v_sub2         BIGINT;
    v_stu          RECORD;
BEGIN
    SELECT school_id INTO v_school_id FROM faculty WHERE id = v_teacher_id;
    IF v_school_id IS NULL THEN
        RETURN;
    END IF;

    DELETE FROM quiz_submission_answers
    WHERE submission_id IN (
        SELECT submission_id FROM quiz_submissions
        WHERE quiz_id IN (SELECT quiz_id FROM teacher_quizzes WHERE title LIKE 'Teacher Portal Demo:%')
    );
    DELETE FROM quiz_submissions
    WHERE quiz_id IN (SELECT quiz_id FROM teacher_quizzes WHERE title LIKE 'Teacher Portal Demo:%');
    DELETE FROM quiz_question_options
    WHERE question_id IN (
        SELECT question_id FROM quiz_questions
        WHERE quiz_id IN (SELECT quiz_id FROM teacher_quizzes WHERE title LIKE 'Teacher Portal Demo:%')
    );
    DELETE FROM quiz_questions
    WHERE quiz_id IN (SELECT quiz_id FROM teacher_quizzes WHERE title LIKE 'Teacher Portal Demo:%');
    DELETE FROM teacher_quizzes WHERE title LIKE 'Teacher Portal Demo:%';

    v_qz_pub := (SELECT COALESCE(MAX(quiz_id), 0) + 1 FROM teacher_quizzes);
    v_qz_cond := v_qz_pub + 1;
    v_qz_draft := v_qz_pub + 2;
    v_qz_hist := v_qz_pub + 3;

    INSERT INTO teacher_quizzes (
        quiz_id, school_id, teacher_id, subject_id, title, instructions,
        class_name, section, time_limit_minutes, total_marks,
        scheduled_at, shuffle_questions, lifecycle_status, conducted_on, created_at, updated_at
    )
    VALUES
        (v_qz_pub, v_school_id, v_teacher_id, v_subject_id,
         'Teacher Portal Demo: Algebra Quick Check',
         'Answer all questions within the time limit.',
         v_class_name, v_section, 20, 20,
         (CURRENT_TIMESTAMP + INTERVAL '2 days'), FALSE, 'PUBLISHED', NULL, NOW() - INTERVAL '3 days', NOW()),
        (v_qz_cond, v_school_id, v_teacher_id, v_subject_id,
         'Teacher Portal Demo: Weekly Math Quiz',
         'Conducted assessment — review student answers.',
         v_class_name, v_section, 30, 20,
         NULL, FALSE, 'CONDUCTED', v_today, NOW() - INTERVAL '7 days', NOW()),
        (v_qz_draft, v_school_id, v_teacher_id, v_subject_id,
         'Teacher Portal Demo: Geometry Draft',
         'Draft quiz — not yet published.',
         v_class_name, v_section, 25, 15,
         NULL, TRUE, 'DRAFT', NULL, NOW(), NOW()),
        (v_qz_hist, v_school_id, v_teacher_id, v_subject_id,
         'Teacher Portal Demo: Term 1 Revision',
         'Archived quiz from previous month.',
         v_class_name, v_section, 30, 20,
         NULL, FALSE, 'CONDUCTED', (v_today - INTERVAL '45 days')::DATE, NOW() - INTERVAL '60 days', NOW());

    v_qq1 := (SELECT COALESCE(MAX(question_id), 0) + 1 FROM quiz_questions);
    v_qq2 := v_qq1 + 1;
    v_qq3 := v_qq1 + 2;
    v_qq4 := v_qq1 + 3;

    INSERT INTO quiz_questions (question_id, quiz_id, sort_order, question_type, question_text, max_marks, answer_key)
    VALUES
        (v_qq1, v_qz_pub, 1, 'MCQ', 'What is the value of 2³?', 10, NULL),
        (v_qq2, v_qz_pub, 2, 'SHORT_ANSWER', 'Define a prime number.', 10, 'A number divisible only by 1 and itself'),
        (v_qq3, v_qz_cond, 1, 'MCQ', 'Solve: 5x + 10 = 30. What is x?', 10, NULL),
        (v_qq4, v_qz_cond, 2, 'SHORT_ANSWER', 'Write the formula for area of a circle.', 10, 'πr²');

    INSERT INTO quiz_question_options (option_id, question_id, sort_order, option_text, correct)
    VALUES
        ((SELECT COALESCE(MAX(option_id), 0) + 1 FROM quiz_question_options), v_qq1, 1, '6', FALSE),
        ((SELECT COALESCE(MAX(option_id), 0) + 2 FROM quiz_question_options), v_qq1, 2, '8', TRUE),
        ((SELECT COALESCE(MAX(option_id), 0) + 3 FROM quiz_question_options), v_qq1, 3, '9', FALSE),
        ((SELECT COALESCE(MAX(option_id), 0) + 4 FROM quiz_question_options), v_qq1, 4, '12', FALSE),
        ((SELECT COALESCE(MAX(option_id), 0) + 5 FROM quiz_question_options), v_qq3, 1, '4', TRUE),
        ((SELECT COALESCE(MAX(option_id), 0) + 6 FROM quiz_question_options), v_qq3, 2, '2', FALSE),
        ((SELECT COALESCE(MAX(option_id), 0) + 7 FROM quiz_question_options), v_qq3, 3, '5', FALSE),
        ((SELECT COALESCE(MAX(option_id), 0) + 8 FROM quiz_question_options), v_qq3, 4, '6', FALSE);

    SELECT option_id INTO v_opt1 FROM quiz_question_options WHERE question_id = v_qq3 AND correct = TRUE LIMIT 1;

    v_sub1 := (SELECT COALESCE(MAX(submission_id), 0) + 1 FROM quiz_submissions);
    v_sub2 := v_sub1 + 1;

    FOR v_stu IN
        SELECT id FROM students
        WHERE school_id = v_school_id AND class_name = v_class_name AND section = v_section
        ORDER BY id LIMIT 2
    LOOP
        IF v_stu.id = (SELECT MIN(id) FROM students WHERE school_id = v_school_id AND class_name = v_class_name AND section = v_section) THEN
            INSERT INTO quiz_submissions (submission_id, quiz_id, student_id, total_score, teacher_remark, grade_status, submitted_at, created_at)
            VALUES (v_sub1, v_qz_cond, v_stu.id, 16, 'Good effort on short answer.', 'CHECKED', NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days');

            INSERT INTO quiz_submission_answers (answer_id, submission_id, question_id, answer_text, selected_option_id, marks_awarded)
            VALUES
                ((SELECT COALESCE(MAX(answer_id), 0) + 1 FROM quiz_submission_answers), v_sub1, v_qq3, NULL, v_opt1, 10),
                ((SELECT COALESCE(MAX(answer_id), 0) + 2 FROM quiz_submission_answers), v_sub1, v_qq4, 'pi * r * r', NULL, 6);
        ELSE
            INSERT INTO quiz_submissions (submission_id, quiz_id, student_id, total_score, teacher_remark, grade_status, submitted_at, created_at)
            VALUES (v_sub2, v_qz_cond, v_stu.id, NULL, NULL, 'PENDING', NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day');

            INSERT INTO quiz_submission_answers (answer_id, submission_id, question_id, answer_text, selected_option_id, marks_awarded)
            VALUES
                ((SELECT COALESCE(MAX(answer_id), 0) + 1 FROM quiz_submission_answers), v_sub2, v_qq3, NULL, v_opt1, NULL),
                ((SELECT COALESCE(MAX(answer_id), 0) + 2 FROM quiz_submission_answers), v_sub2, v_qq4, '2*pi*r', NULL, NULL);
        END IF;
    END LOOP;
END $$;
