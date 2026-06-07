-- Demo session notes + Saturday slots so "today" shows classes when tested on weekends.

INSERT INTO class_schedule_slots (school_id, class_name, section, subject_id, day_of_week, start_time, end_time, slot_type, title, is_active)
SELECT 19, '8', 'A', 1, 6, '09:30', '10:15', 'CLASS', 'Teacher Portal Demo: Mathematics (Sat)', TRUE
WHERE NOT EXISTS (
    SELECT 1 FROM class_schedule_slots
    WHERE school_id = 19 AND class_name = '8' AND section = 'A' AND day_of_week = 6 AND slot_type = 'CLASS'
);

INSERT INTO class_schedule_slots (school_id, class_name, section, subject_id, day_of_week, start_time, end_time, slot_type, title, is_active)
SELECT 19, '9', 'B', 2, 6, '11:00', '11:45', 'CLASS', 'Teacher Portal Demo: English (Sat)', TRUE
WHERE NOT EXISTS (
    SELECT 1 FROM class_schedule_slots
    WHERE school_id = 19 AND class_name = '9' AND section = 'B' AND day_of_week = 6 AND slot_type = 'CLASS'
);

DELETE FROM teacher_session_notes WHERE faculty_id = 1 AND school_id = 19;

-- Grade 8 A Math
INSERT INTO teacher_session_notes (
    faculty_id, school_id, session_date, session_kind, ref_id,
    class_name, section, subject_id, session_start_time, session_end_time,
    topic_covered, description_notes, homework, created_at, updated_at
)
SELECT
    1, 19, d.dt, 'TIMETABLE_SLOT', s.id,
    '8', 'A', s.subject_id, s.start_time, s.end_time,
    CASE EXTRACT(ISODOW FROM d.dt)
        WHEN 1 THEN 'Algebra — linear equations'
        WHEN 2 THEN 'Geometry — angles and triangles'
        WHEN 3 THEN 'Statistics — mean and median'
        WHEN 4 THEN 'Fractions — operations'
        WHEN 5 THEN 'Unit review and quiz prep'
        ELSE 'Math practice'
    END,
    'Teacher Portal Demo session note.',
    'Complete assigned practice set.',
    NOW(), NOW()
FROM generate_series(CURRENT_DATE - 14, CURRENT_DATE - 1, '1 day'::interval) AS d(dt)
JOIN LATERAL (
    SELECT css.id, css.subject_id, css.start_time, css.end_time
    FROM class_schedule_slots css
    WHERE css.school_id = 19 AND css.class_name = '8' AND css.section = 'A'
      AND css.day_of_week = EXTRACT(ISODOW FROM d.dt)::int
    ORDER BY css.start_time
    LIMIT 1
) s ON true
WHERE EXTRACT(ISODOW FROM d.dt) BETWEEN 1 AND 5
ON CONFLICT (faculty_id, session_kind, ref_id, session_date) DO UPDATE SET
    topic_covered = EXCLUDED.topic_covered,
    updated_at = NOW();

-- Grade 9 B English
INSERT INTO teacher_session_notes (
    faculty_id, school_id, session_date, session_kind, ref_id,
    class_name, section, subject_id, session_start_time, session_end_time,
    topic_covered, description_notes, homework, created_at, updated_at
)
SELECT
    1, 19, d.dt, 'TIMETABLE_SLOT', s.id,
    '9', 'B', s.subject_id, s.start_time, s.end_time,
    CASE EXTRACT(ISODOW FROM d.dt)
        WHEN 1 THEN 'Essay structure — introduction'
        WHEN 2 THEN 'Reading comprehension — poetry'
        WHEN 3 THEN 'Grammar — tenses review'
        WHEN 4 THEN 'Creative writing — short story'
        WHEN 5 THEN 'Literature — chapter discussion'
        ELSE 'English skills'
    END,
    'Teacher Portal Demo session note.',
    'Read next chapter and summarize.',
    NOW(), NOW()
FROM generate_series(CURRENT_DATE - 14, CURRENT_DATE - 1, '1 day'::interval) AS d(dt)
JOIN LATERAL (
    SELECT css.id, css.subject_id, css.start_time, css.end_time
    FROM class_schedule_slots css
    WHERE css.school_id = 19 AND css.class_name = '9' AND css.section = 'B'
      AND css.day_of_week = EXTRACT(ISODOW FROM d.dt)::int
    ORDER BY css.start_time
    LIMIT 1
) s ON true
WHERE EXTRACT(ISODOW FROM d.dt) BETWEEN 1 AND 5
ON CONFLICT (faculty_id, session_kind, ref_id, session_date) DO UPDATE SET
    topic_covered = EXCLUDED.topic_covered,
    updated_at = NOW();
