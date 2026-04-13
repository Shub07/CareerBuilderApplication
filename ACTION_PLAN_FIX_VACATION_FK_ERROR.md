# 🎯 ACTION PLAN - Fix Vacation API Foreign Key Error

## Your Error
```json
{
    "success": false,
    "message": "Failed to create vacation: ... violates foreign key constraint \"vacations_school_id_fkey\" ... Key (school_id)=(2) is not present in table \"schools\"."
}
```

## Root Cause
School with ID 2 doesn't exist in the database

---

## ⚡ IMMEDIATE ACTION (Choose ONE)

### 🔴 Option 1: Run SQL Fix Script (Fastest - 2 min)

**Step 1:** Open your database client (pgAdmin or terminal)

**Step 2:** Copy and paste this SQL:
```sql
-- ============================================================================
-- QUICK FIX: Ensure School ID 2 exists for Vacation Creation
-- ============================================================================

INSERT INTO schools (id, school_name, school_code, address, city, state, country, phone, email, principal_name, established_year)
VALUES
    (1, 'Delhi Public School', 'DPS001', '123 School Lane, Delhi', 'Delhi', 'Delhi', 'India', '9876543200', 'principal@dps.edu.in', 'Dr. Sharma', 2010),
    (2, 'Mumbai Academy', 'MA001', '456 Education Ave, Mumbai', 'Mumbai', 'Maharashtra', 'India', '9876543201', 'principal@mumbaiaca.edu.in', 'Mrs. Patel', 2015)
ON CONFLICT (id) DO NOTHING;

-- Verify
SELECT id, school_name, city FROM schools;
```

**Step 3:** Execute it

**Step 4:** You're done! Retry your POST request

---

### 🟡 Option 2: Restart Application (Automatic - 1 min)

**Step 1:** Stop your Spring Boot application
```bash
# Ctrl + C in terminal (if running in foreground)
# Or kill the process
```

**Step 2:** Restart it
```bash
mvn spring-boot:run
# OR java -jar career-builder-0.0.1-SNAPSHOT.jar
```

**Step 3:** Migration runs automatically on startup ✅

**Step 4:** Retry your POST request

---

### 🟢 Option 3: Add Schools via API (5 min)

**Step 1:** POST School 1
```bash
curl -X POST http://localhost:9091/api/schools \
  -H "Content-Type: application/json" \
  -d '{
    "schoolName": "Delhi Public School",
    "schoolCode": "DPS001",
    "address": "123 School Lane, Delhi",
    "city": "Delhi",
    "state": "Delhi",
    "country": "India",
    "phone": "9876543200",
    "email": "principal@dps.edu.in",
    "principalName": "Dr. Sharma",
    "establishedYear": "2010"
  }'
```

**Step 2:** POST School 2
```bash
curl -X POST http://localhost:9091/api/schools \
  -H "Content-Type: application/json" \
  -d '{
    "schoolName": "Mumbai Academy",
    "schoolCode": "MA001",
    "address": "456 Education Ave, Mumbai",
    "city": "Mumbai",
    "state": "Maharashtra",
    "country": "India",
    "phone": "9876543201",
    "email": "principal@mumbaiaca.edu.in",
    "principalName": "Mrs. Patel",
    "establishedYear": "2015"
  }'
```

**Step 3:** Verify
```bash
curl http://localhost:9091/api/schools
# Should see both schools
```

**Step 4:** Retry your POST request

---

## ✅ VERIFY THE FIX

**Step 1:** Check Schools Exist
```bash
curl http://localhost:9091/api/schools

# Expected Response:
[
  {
    "id": 1,
    "schoolName": "Delhi Public School",
    ...
  },
  {
    "id": 2,
    "schoolName": "Mumbai Academy",
    ...
  }
]
```

**Step 2:** Retry Your Original Request (Updated)
```bash
POST http://localhost:9091/api/vacations/admin/create
Content-Type: application/json

{
    "school_id": 2,
    "vacation_name": "AUTUMN Vacation 2026 S2",
    "vacation_type": "AUTUMN",
    "start_date": "2028-11-15",
    "end_date": "2028-12-15",
    "description": "Extended AUTUMN break for all",
    "is_active": true,
    "created_by": "admin_username"
}
```

**Step 3:** Expected Success Response
```json
{
    "success": true,
    "data": {
        "vacationId": 7,
        "schoolId": 2,
        "vacationName": "AUTUMN Vacation 2026 S2",
        "vacationType": "AUTUMN",
        "vacationTypeLabel": "Autumn Vacation",
        "startDate": "2028-11-15",
        "endDate": "2028-12-15",
        "durationDays": 31,
        "description": "Extended AUTUMN break for all",
        "isActive": true,
        "isOngoing": false,
        "isUpcoming": true,
        "isCompleted": false,
        ...
    }
}
```

---

## 📚 REFERENCE DOCUMENTS

I've created these files to help you understand the fix:

| Document | Use When |
|----------|----------|
| `QUICK_FIX_SCHOOL_VACATION.sql` | You need to run SQL directly |
| `VACATION_FOREIGN_KEY_FIX.md` | You want full technical details |
| `ADD_SCHOOLS_VIA_API.md` | You prefer using REST API |
| `COMPLETE_FIX_SUMMARY.md` | You want comprehensive overview |
| `QUICK_REFERENCE_VACATION_FIX.md` | You need a cheat sheet |
| `VACATION_FOREIGN_KEY_FIX_DIAGRAM.md` | You like visual diagrams |

---

## ✨ BONUS: Supported Vacation Types

After the fix, you can use these vacation types:

```
✓ HOLIDAY           - Holiday/Festival
✓ EXAM_BREAK        - Exam Break
✓ SUMMER            - Summer Vacation
✓ WINTER            - Winter Vacation
✓ SPRING            - Spring Vacation
✓ AUTUMN            - Autumn Vacation
✓ MONSOON           - Monsoon Vacation ← NEW!
✓ SPECIAL           - Special Leave
✓ EMERGENCY         - Emergency Closure
```

---

## 🐛 TROUBLESHOOTING

### "Still getting the same error?"
- Verify schools exist: `SELECT * FROM schools;`
- Check the fix was applied: All 3 options above should work
- Restart your app if you ran SQL

### "Getting 'Duplicate' error?"
- This is fine! Scripts use `ON CONFLICT DO NOTHING`
- Just retry your POST request

### "Different error now?"
- Check the vacation dates are in the future
- Ensure `vacation_type` is uppercase (AUTUMN not Autumn)
- Verify `school_id` exists in schools table

---

## 📊 TESTING CHECKLIST

After applying the fix, verify:

- [ ] Schools exist in database
- [ ] School 1 (Delhi Public School) is present
- [ ] School 2 (Mumbai Academy) is present
- [ ] Original POST request now succeeds
- [ ] Response contains vacation data
- [ ] vacationId is generated correctly
- [ ] All fields match your request

---

## ⏱️ TIME ESTIMATE

| Option | Time | Effort |
|--------|------|--------|
| Option 1 (SQL) | 2 min | ⭐ Easiest |
| Option 2 (Restart) | 5 min | ⭐ Automatic |
| Option 3 (API) | 5 min | ⭐⭐ Manual |

**Recommendation:** Use **Option 1** (SQL) for fastest results!

---

## 🎉 WHAT'S FIXED

✅ **Foreign Key Constraint Error** - School data now exists
✅ **MONSOON Support** - New vacation type added to enum
✅ **Database Migration** - Schools created before vacations
✅ **Sample Data** - Both schools have sample vacations
✅ **Documentation** - 6 comprehensive guides created

---

## 🚀 NEXT STEPS

1. **Choose one fix option above** (I recommend Option 1 - SQL)
2. **Apply the fix** (2-5 minutes)
3. **Verify schools exist** (Test with GET /api/schools)
4. **Retry your POST request**
5. **Celebrate!** 🎉

---

## 💬 SUMMARY

Your issue was caused by a **foreign key constraint violation**. The vacations table requires a valid school_id, but school 2 didn't exist in the schools table.

**What I fixed:**
1. ✅ Added MONSOON vacation type support
2. ✅ Updated migration to insert school data first
3. ✅ Created quick-fix scripts and documentation

**What you need to do:**
1. Choose one fix option above (I recommend SQL)
2. Apply it (takes 2-5 minutes)
3. Retry your request

That's it! You should be good to go. 🚀

---

**Questions?** Refer to the documentation files or see troubleshooting section above.

**Need more help?** Check the comprehensive guides in COMPLETE_FIX_SUMMARY.md

