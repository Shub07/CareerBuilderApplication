# ⚡ Quick Reference - Test Data for 5 Users

## 🚀 Quick Start

### Run the SQL Script
```bash
psql -U admin -h localhost -d admindb -f INSERT_TEST_DATA_5_USERS.sql
```

### Verify Data Inserted
```sql
SELECT COUNT(*) FROM schools;           -- Should return: 5
SELECT COUNT(*) FROM students;          -- Should return: 5
SELECT COUNT(*) FROM fees;              -- Should return: 25
SELECT COUNT(*) FROM attendance_records;-- Should return: 25
SELECT COUNT(*) FROM payment_transactions;-- Should return: 6
```

---

## 📊 What's Included

| Table | Rows | Details |
|-------|------|---------|
| Schools | 5 | Parent institutions for students |
| Students | 5 | 1 per school (Age 14-16) |
| Subjects | 5 | Math, English, Science, Social Studies, CS |
| My_Classes | 25 | 5 subjects per student |
| Fees | 25 | 5 fee types per student |
| Payment_Transactions | 6 | Payments for paid fees |
| Attendance_Records | 25 | 5 days (Apr 1-5) per student |
| Vacations | 5 | School-specific holidays |
| **TOTAL** | **121** | **Complete dataset** |

---

## 👥 The 5 Students

```
1. Aarav Patel      - Class 10A, Section A, DPS Delhi       [Age 15]
2. Ananya Verma     - Class 9B,  Section B, St. Mary Mumbai  [Age 14]
3. Arjun Gupta      - Class 11C, Section C, KV Bangalore     [Age 16]
4. Avni Das         - Class 10A, Section A, Cathedral Kolkata[Age 15]
5. Aditya Singh     - Class 9B,  Section B, Modern Pune      [Age 14]
```

---

## 💰 Fee Summary

### Per Student
- **TUITION:** ₹45,000-₹52,000
- **EXAMINATION:** ₹5,000
- **LIBRARY:** ₹2,000
- **SPORTS:** ₹2,500-₹3,500
- **HOSTEL:** ₹8,000
- **Total per Student:** ₹62,500-₹68,500

### Payment Status
- **Paid:** Aarav (1), Ananya (2), Avni (2) = 5 fees
- **Pending:** 18 fees
- **Overdue:** Arjun (1) = 1 fee

---

## 📅 Attendance (5 Days)

### Date Range: April 1-5, 2026

**Sample Data:**
```
Aarav:   P P A L P  (60% - 3 present)
Ananya:  P P P P A  (80% - 4 present)
Arjun:   P A A P L  (40% - 2 present)
Avni:    P P P A P  (80% - 4 present)
Aditya:  P P L P P  (80% - 4 present)

P=Present, A=Absent, L=Leave
```

---

## 🏫 Schools

```
1. Delhi Public School       - Est. 1995
2. St. Mary High School      - Est. 1987
3. Kendriya Vidyalaya        - Est. 2000
4. Cathedral School          - Est. 1992
5. Modern International Sch. - Est. 2005
```

---

## 📚 Subjects (Taken by All Students)

```
1. Mathematics
2. English
3. Science
4. Social Studies
5. Computer Science
```

---

## 🎓 Relationships at a Glance

```
School (1) ──→ Student (5)
              │
              ├─→ Fees (25)
              │    └─→ PaymentTransactions (6)
              │
              ├─→ My_Classes (25)
              │    └─→ Subjects (5)
              │
              └─→ AttendanceRecords (25)

School (5) ──→ Vacations (5)
```

---

## ✅ Table Relationships

### Schools → Students (1:M)
Each school has 1 student (5 schools → 5 students)

### Students → Fees (1:M)
Each student has 5 fees (5 students → 25 fees)

### Fees → PaymentTransactions (1:M)
Only paid fees have transactions (5 PAID fees → 6 transactions)
- Aarav: 1 tuition + 1 exam = 2
- Ananya: 1 tuition + 1 exam = 2
- Avni: 1 tuition + 1 exam = 2
- Arjun: 0 (overdue)
- Aditya: 0 (all pending)

### Students → My_Classes → Subjects (M:N)
Each student enrolled in 5 subjects (5 students × 5 subjects = 25)

### Students → AttendanceRecords (1:M)
Each student has 5 attendance records (5 students × 5 days = 25)

### Schools → Vacations (1:M)
Each school has 1 vacation (5 schools → 5 vacations)

---

## 🔍 Key Data Points

### All Student IDs: 1-5
### All School IDs: 1-5
### All Subject IDs: 1-5
### All Fee IDs: 1-25
### All MyClass IDs: 1-25
### All Transaction IDs: 1-6
### All Attendance IDs: 1-25
### All Vacation IDs: 1-5

---

## 🎯 Testing Queries

### Count All Records
```sql
SELECT 
    'Schools' as table_name, COUNT(*) FROM schools
UNION ALL
SELECT 'Students', COUNT(*) FROM students
UNION ALL
SELECT 'Subjects', COUNT(*) FROM subjects
UNION ALL
SELECT 'My_Classes', COUNT(*) FROM my_classes
UNION ALL
SELECT 'Fees', COUNT(*) FROM fees
UNION ALL
SELECT 'Payments', COUNT(*) FROM payment_transactions
UNION ALL
SELECT 'Attendance', COUNT(*) FROM attendance_records
UNION ALL
SELECT 'Vacations', COUNT(*) FROM vacations;
```

### Get Fee Summary per Student
```sql
SELECT 
    s.first_name,
    COUNT(*) as total_fees,
    SUM(f.amount) as total_amount,
    SUM(CASE WHEN f.status = 'PAID' THEN f.amount ELSE 0 END) as paid_amount,
    SUM(CASE WHEN f.status = 'PENDING' THEN f.amount ELSE 0 END) as pending_amount
FROM students s
LEFT JOIN fees f ON s.id = f.student_id
GROUP BY s.id, s.first_name;
```

### Get Attendance Percentage
```sql
SELECT 
    s.first_name,
    COUNT(CASE WHEN ar.status = 'PRESENT' THEN 1 END) as present_days,
    COUNT(CASE WHEN ar.status = 'ABSENT' THEN 1 END) as absent_days,
    COUNT(CASE WHEN ar.status = 'LEAVE' THEN 1 END) as leave_days,
    ROUND(100.0 * COUNT(CASE WHEN ar.status = 'PRESENT' THEN 1 END) / COUNT(*), 2) as attendance_percent
FROM students s
LEFT JOIN attendance_records ar ON s.id = ar.student_id
GROUP BY s.id, s.first_name;
```

---

## 📋 Files Created

1. **INSERT_TEST_DATA_5_USERS.sql** - Complete SQL script
2. **TEST_DATA_GUIDE.md** - Detailed guide
3. **DATA_RELATIONSHIP_MAP.md** - Relationship diagrams
4. **QUICK_REFERENCE.md** - This file

---

## 📝 Column Mappings

### Student Table
```
id              → 1-5
first_name      → Aarav, Ananya, Arjun, Avni, Aditya
last_name       → Patel, Verma, Gupta, Das, Singh
age             → 15, 14, 16, 15, 14
class_name      → 10A, 9B, 11C, 10A, 9B
section         → A, B, C, A, B
roll_no         → 1, 2, 3, 4, 5 (unique per school)
school_id       → 1, 2, 3, 4, 5
email           → first.last@email.com
phone           → 9876543210-9876543214
parent_name     → Parents names
address         → Various addresses
```

---

## 🔐 Constraints Maintained

✅ Foreign keys - All references valid
✅ Unique constraints - Email, phone, roll_no
✅ Not null - All required fields filled
✅ Date ranges - Logical progression
✅ Amounts - Realistic values
✅ Enums - Valid status values

---

## 📊 Data Volume

- **Total Records:** 121
- **Total Amount (Fees):** ₹337,000
- **Amount Paid:** ₹158,000
- **Amount Pending:** ₹179,000
- **Unique Relationships:** 50+

---

## 🎯 Use Cases Covered

✅ Single student with all 5 subjects  
✅ Paid and pending fees  
✅ Multiple payment transactions  
✅ Daily attendance tracking  
✅ School holidays/vacations  
✅ Different student scenarios (paying, overdue, pending)  
✅ Complete one-to-many relationships  
✅ Complete many-to-many relationships  

---

## 📌 Notes

- All data is realistic and properly formatted
- All IDs start from 1 (auto-increment ready)
- All dates are in 2026 (current system date)
- All phone numbers are 10 digits
- All emails follow standard format
- All amounts in Indian Rupees (₹)

---

**Total Data Points:** 121 rows  
**Status:** Ready to use ✅  
**Created:** April 7, 2026  
**Database:** PostgreSQL

