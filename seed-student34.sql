DO $$
DECLARE
    v_student_id BIGINT := 34;
    v_school_id BIGINT := 19;
    v_class_name VARCHAR := '8';
    v_section VARCHAR := 'A';
    v_today DATE := CURRENT_DATE;
    v_day_of_week INTEGER := EXTRACT(ISODOW FROM CURRENT_DATE);
    v_teacher_math BIGINT;
    v_teacher_english BIGINT;
    v_teacher_science BIGINT;
    v_faculty_math BIGINT;
    v_faculty_english BIGINT;
    v_faculty_science BIGINT;
    v_assessment_completed_id BIGINT;
    v_assessment_progress_id BIGINT;
    v_progress_completed_id BIGINT;
    v_progress_in_progress_id BIGINT;
    v_fee_id BIGINT;
    v_attendance_id BIGINT;
    v_leave_id BIGINT;
    v_activity_id BIGINT;
    v_quick_check_id BIGINT;
    v_notice_id BIGINT;
    v_vacation_id BIGINT;
BEGIN
    IF NOT EXISTS (SELECT 1 FROM teachers WHERE teacher_email = 'seed.math19@invitto.local') THEN
        INSERT INTO teachers (teacher_id, teacher_email, teacher_name, teacher_phone)
        VALUES ((SELECT COALESCE(MAX(teacher_id), 0) + 1 FROM teachers), 'seed.math19@invitto.local', 'Aarav Mehta', '9000000101');
    END IF;

    IF NOT EXISTS (SELECT 1 FROM teachers WHERE teacher_email = 'seed.english19@invitto.local') THEN
        INSERT INTO teachers (teacher_id, teacher_email, teacher_name, teacher_phone)
        VALUES ((SELECT COALESCE(MAX(teacher_id), 0) + 1 FROM teachers), 'seed.english19@invitto.local', 'Riya Sharma', '9000000102');
    END IF;

    IF NOT EXISTS (SELECT 1 FROM teachers WHERE teacher_email = 'seed.science19@invitto.local') THEN
        INSERT INTO teachers (teacher_id, teacher_email, teacher_name, teacher_phone)
        VALUES ((SELECT COALESCE(MAX(teacher_id), 0) + 1 FROM teachers), 'seed.science19@invitto.local', 'Kabir Nanda', '9000000103');
    END IF;

    SELECT teacher_id INTO v_teacher_math FROM teachers WHERE teacher_email = 'seed.math19@invitto.local';
    SELECT teacher_id INTO v_teacher_english FROM teachers WHERE teacher_email = 'seed.english19@invitto.local';
    SELECT teacher_id INTO v_teacher_science FROM teachers WHERE teacher_email = 'seed.science19@invitto.local';

    IF NOT EXISTS (SELECT 1 FROM faculty WHERE email = 'seed.math19@invitto.local') THEN
        INSERT INTO faculty (
            faculty_id,
            first_name,
            last_name,
            gender,
            age,
            subject,
            qualification,
            experience,
            phone,
            email,
            address,
            school_id,
            school_name,
            experience_years,
            faculty_code,
            subject_id
        )
        VALUES (
            'FAC-IND-MATH-19',
            'Aarav',
            'Mehta',
            'Male',
            32,
            'Mathematics',
            'M.Sc Mathematics, B.Ed',
            8,
            '9000000101',
            'seed.math19@invitto.local',
            'Independent Learners Faculty Desk',
            v_school_id,
            'Independent Learners',
            8,
            'FAC-IND-MATH-19',
            1
        );
    END IF;

    IF NOT EXISTS (SELECT 1 FROM faculty WHERE email = 'seed.english19@invitto.local') THEN
        INSERT INTO faculty (
            faculty_id,
            first_name,
            last_name,
            gender,
            age,
            subject,
            qualification,
            experience,
            phone,
            email,
            address,
            school_id,
            school_name,
            experience_years,
            faculty_code,
            subject_id
        )
        VALUES (
            'FAC-IND-ENG-19',
            'Riya',
            'Sharma',
            'Female',
            29,
            'English',
            'M.A English, B.Ed',
            6,
            '9000000102',
            'seed.english19@invitto.local',
            'Independent Learners Faculty Desk',
            v_school_id,
            'Independent Learners',
            6,
            'FAC-IND-ENG-19',
            2
        );
    END IF;

    IF NOT EXISTS (SELECT 1 FROM faculty WHERE email = 'seed.science19@invitto.local') THEN
        INSERT INTO faculty (
            faculty_id,
            first_name,
            last_name,
            gender,
            age,
            subject,
            qualification,
            experience,
            phone,
            email,
            address,
            school_id,
            school_name,
            experience_years,
            faculty_code,
            subject_id
        )
        VALUES (
            'FAC-IND-SCI-19',
            'Kabir',
            'Nanda',
            'Male',
            35,
            'Science',
            'M.Sc Physics, B.Ed',
            10,
            '9000000103',
            'seed.science19@invitto.local',
            'Independent Learners Faculty Desk',
            v_school_id,
            'Independent Learners',
            10,
            'FAC-IND-SCI-19',
            3
        );
    END IF;

    SELECT id INTO v_faculty_math FROM faculty WHERE email = 'seed.math19@invitto.local';
    SELECT id INTO v_faculty_english FROM faculty WHERE email = 'seed.english19@invitto.local';
    SELECT id INTO v_faculty_science FROM faculty WHERE email = 'seed.science19@invitto.local';

    DELETE FROM my_classes WHERE student_id = v_student_id;

    INSERT INTO my_classes (student_id, subject_id, class_name, section)
    VALUES
        (v_student_id, 1, v_class_name, v_section),
        (v_student_id, 2, v_class_name, v_section),
        (v_student_id, 3, v_class_name, v_section),
        (v_student_id, 4, v_class_name, v_section),
        (v_student_id, 5, v_class_name, v_section);

    DELETE FROM class_subject_teachers
    WHERE school_id = v_school_id
      AND class_name = v_class_name
      AND section = v_section
      AND faculty_id IN (v_faculty_math, v_faculty_english, v_faculty_science);

    INSERT INTO class_subject_teachers (id, is_active, class_name, school_id, section, faculty_id, subject_id)
    VALUES
        ((SELECT COALESCE(MAX(id), 0) + 1 FROM class_subject_teachers), TRUE, v_class_name, v_school_id, v_section, v_faculty_math, 1),
        ((SELECT COALESCE(MAX(id), 0) + 2 FROM class_subject_teachers), TRUE, v_class_name, v_school_id, v_section, v_faculty_english, 2),
        ((SELECT COALESCE(MAX(id), 0) + 3 FROM class_subject_teachers), TRUE, v_class_name, v_school_id, v_section, v_faculty_science, 3);

    DELETE FROM class_schedule_slots
    WHERE school_id = v_school_id
      AND class_name = v_class_name
      AND section = v_section
      AND title LIKE 'Seed:%Shubham%';

    INSERT INTO class_schedule_slots (id, is_active, class_name, day_of_week, end_time, school_id, section, slot_type, start_time, title, subject_id)
    VALUES
        ((SELECT COALESCE(MAX(id), 0) + 1 FROM class_schedule_slots), TRUE, v_class_name, v_day_of_week, TIME '09:45', v_school_id, v_section, 'CLASS', TIME '09:00', 'Seed: Shubham Mathematics', 1),
        ((SELECT COALESCE(MAX(id), 0) + 2 FROM class_schedule_slots), TRUE, v_class_name, v_day_of_week, TIME '10:45', v_school_id, v_section, 'CLASS', TIME '10:00', 'Seed: Shubham English', 2),
        ((SELECT COALESCE(MAX(id), 0) + 3 FROM class_schedule_slots), TRUE, v_class_name, v_day_of_week, TIME '12:00', v_school_id, v_section, 'SPECIAL', TIME '11:15', 'Seed: Shubham Science Lab', 3);

    DELETE FROM class_sessions
    WHERE class_name = v_class_name
      AND section = v_section
      AND topic LIKE 'Seed:%Shubham%';

    INSERT INTO class_sessions (session_id, class_name, end_time, enrolled_count, section, session_date, session_type, start_time, status, topic, subject_id, teacher_id)
    VALUES
        ((SELECT COALESCE(MAX(session_id), 0) + 1 FROM class_sessions), v_class_name, TIME '09:45', 18, v_section, v_today, 'REGULAR', TIME '09:00', 'COMPLETED', 'Seed: Shubham Mathematics Revision', 1, v_teacher_math),
        ((SELECT COALESCE(MAX(session_id), 0) + 2 FROM class_sessions), v_class_name, TIME '10:45', 18, v_section, v_today, 'REGULAR', TIME '10:00', 'SCHEDULED', 'Seed: Shubham Grammar Workshop', 2, v_teacher_english),
        ((SELECT COALESCE(MAX(session_id), 0) + 3 FROM class_sessions), v_class_name, TIME '12:00', 18, v_section, v_today, 'SPECIAL', TIME '11:15', 'SCHEDULED', 'Seed: Shubham Science Practice', 3, v_teacher_science);

    DELETE FROM study_materials WHERE title LIKE 'Seed:%Shubham%';

    INSERT INTO study_materials (material_id, file_path, material_type, pages, title, uploaded_at, uploaded_by, subject_id)
    VALUES
        ((SELECT COALESCE(MAX(material_id), 0) + 1 FROM study_materials), 'C:/Users/Admin/Downloads/career-builder-backend-public-apis/career-builder-backend-main/seed-materials/student34-math-notes.txt', 'DOC', 14, 'Seed: Shubham Maths Notes', NOW() - INTERVAL '2 days', 'Aarav Mehta', 1),
        ((SELECT COALESCE(MAX(material_id), 0) + 2 FROM study_materials), 'C:/Users/Admin/Downloads/career-builder-backend-public-apis/career-builder-backend-main/seed-materials/student34-english-grammar.txt', 'DOC', 11, 'Seed: Shubham English Grammar', NOW() - INTERVAL '1 day', 'Riya Sharma', 2),
        ((SELECT COALESCE(MAX(material_id), 0) + 3 FROM study_materials), 'C:/Users/Admin/Downloads/career-builder-backend-public-apis/career-builder-backend-main/seed-materials/student34-science-lab.txt', 'DOC', 9, 'Seed: Shubham Science Lab Sheet', NOW(), 'Kabir Nanda', 3);

    DELETE FROM assignments WHERE title LIKE 'Seed:%Shubham%';

    INSERT INTO assignments (assignment_id, title, description, subject_id, teacher_id, due_date, created_at, updated_at)
    VALUES
        ((SELECT COALESCE(MAX(assignment_id), 0) + 1 FROM assignments), 'Seed: Shubham Algebra Practice', 'Seed: Shubham assignment on algebra worksheets and ratio problems.', 1, v_faculty_math, v_today + 5, NOW() - INTERVAL '1 day', NOW()),
        ((SELECT COALESCE(MAX(assignment_id), 0) + 2 FROM assignments), 'Seed: Shubham Essay Writing', 'Seed: Shubham assignment on descriptive essay writing and grammar corrections.', 2, v_faculty_english, v_today + 7, NOW() - INTERVAL '1 day', NOW()),
        ((SELECT COALESCE(MAX(assignment_id), 0) + 3 FROM assignments), 'Seed: Shubham Science Observation', 'Seed: Shubham assignment to record three home science experiments.', 3, v_faculty_science, v_today + 3, NOW() - INTERVAL '2 days', NOW());

    DELETE FROM fees WHERE student_id = v_student_id AND description LIKE 'Seed:%Shubham%';
    v_fee_id := COALESCE((SELECT MAX(id) FROM fees), 0) + 1;

    INSERT INTO fees (id, due_date, fee_type, status, student_id, term, total_amount, academic_year, amount, created_at, description, due_reminder_sent, overdue_reminder_sent, paid_amount, updated_at)
    VALUES
        (v_fee_id, v_today + 10, 'TUITION', 'PENDING', v_student_id, 'Term 1', 15000, '2026-27', 15000, NOW() - INTERVAL '5 days', 'Seed: Shubham tuition instalment', FALSE, FALSE, 0, NOW()),
        (v_fee_id + 1, v_today - 4, 'LIBRARY', 'OVERDUE', v_student_id, 'Term 1', 1200, '2026-27', 1200, NOW() - INTERVAL '10 days', 'Seed: Shubham library dues', TRUE, TRUE, 0, NOW()),
        (v_fee_id + 2, v_today - 15, 'EXAMINATION', 'PAID', v_student_id, 'Term 1', 2500, '2026-27', 2500, NOW() - INTERVAL '20 days', 'Seed: Shubham examination fee', TRUE, FALSE, 2500, NOW());

    DELETE FROM attendance_records WHERE student_id = v_student_id AND remarks LIKE 'Seed:%Shubham%';
    v_attendance_id := COALESCE((SELECT MAX(attendance_id) FROM attendance_records), 0) + 1;

    INSERT INTO attendance_records (attendance_id, att_date, status, student_id, class_name, section, remarks, created_at, updated_at)
    VALUES
        (v_attendance_id, v_today - 4, 'PRESENT', v_student_id, v_class_name, v_section, 'Seed: Shubham attended maths class', NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days'),
        (v_attendance_id + 1, v_today - 3, 'PRESENT', v_student_id, v_class_name, v_section, 'Seed: Shubham attended english class', NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days'),
        (v_attendance_id + 2, v_today - 2, 'ABSENT', v_student_id, v_class_name, v_section, 'Seed: Shubham was absent for science class', NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days'),
        (v_attendance_id + 3, v_today - 1, 'LEAVE', v_student_id, v_class_name, v_section, 'Seed: Shubham approved medical leave', NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day'),
        (v_attendance_id + 4, v_today, 'PRESENT', v_student_id, v_class_name, v_section, 'Seed: Shubham marked present today', NOW(), NOW());

    DELETE FROM leave_requests WHERE student_id = v_student_id AND reason LIKE 'Seed:%Shubham%';
    v_leave_id := COALESCE((SELECT MAX(leave_id) FROM leave_requests), 0) + 1;

    INSERT INTO leave_requests (leave_id, from_date, status, to_date, student_id, approved_by, approved_on, created_at, half_day_period, is_half_day, leave_type, reason, rejection_reason, updated_at)
    VALUES
        (v_leave_id, v_today + 2, 'APPLIED', v_today + 3, v_student_id, NULL, NULL, NOW() - INTERVAL '2 hours', NULL, FALSE, 'SICK', 'Seed: Shubham requested leave for fever recovery', NULL, NOW()),
        (v_leave_id + 1, v_today - 6, 'APPROVED', v_today - 5, v_student_id, 'School Admin', NOW() - INTERVAL '5 days', NOW() - INTERVAL '6 days', 'FIRST_HALF', TRUE, 'PERSONAL', 'Seed: Shubham family function half-day leave', NULL, NOW() - INTERVAL '5 days');

    DELETE FROM daily_life_activities WHERE student_id = v_student_id AND note LIKE 'Seed:%Shubham%';
    v_activity_id := COALESCE((SELECT MAX(activity_id) FROM daily_life_activities), 0) + 1;

    INSERT INTO daily_life_activities (activity_id, activity_date, activity_type, completed, created_at, duration_minutes, end_time, note, start_time, updated_at, student_id)
    VALUES
        (v_activity_id, v_today, 'SCHOOL_TIME', TRUE, NOW() - INTERVAL '8 hours', 180, TIME '11:30', 'Seed: Shubham completed core school sessions', TIME '08:30', NOW() - INTERVAL '5 hours', v_student_id),
        (v_activity_id + 1, v_today, 'STUDY_HOMEWORK', TRUE, NOW() - INTERVAL '4 hours', 90, TIME '17:30', 'Seed: Shubham revised algebra homework', TIME '16:00', NOW() - INTERVAL '2 hours', v_student_id),
        (v_activity_id + 2, v_today, 'PHYSICAL_ACTIVITY', FALSE, NOW() - INTERVAL '2 hours', 45, TIME '19:00', 'Seed: Shubham planned evening cycling', TIME '18:15', NOW() - INTERVAL '1 hour', v_student_id),
        (v_activity_id + 3, v_today, 'REST_TIME', TRUE, NOW() - INTERVAL '10 hours', 60, TIME '14:00', 'Seed: Shubham afternoon rest block', TIME '13:00', NOW() - INTERVAL '9 hours', v_student_id);

    DELETE FROM daily_life_quick_checks WHERE student_id = v_student_id AND label LIKE 'Seed:%Shubham%';
    v_quick_check_id := COALESCE((SELECT MAX(quick_check_id) FROM daily_life_quick_checks), 0) + 1;

    INSERT INTO daily_life_quick_checks (quick_check_id, check_date, check_key, completed, created_at, label, updated_at, student_id)
    VALUES
        (v_quick_check_id, v_today, 'water_intake', TRUE, NOW() - INTERVAL '6 hours', 'Seed: Shubham drank enough water', NOW() - INTERVAL '2 hours', v_student_id),
        (v_quick_check_id + 1, v_today, 'homework_done', TRUE, NOW() - INTERVAL '5 hours', 'Seed: Shubham completed homework', NOW() - INTERVAL '2 hours', v_student_id),
        (v_quick_check_id + 2, v_today, 'sleep_goal', FALSE, NOW() - INTERVAL '5 hours', 'Seed: Shubham met sleep goal', NOW() - INTERVAL '2 hours', v_student_id);

    DELETE FROM notices WHERE school_id = v_school_id AND source = 'SEED_STUDENT_34';
    v_notice_id := COALESCE((SELECT MAX(notice_id) FROM notices), 0) + 1;

    INSERT INTO notices (notice_id, body, created_at, school_id, title, category, description, is_pinned, source, updated_at)
    VALUES
        (v_notice_id, 'Mathematics revision workshop scheduled this Friday for Class 8A.', NOW() - INTERVAL '1 day', v_school_id, 'Seed: Maths Workshop', 'ACADEMIC', 'Seed notice for Shubham student experience.', TRUE, 'SEED_STUDENT_34', NOW()),
        (v_notice_id + 1, 'Fee reminder for pending tuition and library dues.', NOW() - INTERVAL '2 days', v_school_id, 'Seed: Fee Reminder', 'GENERAL', 'Seed notice for pending fee items.', FALSE, 'SEED_STUDENT_34', NOW()),
        (v_notice_id + 2, 'Science practice session opens tomorrow at 11:15 AM.', NOW() - INTERVAL '3 hours', v_school_id, 'Seed: Science Session', 'EVENTS', 'Seed notice for upcoming science class.', FALSE, 'SEED_STUDENT_34', NOW());

    DELETE FROM vacations WHERE school_id = v_school_id AND created_by = 'seed-student-34';
    v_vacation_id := COALESCE((SELECT MAX(vacation_id) FROM vacations), 0) + 1;

    INSERT INTO vacations (vacation_id, school_id, vacation_name, vacation_type, start_date, end_date, description, is_active, notice_sent, notice_sent_date, created_by, updated_by, created_at, updated_at)
    VALUES
        (v_vacation_id, v_school_id, 'Seed: Autumn Break', 'AUTUMN', v_today + 15, v_today + 18, 'Seed vacation for Independent Learners students.', TRUE, TRUE, NOW() - INTERVAL '1 day', 'seed-student-34', 'seed-student-34', NOW() - INTERVAL '2 days', NOW()),
        (v_vacation_id + 1, v_school_id, 'Seed: Winter Prep Break', 'WINTER', v_today + 40, v_today + 42, 'Seed short break before assessment cycle.', TRUE, FALSE, NULL, 'seed-student-34', 'seed-student-34', NOW() - INTERVAL '2 days', NOW());

    IF NOT EXISTS (
        SELECT 1
        FROM career_assessments
        WHERE school_id = v_school_id
          AND title = 'Seed: Personality Snapshot for Shubham'
    ) THEN
        INSERT INTO career_assessments (school_id, title, description, type, category, estimated_minutes, order_index, is_active, created_at)
        VALUES (v_school_id, 'Seed: Personality Snapshot for Shubham', 'Seed assessment for personality indicators.', 'PERSONALITY_PROFILE', 'Pathway Planners', 12, 1, TRUE, NOW())
        RETURNING id INTO v_assessment_completed_id;
    ELSE
        SELECT id INTO v_assessment_completed_id
        FROM career_assessments
        WHERE school_id = v_school_id
          AND title = 'Seed: Personality Snapshot for Shubham';
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM career_assessments
        WHERE school_id = v_school_id
          AND title = 'Seed: Aptitude Practice for Shubham'
    ) THEN
        INSERT INTO career_assessments (school_id, title, description, type, category, estimated_minutes, order_index, is_active, created_at)
        VALUES (v_school_id, 'Seed: Aptitude Practice for Shubham', 'Seed assessment for aptitude practice.', 'APTITUDE_ASSESSMENT', 'Pathway Planners', 15, 2, TRUE, NOW())
        RETURNING id INTO v_assessment_progress_id;
    ELSE
        SELECT id INTO v_assessment_progress_id
        FROM career_assessments
        WHERE school_id = v_school_id
          AND title = 'Seed: Aptitude Practice for Shubham';
    END IF;

    IF NOT EXISTS (SELECT 1 FROM career_questions WHERE assessment_id = v_assessment_completed_id) THEN
        INSERT INTO career_questions (assessment_id, question_text, question_order)
        VALUES
            (v_assessment_completed_id, 'Which activity do you enjoy the most?', 1),
            (v_assessment_completed_id, 'How do you solve a difficult problem?', 2),
            (v_assessment_completed_id, 'What motivates you in group work?', 3);

        INSERT INTO career_question_options (question_id, option_text, option_index, trait_tag)
        SELECT id, option_text, option_index, trait_tag
        FROM (
            SELECT id, 'Drawing or designing something new' AS option_text, 0 AS option_index, 'Creative' AS trait_tag FROM career_questions WHERE assessment_id = v_assessment_completed_id AND question_order = 1
            UNION ALL
            SELECT id, 'Exploring science experiments', 1, 'Analytical' FROM career_questions WHERE assessment_id = v_assessment_completed_id AND question_order = 1
            UNION ALL
            SELECT id, 'Helping friends with a project', 2, 'Social' FROM career_questions WHERE assessment_id = v_assessment_completed_id AND question_order = 1
            UNION ALL
            SELECT id, 'Break it into logical steps', 0, 'Logical' FROM career_questions WHERE assessment_id = v_assessment_completed_id AND question_order = 2
            UNION ALL
            SELECT id, 'Ask questions and research', 1, 'Research' FROM career_questions WHERE assessment_id = v_assessment_completed_id AND question_order = 2
            UNION ALL
            SELECT id, 'Lead the team discussion', 2, 'Leadership' FROM career_questions WHERE assessment_id = v_assessment_completed_id AND question_order = 2
            UNION ALL
            SELECT id, 'Achieving the final result', 0, 'Technical' FROM career_questions WHERE assessment_id = v_assessment_completed_id AND question_order = 3
            UNION ALL
            SELECT id, 'Making everyone comfortable', 1, 'Social' FROM career_questions WHERE assessment_id = v_assessment_completed_id AND question_order = 3
            UNION ALL
            SELECT id, 'Presenting ideas creatively', 2, 'Artistic' FROM career_questions WHERE assessment_id = v_assessment_completed_id AND question_order = 3
        ) seeded_completed;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM career_questions WHERE assessment_id = v_assessment_progress_id) THEN
        INSERT INTO career_questions (assessment_id, question_text, question_order)
        VALUES
            (v_assessment_progress_id, 'Which school subject feels easiest to you?', 1),
            (v_assessment_progress_id, 'What kind of challenge excites you most?', 2);

        INSERT INTO career_question_options (question_id, option_text, option_index, trait_tag)
        SELECT id, option_text, option_index, trait_tag
        FROM (
            SELECT id, 'Mathematics and data' AS option_text, 0 AS option_index, 'Analytical' AS trait_tag FROM career_questions WHERE assessment_id = v_assessment_progress_id AND question_order = 1
            UNION ALL
            SELECT id, 'Writing and communication', 1, 'Creative' FROM career_questions WHERE assessment_id = v_assessment_progress_id AND question_order = 1
            UNION ALL
            SELECT id, 'Science experiments', 2, 'Research' FROM career_questions WHERE assessment_id = v_assessment_progress_id AND question_order = 1
            UNION ALL
            SELECT id, 'Building something practical', 0, 'Technical' FROM career_questions WHERE assessment_id = v_assessment_progress_id AND question_order = 2
            UNION ALL
            SELECT id, 'Leading an event', 1, 'Leadership' FROM career_questions WHERE assessment_id = v_assessment_progress_id AND question_order = 2
            UNION ALL
            SELECT id, 'Supporting people directly', 2, 'Social' FROM career_questions WHERE assessment_id = v_assessment_progress_id AND question_order = 2
        ) seeded_progress;
    END IF;

    DELETE FROM career_student_answers
    WHERE progress_id IN (
        SELECT id
        FROM career_student_progress
        WHERE student_id = v_student_id
          AND assessment_id IN (v_assessment_completed_id, v_assessment_progress_id)
    );

    DELETE FROM career_results
    WHERE student_id = v_student_id
      AND assessment_id IN (v_assessment_completed_id, v_assessment_progress_id);

    DELETE FROM career_student_progress
    WHERE student_id = v_student_id
      AND assessment_id IN (v_assessment_completed_id, v_assessment_progress_id);

    INSERT INTO career_student_progress (completed_at, current_question_index, last_saved_at, started_at, status, assessment_id, student_id)
    VALUES (NOW() - INTERVAL '2 days', 3, NOW() - INTERVAL '2 days', NOW() - INTERVAL '3 days', 'COMPLETED', v_assessment_completed_id, v_student_id)
    RETURNING id INTO v_progress_completed_id;

    INSERT INTO career_results (created_at, interest_indicators_json, report_available, score, strength_areas_json, assessment_id, progress_id, student_id)
    VALUES (
        NOW() - INTERVAL '2 days',
        '["Science","Technology"]',
        TRUE,
        84,
        '["Analytical thinking","Structured problem solving"]',
        v_assessment_completed_id,
        v_progress_completed_id,
        v_student_id
    );

    INSERT INTO career_student_progress (completed_at, current_question_index, last_saved_at, started_at, status, assessment_id, student_id)
    VALUES (NULL, 1, NOW() - INTERVAL '4 hours', NOW() - INTERVAL '1 day', 'IN_PROGRESS', v_assessment_progress_id, v_student_id)
    RETURNING id INTO v_progress_in_progress_id;
END $$;