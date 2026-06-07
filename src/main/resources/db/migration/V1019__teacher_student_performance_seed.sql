-- Demo attendance, quiz results, activity feed, and remarks for teacher portal performance (faculty 1)
-- Classes: Grade 8 A (Mathematics) and Grade 9 B (English)

DO $$
DECLARE
    v_faculty_id BIGINT := 1;
    v_school_id  BIGINT := 19;
    v_sub_math   BIGINT := 1;
    v_sub_eng    BIGINT := 2;
    v_d          DATE;
    v_status     VARCHAR(20);
    v_stu        RECORD;
    v_aid        BIGINT;
BEGIN
    DELETE FROM student_performance_activities WHERE title LIKE 'Teacher Portal Demo:%';
    DELETE FROM student_quiz_results WHERE title LIKE 'Teacher Portal Demo:%';
    DELETE FROM teacher_student_remarks WHERE remark_text LIKE 'Teacher Portal Demo:%';

    PERFORM setval(
        'attendance_records_attendance_id_seq',
        GREATEST((SELECT COALESCE(MAX(attendance_id), 1) FROM attendance_records), 1)
    );

    -- Attendance: last 90 weekdays per demo student (skip if row exists)
    FOR v_stu IN
        SELECT id, class_name, section FROM students
        WHERE school_id = v_school_id
          AND ((class_name = '8' AND section = 'A') OR (class_name = '9' AND section = 'B'))
    LOOP
        FOR i IN 0..89 LOOP
            v_d := CURRENT_DATE - i;
            IF EXTRACT(DOW FROM v_d) IN (0, 6) THEN
                CONTINUE;
            END IF;
            IF EXISTS (SELECT 1 FROM attendance_records ar WHERE ar.student_id = v_stu.id AND ar.att_date = v_d) THEN
                CONTINUE;
            END IF;
            IF (v_stu.id + i) % 7 = 0 THEN
                v_status := 'ABSENT';
            ELSIF (v_stu.id + i) % 11 = 0 THEN
                v_status := 'LEAVE';
            ELSE
                v_status := 'PRESENT';
            END IF;
            INSERT INTO attendance_records (att_date, status, student_id, class_name, section, check_in_time, created_at, updated_at)
            VALUES (
                v_d, v_status, v_stu.id, v_stu.class_name, v_stu.section,
                CASE WHEN v_status = 'PRESENT' THEN TIME '09:05' ELSE NULL END,
                NOW(), NOW()
            )
            ON CONFLICT ON CONSTRAINT uk_attendance_student_date DO NOTHING;
        END LOOP;
    END LOOP;

    v_aid := (SELECT COALESCE(MAX(id), 0) FROM student_performance_activities);
    INSERT INTO student_performance_activities (id, student_id, activity_type, title, description, activity_date, severity, created_at)
    VALUES
        (v_aid + 1, 34, 'EXAM_RESULT', 'Teacher Portal Demo: Scored 85% on Unit Test', 'Strong algebra performance', CURRENT_DATE - 3, 'SUCCESS', NOW()),
        (v_aid + 2, 37, 'EXAM_RESULT', 'Teacher Portal Demo: Scored 72% on Unit Test', 'Needs practice on word problems', CURRENT_DATE - 3, 'WARNING', NOW()),
        (v_aid + 3, 34, 'ASSIGNMENT_SUBMITTED', 'Teacher Portal Demo: Submitted homework on time', NULL, CURRENT_DATE - 5, 'INFO', NOW()),
        (v_aid + 4, 46, 'EXAM_RESULT', 'Teacher Portal Demo: Essay unit test 78%', 'Good structure and vocabulary', CURRENT_DATE - 2, 'SUCCESS', NOW()),
        (v_aid + 5, 47, 'ATTENDANCE_ABSENT', 'Teacher Portal Demo: Absence noted', 'Parent informed via SMS', CURRENT_DATE - 7, 'DANGER', NOW());

    INSERT INTO student_quiz_results (student_id, subject_id, faculty_id, title, quiz_date, obtained_marks, total_marks, passed, created_at)
    VALUES
        (34, v_sub_math, v_faculty_id, 'Teacher Portal Demo: Algebra Quiz 3', CURRENT_DATE - 10, 18, 20, TRUE, NOW()),
        (37, v_sub_math, v_faculty_id, 'Teacher Portal Demo: Algebra Quiz 3', CURRENT_DATE - 10, 14, 20, TRUE, NOW()),
        (46, v_sub_eng, v_faculty_id, 'Teacher Portal Demo: Grammar Quiz 2', CURRENT_DATE - 8, 16, 20, TRUE, NOW()),
        (47, v_sub_eng, v_faculty_id, 'Teacher Portal Demo: Grammar Quiz 2', CURRENT_DATE - 8, 12, 20, FALSE, NOW());

    INSERT INTO teacher_student_remarks (student_id, faculty_id, remark_text, created_at, updated_at)
    VALUES
        (34, v_faculty_id, 'Teacher Portal Demo: Consistent performer; encourage advanced problems.', NOW(), NOW()),
        (37, v_faculty_id, 'Teacher Portal Demo: Improving steadily; schedule extra practice.', NOW(), NOW())
    ON CONFLICT ON CONSTRAINT uk_tsr_student_faculty
    DO UPDATE SET remark_text = EXCLUDED.remark_text, updated_at = NOW();
END $$;
