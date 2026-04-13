# Career Builder API - Postman Testing & Troubleshooting Guide

## Quick Start

### Step 1: Verify Application is Running
```
URL: http://localhost:9091
Expected: No response or favicon error (this is normal)
```

### Step 2: Import Postman Collection
1. Open Postman
2. Click **Import**
3. Select file: `Attendance_API_Complete.postman_collection.json`
4. Click **Import**

### Step 3: Run Your First Request
1. Select: **1. Mark Attendance**
2. Click **Send**
3. Expected Response (Status 201):
```json
{
  "success": true,
  "message": "Attendance marked successfully",
  "data": { /* attendance record */ }
}
```

---

## JSON Request Format Requirements

### Valid JSON Structure
✅ **CORRECT:**
```json
{
  "studentId": 1,
  "date": "2026-04-01",
  "status": "PRESENT",
  "className": "10A",
  "remarks": "Present in class"
}
```

❌ **INCORRECT (Missing quotes):**
```json
{
  studentId: 1,
  date: 2026-04-01,
  status: PRESENT
}
```

❌ **INCORRECT (Wrong data type):**
```json
{
  "studentId": "1",
  "date": 2026-04-01,
  "status": "present"
}
```

---

## Common Issues & Solutions

### Issue 1: 400 Bad Request - Validation Error
**Error Message:**
```json
{
  "message": "Validation failed",
  "errors": {
    "studentId": "Student ID is required",
    "date": "Date is required"
  }
}
```

**Solution:**
- Ensure all required fields are included
- Use correct data types (numbers for IDs, strings for dates/statuses)
- Date format must be YYYY-MM-DD

---

### Issue 2: 404 Not Found - Resource Not Found
**Error Message:**
```json
{
  "success": false,
  "message": "Failed to fetch attendance record",
  "error": "Attendance record not found with ID: 999"
}
```

**Solution:**
- Verify the attendance ID exists in the database
- Use a valid student ID when creating records
- Check if the record was already deleted

---

### Issue 3: 409 Conflict - Duplicate Entry
**Error Message:**
```json
{
  "success": false,
  "message": "Duplicate or constraint violation",
  "detail": "Check unique fields"
}
```

**Solution:**
- Cannot mark attendance twice for same student on same date
- Update existing record instead (use PUT endpoint)
- Check if record already exists before creating new one

---

### Issue 4: Invalid Status Value
**Error Message:**
```json
{
  "success": false,
  "message": "Validation failed",
  "errors": {
    "status": "Invalid enum value"
  }
}
```

**Solution:**
- Use ONLY these values: `PRESENT`, `ABSENT`, or `LEAVE`
- Status is case-sensitive
- ❌ Wrong: `present`, `Present`, `absent_leave`
- ✅ Correct: `PRESENT`, `ABSENT`, `LEAVE`

---

### Issue 5: Invalid Date Format
**Error Message:**
```json
{
  "success": false,
  "error": "Text '04-01-2026' could not be parsed"
}
```

**Solution:**
- Use format: `YYYY-MM-DD`
- ❌ Wrong: `04-01-2026`, `2026/04/01`, `April 1, 2026`
- ✅ Correct: `2026-04-01`

---

## Testing Scenarios

### Scenario 1: Mark Attendance for One Student
1. **Endpoint:** POST /api/attendance/mark
2. **Data:**
   ```json
   {
     "studentId": 1,
     "date": "2026-04-01",
     "status": "PRESENT",
     "className": "10A",
     "remarks": "Marked present"
   }
   ```
3. **Expected:** Status 201, success: true

---

### Scenario 2: Bulk Mark Attendance
1. **Endpoint:** POST /api/attendance/bulk-mark
2. **Data:**
   ```json
   {
     "schoolId": 1,
     "date": "2026-04-01",
     "className": "10A",
     "section": "A",
     "records": [
       {
         "studentId": 1,
         "status": "PRESENT",
         "remarks": "Present"
       },
       {
         "studentId": 2,
         "status": "ABSENT",
         "remarks": "Sick"
       }
     ]
   }
   ```
3. **Expected:** Status 201, all records processed

---

### Scenario 3: Get Student Summary
1. **Endpoint:** GET /api/attendance/student/1/summary
2. **Query Params:** fromDate=2026-04-01&toDate=2026-04-30
3. **Expected:** Status 200, returns summary with statistics

---

### Scenario 4: Get Class Report
1. **Endpoint:** GET /api/attendance/class/report
2. **Query Params:** className=10A&section=A&date=2026-04-01
3. **Expected:** Status 200, returns class attendance summary

---

## Postman Environment Setup (Optional)

### Create Environment Variable
1. Click **Environments** (left panel)
2. Click **Create**
3. Add variables:
   ```
   base_url: http://localhost:9091
   student_id: 1
   class_name: 10A
   section: A
   ```

### Use in Requests
Replace URLs:
```
❌ http://localhost:9091/api/attendance/student/1
✅ {{base_url}}/api/attendance/student/{{student_id}}
```

---

## Postman Pre-request Script (Optional)

For automatic date generation, add pre-request script:
```javascript
// Set current date
let today = new Date();
let date_str = today.toISOString().split('T')[0];
pm.environment.set("current_date", date_str);

// Set date 30 days ago
let past = new Date(today.getTime() - 30*24*60*60*1000);
let past_str = past.toISOString().split('T')[0];
pm.environment.set("past_date", past_str);
```

Then use: `{{current_date}}` and `{{past_date}}` in requests

---

## Response Status Codes

| Code | Meaning | Example |
|------|---------|---------|
| 200 | OK - Request successful | GET records |
| 201 | Created - Resource created | POST mark attendance |
| 400 | Bad Request - Invalid data | Missing required field |
| 404 | Not Found - Resource doesn't exist | Invalid ID |
| 409 | Conflict - Duplicate entry | Marking same attendance twice |
| 500 | Server Error - Internal error | Database connection issue |

---

## Debugging Steps

### Step 1: Check Request Format
```
✓ Content-Type: application/json
✓ All required fields present
✓ Correct data types
✓ Valid enum values
```

### Step 2: Check Response Headers
```
Response Status: 200/201/400/404
Response Time: < 1000ms (ideally)
Content-Type: application/json
```

### Step 3: Validate JSON
- Use Postman's JSON validator
- Copy response to https://jsonlint.com/
- Check for syntax errors

### Step 4: Check Database
```sql
-- Check if student exists
SELECT * FROM students WHERE id = 1;

-- Check attendance records
SELECT * FROM attendance_records 
WHERE student_id = 1 AND att_date = '2026-04-01';
```

---

## API Health Check

### Verify All Endpoints Working
Run these requests in sequence:

1. **Mark Attendance**
   - POST /api/attendance/mark
   - Status: 201

2. **Get Attendance**
   - GET /api/attendance/1
   - Status: 200

3. **Get Student Summary**
   - GET /api/attendance/student/1/summary
   - Status: 200

4. **Get Statistics**
   - GET /api/attendance/student/1/statistics
   - Status: 200

If all return success, API is healthy! ✅

---

## Sample cURL Commands

```bash
# Mark Attendance
curl -X POST http://localhost:9091/api/attendance/mark \
  -H "Content-Type: application/json" \
  -d '{
    "studentId": 1,
    "date": "2026-04-01",
    "status": "PRESENT",
    "className": "10A",
    "remarks": "Present"
  }'

# Get Student Summary
curl -X GET "http://localhost:9091/api/attendance/student/1/summary?fromDate=2026-04-01&toDate=2026-04-30"

# Get Class Report
curl -X GET "http://localhost:9091/api/attendance/class/report?className=10A&section=A&date=2026-04-01"
```

---

## Performance Tips

1. **Use Pagination:** Add page and size parameters for large datasets
2. **Filter by Date Range:** Use fromDate and toDate to limit results
3. **Batch Operations:** Use bulk endpoints for multiple records
4. **Cache Results:** Reuse responses when possible

---

## Support Information

- **Server:** http://localhost:9091
- **API Base Path:** /api/attendance
- **Documentation:** See ATTENDANCE_API_COMPLETE_GUIDE.md
- **Database:** PostgreSQL on localhost:5432
- **Database Name:** admindb

---

**Last Updated:** April 1, 2026
**Version:** 1.0
**Status:** Production Ready ✅

