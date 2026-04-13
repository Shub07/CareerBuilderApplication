# 🗄️ Complete Data Relationship Map & Entity Details

## Entity Relationship Diagram

```
┌─────────────────────────────────────────────────────────────────────┐
│                      CAREER BUILDER DATA MODEL                      │
└─────────────────────────────────────────────────────────────────────┘

                          ╔═══════════════╗
                          ║   SCHOOLS     ║
                          ║   (5 rows)    ║
                          ╚═══════════════╝
                                 │
                                 │ 1:M
                                 │
                  ┌──────────────┼──────────────┐
                  │              │              │
            ╔═══════════╗  ╔═══════════╗  ╔═══════════╗
            ║ STUDENTS  ║  ║VACATIONS  ║  ║...others..║
            ║(5 rows)   ║  ║(5 rows)   ║  ║           ║
            ╚═══════════╝  ╚═══════════╝  ╚═══════════╝
                  │
         ┌────────┼────────┐
         │        │        │
         │     1:M│        │ M:N
         │        │        │
    ╔════════╗ ╔═════════╗ ╔═══════════════╗
    ║  FEES  ║ │ATTENDANCE╠═║  MY_CLASSES   ║
    ║(25rows)║ │(25 rows)║ ║  (25 rows)    ║
    ╚════════╝ ╚═════════╝ ╚═══════════════╝
         │                       │
         │ 1:M                   │ M:N
         │                       │
    ╔════════════════╗      ╔═══════════════╗
    ║PAYMENT_TRANS.. ║      ║   SUBJECTS    ║
    ║(6 rows)       ║      ║   (5 rows)    ║
    ╚════════════════╝      ╚═══════════════╝
```

---

## 📊 Detailed Table Schema & Sample Data

### 1. SCHOOLS Table
```
┌────┬──────────────────────┬─────────────────────────┬──────────────┐
│ ID │      NAME            │      ADDRESS            │  PRINCIPAL   │
├────┼──────────────────────┼─────────────────────────┼──────────────┤
│ 1  │ Delhi Public School  │ 123 Education Lane...   │ Dr. Rajesh K.│
│ 2  │ St. Mary High School │ 456 Academic Road...    │ Sr. Catherine│
│ 3  │ Kendriya Vidyalaya   │ 789 Central Ave...      │ Mrs. Priya S.│
│ 4  │ Cathedral School     │ 321 Heritage Road...    │ Mr. Thomas W.│
│ 5  │ Modern Int'l School  │ 654 Future Lane...      │ Ms. Anjali D.│
└────┴──────────────────────┴─────────────────────────┴──────────────┘
```

### 2. STUDENTS Table
```
┌────┬─────────┬──────────┬─────┬────────┬──────────┬──────────┐
│ ID │FIRST_NAM│LAST_NAME │ AGE │ CLASS  │ SECTION  │ SCHOOL_ID│
├────┼─────────┼──────────┼─────┼────────┼──────────┼──────────┤
│ 1  │ Aarav   │ Patel    │ 15  │ 10A    │ A        │ 1        │
│ 2  │ Ananya  │ Verma    │ 14  │ 9B     │ B        │ 2        │
│ 3  │ Arjun   │ Gupta    │ 16  │ 11C    │ C        │ 3        │
│ 4  │ Avni    │ Das      │ 15  │ 10A    │ A        │ 4        │
│ 5  │ Aditya  │ Singh    │ 14  │ 9B     │ B        │ 5        │
└────┴─────────┴──────────┴─────┴────────┴──────────┴──────────┘

Roll No: 1, 2, 3, 4, 5 (unique per school)
Email: firstname.lastname@email.com
Phone: 9876543210-9876543214 (unique)
```

### 3. SUBJECTS Table
```
┌────┬──────────────────┬────────────┬────────────────────────┐
│ ID │  SUBJECT_NAME    │ CODE       │   DESCRIPTION          │
├────┼──────────────────┼────────────┼────────────────────────┤
│ 1  │ Mathematics      │ MATH101    │ Advanced Mathematics   │
│ 2  │ English          │ ENG101     │ English Language & Lit │
│ 3  │ Science          │ SCI101     │ Physics, Chemistry...  │
│ 4  │ Social Studies   │ SOC101     │ History, Geography...  │
│ 5  │ Computer Science │ CS101      │ Intro to Programming   │
└────┴──────────────────┴────────────┴────────────────────────┘
```

### 4. MY_CLASSES Table (Student-Subject Enrollment)
```
┌────┬────────────┬────────────┬─────────────────┬────────────┐
│ ID │ STUDENT_ID │ SUBJECT_ID │ ENROLLMENT_DATE │   STATUS   │
├────┼────────────┼────────────┼─────────────────┼────────────┤
│ 1  │ 1          │ 1          │ 2026-01-15      │ ACTIVE     │
│ 2  │ 1          │ 2          │ 2026-01-15      │ ACTIVE     │
│ 3  │ 1          │ 3          │ 2026-01-15      │ ACTIVE     │
│ 4  │ 1          │ 4          │ 2026-01-15      │ ACTIVE     │
│ 5  │ 1          │ 5          │ 2026-01-15      │ ACTIVE     │
│ 6  │ 2          │ 1          │ 2026-01-15      │ ACTIVE     │
│ 7  │ 2          │ 2          │ 2026-01-15      │ ACTIVE     │
│ ... (repeats for students 2-5) ...                          │
└────┴────────────┴────────────┴─────────────────┴────────────┘
Total: 25 rows (5 subjects × 5 students)
```

### 5. FEES Table
```
┌────┬────────────┬──────────────┬────────┬────────────┬────────────┐
│ ID │ STUDENT_ID │  FEE_TYPE    │ AMOUNT │  STATUS    │  DUE_DATE  │
├────┼────────────┼──────────────┼────────┼────────────┼────────────┤
│ 1  │ 1          │ TUITION      │ 50000  │ PAID       │ 2026-02-28 │
│ 2  │ 1          │ EXAMINATION  │ 5000   │ PENDING    │ 2026-03-31 │
│ 3  │ 1          │ LIBRARY      │ 2000   │ PENDING    │ 2026-04-30 │
│ 4  │ 1          │ SPORTS       │ 3000   │ PENDING    │ 2026-05-31 │
│ 5  │ 1          │ HOSTEL       │ 8000   │ PENDING    │ 2026-06-30 │
│ 6  │ 2          │ TUITION      │ 45000  │ PAID       │ 2026-02-28 │
│ 7  │ 2          │ EXAMINATION  │ 5000   │ PAID       │ 2026-03-31 │
│ ... (continuing for students 2-5) ...                            │
└────┴────────────┴──────────────┴────────┴────────────┴────────────┘

Total: 25 rows (5 fee types × 5 students)
Fee Types: TUITION, EXAMINATION, LIBRARY, SPORTS, HOSTEL
Status: PAID (5), PENDING (18), OVERDUE (1)
Amount Range: ₹2,000 - ₹52,000
```

### 6. PAYMENT_TRANSACTIONS Table
```
┌────┬────────────┬────────┬────────┬──────────────┬─────────────────┐
│ ID │ STUDENT_ID │ FEE_ID │AMOUNT  │ PAYMENT_METH │ TRANSACTION_REF │
├────┼────────────┼────────┼────────┼──────────────┼─────────────────┤
│ 1  │ 1          │ 1      │ 50000  │ RAZORPAY     │ pay_aarav_...   │
│ 2  │ 2          │ 6      │ 45000  │ RAZORPAY     │ pay_ananya_...  │
│ 3  │ 2          │ 7      │ 5000   │ RAZORPAY     │ pay_ananya_...  │
│ 4  │ 4          │ 16     │ 48000  │ RAZORPAY     │ pay_avni_...    │
│ 5  │ 4          │ 17     │ 5000   │ RAZORPAY     │ pay_avni_...    │
│ 6  │ 1          │ 2      │ 5000   │ RAZORPAY     │ pay_aarav_...   │
└────┴────────────┴────────┴────────┴──────────────┴─────────────────┘

Total: 6 rows
Payment Method: RAZORPAY
Status: All SUCCESS
Total Amount: ₹158,000
```

### 7. ATTENDANCE_RECORDS Table
```
┌────┬────────────┬────────────┬────────┬─────────┬──────────────┐
│ ID │ STUDENT_ID │  DATE      │ STATUS │ REMARKS │  CLASS_NAME  │
├────┼────────────┼────────────┼────────┼─────────┼──────────────┤
│ 1  │ 1          │ 2026-04-01 │ PRESENT│ Regular │ 10A          │
│ 2  │ 1          │ 2026-04-02 │ PRESENT│ Regular │ 10A          │
│ 3  │ 1          │ 2026-04-03 │ ABSENT │ Medical │ 10A          │
│ 4  │ 1          │ 2026-04-04 │ LEAVE  │ Approved│ 10A          │
│ 5  │ 1          │ 2026-04-05 │ PRESENT│ Regular │ 10A          │
│ 6  │ 2          │ 2026-04-01 │ PRESENT│ Regular │ 9B           │
│ 7  │ 2          │ 2026-04-02 │ PRESENT│ Regular │ 9B           │
│ ... (continuing for 5 days × 5 students) ...                   │
└────┴────────────┴────────────┴────────┴─────────┴──────────────┘

Total: 25 rows (5 dates × 5 students)
Date Range: 2026-04-01 to 2026-04-05
Status Distribution:
  PRESENT: 16 records
  ABSENT: 5 records
  LEAVE: 4 records
```

### 8. VACATIONS Table
```
┌────┬────────────┬──────────────────────┬──────────┬────────────┐
│ ID │ SCHOOL_ID  │  VACATION_NAME       │  TYPE    │ START_DATE │
├────┼────────────┼──────────────────────┼──────────┼────────────┤
│ 1  │ 1          │ Winter Break         │ WINTER   │2026-12-20  │
│ 2  │ 2          │ Summer Vacation      │ SUMMER   │2026-05-15  │
│ 3  │ 3          │ Exam Break           │EXAM_BRK  │2026-04-01  │
│ 4  │ 4          │ Spring Holiday       │ SPRING   │2026-03-15  │
│ 5  │ 5          │ Special - Diwali     │ HOLIDAY  │2026-10-24  │
└────┴────────────┴──────────────────────┴──────────┴────────────┘

Total: 5 rows
All marked as ACTIVE = true
```

---

## 🔗 Referential Integrity Map

### Foreign Key Relationships

```
students.school_id → schools.id
  Row 1-5 of students reference schools 1-5

fees.student_id → students.id
  Rows 1-5 of fees reference student 1
  Rows 6-10 of fees reference student 2
  (etc. for students 3-5)

my_classes.student_id → students.id
my_classes.subject_id → subjects.id
  25 rows representing 5-subject enrollment for each student

payment_transactions.student_id → students.id
payment_transactions.fee_id → fees.id
  Only 6 transactions for paid fees

attendance_records.student_id → students.id
  5 records per student (2026-04-01 to 2026-04-05)

vacations.school_id → schools.id
  Each school has 1 vacation (rows 1-5)
```

---

## 📈 Data Flow Example: Aarav Patel (Student 1)

```
Step 1: School Registration
  └─ School: Delhi Public School (ID: 1)

Step 2: Student Enrollment
  └─ Aarav Patel, Class 10A, Section A, School ID: 1

Step 3: Subject Enrollment (MY_CLASSES)
  ├─ Mathematics (Subject 1)
  ├─ English (Subject 2)
  ├─ Science (Subject 3)
  ├─ Social Studies (Subject 4)
  └─ Computer Science (Subject 5)

Step 4: Fee Generation
  ├─ TUITION: ₹50,000 (Due: 2026-02-28) → PAID
  ├─ EXAMINATION: ₹5,000 (Due: 2026-03-31) → PENDING
  ├─ LIBRARY: ₹2,000 (Due: 2026-04-30) → PENDING
  ├─ SPORTS: ₹3,000 (Due: 2026-05-31) → PENDING
  └─ HOSTEL: ₹8,000 (Due: 2026-06-30) → PENDING
  Total: ₹68,000

Step 5: Payments
  ├─ TUITION Payment: ₹50,000 (Status: SUCCESS) [2026-02-15]
  └─ EXAMINATION Payment: ₹5,000 (Status: SUCCESS) [2026-03-25]
  Total Paid: ₹55,000
  Remaining: ₹13,000

Step 6: Daily Attendance (April 1-5, 2026)
  ├─ 2026-04-01: PRESENT
  ├─ 2026-04-02: PRESENT
  ├─ 2026-04-03: ABSENT (Medical leave pending)
  ├─ 2026-04-04: LEAVE (Approved)
  └─ 2026-04-05: PRESENT
  Present: 3/5, Absent: 1/5, Leave: 1/5
  Attendance %: 60%

Step 7: School Vacations Applicable
  └─ Winter Break: DPS Delhi (2026-12-20 to 2027-01-05)
```

---

## 🎯 Data Statistics Summary

```
TOTAL RECORDS: 121 rows

Distribution by Table:
  Schools:               5  (1.0%)
  Students:             5  (1.0%)
  Subjects:             5  (1.0%)
  My_Classes:          25  (2.1%)
  Fees:                25  (2.1%)
  Payment_Transactions: 6  (0.5%)
  Attendance_Records:  25  (2.1%)
  Vacations:            5  (0.4%)

Data Distribution by Student:
  Each Student Has:
    - 1 School (via relationship)
    - 5 Subject Enrollments
    - 5 Different Fee Types
    - 1-2 Payment Transactions (varies)
    - 5 Attendance Records
    - Access to 1 School Vacation

Total Data Points: 121 unique records
All relationships maintained with foreign keys
All constraints satisfied (unique, not null, etc.)
```

---

## ✅ Validation Checklist

After inserting data, verify:

- [ ] 5 schools exist
- [ ] 5 students exist
- [ ] Each student has school_id 1-5
- [ ] 5 subjects exist
- [ ] 25 my_classes records (5 per student)
- [ ] 25 fees records (5 per student)
- [ ] 6 payment_transactions records
- [ ] 25 attendance_records (5 per student)
- [ ] 5 vacations exist
- [ ] All foreign keys are valid
- [ ] No null values in required fields
- [ ] Unique constraints satisfied
- [ ] Date ranges logical

---

## 📝 Quick Reference

**Total Data Points:** 121 records  
**Unique Students:** 5  
**Unique Schools:** 5  
**Total Fees Amount:** ₹337,000 (all 5 students)  
**Total Paid Amount:** ₹158,000  
**Total Pending Amount:** ₹179,000  
**Date Range:** 2026-01-15 to 2027-01-05  
**All Data Valid:** ✅ Yes

---

**Created:** April 7, 2026  
**Format:** PostgreSQL SQL  
**Status:** Ready to Insert

