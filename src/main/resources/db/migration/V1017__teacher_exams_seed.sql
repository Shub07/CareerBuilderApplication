-- Demo exams and marks for teacher portal (faculty 1): Grade 8 A / Mathematics and Grade 9 B / English

DO $$
DECLARE
    v_school_id    BIGINT := 19;
    v_teacher_id   BIGINT := 1;
    v_sub_math     BIGINT := 1;
    v_sub_english  BIGINT := 2;
    v_class8       VARCHAR(50) := '8';
    v_section8     VARCHAR(10) := 'A';
    v_class9       VARCHAR(50) := '9';
    v_section9     VARCHAR(10) := 'B';
    v_ex_pending8  BIGINT;
    v_ex_upcoming8 BIGINT;
    v_ex_locked8   BIGINT;
    v_ex_pending9  BIGINT;
    v_ex_locked9   BIGINT;
    v_stu          RECORD;
    v_rid          BIGINT;
BEGIN
    DELETE FROM exam_results
    WHERE exam_id IN (SELECT exam_id FROM exams WHERE exam_name LIKE 'Teacher Portal Demo:%');
    DELETE FROM exams WHERE exam_name LIKE 'Teacher Portal Demo:%';

    INSERT INTO students (
        address, age, class_name, email, first_name, last_name, parent_name, phone, roll_no, school_id, section
    )
    SELECT 'Demo Address 9B-1', 14, v_class9, 'demo.9b.s1@teacherportal.local', 'Priya', 'Mehta',
           'Parent Mehta', '9000000901', 1, v_school_id, v_section9
    WHERE NOT EXISTS (
        SELECT 1 FROM students s WHERE s.school_id = v_school_id AND s.class_name = v_class9
          AND s.section = v_section9 AND s.roll_no = 1
    );

    INSERT INTO students (
        address, age, class_name, email, first_name, last_name, parent_name, phone, roll_no, school_id, section
    )
    SELECT 'Demo Address 9B-2', 14, v_class9, 'demo.9b.s2@teacherportal.local', 'Arjun', 'Desai',
           'Parent Desai', '9000000902', 2, v_school_id, v_section9
    WHERE NOT EXISTS (
        SELECT 1 FROM students s WHERE s.school_id = v_school_id AND s.class_name = v_class9
          AND s.section = v_section9 AND s.roll_no = 2
    );

    v_ex_pending8 := (SELECT COALESCE(MAX(exam_id), 0) + 1 FROM exams);
    v_ex_upcoming8 := v_ex_pending8 + 1;
    v_ex_locked8 := v_ex_pending8 + 2;
    v_ex_pending9 := v_ex_pending8 + 3;
    v_ex_locked9 := v_ex_pending8 + 4;

    INSERT INTO exams (exam_id, exam_name, exam_date, exam_type, subject_id, start_time, duration_minutes, venue)
    VALUES
        (v_ex_pending8, 'Teacher Portal Demo: Unit Test 1', CURRENT_DATE - 3, 'INTERNAL', v_sub_math,
         TIME '10:00', 90, 'Hall A'),
        (v_ex_upcoming8, 'Teacher Portal Demo: Midterm Mathematics', CURRENT_DATE + 5, 'MID_TERM', v_sub_math,
         TIME '09:00', 120, 'Hall B'),
        (v_ex_locked8, 'Teacher Portal Demo: Weekly Assessment', CURRENT_DATE - 10, 'WEEKLY', v_sub_math,
         TIME '14:00', 60, 'Room 101'),
        (v_ex_pending9, 'Teacher Portal Demo: Essay Unit Test', CURRENT_DATE - 2, 'INTERNAL', v_sub_english,
         TIME '11:15', 90, 'Hall C'),
        (v_ex_locked9, 'Teacher Portal Demo: Grammar Final', CURRENT_DATE - 8, 'FINAL', v_sub_english,
         TIME '10:30', 120, 'Room 204');

    v_rid := (SELECT COALESCE(MAX(result_id), 0) FROM exam_results);

    FOR v_stu IN
        SELECT id, roll_no FROM students
        WHERE school_id = v_school_id AND class_name = v_class8 AND section = v_section8
        ORDER BY roll_no
    LOOP
        v_rid := v_rid + 1;
        INSERT INTO exam_results (result_id, student_id, exam_id, subject_id, obtained_marks, total_marks, marks_locked, remarks)
        VALUES (v_rid, v_stu.id, v_ex_pending8, v_sub_math,
                CASE WHEN v_stu.roll_no = 8 THEN 16 WHEN v_stu.roll_no = 12 THEN 12 ELSE 10 END,
                20, FALSE, CASE WHEN v_stu.roll_no = 12 THEN 'Needs improvement' ELSE 'Good work' END);

        v_rid := v_rid + 1;
        INSERT INTO exam_results (result_id, student_id, exam_id, subject_id, obtained_marks, total_marks, marks_locked, remarks)
        VALUES (v_rid, v_stu.id, v_ex_upcoming8, v_sub_math, 0, 20, FALSE, NULL);

        v_rid := v_rid + 1;
        INSERT INTO exam_results (result_id, student_id, exam_id, subject_id, obtained_marks, total_marks, marks_locked, marks_locked_at, remarks)
        VALUES (v_rid, v_stu.id, v_ex_locked8, v_sub_math,
                CASE WHEN v_stu.roll_no = 8 THEN 18 WHEN v_stu.roll_no = 12 THEN 14 ELSE 11 END,
                20, TRUE, NOW() - INTERVAL '9 days',
                CASE WHEN v_stu.roll_no = 8 THEN 'Excellent' ELSE NULL END);
    END LOOP;

    FOR v_stu IN
        SELECT id, roll_no FROM students
        WHERE school_id = v_school_id AND class_name = v_class9 AND section = v_section9
        ORDER BY roll_no
    LOOP
        v_rid := v_rid + 1;
        INSERT INTO exam_results (result_id, student_id, exam_id, subject_id, obtained_marks, total_marks, marks_locked, remarks)
        VALUES (v_rid, v_stu.id, v_ex_pending9, v_sub_english,
                CASE WHEN v_stu.roll_no = 1 THEN 15 ELSE 11 END, 20, FALSE, 'Draft entry');

        v_rid := v_rid + 1;
        INSERT INTO exam_results (result_id, student_id, exam_id, subject_id, obtained_marks, total_marks, marks_locked, marks_locked_at, remarks)
        VALUES (v_rid, v_stu.id, v_ex_locked9, v_sub_english,
                CASE WHEN v_stu.roll_no = 1 THEN 17 ELSE 13 END, 20, TRUE, NOW() - INTERVAL '7 days', 'Submitted');
    END LOOP;
END $$;
