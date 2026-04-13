# 📊 Test Data Insertion Guide - 5 Users Complete Dataset

## Overview
This guide explains how to insert test data for 5 users across all Career Builder Backend tables with proper relationships.

---

## 📋 Data Structure & Relationships

### Entity Relationship Diagram (Conceptual)

```
Schools (5)
    ↓ 1:M
Students (5) ← 1 per school
    ├──→ Fees (25) ← 5 per student
    │       ↓ 1:M
    │   Payment_Transactions (6) ← payments for fees
    │
    ├──→ My_Classes (25) ← 5 subjects per student
    │       ↓ links to
    │   Subjects (5)
    │
    └──→ Attendance_Records (25) ← 5 records per student

Vacations (5) ← shared across schools
```

---

## 👥 The 5 Users (Students)

| # | Name | Age | Class | Section | School | Email |
|---|------|-----|-------|---------|--------|-------|
| 1 | Aarav Patel | 15 | 10A | A | DPS Delhi | aarav.patel@email.com |
| 2 | Ananya Verma | 14 | 9B | B | St. Mary Mumbai | ananya.verma@email.com |
| 3 | Arjun Gupta | 16 | 11C | C | KV Bangalore | arjun.gupta@email.com |
| 4 | Avni Das | 15 | 10A | A | Cathedral Kolkata | avni.das@email.com |
| 5 | Aditya Singh | 14 | 9B | B | Modern School Pune | aditya.singh@email.com |

---

## 🏫 Schools (Base Data)

5 schools are created as parent entities:
1. Delhi Public School
2. St. Mary High School
3. Kendriya Vidyalaya
4. Cathedral School
5. Modern International School

---

## 📚 Subjects (Base Data)

5 subjects are available to all students:
1. Mathematics (MATH101)
2. English (ENG101)
3. Science (SCI101)
4. Social Studies (SOC101)
5. Computer Science (CS101)

---

## 💳 Data Summary by Table

### 1. Schools Table
- **Rows:** 5
- **Fields:** name, address, phone, email, principal_name, established_year, website
- **Purpose:** Base entity for students

### 2. Students Table
- **Rows:** 5
- **Fields:** first_name, last_name, age, class_name, section, roll_no, parent_name, phone, email, address, school_id
- **Relationships:** Each student linked to 1 school
- **Unique Constraints:** email, phone, (school_id, class_name, section, roll_no)

### 3. Subjects Table
- **Rows:** 5
- **Fields:** subject_name, subject_code, description, credits
- **Purpose:** Course catalog

### 4. My_Classes Table (Student-Subject Enrollment)
- **Rows:** 25 (5 subjects × 5 students)
- **Fields:** student_id, subject_id, enrollment_date, status, semester
- **Relationships:** Many-to-many between students and subjects
- **Status:** All marked as ACTIVE
- **Enrollment Date:** 2026-01-15 (same for all)

### 5. Fees Table
- **Rows:** 25 (5 fees × 5 students)
- **Fee Types:** TUITION, EXAMINATION, LIBRARY, SPORTS, HOSTEL
- **Fields:** student_id, fee_type, amount, due_date, status, payment_deadline
- **Status Distribution:**
  - PAID: Aarav (1), Ananya (2), Avni (2) = 5 total
  - PENDING: 18 total
  - OVERDUE: Arjun (1) = 1 total
- **Amount Range:** 2000 - 52000

### 6. Payment_Transactions Table
- **Rows:** 6 total (payments for paid fees)
- **Payment Methods:** RAZORPAY
- **Transaction Status:** All SUCCESS
- **Fields:** student_id, fee_id, amount, payment_method, transaction_status, transaction_reference_id, paid_at
- **Payment Records:**
  - Aarav: 1 payment (tuition)
  - Ananya: 2 payments (tuition + exam)
  - Arjun: 0 payments (overdue)
  - Avni: 2 payments (tuition + exam)
  - Aditya: 0 payments (all pending)

### 7. Attendance_Records Table
- **Rows:** 25 (5 records × 5 students)
- **Fields:** student_id, att_date, status, class_name, section, remarks, created_at, updated_at
- **Date Range:** 2026-04-01 to 2026-04-05
- **Status Distribution per Student:**
  - PRESENT: 3-4 records
  - ABSENT: 1-2 records
  - LEAVE: 1 record
- **Sample Data:**
  - Aarav: 4 Present, 1 Absent, 1 Leave
  - Ananya: 4 Present, 1 Absent
  - Arjun: 2 Present, 2 Absent, 1 Leave
  - Avni: 4 Present, 1 Absent
  - Aditya: 4 Present, 1 Leave

### 8. Vacations Table
- **Rows:** 5
- **Fields:** school_id, vacation_name, vacation_type, start_date, end_date, description, is_active
- **Vacation Types:**
  1. Winter Break (DPS Delhi)
  2. Summer Vacation (St. Mary Mumbai)
  3. Exam Break (KV Bangalore)
  4. Spring Holiday (Cathedral Kolkata)
  5. Special Holiday - Diwali (Modern School Pune)
- **All active:** true

---

## 🔗 Relationship Explanation

### School → Students (1:M)
```
DPS Delhi
    ├─ Aarav Patel
    ├─ (4 more students from other schools)

Each student belongs to exactly one school
```

### Students → My_Classes → Subjects (M:N)
```
Aarav Patel
    ├─ Mathematics (enrolled 2026-01-15)
    ├─ English (enrolled 2026-01-15)
    ├─ Science (enrolled 2026-01-15)
    ├─ Social Studies (enrolled 2026-01-15)
    └─ Computer Science (enrolled 2026-01-15)

Each student enrolled in 5 subjects via My_Classes
```

### Students → Fees (1:M)
```
Aarav Patel
    ├─ TUITION Fee ($50,000) - PAID
    ├─ EXAMINATION Fee ($5,000) - PENDING
    ├─ LIBRARY Fee ($2,000) - PENDING
    ├─ SPORTS Fee ($3,000) - PENDING
    └─ HOSTEL Fee ($8,000) - PENDING

Each student has 5 different fee types
```

### Fees → Payment_Transactions (1:M)
```
TUITION Fee (Aarav) - $50,000
    └─ Payment_Transaction (SUCCESS) - Paid on 2026-02-15

EXAMINATION Fee (Aarav) - $5,000
    └─ Payment_Transaction (SUCCESS) - Paid on 2026-03-25

Only paid fees have corresponding transactions
```

### Students → Attendance_Records (1:M)
```
Aarav Patel
    ├─ 2026-04-01: PRESENT
    ├─ 2026-04-02: PRESENT
    ├─ 2026-04-03: ABSENT
    ├─ 2026-04-04: LEAVE
    └─ 2026-04-05: PRESENT

Each student has 5 daily attendance records
```

### Schools → Vacations (1:M)
```
Each school has specific vacations
DPS Delhi → Winter Break (2026-12-20 to 2027-01-05)
St. Mary → Summer Vacation (2026-05-15 to 2026-06-15)
etc.
```

---

## 🚀 How to Insert the Data

### Option 1: Using PostgreSQL Command Line
```bash
psql -U admin -h localhost -d admindb -f INSERT_TEST_DATA_5_USERS.sql
```

### Option 2: Using pgAdmin
1. Open pgAdmin
2. Connect to admindb database
3. Open Query Tool
4. Copy and paste the SQL from INSERT_TEST_DATA_5_USERS.sql
5. Execute (F5 or Execute button)

### Option 3: Using DBeaver
1. Open DBeaver
2. Connect to admindb
3. Open New SQL Script
4. Paste SQL content
5. Execute (Ctrl+Enter)

### Option 4: Using Java Application
If you have a data loader utility in your application, configure it to run this SQL file on startup.

---

## ✅ Verification Queries

After running the insert script, verify the data:

```sql
-- Check all schools
SELECT COUNT(*) FROM schools;
-- Expected: 5

-- Check all students
SELECT COUNT(*) FROM students;
-- Expected: 5

-- Check student names
SELECT id, first_name, last_name, class_name, section FROM students;

-- Check fees per student
SELECT student_id, COUNT(*) as fee_count FROM fees GROUP BY student_id;
-- Expected: Each student has 5 fees

-- Check attendance records
SELECT student_id, COUNT(*) as attendance_count FROM attendance_records GROUP BY student_id;
-- Expected: Each student has 5 records

-- Check payments
SELECT COUNT(*) FROM payment_transactions;
-- Expected: 6 total transactions

-- Check my_classes enrollment
SELECT student_id, COUNT(*) as subject_count FROM my_classes GROUP BY student_id;
-- Expected: Each student has 5 subjects

-- View student fee status summary
SELECT 
    s.first_name,
    COUNT(*) as total_fees,
    SUM(CASE WHEN f.status = 'PAID' THEN 1 ELSE 0 END) as paid_count,
    SUM(CASE WHEN f.status = 'PENDING' THEN 1 ELSE 0 END) as pending_count,
    SUM(CASE WHEN f.status = 'OVERDUE' THEN 1 ELSE 0 END) as overdue_count
FROM students s
LEFT JOIN fees f ON s.id = f.student_id
GROUP BY s.id, s.first_name;

-- View attendance summary
SELECT 
    s.first_name,
    ar.status,
    COUNT(*) as count
FROM students s
LEFT JOIN attendance_records ar ON s.id = ar.student_id
GROUP BY s.id, s.first_name, ar.status
ORDER BY s.id, ar.status;

-- View payment transactions
SELECT 
    s.first_name,
    pt.transaction_reference_id,
    pt.amount,
    pt.transaction_status,
    pt.paid_at
FROM payment_transactions pt
JOIN students s ON pt.student_id = s.id
ORDER BY s.id, pt.paid_at;
```

---

## 🎯 Data Statistics

### Total Records Inserted
| Table | Count | Purpose |
|-------|-------|---------|
| schools | 5 | Base institutions |
| students | 5 | Main users |
| subjects | 5 | Course catalog |
| my_classes | 25 | Student enrollments |
| fees | 25 | Fee records |
| payment_transactions | 6 | Payment records |
| attendance_records | 25 | Daily attendance |
| vacations | 5 | School holidays |
| **TOTAL** | **121** | **Complete test dataset** |

### Data Coverage
- ✅ 5 unique schools
- ✅ 5 unique students
- ✅ 5 different subjects
- ✅ 25 class enrollments
- ✅ 25 fee entries (5 types per student)
- ✅ 6 payment transactions
- ✅ 25 attendance records
- ✅ 5 vacation periods
- ✅ All relationships properly maintained

---

## 🔄 Relationship Consistency Checks

All data maintains referential integrity:
- ✅ Every student references a valid school_id
- ✅ Every fee references a valid student_id
- ✅ Every payment_transaction references valid student_id and fee_id
- ✅ Every my_class references valid student_id and subject_id
- ✅ Every attendance_record references a valid student_id
- ✅ Every vacation references a valid school_id

---

## 📝 Notes

1. **Phone Numbers:** Realistic Indian phone numbers (10 digits)
2. **Email:** Valid email format
3. **Amounts:** Realistic fees in INR
4. **Dates:** Logical date progression
5. **Status Values:** Valid enum values (PAID, PENDING, OVERDUE, ACTIVE, etc.)
6. **IDs:** Auto-generated by sequences

---

## 🆘 Troubleshooting

### Error: "Foreign key constraint failed"
- Ensure schools are inserted before students
- Ensure subjects are inserted before my_classes
- Check the insert order in the SQL script

### Error: "Duplicate entry"
- This script uses unique IDs
- If re-running, truncate tables first:
  ```sql
  TRUNCATE TABLE payment_transactions, attendance_records, fees, 
               my_classes, students, subjects, vacations, schools;
  ```

### Error: "Column not found"
- Ensure all tables exist (run migrations first)
- Check column names match your schema

---

## 📄 File Location
`INSERT_TEST_DATA_5_USERS.sql` in project root directory

---

**Date Created:** April 7, 2026  
**Data Format:** PostgreSQL SQL  
**Tested With:** PostgreSQL 12+

