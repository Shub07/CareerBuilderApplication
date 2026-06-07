-- Demo self-attendance and leave data for faculty 1 (school 19)

DELETE FROM teacher_leave_requests
WHERE faculty_id = 1 AND reason LIKE 'Teacher Portal Demo:%';

DELETE FROM teacher_attendance_entries
WHERE faculty_id = 1 AND (late_note LIKE 'Teacher Portal Demo:%' OR work_summary LIKE 'Teacher Portal Demo:%' OR late_note IS NULL);

-- Re-delete entries we may have inserted without marker (faculty 1 only, last 90 days)
DELETE FROM teacher_attendance_entries
WHERE faculty_id = 1 AND work_date >= CURRENT_DATE - 90;

INSERT INTO teacher_leave_balances (faculty_id, casual_total, medical_total, half_day_total, created_at, updated_at)
VALUES (1, 12, 10, 6, NOW(), NOW())
ON CONFLICT (faculty_id) DO UPDATE SET
    casual_total = EXCLUDED.casual_total,
    medical_total = EXCLUDED.medical_total,
    half_day_total = EXCLUDED.half_day_total,
    updated_at = NOW();

DO $$
DECLARE
    v_faculty_id BIGINT := 1;
    v_school_id  BIGINT := 19;
    v_d          DATE;
    v_dow        INT;
    v_status     VARCHAR(20);
    v_check_in   TIME;
    v_check_out  TIME;
    v_worked     INT;
BEGIN
    FOR i IN 1..45 LOOP
        v_d := CURRENT_DATE - i;
        v_dow := EXTRACT(DOW FROM v_d);
        IF v_dow IN (0, 6) THEN
            CONTINUE;
        END IF;

        IF v_d = CURRENT_DATE - 10 THEN
            CONTINUE;
        END IF;

        IF v_d = CURRENT_DATE - 18 THEN
            v_status := 'LATE';
            v_check_in := TIME '09:22';
            v_check_out := TIME '16:10';
            v_worked := 406;
        ELSIF v_d = CURRENT_DATE - 25 THEN
            v_status := 'HALF_DAY';
            v_check_in := TIME '09:05';
            v_check_out := TIME '13:05';
            v_worked := 240;
        ELSE
            v_status := 'PRESENT';
            v_check_in := TIME '08:55';
            v_check_out := TIME '16:15';
            v_worked := 440;
        END IF;

        INSERT INTO teacher_attendance_entries (
            faculty_id, school_id, work_date, check_in_time, check_out_time,
            worked_minutes, status, late_note, work_summary, created_at, updated_at
        ) VALUES (
            v_faculty_id, v_school_id, v_d, v_check_in, v_check_out, v_worked, v_status,
            CASE WHEN v_status = 'LATE' THEN 'Teacher Portal Demo: traffic delay' ELSE NULL END,
            'Teacher Portal Demo: regular school day',
            NOW(), NOW()
        );
    END LOOP;
END $$;

INSERT INTO teacher_leave_requests (
    faculty_id, school_id, leave_type, from_date, to_date, reason, rejection_reason, status, created_at, updated_at
)
VALUES
    (1, 19, 'MEDICAL', CURRENT_DATE - 14, CURRENT_DATE - 13,
     'Teacher Portal Demo: medical appointment', NULL, 'APPROVED', NOW() - INTERVAL '20 days', NOW()),
    (1, 19, 'CASUAL', CURRENT_DATE - 10, CURRENT_DATE - 10,
     'Teacher Portal Demo: personal leave day', NULL, 'APPROVED', NOW() - INTERVAL '11 days', NOW()),
    (1, 19, 'CASUAL', CURRENT_DATE + 7, CURRENT_DATE + 8,
     'Teacher Portal Demo: family event', NULL, 'APPLIED', NOW() - INTERVAL '2 days', NOW()),
    (1, 19, 'CASUAL', CURRENT_DATE - 35, CURRENT_DATE - 34,
     'Teacher Portal Demo: personal work', 'Teacher Portal Demo: peak exam week - request denied', 'REJECTED',
     NOW() - INTERVAL '36 days', NOW());
