-- ============================================================================
-- EXAMS & TESTS TABLE SCHEMA - Complete Database Structure
-- ============================================================================

-- 1. EXAMS TABLE (Already exists - showing for reference)
-- CREATE TABLE exams (
--     exam_id BIGINT PRIMARY KEY AUTO_INCREMENT,
--     exam_name VARCHAR(150) NOT NULL,
--     exam_date DATE NOT NULL,
--     exam_type VARCHAR(50) NOT NULL,
--     subject_id BIGINT NOT NULL,
--     start_time TIME,
--     duration_minutes INTEGER
-- );

-- ============================================================================
-- 2. EXAM_SECTIONS TABLE (For Marks Breakdown)
-- ============================================================================
CREATE TABLE IF NOT EXISTS exam_sections (
    section_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    exam_id BIGINT NOT NULL,
    section_name VARCHAR(100) NOT NULL,
    section_type VARCHAR(50),  -- MCQ, SHORT_ANSWER, LONG_ANSWER, PRACTICAL
    total_marks INTEGER NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (exam_id) REFERENCES exams(exam_id) ON DELETE CASCADE,
    UNIQUE KEY uk_exam_section (exam_id, section_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================================
-- 3. EXAM_RESULT_BREAKDOWN TABLE (Student marks per section)
-- ============================================================================
CREATE TABLE IF NOT EXISTS exam_result_breakdown (
    breakdown_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    exam_result_id BIGINT NOT NULL,
    section_id BIGINT NOT NULL,
    section_name VARCHAR(100) NOT NULL,
    obtained_marks INTEGER NOT NULL,
    total_marks INTEGER NOT NULL,
    percentage DECIMAL(5,2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (exam_result_id) REFERENCES exam_results(result_id) ON DELETE CASCADE,
    FOREIGN KEY (section_id) REFERENCES exam_sections(section_id),
    INDEX idx_result_section (exam_result_id, section_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================================
-- 4. EXAM_QUESTIONS TABLE (For question-wise analysis)
-- ============================================================================
CREATE TABLE IF NOT EXISTS exam_questions (
    question_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    exam_id BIGINT NOT NULL,
    section_id BIGINT NOT NULL,
    question_number INTEGER NOT NULL,
    question_text VARCHAR(500),
    marks INTEGER NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (exam_id) REFERENCES exams(exam_id) ON DELETE CASCADE,
    FOREIGN KEY (section_id) REFERENCES exam_sections(section_id) ON DELETE CASCADE,
    UNIQUE KEY uk_exam_question (exam_id, question_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================================
-- 5. STUDENT_EXAM_ANSWERS TABLE (Student answer to each question)
-- ============================================================================
CREATE TABLE IF NOT EXISTS student_exam_answers (
    answer_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    exam_result_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    obtained_marks INTEGER NOT NULL,
    status VARCHAR(50),  -- CORRECT, WRONG, PARTIAL
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (exam_result_id) REFERENCES exam_results(result_id) ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES exam_questions(question_id) ON DELETE CASCADE,
    UNIQUE KEY uk_result_question (exam_result_id, question_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================================
-- 6. EXAM_SCHEDULES TABLE (Upcoming exams)
-- ============================================================================
CREATE TABLE IF NOT EXISTS exam_schedules (
    schedule_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    exam_id BIGINT NOT NULL,
    class_id BIGINT,
    section_id BIGINT,
    scheduled_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    is_published BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (exam_id) REFERENCES exams(exam_id) ON DELETE CASCADE,
    INDEX idx_schedule_date (scheduled_date),
    INDEX idx_schedule_class (class_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================================
-- 7. EXAM_FEEDBACK TABLE (Teacher feedback)
-- ============================================================================
CREATE TABLE IF NOT EXISTS exam_feedback (
    feedback_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    exam_result_id BIGINT NOT NULL,
    teacher_id BIGINT,
    teacher_name VARCHAR(100),
    feedback_text VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (exam_result_id) REFERENCES exam_results(result_id) ON DELETE CASCADE,
    UNIQUE KEY uk_result_feedback (exam_result_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================================
-- 8. EXAM_PERFORMANCE_COMPARISON TABLE (For class average, highest score, rank)
-- ============================================================================
CREATE TABLE IF NOT EXISTS exam_performance_comparison (
    comparison_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    exam_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    class_average DECIMAL(5,2),
    highest_score DECIMAL(5,2),
    student_rank INTEGER,
    total_students_count INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (exam_id) REFERENCES exams(exam_id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    UNIQUE KEY uk_exam_student (exam_id, student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================================
-- ADD INDEXES FOR PERFORMANCE
-- ============================================================================

CREATE INDEX idx_exam_date ON exams(exam_date);
CREATE INDEX idx_exam_type ON exams(exam_type);
CREATE INDEX idx_exam_subject ON exams(subject_id);

CREATE INDEX idx_result_student ON exam_results(student_id);
CREATE INDEX idx_result_exam ON exam_results(exam_id);
CREATE INDEX idx_result_subject ON exam_results(subject_id);

-- ============================================================================
-- SAMPLE DATA FOR TESTING
-- ============================================================================

-- Assuming exams, subjects, and students already exist
-- Insert sample exam sections
INSERT INTO exam_sections (exam_id, section_name, section_type, total_marks) VALUES
(1, 'Section A: MCQ', 'MCQ', 20),
(1, 'Section B: Short Answer', 'SHORT_ANSWER', 40),
(1, 'Section C: Long Answer', 'LONG_ANSWER', 40);

-- Insert exam questions
INSERT INTO exam_questions (exam_id, section_id, question_number, question_text, marks) VALUES
(1, 1, 1, 'Question about laws of motion', 5),
(1, 1, 2, 'Question about energy', 5),
(1, 1, 3, 'Question about velocity', 5),
(1, 1, 4, 'Question about acceleration', 5),
(1, 2, 5, 'Short question about forces', 8),
(1, 2, 6, 'Short question about momentum', 8),
(1, 2, 7, 'Short question about power', 8),
(1, 2, 8, 'Short question about work', 8),
(1, 3, 9, 'Long question about motion laws', 20),
(1, 3, 10, 'Long question about energy conservation', 20);

-- Insert exam result breakdown
INSERT INTO exam_result_breakdown (exam_result_id, section_id, section_name, obtained_marks, total_marks, percentage) VALUES
(1, 1, 'Section A: MCQ', 18, 20, 90.00),
(1, 2, 'Section B: Short Answer', 36, 40, 90.00),
(1, 3, 'Section C: Long Answer', 38, 40, 95.00);

-- Insert student exam answers
INSERT INTO student_exam_answers (exam_result_id, question_id, obtained_marks, status) VALUES
(1, 1, 5, 'CORRECT'),
(1, 2, 5, 'CORRECT'),
(1, 3, 4, 'PARTIAL'),
(1, 4, 4, 'CORRECT'),
(1, 5, 8, 'CORRECT'),
(1, 6, 7, 'PARTIAL'),
(1, 7, 8, 'CORRECT'),
(1, 8, 8, 'CORRECT'),
(1, 9, 18, 'CORRECT'),
(1, 10, 20, 'CORRECT');

-- Insert exam schedule
INSERT INTO exam_schedules (exam_id, class_id, section_id, scheduled_date, start_time, end_time, is_published) VALUES
(1, 1, 1, '2026-12-08', '10:00:00', '12:00:00', TRUE);

-- Insert exam feedback
INSERT INTO exam_feedback (exam_result_id, teacher_id, teacher_name, feedback_text) VALUES
(1, 1, 'Mrs. Lucia', 'Excellent performance. Focus more on proof-based questions in the future.');

-- Insert performance comparison
INSERT INTO exam_performance_comparison (exam_id, student_id, class_average, highest_score, student_rank, total_students_count) VALUES
(1, 1, 78.00, 98.00, 5, 30);

-- ============================================================================
-- VERIFICATION QUERIES
-- ============================================================================

SELECT '=== EXAM SECTIONS ===' as section;
SELECT * FROM exam_sections;

SELECT '=== EXAM QUESTIONS ===' as section;
SELECT * FROM exam_questions;

SELECT '=== EXAM RESULT BREAKDOWN ===' as section;
SELECT * FROM exam_result_breakdown;

SELECT '=== STUDENT EXAM ANSWERS ===' as section;
SELECT * FROM student_exam_answers;

SELECT '=== EXAM SCHEDULES ===' as section;
SELECT * FROM exam_schedules;

SELECT '=== EXAM FEEDBACK ===' as section;
SELECT * FROM exam_feedback;

SELECT '=== EXAM PERFORMANCE COMPARISON ===' as section;
SELECT * FROM exam_performance_comparison;

