# ✅ Vacation API - Foreign Key Error Resolution

## Problem Summary
When posting vacation data with `school_id: 2`, you received this error:
```json
{
    "success": false,
    "message": "Failed to create vacation: ... violates foreign key constraint \"vacations_school_id_fkey\" ... Key (school_id)=(2) is not present in table \"schools\"."
}
```

## Root Cause
The **schools table doesn't have a record with id=2** (Mumbai Academy). The vacations table has a foreign key constraint that requires the school_id to reference an existing school.

## Solution Implemented

### 1. ✅ Updated Vacation Model
- Added `MONSOON` vacation type to the enum (as mentioned in your earlier request)
- Location: `src/main/java/com/org/careerbuilder/models/Vacation.java`

### 2. ✅ Updated Database Migration
- Updated `vacations_migration.sql` to include school data insertion BEFORE vacation creation
- Added schools: 
  - **School ID 1**: Delhi Public School (DPS001)
  - **School ID 2**: Mumbai Academy (MA001)
- Added sample vacation data for both schools

### 3. ✅ Created Quick Fix Script
- File: `QUICK_FIX_SCHOOL_VACATION.sql`
- Use this to manually insert missing school data

## What to Do Now

### Option A: Run the Quick Fix Script (Immediate)
1. Open your database client (pgAdmin or psql)
2. Execute: `QUICK_FIX_SCHOOL_VACATION.sql`
3. This will insert the missing schools

```bash
# In pgAdmin: Paste the script and execute
# Or in terminal:
psql -U postgres -d your_database -f QUICK_FIX_SCHOOL_VACATION.sql
```

### Option B: Restart Application (Automatic)
1. Stop your Spring Boot application
2. The migration will run automatically on startup
3. All necessary schools and sample vacation data will be created

## Retry Your API Request

After executing the fix, retry your POST request:

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

✅ **Expected Result**: HTTP 200 with vacation created successfully

## Valid School IDs
After the fix, you can use:
- **school_id: 1** - Delhi Public School
- **school_id: 2** - Mumbai Academy

## Valid Vacation Types
All of these vacation types are now supported:
- `HOLIDAY` - Holiday/Festival
- `EXAM_BREAK` - Exam Break
- `SUMMER` - Summer Vacation
- `WINTER` - Winter Vacation
- `SPRING` - Spring Vacation
- `AUTUMN` - Autumn Vacation
- `MONSOON` - Monsoon Vacation (newly added)
- `SPECIAL` - Special Leave
- `EMERGENCY` - Emergency Closure

## Files Modified
1. ✅ `src/main/java/com/org/careerbuilder/models/Vacation.java` - Added MONSOON type
2. ✅ `vacations_migration.sql` - Added school insertion and sample vacation data
3. ✅ `QUICK_FIX_SCHOOL_VACATION.sql` - New quick fix script

## Troubleshooting

### If you still get "school not found" error:
1. Verify schools table exists: `SELECT * FROM schools;`
2. Check if school_id exists: `SELECT * FROM schools WHERE id = 2;`
3. Run `QUICK_FIX_SCHOOL_VACATION.sql` if school doesn't exist

### If you get duplicate key error:
- The migration has `ON CONFLICT DO NOTHING` to handle this
- Safe to retry multiple times

## Testing the Fix

You can test that schools are created with:
```bash
GET http://localhost:9091/api/schools
```

Should return both schools (id 1 and 2)

