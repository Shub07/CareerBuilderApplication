-- Weekly class timetable + teacher activities for faculty 1 (school 19)
-- Classes: Grade 8 A / Mathematics, Grade 9 B / English

DELETE FROM class_schedule_slots
WHERE school_id = 19
  AND (title LIKE 'Seed:%' OR title LIKE 'Teacher Portal Demo:%');

DELETE FROM teacher_schedule_entries
WHERE faculty_id = 1
  AND title LIKE 'Teacher Portal Demo:%';

-- Grade 8 A — Mathematics (subject 1)
INSERT INTO class_schedule_slots (school_id, class_name, section, subject_id, day_of_week, start_time, end_time, slot_type, title, is_active)
VALUES
    (19, '8', 'A', 1, 1, '09:00', '09:45', 'CLASS', 'Teacher Portal Demo: Mathematics', TRUE),
    (19, '8', 'A', 1, 1, '10:00', '10:15', 'BREAK', 'Teacher Portal Demo: Break', TRUE),
    (19, '8', 'A', 1, 3, '09:00', '09:45', 'CLASS', 'Teacher Portal Demo: Mathematics', TRUE),
    (19, '8', 'A', 1, 3, '10:00', '10:15', 'BREAK', 'Teacher Portal Demo: Break', TRUE),
    (19, '8', 'A', 1, 5, '11:00', '11:45', 'CLASS', 'Teacher Portal Demo: Mathematics', TRUE);

-- Grade 9 B — English (subject 2)
INSERT INTO class_schedule_slots (school_id, class_name, section, subject_id, day_of_week, start_time, end_time, slot_type, title, is_active)
VALUES
    (19, '9', 'B', 2, 2, '10:00', '10:45', 'CLASS', 'Teacher Portal Demo: English', TRUE),
    (19, '9', 'B', 2, 2, '11:00', '11:15', 'BREAK', 'Teacher Portal Demo: Break', TRUE),
    (19, '9', 'B', 2, 4, '10:00', '10:45', 'CLASS', 'Teacher Portal Demo: English', TRUE),
    (19, '9', 'B', 2, 5, '09:00', '09:45', 'CLASS', 'Teacher Portal Demo: English', TRUE);

-- Teacher-created activities (recurring + one-off)
INSERT INTO teacher_schedule_entries (
    faculty_id, school_id, title, activity_type, recurring, day_of_week, specific_date,
    start_time, end_time, class_name, section, section_label, venue, notes, created_at, updated_at
)
VALUES
    (1, 19, 'Teacher Portal Demo: Staff briefing', 'MEETING', TRUE, 1, NULL, '08:00', '08:25', NULL, NULL, NULL, 'Staff Room', 'Weekly sync with coordinators', NOW(), NOW()),
    (1, 19, 'Teacher Portal Demo: Department planning', 'MEETING', TRUE, 5, NULL, '15:30', '16:15', NULL, NULL, NULL, 'Conference Room B', NULL, NOW(), NOW()),
    (1, 19, 'Teacher Portal Demo: Parent meet prep', 'OTHER', FALSE, NULL, CURRENT_DATE + 2, '14:00', '15:00', NULL, NULL, NULL, 'Room 204', 'Prepare progress summaries', NOW(), NOW());
