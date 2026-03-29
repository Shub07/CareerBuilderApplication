-- ============================================================================
-- VERIFICATION SCRIPT - Check if test data exists
-- ============================================================================

-- Run these queries one by one to diagnose the 500 error

-- 1. Check if exams table has data
SELECT '=== EXAMS ===' as info;
SELECT COUNT(*) as total_exams FROM exams;
SELECT * FROM exams LIMIT 5;

-- 2. Check if exam_results table has data
SELECT '=== EXAM RESULTS ===' as info;
SELECT COUNT(*) as total_results FROM exam_results;
SELECT * FROM exam_results WHERE student_id = 1 LIMIT 5;

-- 3. Check specific student data
SELECT '=== STUDENT 1 RESULTS ===' as info;
SELECT
    er.id,
    s.first_name,
    e.exam_name,
    er.obtained_marks,
    er.total_marks,
    e.exam_date
FROM exam_results er
JOIN students s ON er.student_id = s.id
JOIN exams e ON er.exam_id = e.id
WHERE er.student_id = 1
ORDER BY e.exam_date DESC;

-- 4. Check exam sections
SELECT '=== EXAM SECTIONS ===' as info;
SELECT COUNT(*) as total_sections FROM exam_sections;
SELECT * FROM exam_sections LIMIT 5;

-- 5. Check exam result breakdown
SELECT '=== RESULT BREAKDOWNS ===' as info;
SELECT COUNT(*) as total_breakdowns FROM exam_result_breakdown;
SELECT * FROM exam_result_breakdown LIMIT 5;

-- 6. Check student answers
SELECT '=== STUDENT ANSWERS ===' as info;
SELECT COUNT(*) as total_answers FROM student_exam_answers;
SELECT * FROM student_exam_answers LIMIT 5;

-- 7. Check feedback
SELECT '=== FEEDBACK ===' as info;
SELECT COUNT(*) as total_feedback FROM exam_feedback;
SELECT * FROM exam_feedback LIMIT 5;

-- 8. Check upcoming exams (simulate query)
SELECT '=== UPCOMING EXAMS (Student 1) ===' as info;
SELECT
    er.id,
    e.exam_name,
    e.exam_date,
    CURRENT_DATE as today,
    (e.exam_date >= CURRENT_DATE) as is_upcoming
FROM exam_results er
JOIN exams e ON er.exam_id = e.id
WHERE er.student_id = 1
AND e.exam_date >= CURRENT_DATE
ORDER BY e.exam_date ASC;

-- 9. Check completed exams (simulate query)
SELECT '=== COMPLETED EXAMS (Student 1) ===' as info;
SELECT
    er.id,
    e.exam_name,
    e.exam_date,
    er.obtained_marks,
    er.total_marks,
    CURRENT_DATE as today
FROM exam_results er
JOIN exams e ON er.exam_id = e.id
WHERE er.student_id = 1
ORDER BY e.exam_date DESC;

-- 10. Summary
SELECT '=== SUMMARY ===' as info;
SELECT
    'Exams' as table_name, COUNT(*) as row_count FROM exams
UNION ALL
SELECT 'Students' as table_name, COUNT(*) as row_count FROM students
UNION ALL
SELECT 'Exam Results' as table_name, COUNT(*) as row_count FROM exam_results
UNION ALL
SELECT 'Exam Sections' as table_name, COUNT(*) as row_count FROM exam_sections
UNION ALL
SELECT 'Result Breakdowns' as table_name, COUNT(*) as row_count FROM exam_result_breakdown
UNION ALL
SELECT 'Student Answers' as table_name, COUNT(*) as row_count FROM student_exam_answers
UNION ALL
SELECT 'Exam Feedback' as table_name, COUNT(*) as row_count FROM exam_feedback;

