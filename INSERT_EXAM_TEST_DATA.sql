-- ============================================================================
-- INSERT TEST DATA FOR EXAMS & TESTS SECTION
-- ============================================================================

-- Note: This assumes students, subjects, and exams already exist
-- Adjust IDs based on your actual database

-- ============================================================================
-- 1. INSERT SAMPLE EXAMS (If not already present)
-- ============================================================================

INSERT INTO exams (exam_name, exam_date, exam_type, subject_id, start_time, duration_minutes)
VALUES
('Unit Test - 1', '2026-12-08', 'INTERNAL', 1, '10:00:00', 120),
('Mid-Term Exam', '2026-12-15', 'WEEKLY', 2, '10:00:00', 120),
('Practical Exam', '2026-12-20', 'FINAL', 3, '14:00:00', 60),
('Monthly Test', '2026-11-05', 'INTERNAL', 1, '09:00:00', 90),
('Weekly Assessment', '2026-11-12', 'WEEKLY', 2, '10:30:00', 60),
('Final Assessment', '2026-11-25', 'FINAL', 3, '15:00:00', 120)
ON CONFLICT DO NOTHING;

-- ============================================================================
-- 2. INSERT EXAM SECTIONS (Marks Breakdown Structure)
-- ============================================================================

-- For Exam 1 (Unit Test - 1)
INSERT INTO exam_sections (exam_id, section_name, section_type, total_marks) VALUES
(1, 'Section A: MCQ', 'MCQ', 20),
(1, 'Section B: Short Answer', 'SHORT_ANSWER', 40),
(1, 'Section C: Long Answer', 'LONG_ANSWER', 40);

-- For Exam 4 (Monthly Test)
INSERT INTO exam_sections (exam_id, section_name, section_type, total_marks) VALUES
(4, 'Section A: MCQ', 'MCQ', 20),
(4, 'Section B: Short Answer', 'SHORT_ANSWER', 40),
(4, 'Section C: Long Answer', 'LONG_ANSWER', 40);

-- ============================================================================
-- 3. INSERT EXAM RESULTS FOR STUDENTS
-- ============================================================================

-- ✅ STUDENT 1: John Doe (ID: 1)
INSERT INTO exam_results (student_id, exam_id, subject_id, obtained_marks, total_marks, grade, rank, feedback, delta_percent)
VALUES
-- Upcoming exams (future dates)
(1, 1, 1, 97, 100, 'Grade A', 5, 'Great work! Keep it up.', 5),
(1, 2, 2, 85, 100, 'Grade B', 12, 'Good effort. Focus on physics concepts.', 2),

-- Completed exams (past dates)
(1, 4, 1, 92, 100, 'Grade A', 3, 'Excellent performance. Focus more on proof-based questions in the future.', 8),
(1, 5, 2, 78, 100, 'Grade B+', 15, 'Average. Need more practice.', -3);

-- ✅ STUDENT 2: Jane Smith (ID: 2)
INSERT INTO exam_results (student_id, exam_id, subject_id, obtained_marks, total_marks, grade, rank, feedback, delta_percent)
VALUES
-- Upcoming exams
(2, 1, 1, 88, 100, 'Grade B', 8, 'Good performance.', 3),
(2, 2, 2, 91, 100, 'Grade A', 2, 'Outstanding! Well done.', 6),

-- Completed exams
(2, 4, 1, 95, 100, 'Grade A', 1, 'Perfect! Best in class.', 12),
(2, 5, 2, 82, 100, 'Grade B', 9, 'Good work. Continue improving.', 1);

-- ✅ STUDENT 3: Bob Wilson (ID: 3)
INSERT INTO exam_results (student_id, exam_id, subject_id, obtained_marks, total_marks, grade, rank, feedback, delta_percent)
VALUES
-- Upcoming exams
(3, 1, 1, 75, 100, 'Grade B', 18, 'Satisfactory work.', 0),
(3, 2, 2, 68, 100, 'Grade C', 25, 'Needs improvement.', -5),

-- Completed exams
(3, 4, 1, 80, 100, 'Grade B', 7, 'Good effort. Work on accuracy.', 4),
(3, 5, 2, 72, 100, 'Grade C', 20, 'Below average. More practice needed.', -2);

-- ============================================================================
-- 4. INSERT EXAM SECTIONS BREAKDOWN (Marks per section for each student result)
-- ============================================================================

-- STUDENT 1 - Exam 4 (Monthly Test) - Result ID 3
INSERT INTO exam_result_breakdown (exam_result_id, section_id, section_name, obtained_marks, total_marks, percentage)
VALUES
(3, 4, 'Section A: MCQ', 18, 20, 90.00),
(3, 5, 'Section B: Short Answer', 36, 40, 90.00),
(3, 6, 'Section C: Long Answer', 38, 40, 95.00);

-- STUDENT 1 - Exam 5 (Weekly Assessment) - Result ID 4
INSERT INTO exam_result_breakdown (exam_result_id, section_id, section_name, obtained_marks, total_marks, percentage)
VALUES
(4, 4, 'Section A: MCQ', 15, 20, 75.00),
(4, 5, 'Section B: Short Answer', 32, 40, 80.00),
(4, 6, 'Section C: Long Answer', 31, 40, 77.50);

-- STUDENT 2 - Exam 4 (Monthly Test) - Result ID 5
INSERT INTO exam_result_breakdown (exam_result_id, section_id, section_name, obtained_marks, total_marks, percentage)
VALUES
(5, 4, 'Section A: MCQ', 19, 20, 95.00),
(5, 5, 'Section B: Short Answer', 38, 40, 95.00),
(5, 6, 'Section C: Long Answer', 38, 40, 95.00);

-- STUDENT 2 - Exam 5 (Weekly Assessment) - Result ID 6
INSERT INTO exam_result_breakdown (exam_result_id, section_id, section_name, obtained_marks, total_marks, percentage)
VALUES
(6, 4, 'Section A: MCQ', 16, 20, 80.00),
(6, 5, 'Section B: Short Answer', 33, 40, 82.50),
(6, 6, 'Section C: Long Answer', 33, 40, 82.50);

-- STUDENT 3 - Exam 4 (Monthly Test) - Result ID 7
INSERT INTO exam_result_breakdown (exam_result_id, section_id, section_name, obtained_marks, total_marks, percentage)
VALUES
(7, 4, 'Section A: MCQ', 16, 20, 80.00),
(7, 5, 'Section B: Short Answer', 32, 40, 80.00),
(7, 6, 'Section C: Long Answer', 32, 40, 80.00);

-- STUDENT 3 - Exam 5 (Weekly Assessment) - Result ID 8
INSERT INTO exam_result_breakdown (exam_result_id, section_id, section_name, obtained_marks, total_marks, percentage)
VALUES
(8, 4, 'Section A: MCQ', 14, 20, 70.00),
(8, 5, 'Section B: Short Answer', 29, 40, 72.50),
(8, 6, 'Section C: Long Answer', 29, 40, 72.50);

-- ============================================================================
-- 5. INSERT EXAM QUESTIONS
-- ============================================================================

INSERT INTO exam_questions (exam_id, section_id, question_number, question_text, marks)
VALUES
-- Exam 1, Section A: MCQ
(1, 1, 1, 'What is the SI unit of force?', 5),
(1, 1, 2, 'What is the formula for kinetic energy?', 5),
(1, 1, 3, 'Define velocity', 5),
(1, 1, 4, 'What is acceleration?', 5),

-- Exam 1, Section B: Short Answer
(1, 2, 5, 'Explain the first law of motion', 8),
(1, 2, 6, 'Explain the second law of motion', 8),
(1, 2, 7, 'Explain the third law of motion', 8),
(1, 2, 8, 'What is momentum?', 8),

-- Exam 1, Section C: Long Answer
(1, 3, 9, 'Derive the equations of motion', 20),
(1, 3, 10, 'Explain energy conservation principle', 20);

-- ============================================================================
-- 6. INSERT STUDENT EXAM ANSWERS (Question-wise analysis)
-- ============================================================================

-- STUDENT 1 - Exam 4 (Result ID 3) - Answers
INSERT INTO student_exam_answers (exam_result_id, question_id, obtained_marks, status)
VALUES
(3, 1, 5, 'CORRECT'),
(3, 2, 5, 'CORRECT'),
(3, 3, 4, 'PARTIAL'),
(3, 4, 4, 'CORRECT'),
(3, 5, 8, 'CORRECT'),
(3, 6, 7, 'PARTIAL'),
(3, 7, 8, 'CORRECT'),
(3, 8, 8, 'CORRECT'),
(3, 9, 18, 'CORRECT'),
(3, 10, 20, 'CORRECT');

-- STUDENT 1 - Exam 5 (Result ID 4) - Answers
INSERT INTO student_exam_answers (exam_result_id, question_id, obtained_marks, status)
VALUES
(4, 1, 5, 'CORRECT'),
(4, 2, 5, 'CORRECT'),
(4, 3, 3, 'WRONG'),
(4, 4, 2, 'PARTIAL'),
(4, 5, 8, 'CORRECT'),
(4, 6, 6, 'PARTIAL'),
(4, 7, 8, 'CORRECT'),
(4, 8, 0, 'WRONG'),
(4, 9, 15, 'PARTIAL'),
(4, 10, 16, 'PARTIAL');

-- STUDENT 2 - Exam 4 (Result ID 5) - Answers (High performer)
INSERT INTO student_exam_answers (exam_result_id, question_id, obtained_marks, status)
VALUES
(5, 1, 5, 'CORRECT'),
(5, 2, 5, 'CORRECT'),
(5, 3, 5, 'CORRECT'),
(5, 4, 4, 'PARTIAL'),
(5, 5, 8, 'CORRECT'),
(5, 6, 8, 'CORRECT'),
(5, 7, 8, 'CORRECT'),
(5, 8, 8, 'CORRECT'),
(5, 9, 19, 'CORRECT'),
(5, 10, 19, 'CORRECT');

-- STUDENT 2 - Exam 5 (Result ID 6) - Answers
INSERT INTO student_exam_answers (exam_result_id, question_id, obtained_marks, status)
VALUES
(6, 1, 5, 'CORRECT'),
(6, 2, 5, 'CORRECT'),
(6, 3, 4, 'PARTIAL'),
(6, 4, 2, 'PARTIAL'),
(6, 5, 8, 'CORRECT'),
(6, 6, 8, 'CORRECT'),
(6, 7, 8, 'CORRECT'),
(6, 8, 4, 'PARTIAL'),
(6, 9, 16, 'PARTIAL'),
(6, 10, 17, 'CORRECT');

-- STUDENT 3 - Exam 4 (Result ID 7) - Answers (Average performer)
INSERT INTO student_exam_answers (exam_result_id, question_id, obtained_marks, status)
VALUES
(7, 1, 5, 'CORRECT'),
(7, 2, 5, 'CORRECT'),
(7, 3, 3, 'WRONG'),
(7, 4, 3, 'PARTIAL'),
(7, 5, 8, 'CORRECT'),
(7, 6, 8, 'CORRECT'),
(7, 7, 8, 'CORRECT'),
(7, 8, 0, 'WRONG'),
(7, 9, 16, 'PARTIAL'),
(7, 10, 16, 'CORRECT');

-- STUDENT 3 - Exam 5 (Result ID 8) - Answers
INSERT INTO student_exam_answers (exam_result_id, question_id, obtained_marks, status)
VALUES
(8, 1, 5, 'CORRECT'),
(8, 2, 4, 'PARTIAL'),
(8, 3, 2, 'WRONG'),
(8, 4, 3, 'PARTIAL'),
(8, 5, 8, 'CORRECT'),
(8, 6, 6, 'PARTIAL'),
(8, 7, 8, 'CORRECT'),
(8, 8, 2, 'WRONG'),
(8, 9, 14, 'PARTIAL'),
(8, 10, 15, 'PARTIAL');

-- ============================================================================
-- 7. INSERT EXAM FEEDBACK FROM TEACHERS
-- ============================================================================

INSERT INTO exam_feedback (exam_result_id, teacher_id, teacher_name, feedback_text)
VALUES
(3, 1, 'Mrs. Lucia', 'Excellent performance. Focus more on proof-based questions in the future.'),
(4, 1, 'Mrs. Lucia', 'Good work overall. Need to improve on short answer section.'),
(5, 1, 'Mrs. Lucia', 'Perfect score! Outstanding performance. Keep it up!'),
(6, 1, 'Mrs. Lucia', 'Great job. Excellent understanding of concepts.'),
(7, 1, 'Mrs. Lucia', 'Satisfactory work. Focus on improving accuracy.'),
(8, 1, 'Mrs. Lucia', 'Need more practice. Review the theory concepts.');

-- ============================================================================
-- 8. INSERT EXAM SCHEDULES
-- ============================================================================

INSERT INTO exam_schedules (exam_id, class_id, section_id, scheduled_date, start_time, end_time, is_published)
VALUES
(1, 1, 1, '2026-12-08', '10:00:00', '12:00:00', TRUE),
(2, 1, 1, '2026-12-15', '10:00:00', '12:00:00', TRUE),
(3, 1, 1, '2026-12-20', '14:00:00', '15:00:00', TRUE),
(4, 1, 1, '2026-11-05', '09:00:00', '10:30:00', TRUE),
(5, 1, 1, '2026-11-12', '10:30:00', '11:30:00', TRUE),
(6, 1, 1, '2026-11-25', '15:00:00', '17:00:00', TRUE);

-- ============================================================================
-- VERIFICATION QUERIES
-- ============================================================================

SELECT '=== EXAM RESULTS ===' as info;
SELECT er.id, s.first_name, e.exam_name, er.obtained_marks, er.total_marks, er.grade
FROM exam_results er
JOIN students s ON er.student_id = s.id
JOIN exams e ON er.exam_id = e.id
ORDER BY er.student_id, e.exam_date DESC;

SELECT '=== EXAM RESULT BREAKDOWN ===' as info;
SELECT erb.exam_result_id, erb.section_name, erb.obtained_marks, erb.total_marks, erb.percentage
FROM exam_result_breakdown erb
ORDER BY erb.exam_result_id;

SELECT '=== STUDENT EXAM ANSWERS ===' as info;
SELECT sea.exam_result_id, sea.question_id, sea.obtained_marks, sea.status
FROM student_exam_answers sea
ORDER BY sea.exam_result_id, sea.question_id;

SELECT '=== EXAM FEEDBACK ===' as info;
SELECT ef.exam_result_id, ef.teacher_name, ef.feedback_text
FROM exam_feedback ef
ORDER BY ef.exam_result_id;

-- ============================================================================
-- SUMMARY
-- ============================================================================
-- Created data for 3 students: John Doe (1), Jane Smith (2), Bob Wilson (3)
-- Total Exams: 6
-- Total Exam Results: 12 (4 per student)
-- Total Sections: 3
-- Total Questions: 10
-- Total Answers: 60 (10 per result)
-- Total Feedback: 6
-- ============================================================================

