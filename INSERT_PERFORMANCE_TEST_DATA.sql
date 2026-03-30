-- ============================================================================
-- Student Performance Report Test Data (March 2026)
-- ============================================================================
-- This script inserts sample data for testing the Student Performance Report API
-- Includes: Exams, Subjects, Exam Results for Student ID = 1
-- ============================================================================

-- Clear existing test data (Optional - comment out if you want to append)
DELETE FROM exam_results WHERE student_id = 1;
DELETE FROM exams WHERE exam_id > 10;

-- ============================================================================
-- 1. INSERT EXAMS (4 different types for student 1)
-- ============================================================================

-- INTERNAL ASSESSMENT EXAMS
INSERT INTO exams (exam_name, exam_date, exam_type, subject_id, start_time, duration_minutes) VALUES
  ('Internal Assessment - Mathematics', '2026-03-15'::date, 'INTERNAL', 1, '09:00:00'::time, 60),
  ('Internal Assessment - English', '2026-03-16'::date, 'INTERNAL', 2, '10:00:00'::time, 60),
  ('Internal Assessment - Science', '2026-03-17'::date, 'INTERNAL', 3, '11:00:00'::time, 60),
  ('Internal Assessment - History', '2026-03-18'::date, 'INTERNAL', 4, '14:00:00'::time, 60);

-- WEEKLY TEST EXAMS
INSERT INTO exams (exam_name, exam_date, exam_type, subject_id, start_time, duration_minutes) VALUES
  ('Weekly Test - Mathematics', '2026-03-19'::date, 'WEEKLY', 1, '09:00:00'::time, 45),
  ('Weekly Test - English', '2026-03-20'::date, 'WEEKLY', 2, '10:00:00'::time, 45),
  ('Weekly Test - Science', '2026-03-21'::date, 'WEEKLY', 3, '11:00:00'::time, 45),
  ('Weekly Test - History', '2026-03-22'::date, 'WEEKLY', 4, '14:00:00'::time, 45);

-- FINAL EXAM
INSERT INTO exams (exam_name, exam_date, exam_type, subject_id, start_time, duration_minutes) VALUES
  ('Final Exam - Mathematics', '2026-03-25'::date, 'FINAL', 1, '09:00:00'::time, 120),
  ('Final Exam - English', '2026-03-26'::date, 'FINAL', 2, '10:00:00'::time, 120),
  ('Final Exam - Science', '2026-03-27'::date, 'FINAL', 3, '11:00:00'::time, 120),
  ('Final Exam - History', '2026-03-28'::date, 'FINAL', 4, '14:00:00'::time, 120);

-- ============================================================================
-- 2. INSERT EXAM RESULTS FOR STUDENT ID = 1 (All Exams Completed)
-- ============================================================================

-- INTERNAL ASSESSMENT RESULTS (High Scores)
INSERT INTO exam_results (student_id, exam_id, subject_id, obtained_marks, total_marks, delta_percent, grade, rank, feedback) VALUES
  (1, 1, 1, 48, 50, 96, 'A', 1, 'Excellent performance in fundamental concepts'),
  (1, 2, 2, 44, 50, 88, 'A', 2, 'Good comprehension of grammar and literature'),
  (1, 3, 3, 46, 50, 92, 'A', 1, 'Strong grasp of scientific principles'),
  (1, 4, 4, 42, 50, 84, 'B+', 3, 'Good historical knowledge with minor gaps');

-- WEEKLY TEST RESULTS
INSERT INTO exam_results (student_id, exam_id, subject_id, obtained_marks, total_marks, delta_percent, grade, rank, feedback) VALUES
  (1, 5, 1, 47, 50, 94, 'A', 1, 'Consistent high performance'),
  (1, 6, 2, 43, 50, 86, 'A', 2, 'Strong writing skills'),
  (1, 7, 3, 45, 50, 90, 'A', 1, 'Excellent lab work'),
  (1, 8, 4, 40, 50, 80, 'B', 4, 'Requires more practice on analytical questions');

-- FINAL EXAM RESULTS
INSERT INTO exam_results (student_id, exam_id, subject_id, obtained_marks, total_marks, delta_percent, grade, rank, feedback) VALUES
  (1, 9, 1, 95, 100, 95, 'A', 1, 'Outstanding performance - Master the subject'),
  (1, 10, 2, 86, 100, 86, 'A', 2, 'Excellent communication skills demonstrated'),
  (1, 11, 3, 92, 100, 92, 'A', 1, 'Exceptional understanding of core concepts'),
  (1, 12, 4, 82, 100, 82, 'B+', 3, 'Good historical analysis with room for improvement');

-- ============================================================================
-- 3. VERIFY INSERTED DATA
-- ============================================================================

SELECT '=== EXAMS INSERTED ===' as verification;
SELECT exam_id, exam_name, exam_date, exam_type, subject_id FROM exams WHERE exam_id > 0 ORDER BY exam_id;

SELECT '=== EXAM RESULTS INSERTED ===' as verification;
SELECT result_id, student_id, exam_id, subject_id, obtained_marks, total_marks,
       ROUND((obtained_marks::numeric / total_marks * 100), 2) as percentage,
       grade FROM exam_results WHERE student_id = 1 ORDER BY result_id;

SELECT '=== SUBJECT PERFORMANCE SUMMARY ===' as verification;
SELECT s.subject_name,
       SUM(er.obtained_marks) as total_obtained,
       SUM(er.total_marks) as total_possible,
       ROUND((SUM(er.obtained_marks)::numeric / SUM(er.total_marks) * 100), 2) as avg_percentage,
       COUNT(*) as num_tests
FROM exam_results er
JOIN subjects s ON er.subject_id = s.subject_id
WHERE er.student_id = 1
GROUP BY s.subject_name
ORDER BY avg_percentage DESC;

SELECT '=== OVERALL PERFORMANCE ===' as verification;
SELECT ROUND((SUM(obtained_marks)::numeric / SUM(total_marks) * 100), 2) as overall_percentage,
       COUNT(*) as total_exams,
       COUNT(DISTINCT subject_id) as subjects_covered
FROM exam_results
WHERE student_id = 1;

