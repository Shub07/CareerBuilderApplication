# ✅ VACATION API FIX - EXECUTION CHECKLIST

## Problem Statement
```
❌ Foreign Key Constraint Error
Error: Key (school_id)=(2) is not present in table "schools"
Cause: School 2 doesn't exist in database
```

---

## Pre-Fix Verification

- [ ] **Error Confirmed**: Getting the FK constraint error message
- [ ] **Database Access**: Have access to database (pgAdmin/psql)
- [ ] **Application Access**: Application is running on localhost:9091
- [ ] **Postman/API Tool**: Have a way to test API requests

---

## Fix Execution (Choose ONE Method Below)

### ✅ METHOD 1: SQL FIX (RECOMMENDED - 2 minutes)

**Prerequisites:**
- [ ] Database client open (pgAdmin or psql)
- [ ] Connection successful

**Steps:**
- [ ] Open file: `QUICK_FIX_SCHOOL_VACATION.sql`
- [ ] Copy all content
- [ ] Paste into database client
- [ ] Click Execute / Run
- [ ] Verify: "Schools after insert:" shows 2 rows with school_id 1 and 2

**Confirmation:**
```
Status after running:
✅ Schools table now contains school_id 1
✅ Schools table now contains school_id 2
✅ Ready to create vacations
```

---

### ✅ METHOD 2: RESTART APPLICATION (1 minute)

**Prerequisites:**
- [ ] Application is running
- [ ] Terminal/console access

**Steps:**
- [ ] Stop Spring Boot application (Ctrl+C or kill process)
- [ ] Wait 3-5 seconds for graceful shutdown
- [ ] Restart application (`mvn spring-boot:run` or `java -jar ...`)
- [ ] Wait for application startup (check for "Started CareerBuilderApplication")
- [ ] Verify: Application accessible at http://localhost:9091

**Confirmation:**
```
Spring Boot logs should show:
✅ Application started successfully
✅ Migration executed automatically
✅ Schools table populated during startup
```

---

### ✅ METHOD 3: REST API (5 minutes)

**Prerequisites:**
- [ ] Application running
- [ ] Postman or REST client available
- [ ] Application URL accessible

**Step 1: Add School 1**
- [ ] Open Postman/REST client
- [ ] Create new POST request
- [ ] URL: `http://localhost:9091/api/schools`
- [ ] Header: `Content-Type: application/json`
- [ ] Body: See `ADD_SCHOOLS_VIA_API.md` for School 1 payload
- [ ] Click Send
- [ ] Verify: HTTP 200 response with school data

**Step 2: Add School 2**
- [ ] Create new POST request (same URL)
- [ ] Body: See `ADD_SCHOOLS_VIA_API.md` for School 2 payload
- [ ] Click Send
- [ ] Verify: HTTP 200 response with school data

**Step 3: Verify Both Schools**
- [ ] Create new GET request
- [ ] URL: `http://localhost:9091/api/schools`
- [ ] Click Send
- [ ] Verify: Response contains array with 2 schools

---

## Post-Fix Verification

### ✅ VERIFICATION 1: Schools Exist

**Test:**
```bash
GET http://localhost:9091/api/schools
```

**Expected Response:**
```json
[
    {
        "id": 1,
        "schoolName": "Delhi Public School",
        "schoolCode": "DPS001",
        "city": "Delhi",
        ...
    },
    {
        "id": 2,
        "schoolName": "Mumbai Academy",
        "schoolCode": "MA001",
        "city": "Mumbai",
        ...
    }
]
```

**Checklist:**
- [ ] Status code is 200 (Success)
- [ ] Response contains 2 schools
- [ ] School 1 has id: 1
- [ ] School 2 has id: 2
- [ ] School 1 name: "Delhi Public School"
- [ ] School 2 name: "Mumbai Academy"

---

### ✅ VERIFICATION 2: Create Vacation for School 2

**Test:**
```bash
POST http://localhost:9091/api/vacations/admin/create
Content-Type: application/json
```

**Request Body:**
```json
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

**Expected Response:**
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
        ...
    }
}
```

**Checklist:**
- [ ] Status code is 200 (Success)
- [ ] "success": true
- [ ] Response contains vacation data
- [ ] vacationId is generated (e.g., 7 or higher)
- [ ] schoolId is 2
- [ ] Vacation name matches request
- [ ] Vacation type is AUTUMN

---

### ✅ VERIFICATION 3: Test All Vacation Types

**Test each type with this pattern:**
```bash
POST http://localhost:9091/api/vacations/admin/create
```

**Types to Test:**
- [ ] HOLIDAY - Success
- [ ] EXAM_BREAK - Success
- [ ] SUMMER - Success
- [ ] WINTER - Success
- [ ] SPRING - Success
- [ ] AUTUMN - Success
- [ ] MONSOON - Success (newly added)
- [ ] SPECIAL - Success
- [ ] EMERGENCY - Success

---

### ✅ VERIFICATION 4: Verify Migration Ran

**Option A: If you used SQL Method or API Method**
- [ ] Check database: `SELECT COUNT(*) FROM schools;` should be >= 2
- [ ] Check vacations: `SELECT COUNT(*) FROM vacations;` should be > 0

**Option B: If you restarted application**
- [ ] Check logs: Should see "Vacation Management System - Database Migration Complete!"
- [ ] Verify: Both schools present in database

---

## Issues & Troubleshooting

### ❌ Issue 1: Still Getting FK Error

**Diagnosis:**
- [ ] Fix wasn't applied completely
- [ ] Application needs restart
- [ ] Database wasn't actually updated

**Solution:**
- [ ] Verify schools exist: `SELECT * FROM schools WHERE id IN (1, 2);`
- [ ] If empty, rerun the fix using **METHOD 1 (SQL)**
- [ ] If schools exist but still getting error, **METHOD 2 (Restart)**

---

### ❌ Issue 2: Getting 400 Error on Vacation POST

**Possible Causes:**
- [ ] vacation_type is not uppercase (use "AUTUMN" not "Autumn")
- [ ] School ID doesn't exist (verify with GET /api/schools)
- [ ] Start date is in the past (must be future date)

**Solution:**
- [ ] Check request body matches exactly (see VERIFICATION 2 above)
- [ ] Ensure dates are in future format YYYY-MM-DD

---

### ❌ Issue 3: Duplicate Key Error

**Diagnosis:**
- [ ] Vacation already exists with same details
- [ ] Running fix script multiple times

**Solution:**
- [ ] This is safe - scripts use `ON CONFLICT DO NOTHING`
- [ ] It won't create duplicates
- [ ] Just retry your vacation POST with different details

---

### ❌ Issue 4: Application Won't Start After Restart

**Diagnosis:**
- [ ] Port 9091 already in use
- [ ] Database migration failed
- [ ] Missing dependencies

**Solution:**
- [ ] Check logs for migration errors
- [ ] Verify database connection
- [ ] Kill process on port 9091: `lsof -i :9091` then `kill -9 <PID>`

---

## Rollback Procedure (If Needed)

**If you need to undo the changes:**

```sql
-- Delete new vacations created (optional)
DELETE FROM vacations WHERE school_id IN (1, 2);

-- Delete new schools created (if you want)
-- DELETE FROM schools WHERE id IN (1, 2);

-- Revert model changes: Restore original Vacation.java
-- (Remove MONSOON from enum)
```

**Note:** Rollback is rarely needed. All changes are safe and non-breaking.

---

## Sign-Off Checklist

**Complete Fix:**
- [ ] Fix method applied (choose 1 of 3)
- [ ] Schools verified to exist
- [ ] Vacation POST request succeeds
- [ ] All 9 vacation types testable
- [ ] No FK constraint errors
- [ ] No duplicate errors

**Ready for Production:**
- [ ] Changes merged/deployed
- [ ] Documentation reviewed
- [ ] Team notified of changes
- [ ] Monitoring enabled

---

## Success Criteria

✅ **All of these must be true:**

- [ ] GET /api/schools returns 2+ schools
- [ ] School 1 (Delhi) with id=1 exists
- [ ] School 2 (Mumbai) with id=2 exists
- [ ] POST /api/vacations/admin/create succeeds
- [ ] Response contains valid vacation data
- [ ] No FK constraint errors
- [ ] All vacation types work
- [ ] Application runs without errors

---

## Final Status

| Item | Status | Date |
|------|--------|------|
| Problem Identified | ✅ | Today |
| Fix Applied | ⏳ | (Your date) |
| Verification Passed | ⏳ | (Your date) |
| Ready for Production | ⏳ | (Your date) |

---

## Notes Section

**Date Applied:** ________________
**Method Used:** [ ] SQL [ ] Restart [ ] API
**Applied By:** ________________
**Verified By:** ________________
**Issues Encountered:** ________________________________

---

## Quick Reference

**Your Original Request:**
```json
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

**Your Original Error:**
```
violates foreign key constraint "vacations_school_id_fkey"
Key (school_id)=(2) is not present in table "schools".
```

**What Fixed It:**
- Added schools to database
- Added MONSOON vacation type
- Updated database migration

---

## Documentation Links

- 🚀 Start Here: `ACTION_PLAN_FIX_VACATION_FK_ERROR.md`
- 📚 Full Guide: `VACATION_FOREIGN_KEY_FIX.md`
- 📊 Visuals: `VACATION_FOREIGN_KEY_FIX_DIAGRAM.md`
- 🔧 Quick Fix: `QUICK_FIX_SCHOOL_VACATION.sql`
- 📑 Index: `VACATION_FK_INDEX.md`

---

## Completion Status

### ✅ Checklist Complete When:
- [ ] All fixes applied
- [ ] All verifications passed
- [ ] All issues resolved
- [ ] This checklist signed off

**Status: READY FOR DEPLOYMENT** 🚀

---

**Date Completed:** ________________
**Signature:** ________________
**Approved By:** ________________

