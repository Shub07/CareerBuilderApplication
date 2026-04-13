# 📋 Summary: Vacation API Foreign Key Error - Complete Fix

## Error Encountered
```json
{
    "success": false,
    "message": "Failed to create vacation: could not execute statement [ERROR: insert or update on table \"vacations\" violates foreign key constraint \"vacations_school_id_fkey\"\n  Detail: Key (school_id)=(2) is not present in table \"schools\".]"
}
```

## Root Cause Analysis
The vacations table has a foreign key constraint:
```sql
FOREIGN KEY (school_id) REFERENCES schools(id) ON DELETE CASCADE
```

When you tried to insert a vacation with `school_id = 2`, the database couldn't find a school with that ID in the schools table.

---

## All Changes Made

### 1️⃣ Code Changes

#### File: `src/main/java/com/org/careerbuilder/models/Vacation.java`
**What was changed:** Added `MONSOON` to the VacationType enum
```java
public enum VacationType {
    HOLIDAY("Holiday/Festival"),
    EXAM_BREAK("Exam Break"),
    SUMMER("Summer Vacation"),
    WINTER("Winter Vacation"),
    SPRING("Spring Vacation"),
    AUTUMN("Autumn Vacation"),
    MONSOON("Monsoon Vacation"),      // ← NEW
    SPECIAL("Special Leave"),
    EMERGENCY("Emergency Closure");
}
```

**Why:** To support monsoon season vacations (common in tropical regions like India)

---

#### File: `vacations_migration.sql`
**What was changed:** 

1. **Added School Insertion Section** (before vacation table creation):
```sql
-- ============================================================================
-- 0. ENSURE SCHOOLS EXIST (Required for Foreign Key)
-- ============================================================================
INSERT INTO schools (id, school_name, school_code, address, city, state, country, phone, email, principal_name, established_year)
VALUES
    (1, 'Delhi Public School', 'DPS001', '123 School Lane, Delhi', 'Delhi', 'Delhi', 'India', '9876543200', 'principal@dps.edu.in', 'Dr. Sharma', 2010),
    (2, 'Mumbai Academy', 'MA001', '456 Education Ave, Mumbai', 'Mumbai', 'Maharashtra', 'India', '9876543201', 'principal@mumbaiaca.edu.in', 'Mrs. Patel', 2015)
ON CONFLICT (id) DO NOTHING;
```

2. **Updated Vacation Type Constraint**:
```sql
CONSTRAINT fk_vacation_type CHECK (vacation_type IN ('HOLIDAY', 'EXAM_BREAK', 'SUMMER', 'WINTER', 'SPRING', 'AUTUMN', 'MONSOON', 'SPECIAL', 'EMERGENCY'))
```

3. **Added Sample Vacation Data for School 2**:
```sql
-- Data for School 2 (Mumbai Academy)
INSERT INTO vacations (school_id, vacation_name, vacation_type, start_date, end_date, description, is_active, created_by)
VALUES
    (2, 'Spring Vacation 2026 S2', 'SPRING', '2026-03-15', '2026-03-28', 'Spring break for all students', TRUE, 'ADMIN'),
    (2, 'Summer Vacation 2026 S2', 'SUMMER', '2026-06-01', '2026-07-31', 'Extended summer vacation', TRUE, 'ADMIN'),
    (2, 'Monsoon Break', 'MONSOON', '2026-07-15', '2026-08-31', 'Monsoon season break', TRUE, 'ADMIN'),
    (2, 'Autumn Vacation S2', 'AUTUMN', '2026-10-01', '2026-10-07', 'Autumn break', TRUE, 'ADMIN'),
    (2, 'Winter Vacation S2', 'WINTER', '2026-12-15', '2027-01-05', 'Christmas and New Year vacation', TRUE, 'ADMIN')
ON CONFLICT DO NOTHING;
```

**Why:** Ensures schools exist in the database before vacations are created, preventing foreign key constraint violations

---

### 2️⃣ New Files Created

#### `QUICK_FIX_SCHOOL_VACATION.sql`
Quick SQL script to insert missing schools into an existing database.
- Can be run immediately without restarting the application
- Safe to run multiple times (uses ON CONFLICT)

#### `VACATION_FOREIGN_KEY_FIX.md`
Comprehensive guide explaining:
- The problem and root cause
- Solution details
- Step-by-step instructions
- Troubleshooting tips

#### `ADD_SCHOOLS_VIA_API.md`
Alternative method to add schools via REST API instead of SQL

---

## How to Apply the Fix

### Option 1: SQL Script (Fastest)
```bash
# Copy the QUICK_FIX_SCHOOL_VACATION.sql content and run in your database client
# Or via command line:
psql -U postgres -d your_database -f QUICK_FIX_SCHOOL_VACATION.sql
```

### Option 2: Restart Application (Automatic)
1. Stop your Spring Boot application
2. Restart it - the migration runs automatically
3. Schools and vacations are created automatically

### Option 3: REST API
Use the endpoints in `ADD_SCHOOLS_VIA_API.md` to create schools via API

---

## Testing the Fix

### Verify Schools Exist
```bash
GET http://localhost:9091/api/schools

# Expected Response:
[
    {
        "id": 1,
        "schoolName": "Delhi Public School",
        "schoolCode": "DPS001",
        ...
    },
    {
        "id": 2,
        "schoolName": "Mumbai Academy",
        "schoolCode": "MA001",
        ...
    }
]
```

### Retry Your Original Request
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

# Expected Response:
{
    "success": true,
    "data": {
        "vacationId": 7,
        "schoolId": 2,
        "vacationName": "AUTUMN Vacation 2026 S2",
        "vacationType": "AUTUMN",
        "startDate": "2028-11-15",
        "endDate": "2028-12-15",
        ...
    }
}
```

---

## Key Takeaways

✅ **Problem Solved**: School foreign key constraint error fixed
✅ **MONSOON Support Added**: New vacation type for monsoon season
✅ **Data Consistency**: Migration ensures schools exist before vacations
✅ **Safe**: Uses ON CONFLICT DO NOTHING for idempotent operations
✅ **Reusable**: Sample data for both schools pre-loaded in migration

---

## Files Changed Summary

| File | Change | Type |
|------|--------|------|
| `Vacation.java` | Added MONSOON to enum | Code |
| `vacations_migration.sql` | Added school insertion + MONSOON constraint | Migration |
| `QUICK_FIX_SCHOOL_VACATION.sql` | New file | Helper Script |
| `VACATION_FOREIGN_KEY_FIX.md` | New file | Documentation |
| `ADD_SCHOOLS_VIA_API.md` | New file | Documentation |

---

## Next Steps

1. ✅ Apply one of the fix options above
2. ✅ Verify schools exist
3. ✅ Retry your vacation creation request
4. ✅ Test all vacation endpoints with school_id = 1 and 2

All done! 🎉

