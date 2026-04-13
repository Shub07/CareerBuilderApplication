-- ============================================================================
-- Career Builder Backend - Complete Test Data for 5 Users
-- ============================================================================
-- This script fills data for 5 students with complete relationships
-- Date: April 7, 2026
-- ============================================================================

-- ============================================================================
-- 1. INSERT SCHOOLS (Base table - needed before students)
-- ============================================================================
INSERT INTO schools (name, address, phone, email, principal_name, established_year, website)
VALUES
    (1, 'Delhi Public School', '123 Education Lane, New Delhi', '9876543210', 'dps@school.com', 'Dr. Rajesh Kumar', 1995, 'www.dpsnewdelhi.edu'),
    (2, 'St. Mary High School', '456 Academic Road, Mumbai', '9876543211', 'stmary@school.com', 'Sr. Catherine Brown', 1987, 'www.stmary.edu'),
    (3, 'Kendriya Vidyalaya', '789 Central Ave, Bangalore', '9876543212', 'kv@school.com', 'Mrs. Priya Sharma', 2000, 'www.kvbangalore.edu'),
    (4, 'Cathedral School', '321 Heritage Road, Kolkata', '9876543213', 'cathedral@school.com', 'Mr. Thomas Wilson', 1992, 'www.cathedral.edu'),
    (5, 'Modern International School', '654 Future Lane, Pune', '9876543214', 'modern@school.com', 'Ms. Anjali Deshmukh', 2005, 'www.moderninternational.edu');

-- ============================================================================
-- 2. INSERT STUDENTS (5 students - 1 from each school)
-- ============================================================================
INSERT INTO students (first_name, last_name, age, class_name, section, roll_no, parent_name, phone, email, address, school_id)
VALUES
    (1, 'Aarav', 'Patel', 15, '10A', 'A', 1, 'Rajesh Patel', '9876543210', 'aarav.patel@email.com', '123 Student Lane, Delhi', 1),
    (2, 'Ananya', 'Verma', 14, '9B', 'B', 2, 'Priya Verma', '9876543211', 'ananya.verma@email.com', '456 Scholar Road, Mumbai', 2),
    (3, 'Arjun', 'Gupta', 16, '11C', 'C', 3, 'Amit Gupta', '9876543212', 'arjun.gupta@email.com', '789 Academy Street, Bangalore', 3),
    (4, 'Avni', 'Das', 15, '10A', 'A', 4, 'Suresh Das', '9876543213', 'avni.das@email.com', '321 Knowledge Lane, Kolkata', 4),
    (5, 'Aditya', 'Singh', 14, '9B', 'B', 5, 'Vikram Singh', '9876543214', 'aditya.singh@email.com', '654 Future Road, Pune', 5);

-- ============================================================================
-- 3. INSERT SUBJECTS (Need for MyClasses relationship)
-- ============================================================================
INSERT INTO subjects (subject_name, subject_code, description, credits)
VALUES
    (1, 'Mathematics', 'MATH101', 'Advanced Mathematics for Grade 10', 4),
    (2, 'English', 'ENG101', 'English Language and Literature', 3),
    (3, 'Science', 'SCI101', 'Physics, Chemistry and Biology', 4),
    (4, 'Social Studies', 'SOC101', 'History, Geography and Civics', 3),
    (5, 'Computer Science', 'CS101', 'Introduction to Programming', 4);

-- ============================================================================
-- 4. INSERT MY CLASSES (Student-Subject relationship) - 5 entries per student
-- ============================================================================
INSERT INTO my_classes (student_id, subject_id, enrollment_date, status, semester)
VALUES
    -- Aarav's classes
    (1, 1, '2026-01-15', 'ACTIVE', 'SEMESTER_1'),
    (1, 2, '2026-01-15', 'ACTIVE', 'SEMESTER_1'),
    (1, 3, '2026-01-15', 'ACTIVE', 'SEMESTER_1'),
    (1, 4, '2026-01-15', 'ACTIVE', 'SEMESTER_1'),
    (1, 5, '2026-01-15', 'ACTIVE', 'SEMESTER_1'),

    -- Ananya's classes
    (2, 1, '2026-01-15', 'ACTIVE', 'SEMESTER_1'),
    (2, 2, '2026-01-15', 'ACTIVE', 'SEMESTER_1'),
    (2, 3, '2026-01-15', 'ACTIVE', 'SEMESTER_1'),
    (2, 4, '2026-01-15', 'ACTIVE', 'SEMESTER_1'),
    (2, 5, '2026-01-15', 'ACTIVE', 'SEMESTER_1'),

    -- Arjun's classes
    (3, 1, '2026-01-15', 'ACTIVE', 'SEMESTER_1'),
    (3, 2, '2026-01-15', 'ACTIVE', 'SEMESTER_1'),
    (3, 3, '2026-01-15', 'ACTIVE', 'SEMESTER_1'),
    (3, 4, '2026-01-15', 'ACTIVE', 'SEMESTER_1'),
    (3, 5, '2026-01-15', 'ACTIVE', 'SEMESTER_1'),

    -- Avni's classes
    (4, 1, '2026-01-15', 'ACTIVE', 'SEMESTER_1'),
    (4, 2, '2026-01-15', 'ACTIVE', 'SEMESTER_1'),
    (4, 3, '2026-01-15', 'ACTIVE', 'SEMESTER_1'),
    (4, 4, '2026-01-15', 'ACTIVE', 'SEMESTER_1'),
    (4, 5, '2026-01-15', 'ACTIVE', 'SEMESTER_1'),

    -- Aditya's classes
    (5, 1, '2026-01-15', 'ACTIVE', 'SEMESTER_1'),
    (5, 2, '2026-01-15', 'ACTIVE', 'SEMESTER_1'),
    (5, 3, '2026-01-15', 'ACTIVE', 'SEMESTER_1'),
    (5, 4, '2026-01-15', 'ACTIVE', 'SEMESTER_1'),
    (5, 5, '2026-01-15', 'ACTIVE', 'SEMESTER_1');

-- ============================================================================
-- 5. INSERT FEES (5 fee entries per student)
-- ============================================================================
INSERT INTO fees (student_id, fee_type, amount, total_amount, due_date, status, payment_deadline, remarks, created_at, updated_at)
VALUES
    -- Aarav's fees
    (1, 'TUITION', 50000.00, 50000.00, '2026-02-28', 'PAID', '2026-03-15', 'February tuition fee', '2026-01-20', '2026-02-15'),
    (1, 'EXAMINATION', 5000.00, 5000.00, '2026-03-31', 'PENDING', '2026-04-15', 'Board examination fee', '2026-01-20', '2026-01-20'),
    (1, 'LIBRARY', 2000.00, 2000.00, '2026-04-30', 'PENDING', '2026-05-15', 'Library membership fee', '2026-01-20', '2026-01-20'),
    (1, 'SPORTS', 3000.00, 3000.00, '2026-05-31', 'PENDING', '2026-06-15', 'Sports activity fee', '2026-01-20', '2026-01-20'),
    (1, 'HOSTEL', 8000.00, 8000.00, '2026-06-30', 'PENDING', '2026-07-15', 'Hostel accommodation fee', '2026-01-20', '2026-01-20'),

    -- Ananya's fees
    (2, 'TUITION', 45000.00, 45000.00, '2026-02-28', 'PAID', '2026-03-15', 'February tuition fee', '2026-01-20', '2026-02-10'),
    (2, 'EXAMINATION', 5000.00, 5000.00, '2026-03-31', 'PAID', '2026-04-15', 'Board examination fee', '2026-01-20', '2026-03-25'),
    (2, 'LIBRARY', 2000.00, 2000.00, '2026-04-30', 'PENDING', '2026-05-15', 'Library membership fee', '2026-01-20', '2026-01-20'),
    (2, 'SPORTS', 2500.00, 2500.00, '2026-05-31', 'PENDING', '2026-06-15', 'Sports activity fee', '2026-01-20', '2026-01-20'),
    (2, 'HOSTEL', 8000.00, 8000.00, '2026-06-30', 'PENDING', '2026-07-15', 'Hostel accommodation fee', '2026-01-20', '2026-01-20'),

    -- Arjun's fees
    (3, 'TUITION', 52000.00, 52000.00, '2026-02-28', 'OVERDUE', '2026-03-15', 'February tuition fee', '2026-01-20', '2026-04-05'),
    (3, 'EXAMINATION', 5000.00, 5000.00, '2026-03-31', 'PENDING', '2026-04-15', 'Board examination fee', '2026-01-20', '2026-01-20'),
    (3, 'LIBRARY', 2000.00, 2000.00, '2026-04-30', 'PENDING', '2026-05-15', 'Library membership fee', '2026-01-20', '2026-01-20'),
    (3, 'SPORTS', 3500.00, 3500.00, '2026-05-31', 'PENDING', '2026-06-15', 'Sports activity fee', '2026-01-20', '2026-01-20'),
    (3, 'HOSTEL', 8000.00, 8000.00, '2026-06-30', 'PENDING', '2026-07-15', 'Hostel accommodation fee', '2026-01-20', '2026-01-20'),

    -- Avni's fees
    (4, 'TUITION', 48000.00, 48000.00, '2026-02-28', 'PAID', '2026-03-15', 'February tuition fee', '2026-01-20', '2026-02-20'),
    (4, 'EXAMINATION', 5000.00, 5000.00, '2026-03-31', 'PAID', '2026-04-15', 'Board examination fee', '2026-01-20', '2026-03-30'),
    (4, 'LIBRARY', 2000.00, 2000.00, '2026-04-30', 'PENDING', '2026-05-15', 'Library membership fee', '2026-01-20', '2026-01-20'),
    (4, 'SPORTS', 2500.00, 2500.00, '2026-05-31', 'PENDING', '2026-06-15', 'Sports activity fee', '2026-01-20', '2026-01-20'),
    (4, 'HOSTEL', 8000.00, 8000.00, '2026-06-30', 'PENDING', '2026-07-15', 'Hostel accommodation fee', '2026-01-20', '2026-01-20'),

    -- Aditya's fees
    (5, 'TUITION', 46000.00, 46000.00, '2026-02-28', 'PENDING', '2026-03-15', 'February tuition fee', '2026-01-20', '2026-01-20'),
    (5, 'EXAMINATION', 5000.00, 5000.00, '2026-03-31', 'PENDING', '2026-04-15', 'Board examination fee', '2026-01-20', '2026-01-20'),
    (5, 'LIBRARY', 2000.00, 2000.00, '2026-04-30', 'PENDING', '2026-05-15', 'Library membership fee', '2026-01-20', '2026-01-20'),
    (5, 'SPORTS', 3000.00, 3000.00, '2026-05-31', 'PENDING', '2026-06-15', 'Sports activity fee', '2026-01-20', '2026-01-20'),
    (5, 'HOSTEL', 8000.00, 8000.00, '2026-06-30', 'PENDING', '2026-07-15', 'Hostel accommodation fee', '2026-01-20', '2026-01-20');

-- ============================================================================
-- 6. INSERT PAYMENT TRANSACTIONS (Payments for paid fees)
-- ============================================================================
INSERT INTO payment_transactions (student_id, fee_id, amount, payment_method, transaction_status, transaction_reference_id, gateway_response, paid_at, created_at, idempotency_key, remarks)
VALUES
    -- Aarav's payments
    (1, 1, 50000.00, 'RAZORPAY', 'SUCCESS', 'pay_aarav_tuition_feb', '{"razorpay_payment_id":"pay_aarav_001"}', '2026-02-15', '2026-02-15', 'idempotency_aarav_1', 'Tuition fee paid via Razorpay'),

    -- Ananya's payments
    (2, 6, 45000.00, 'RAZORPAY', 'SUCCESS', 'pay_ananya_tuition_feb', '{"razorpay_payment_id":"pay_ananya_001"}', '2026-02-10', '2026-02-10', 'idempotency_ananya_1', 'Tuition fee paid'),
    (2, 7, 5000.00, 'RAZORPAY', 'SUCCESS', 'pay_ananya_exam', '{"razorpay_payment_id":"pay_ananya_002"}', '2026-03-25', '2026-03-25', 'idempotency_ananya_2', 'Exam fee paid'),

    -- Arjun's payments (None - overdue)

    -- Avni's payments
    (4, 16, 48000.00, 'RAZORPAY', 'SUCCESS', 'pay_avni_tuition_feb', '{"razorpay_payment_id":"pay_avni_001"}', '2026-02-20', '2026-02-20', 'idempotency_avni_1', 'Tuition fee paid'),
    (4, 17, 5000.00, 'RAZORPAY', 'SUCCESS', 'pay_avni_exam', '{"razorpay_payment_id":"pay_avni_002"}', '2026-03-30', '2026-03-30', 'idempotency_avni_2', 'Exam fee paid'),

    -- Aditya's payments (None - all pending)

    -- Additional payment records to have at least 5
    (1, 2, 5000.00, 'RAZORPAY', 'SUCCESS', 'pay_aarav_exam', '{"razorpay_payment_id":"pay_aarav_002"}', '2026-03-25', '2026-03-25', 'idempotency_aarav_2', 'Exam fee paid');

-- ============================================================================
-- 7. INSERT ATTENDANCE RECORDS (5 records per student)
-- ============================================================================
INSERT INTO attendance_records (student_id, att_date, status, class_name, section, remarks, created_at, updated_at)
VALUES
    -- Aarav's attendance
    (1, '2026-04-01', 'PRESENT', '10A', 'A', 'Regular', '2026-04-01', '2026-04-01'),
    (1, '2026-04-02', 'PRESENT', '10A', 'A', 'Regular', '2026-04-02', '2026-04-02'),
    (1, '2026-04-03', 'ABSENT', '10A', 'A', 'Medical leave pending', '2026-04-03', '2026-04-03'),
    (1, '2026-04-04', 'LEAVE', '10A', 'A', 'Approved leave', '2026-04-04', '2026-04-04'),
    (1, '2026-04-05', 'PRESENT', '10A', 'A', 'Regular', '2026-04-05', '2026-04-05'),

    -- Ananya's attendance
    (2, '2026-04-01', 'PRESENT', '9B', 'B', 'Regular', '2026-04-01', '2026-04-01'),
    (2, '2026-04-02', 'PRESENT', '9B', 'B', 'Regular', '2026-04-02', '2026-04-02'),
    (2, '2026-04-03', 'PRESENT', '9B', 'B', 'Regular', '2026-04-03', '2026-04-03'),
    (2, '2026-04-04', 'PRESENT', '9B', 'B', 'Regular', '2026-04-04', '2026-04-04'),
    (2, '2026-04-05', 'ABSENT', '9B', 'B', 'Sick leave', '2026-04-05', '2026-04-05'),

    -- Arjun's attendance
    (3, '2026-04-01', 'PRESENT', '11C', 'C', 'Regular', '2026-04-01', '2026-04-01'),
    (3, '2026-04-02', 'ABSENT', '11C', 'C', 'No reason provided', '2026-04-02', '2026-04-02'),
    (3, '2026-04-03', 'ABSENT', '11C', 'C', 'No reason provided', '2026-04-03', '2026-04-03'),
    (3, '2026-04-04', 'PRESENT', '11C', 'C', 'Regular', '2026-04-04', '2026-04-04'),
    (3, '2026-04-05', 'LEAVE', '11C', 'C', 'Family emergency', '2026-04-05', '2026-04-05'),

    -- Avni's attendance
    (4, '2026-04-01', 'PRESENT', '10A', 'A', 'Regular', '2026-04-01', '2026-04-01'),
    (4, '2026-04-02', 'PRESENT', '10A', 'A', 'Regular', '2026-04-02', '2026-04-02'),
    (4, '2026-04-03', 'PRESENT', '10A', 'A', 'Regular', '2026-04-03', '2026-04-03'),
    (4, '2026-04-04', 'ABSENT', '10A', 'A', 'Doctor appointment', '2026-04-04', '2026-04-04'),
    (4, '2026-04-05', 'PRESENT', '10A', 'A', 'Regular', '2026-04-05', '2026-04-05'),

    -- Aditya's attendance
    (5, '2026-04-01', 'PRESENT', '9B', 'B', 'Regular', '2026-04-01', '2026-04-01'),
    (5, '2026-04-02', 'PRESENT', '9B', 'B', 'Regular', '2026-04-02', '2026-04-02'),
    (5, '2026-04-03', 'LEAVE', '9B', 'B', 'Sports event', '2026-04-03', '2026-04-03'),
    (5, '2026-04-04', 'PRESENT', '9B', 'B', 'Regular', '2026-04-04', '2026-04-04'),
    (5, '2026-04-05', 'PRESENT', '9B', 'B', 'Regular', '2026-04-05', '2026-04-05');

-- ============================================================================
-- 8. INSERT VACATIONS (5 vacations across schools)
-- ============================================================================
INSERT INTO vacations (school_id, vacation_name, vacation_type, start_date, end_date, description, is_active, created_at, updated_at)
VALUES
    (1, 'Winter Break', 'WINTER', '2026-12-20', '2027-01-05', 'Year-end winter vacation', true, '2026-01-20', '2026-01-20'),
    (2, 'Summer Vacation', 'SUMMER', '2026-05-15', '2026-06-15', 'Long summer vacation', true, '2026-01-20', '2026-01-20'),
    (3, 'Exam Break', 'EXAM_BREAK', '2026-04-01', '2026-04-30', 'Preparation break for board exams', true, '2026-01-20', '2026-01-20'),
    (4, 'Spring Holiday', 'SPRING', '2026-03-15', '2026-03-20', 'Spring break', true, '2026-01-20', '2026-01-20'),
    (5, 'Special Holiday - Diwali', 'HOLIDAY', '2026-10-24', '2026-10-26', 'Festival of lights - Diwali', true, '2026-01-20', '2026-01-20');

-- ============================================================================
-- 9. INSERT EXAMS/TESTS (if exam tables exist)
-- ============================================================================
-- Note: This section depends on your exam tables structure
-- INSERT INTO exams (subject_id, exam_name, exam_date, total_marks, passing_marks, duration_minutes, exam_type)
-- VALUES...

-- ============================================================================
-- 10. INSERT STUDENT EXAM ANSWERS (if applicable)
-- ============================================================================
-- Note: This section depends on your exam answer structure
-- INSERT INTO student_exam_answers...

-- ============================================================================
-- SUMMARY OF DATA INSERTED
-- ============================================================================
-- Schools: 5 rows
-- Students: 5 rows (1 per school)
-- Subjects: 5 rows
-- My Classes: 25 rows (5 subjects per student)
-- Fees: 25 rows (5 fees per student)
-- Payment Transactions: 6 rows (payments for some students)
-- Attendance Records: 25 rows (5 records per student)
-- Vacations: 5 rows
-- TOTAL: 101 rows of diverse, related data
-- ============================================================================

COMMIT;

