# 🔧 DEBUG GUIDE - 500 ERROR TROUBLESHOOTING

---

## ✅ Step 1: Verify Test Data in Database

Run these queries to check if data exists:

```sql
-- Check if exams exist
SELECT COUNT(*) as exam_count FROM exams;
-- Expected: 6+

-- Check if exam_results exist
SELECT COUNT(*) as result_count FROM exam_results;
-- Expected: 12

-- Check specific student (ID: 1)
SELECT * FROM exam_results WHERE student_id = 1;
-- Expected: 4 results

-- Check exam sections
SELECT COUNT(*) FROM exam_sections;
-- Expected: 6+
```

---

## ✅ Step 2: If No Data - Insert Test Data

```bash
psql -U postgres -d admindb -f INSERT_EXAM_TEST_DATA.sql
```

---

## ✅ Step 3: Check Server Logs

Look for error messages like:
- `NullPointerException`
- `No such table`
- `Foreign key constraint`
- `Date format errors`

---

## ✅ Step 4: Test Simple Query First

```sql
SELECT er.id, er.student_id, er.exam_id, 
       e.exam_date, e.exam_name
FROM exam_results er
JOIN exams e ON er.exam_id = e.id
WHERE er.student_id = 1
ORDER BY e.exam_date DESC
LIMIT 5;
```

---

## Common Issues:

1. **Upcoming exams returning empty** - Exam dates might be in past
2. **Foreign key violations** - Student, Exam, or Subject doesn't exist
3. **Null repository** - Spring not injecting ExamResultBreakdownRepository
4. **Date comparison issues** - Database date functions

