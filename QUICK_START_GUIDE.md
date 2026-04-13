# 🚀 QUICK START - Attendance API Testing

## In 3 Easy Steps

### Step 1: Start Application
```bash
cd C:\Users\Admin\Downloads\career-builder-backend-public-apis\career-builder-backend-main
mvn spring-boot:run
```
Or use JAR:
```bash
java -jar target/career-builder-0.0.1-SNAPSHOT.jar
```

**Wait for:** `Started CareerBuilderApplication`

---

### Step 2: Import Postman Collection
1. Open **Postman**
2. Click **Import**
3. Select: `Attendance_API_Complete.postman_collection.json`
4. Click **Import**

---

### Step 3: Test First Endpoint
1. In Postman, find "**1. Mark Attendance**"
2. Click **Send**
3. ✅ You should see a success response!

```json
{
  "success": true,
  "message": "Attendance marked successfully",
  "data": { /* attendance record */ }
}
```

---

## Example: Mark Attendance Request

### Request Details
```
POST http://localhost:9091/api/attendance/mark
Content-Type: application/json
```

### Request Body
```json
{
  "studentId": 1,
  "date": "2026-04-01",
  "status": "PRESENT",
  "className": "10A",
  "remarks": "Present in class"
}
```

### Expected Response (Status 201)
```json
{
  "success": true,
  "message": "Attendance marked successfully",
  "data": {
    "attendanceId": 1,
    "studentId": 1,
    "studentName": "John Doe",
    "rollNo": "1",
    "date": "2026-04-01",
    "status": "PRESENT",
    "className": "10A",
    "remarks": "Present in class",
    "createdAt": "2026-04-01T22:30:00",
    "updatedAt": "2026-04-01T22:30:00"
  }
}
```

---

## All 12 Endpoints

### 1️⃣ Mark Attendance
```
POST /api/attendance/mark
→ Status: 201 Created
```

### 2️⃣ Update Attendance
```
PUT /api/attendance/{id}
→ Status: 200 OK
```

### 3️⃣ Get Attendance
```
GET /api/attendance/{id}
→ Status: 200 OK
```

### 4️⃣ Delete Attendance
```
DELETE /api/attendance/{id}
→ Status: 200 OK
```

### 5️⃣ Bulk Mark
```
POST /api/attendance/bulk-mark
→ Status: 201 Created
```

### 6️⃣ Student Records
```
GET /api/attendance/student/{id}
→ Status: 200 OK
```

### 7️⃣ Student Summary
```
GET /api/attendance/student/{id}/summary
→ Status: 200 OK
```

### 8️⃣ Class Attendance
```
GET /api/attendance/class
→ Status: 200 OK
```

### 9️⃣ Class Report
```
GET /api/attendance/class/report
→ Status: 200 OK
```

### 🔟 School Report
```
GET /api/attendance/school/report
→ Status: 200 OK
```

### 1️⃣1️⃣ Percentage
```
GET /api/attendance/student/{id}/percentage
→ Status: 200 OK
```

### 1️⃣2️⃣ Statistics
```
GET /api/attendance/student/{id}/statistics
→ Status: 200 OK
```

---

## Status Values

✅ Use EXACTLY these values:
- `PRESENT` - Student attended
- `ABSENT` - Student did not attend
- `LEAVE` - Student is on approved leave

---

## Date Format

✅ Always use: `YYYY-MM-DD`

Examples:
- ✅ `2026-04-01`
- ✅ `2026-12-25`
- ❌ `04-01-2026`
- ❌ `April 1, 2026`
- ❌ `2026/04/01`

---

## Common Response Codes

| Code | Meaning |
|------|---------|
| 200 | ✅ Success - Request OK |
| 201 | ✅ Success - Created |
| 400 | ❌ Bad Request - Check data |
| 404 | ❌ Not Found - Invalid ID |
| 409 | ❌ Conflict - Duplicate |
| 500 | ❌ Server Error - Try again |

---

## JSON Validation Tips

### ✅ VALID
```json
{
  "studentId": 1,
  "date": "2026-04-01",
  "status": "PRESENT",
  "className": "10A"
}
```

### ❌ INVALID
```json
{
  studentId: 1,
  "date": "04-01-2026",
  "status": "present",
  "className": "10A",
}
```

**Issues:**
- Missing quotes around `studentId`
- Wrong date format
- Wrong status case
- Trailing comma

---

## Quick Troubleshooting

| Problem | Solution |
|---------|----------|
| 400 Bad Request | Check JSON syntax and required fields |
| 404 Not Found | Verify student/attendance ID exists |
| 409 Conflict | Record already exists, use UPDATE |
| Connection refused | Start application first |
| No response | Check date format (YYYY-MM-DD) |

---

## Environment Variables (Optional)

In Postman, create environment:

```
base_url: http://localhost:9091
student_id: 1
class_name: 10A
section: A
from_date: 2026-04-01
to_date: 2026-04-30
```

Use in requests: `{{base_url}}/api/attendance/mark`

---

## Files You Have

| File | Purpose |
|------|---------|
| `FINAL_API_SOLUTION.md` | Complete solution guide |
| `ATTENDANCE_API_COMPLETE_GUIDE.md` | Full API reference |
| `POSTMAN_TESTING_GUIDE.md` | Testing guide |
| `Attendance_API_Complete.postman_collection.json` | Import into Postman |
| `AttendanceController.java` | REST controller (source code) |

---

## Next Steps

1. ✅ Start application
2. ✅ Import Postman collection
3. ✅ Run "1. Mark Attendance"
4. ✅ Get success response
5. ✅ Test other endpoints

**That's it!** All 12 endpoints are ready to use.

---

**Ready to test?** Start the application and import the Postman collection! 🎉

Questions? See `FINAL_API_SOLUTION.md` for detailed information.

