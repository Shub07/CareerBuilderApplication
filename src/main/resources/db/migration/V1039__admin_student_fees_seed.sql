-- Demo seed for Admin Student Fees module (matches Figma: Rahul Verma ledger + list KPIs)
-- Idempotent: safe to re-run; uses school_id = 19 (consistent with teacher portal seeds)

DO $$
DECLARE
    v_school_id       BIGINT := 19;
    v_class           VARCHAR(50) := 'Grade 10';
    v_section         VARCHAR(10) := 'A';
    v_academic_year   VARCHAR(20) := '2025-2026';
    v_rahul_id        BIGINT;
    v_fee_q1          BIGINT;
    v_fee_q2          BIGINT;
    v_fee_q3          BIGINT;
    v_fee_exam        BIGINT;
    v_txn_q1          BIGINT;
    v_txn_q2          BIGINT;
    v_structure_id    BIGINT;
BEGIN
    -- ── Student: Rahul Verma ──────────────────────────────────────────────────
    INSERT INTO students (
        address, age, class_name, email, first_name, last_name,
        parent_name, phone, roll_no, school_id, section
    )
    SELECT '12 MG Road, Pune', 15, v_class, 'rahul.verma@fees-demo.local',
           'Rahul', 'Verma', 'Mr. Verma', '9876500001', 1, v_school_id, v_section
    WHERE NOT EXISTS (
        SELECT 1 FROM students s
        WHERE s.school_id = v_school_id AND s.class_name = v_class
          AND s.section = v_section AND s.first_name = 'Rahul' AND s.last_name = 'Verma'
    );

    SELECT id INTO v_rahul_id FROM students
    WHERE school_id = v_school_id AND class_name = v_class AND section = v_section
      AND first_name = 'Rahul' AND last_name = 'Verma'
    LIMIT 1;

    IF v_rahul_id IS NULL THEN
        RAISE NOTICE 'Rahul Verma not found — skipping fee seed';
        RETURN;
    END IF;

    -- Profile with admission number from Figma
    INSERT INTO student_profiles (
        student_id, admission_number, admission_date, gender, date_of_birth,
        academic_year, student_type
    )
    SELECT v_rahul_id, 'ADM2024001', DATE '2024-04-01', 'MALE', DATE '2010-06-15',
           v_academic_year, 'NEW_ADMISSION'
    WHERE NOT EXISTS (
        SELECT 1 FROM student_profiles sp WHERE sp.student_id = v_rahul_id
    );

    -- ── Fee structure template ────────────────────────────────────────────────
    INSERT INTO fee_structures (
        school_id, fee_type, fee_name, target_class_name, target_section,
        academic_year, amount, frequency, due_date, late_fee_enabled, active, deleted
    )
    SELECT v_school_id, 'Tuition Fee', 'Annual Tuition Fee', v_class, v_section,
           v_academic_year, 60000.00, 'QUARTERLY', DATE '2026-04-10', FALSE, TRUE, FALSE
    WHERE NOT EXISTS (
        SELECT 1 FROM fee_structures fs
        WHERE fs.school_id = v_school_id AND fs.fee_type = 'Tuition Fee'
          AND fs.target_class_name = v_class AND fs.deleted = FALSE
    );

    SELECT id INTO v_structure_id FROM fee_structures
    WHERE school_id = v_school_id AND fee_type = 'Tuition Fee'
      AND target_class_name = v_class AND deleted = FALSE
    LIMIT 1;

    -- ── Obligations (Fee Breakdown table from Figma) ──────────────────────────
    -- Q1: PAID
    INSERT INTO fees (student_id, fee_type, amount, total_amount, due_date, status,
                      paid_amount, academic_year, term, description, version)
    SELECT v_rahul_id, 'Tuition Fee (Q1)', 15000.00, 15000.00, DATE '2024-04-10',
           'PAID', 15000.00, v_academic_year, 'Q1', 'Tuition Fee Quarter 1', 0
    WHERE NOT EXISTS (
        SELECT 1 FROM fees f WHERE f.student_id = v_rahul_id
          AND f.fee_type = 'Tuition Fee (Q1)' AND f.academic_year = v_academic_year
    );

    -- Q2: PAID
    INSERT INTO fees (student_id, fee_type, amount, total_amount, due_date, status,
                      paid_amount, academic_year, term, description, version)
    SELECT v_rahul_id, 'Tuition Fee (Q2)', 15000.00, 15000.00, DATE '2024-07-10',
           'PAID', 15000.00, v_academic_year, 'Q2', 'Tuition Fee Quarter 2', 0
    WHERE NOT EXISTS (
        SELECT 1 FROM fees f WHERE f.student_id = v_rahul_id
          AND f.fee_type = 'Tuition Fee (Q2)' AND f.academic_year = v_academic_year
    );

    -- Q3: OVERDUE (due in past, unpaid)
    INSERT INTO fees (student_id, fee_type, amount, total_amount, due_date, status,
                      paid_amount, academic_year, term, description, version)
    SELECT v_rahul_id, 'Tuition Fee (Q3)', 15000.00, 15000.00, DATE '2024-10-10',
           'OVERDUE', 0.00, v_academic_year, 'Q3', 'Tuition Fee Quarter 3', 0
    WHERE NOT EXISTS (
        SELECT 1 FROM fees f WHERE f.student_id = v_rahul_id
          AND f.fee_type = 'Tuition Fee (Q3)' AND f.academic_year = v_academic_year
    );

    -- Exam Fee: PENDING
    INSERT INTO fees (student_id, fee_type, amount, total_amount, due_date, status,
                      paid_amount, academic_year, term, description, version)
    SELECT v_rahul_id, 'Exam Fee', 5000.00, 5000.00, DATE '2024-11-15',
           'PENDING', 0.00, v_academic_year, NULL, 'Annual Examination Fee', 0
    WHERE NOT EXISTS (
        SELECT 1 FROM fees f WHERE f.student_id = v_rahul_id
          AND f.fee_type = 'Exam Fee' AND f.academic_year = v_academic_year
    );

    SELECT id INTO v_fee_q1 FROM fees WHERE student_id = v_rahul_id AND fee_type = 'Tuition Fee (Q1)' LIMIT 1;
    SELECT id INTO v_fee_q2 FROM fees WHERE student_id = v_rahul_id AND fee_type = 'Tuition Fee (Q2)' LIMIT 1;
    SELECT id INTO v_fee_q3 FROM fees WHERE student_id = v_rahul_id AND fee_type = 'Tuition Fee (Q3)' LIMIT 1;
    SELECT id INTO v_fee_exam FROM fees WHERE student_id = v_rahul_id AND fee_type = 'Exam Fee' LIMIT 1;

    -- ── Payment history (ledger transactions) ───────────────────────────────────
    INSERT INTO fee_transactions (
        school_id, student_id, fee_id, fee_type, type, amount, mode,
        transaction_date, reference_number, receipt_number, collected_by
    )
    SELECT v_school_id, v_rahul_id, v_fee_q1, 'Tuition Fee (Q1)', 'COLLECTION',
           15000.00, 'UPI', DATE '2024-04-05', 'UPI982734', 'INV-2024-0001', 'Admin User'
    WHERE NOT EXISTS (
        SELECT 1 FROM fee_transactions t WHERE t.receipt_number = 'INV-2024-0001'
    );

    INSERT INTO fee_transactions (
        school_id, student_id, fee_id, fee_type, type, amount, mode,
        transaction_date, reference_number, receipt_number, collected_by
    )
    SELECT v_school_id, v_rahul_id, v_fee_q2, 'Tuition Fee (Q2)', 'COLLECTION',
           15000.00, 'BANK_TRANSFER', DATE '2024-07-08', 'TXN882711', 'INV-2024-0002', 'Finance Dept'
    WHERE NOT EXISTS (
        SELECT 1 FROM fee_transactions t WHERE t.receipt_number = 'INV-2024-0002'
    );

    -- Receipt counter baseline
    INSERT INTO receipt_counters (year, last_value)
    VALUES (2024, 2)
    ON CONFLICT (year) DO UPDATE SET last_value = GREATEST(receipt_counters.last_value, 2);

    -- ── Additional students for list-page variety ─────────────────────────────
    INSERT INTO students (
        address, age, class_name, email, first_name, last_name,
        parent_name, phone, roll_no, school_id, section
    )
    SELECT 'Demo Addr', 15, v_class, 'priya.sharma@fees-demo.local',
           'Priya', 'Sharma', 'Mrs. Sharma', '9876500002', 2, v_school_id, v_section
    WHERE NOT EXISTS (
        SELECT 1 FROM students s WHERE s.school_id = v_school_id
          AND s.first_name = 'Priya' AND s.last_name = 'Sharma'
    );

    INSERT INTO students (
        address, age, class_name, email, first_name, last_name,
        parent_name, phone, roll_no, school_id, section
    )
    SELECT 'Demo Addr', 16, 'Grade 9', 'amit.patel@fees-demo.local',
           'Amit', 'Patel', 'Mr. Patel', '9876500003', 1, v_school_id, 'B'
    WHERE NOT EXISTS (
        SELECT 1 FROM students s WHERE s.school_id = v_school_id
          AND s.first_name = 'Amit' AND s.last_name = 'Patel'
    );

    -- Priya: fully paid
    INSERT INTO fees (student_id, fee_type, amount, total_amount, due_date, status,
                      paid_amount, academic_year, version)
    SELECT s.id, 'Annual Tuition Fee', 45000.00, 45000.00, DATE '2026-03-01',
           'PAID', 45000.00, v_academic_year, 0
    FROM students s
    WHERE s.school_id = v_school_id AND s.first_name = 'Priya' AND s.last_name = 'Sharma'
      AND NOT EXISTS (
          SELECT 1 FROM fees f WHERE f.student_id = s.id AND f.fee_type = 'Annual Tuition Fee'
      );

    -- Amit: partially paid
    INSERT INTO fees (student_id, fee_type, amount, total_amount, due_date, status,
                      paid_amount, academic_year, version)
    SELECT s.id, 'Annual Tuition Fee', 40000.00, 40000.00, DATE '2026-06-01',
           'PARTIAL', 20000.00, v_academic_year, 0
    FROM students s
    WHERE s.school_id = v_school_id AND s.first_name = 'Amit' AND s.last_name = 'Patel'
      AND NOT EXISTS (
          SELECT 1 FROM fees f WHERE f.student_id = s.id AND f.fee_type = 'Annual Tuition Fee'
      );

    RAISE NOTICE 'Admin Student Fees seed complete for school % (Rahul id=%)', v_school_id, v_rahul_id;
END $$;
