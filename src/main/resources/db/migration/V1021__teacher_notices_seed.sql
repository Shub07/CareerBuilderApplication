-- Demo notices for faculty 1 (school 19): Grade 8 A and Grade 9 B

DELETE FROM teacher_notice_student_targets
WHERE teacher_notice_id IN (
    SELECT id FROM teacher_notices WHERE faculty_id = 1 AND title LIKE 'Teacher Portal Demo:%'
);
DELETE FROM teacher_notice_class_targets
WHERE teacher_notice_id IN (
    SELECT id FROM teacher_notices WHERE faculty_id = 1 AND title LIKE 'Teacher Portal Demo:%'
);
DELETE FROM teacher_notices WHERE faculty_id = 1 AND title LIKE 'Teacher Portal Demo:%';

DO $$
DECLARE
    v_faculty_id BIGINT := 1;
    v_school_id  BIGINT := 19;
    v_n1 BIGINT;
    v_n2 BIGINT;
    v_n3 BIGINT;
    v_n4 BIGINT;
BEGIN
    INSERT INTO teacher_notices (
        faculty_id, school_id, notice_type, status, title, description,
        published_at, created_at, updated_at
    ) VALUES (
        v_faculty_id, v_school_id, 'CLASS_ANNOUNCEMENT', 'PUBLISHED',
        'Teacher Portal Demo: Mathematics unit test',
        'Unit test on algebra chapters 4-6 is scheduled for next Wednesday. Bring your formula sheet and calculator.',
        NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days', NOW()
    ) RETURNING id INTO v_n1;

    INSERT INTO teacher_notice_class_targets (teacher_notice_id, class_name, section)
    VALUES (v_n1, '8', 'A');

    INSERT INTO teacher_notices (
        faculty_id, school_id, notice_type, status, title, description,
        published_at, created_at, updated_at
    ) VALUES (
        v_faculty_id, v_school_id, 'CLASS_ANNOUNCEMENT', 'PUBLISHED',
        'Teacher Portal Demo: English essay submission',
        'Submit your draft essay on My School by Friday 5 PM via the assignment portal.',
        NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days', NOW()
    ) RETURNING id INTO v_n2;

    INSERT INTO teacher_notice_class_targets (teacher_notice_id, class_name, section)
    VALUES (v_n2, '9', 'B');

    INSERT INTO teacher_notices (
        faculty_id, school_id, notice_type, status, title, description,
        published_at, created_at, updated_at
    ) VALUES (
        v_faculty_id, v_school_id, 'STUDENT_ALERT', 'PUBLISHED',
        'Teacher Portal Demo: Attendance follow-up',
        'Please meet me after school to discuss recent absences and catch-up work.',
        NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day', NOW()
    ) RETURNING id INTO v_n3;

    INSERT INTO teacher_notice_student_targets (teacher_notice_id, student_id)
    VALUES (v_n3, 37);

    INSERT INTO teacher_notices (
        faculty_id, school_id, notice_type, status, title, description,
        published_at, created_at, updated_at
    ) VALUES (
        v_faculty_id, v_school_id, 'STUDENT_ALERT', 'PUBLISHED',
        'Teacher Portal Demo: Excellent project work',
        'Great job on the recent class project. Keep up the strong effort!',
        NOW() - INTERVAL '5 hours', NOW() - INTERVAL '5 hours', NOW()
    ) RETURNING id INTO v_n4;

    INSERT INTO teacher_notice_student_targets (teacher_notice_id, student_id)
    VALUES (v_n4, 34);

    INSERT INTO teacher_notices (
        faculty_id, school_id, notice_type, status, title, description,
        created_at, updated_at
    ) VALUES (
        v_faculty_id, v_school_id, 'CLASS_ANNOUNCEMENT', 'DRAFT',
        'Teacher Portal Demo: Parent evening draft',
        'Draft notice for upcoming parent-teacher evening - not yet published.',
        NOW() - INTERVAL '1 hour', NOW()
    );

    INSERT INTO teacher_notice_class_targets (teacher_notice_id, class_name, section)
    SELECT id, '8', 'A' FROM teacher_notices
    WHERE faculty_id = v_faculty_id AND title = 'Teacher Portal Demo: Parent evening draft';
END $$;
