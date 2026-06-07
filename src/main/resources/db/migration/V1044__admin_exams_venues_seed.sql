-- Seed exam venues for demo school (matches Figma timetable halls).
INSERT INTO exam_venues (school_id, name, capacity, active)
SELECT 19, 'Examination Hall A', 100, TRUE
WHERE NOT EXISTS (SELECT 1 FROM exam_venues v WHERE v.school_id = 19 AND v.name = 'Examination Hall A');

INSERT INTO exam_venues (school_id, name, capacity, active)
SELECT 19, 'Room 102', 40, TRUE
WHERE NOT EXISTS (SELECT 1 FROM exam_venues v WHERE v.school_id = 19 AND v.name = 'Room 102');

INSERT INTO exam_venues (school_id, name, capacity, active)
SELECT 19, 'Room 104', 40, TRUE
WHERE NOT EXISTS (SELECT 1 FROM exam_venues v WHERE v.school_id = 19 AND v.name = 'Room 104');
