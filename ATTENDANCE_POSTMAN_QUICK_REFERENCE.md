# 🚀 POSTMAN QUICK COPY-PASTE REFERENCE

## Quick Copy Commands for Testing

---

## 1. MARK ATTENDANCE - Individual Student

**Method**: POST  
**URL**: http://localhost:9091/api/attendance/mark

**Headers**:
```
Content-Type: application/json
```

**Body (Raw JSON)**:
```json
{
  "studentId": 1,
  "date": "2026-04-01",
  "status": "PRESENT",
  "className": "10",
  "section": "A",
  "subjectId": 5,
  "remarks": "Present for regular class"
}
```

---

## 2. BULK MARK ATTENDANCE - Entire Class

**Method**: POST  
**URL**: http://localhost:9091/api/attendance/bulk/mark

**Headers**:
```
Content-Type: application/json
```

**Body (Raw JSON)**:
```json
{
  "schoolId": 1,
  "date": "2026-04-01",
  "className": "10",
  "section": "A",
  "subjectId": 5,
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
    },
    {
      "studentId": 3,
      "status": "LEAVE",
      "remarks": "Medical leave"
    },
    {
      "studentId": 4,
      "status": "PRESENT",
      "remarks": "Present"
    },
    {
      "studentId": 5,
      "status": "ABSENT",
      "remarks": "Not available"
    }
  ]
}
```

---

## 3. GET STUDENT ATTENDANCE SUMMARY

**Method**: GET  
**URL**: http://localhost:9091/api/attendance/student/1/summary?from=2026-04-01&to=2026-04-30

**Headers**:
```
(No special headers needed)
```

**Expected Response**:
```json
{
  "studentId": 1,
  "studentName": "Student Name",
  "className": "10",
  "section": "A",
  "attendancePercentage": 92,
  "totalDaysExpected": 24,
  "totalDaysPresent": 22,
  "totalDaysAbsent": 1,
  "totalDaysLeave": 1,
  "periodStartDate": "2026-04-01",
  "periodEndDate": "2026-04-30",
  "status": "GOOD"
}
```

---

## 4. GET CLASS ATTENDANCE REPORT

**Method**: GET  
**URL**: http://localhost:9091/api/attendance/report/class/10/A?date=2026-04-01

**Expected Response**:
```json
{
  "className": "10",
  "section": "A",
  "reportDate": "2026-04-01",
  "totalStudents": 45,
  "presentCount": 42,
  "absentCount": 2,
  "leaveCount": 1,
  "attendancePercentage": 93.33
}
```

---

## 5. GET SCHOOL ATTENDANCE REPORT

**Method**: GET  
**URL**: http://localhost:9091/api/attendance/report/school/1?date=2026-04-01

**Expected Response**: Array of class reports
```json
[
  {
    "className": "10",
    "section": "A",
    "reportDate": "2026-04-01",
    "totalStudents": 45,
    "presentCount": 42,
    "absentCount": 2,
    "leaveCount": 1,
    "attendancePercentage": 93.33
  },
  {
    "className": "10",
    "section": "B",
    "reportDate": "2026-04-01",
    "totalStudents": 48,
    "presentCount": 46,
    "absentCount": 1,
    "leaveCount": 1,
    "attendancePercentage": 97.92
  }
]
```

---

## 6. UPDATE ATTENDANCE

**Method**: PUT  
**URL**: http://localhost:9091/api/attendance/1

**Headers**:
```
Content-Type: application/json
```

**Body (Raw JSON)**:
```json
{
  "status": "LEAVE",
  "remarks": "Updated to leave status"
}
```

---

## 7. GET ATTENDANCE RECORD

**Method**: GET  
**URL**: http://localhost:9091/api/attendance/1

---

## 8. DELETE ATTENDANCE

**Method**: DELETE  
**URL**: http://localhost:9091/api/attendance/1

---

## 9. REQUEST EXCEPTION - Sick Leave

**Method**: POST  
**URL**: http://localhost:9091/api/attendance/exception/request

**Headers**:
```
Content-Type: application/json
```

**Body (Raw JSON)**:
```json
{
  "studentId": 1,
  "exceptionDate": "2026-04-02",
  "exceptionType": "SICK_LEAVE",
  "reason": "Viral fever with cough",
  "attachmentUrl": "https://example.com/medical-cert.pdf"
}
```

---

## 10. REQUEST EXCEPTION - Medical

**Method**: POST  
**URL**: http://localhost:9091/api/attendance/exception/request

**Body (Raw JSON)**:
```json
{
  "studentId": 1,
  "exceptionDate": "2026-04-03",
  "exceptionType": "MEDICAL",
  "reason": "Doctor appointment for check-up",
  "attachmentUrl": "https://example.com/appointment.pdf"
}
```

---

## 11. REQUEST EXCEPTION - Field Trip

**Method**: POST  
**URL**: http://localhost:9091/api/attendance/exception/request

**Body (Raw JSON)**:
```json
{
  "studentId": 1,
  "exceptionDate": "2026-04-04",
  "exceptionType": "FIELD_TRIP",
  "reason": "Educational field trip to museum",
  "attachmentUrl": "https://example.com/permission.pdf"
}
```

---

## 12. APPROVE EXCEPTION

**Method**: PUT  
**URL**: http://localhost:9091/api/attendance/exception/1/approve?approverUserId=5&remarks=Approved

---

## 13. REJECT EXCEPTION

**Method**: PUT  
**URL**: http://localhost:9091/api/attendance/exception/1/reject?rejecterUserId=5&remarks=Rejected

---

## 14. GET PENDING EXCEPTIONS

**Method**: GET  
**URL**: http://localhost:9091/api/attendance/exception/pending?page=0&size=10

---

## 15. GET STUDENT EXCEPTIONS

**Method**: GET  
**URL**: http://localhost:9091/api/attendance/exception/student/1?page=0&size=10

---

## 16. GET EXCEPTION BY ID

**Method**: GET  
**URL**: http://localhost:9091/api/attendance/exception/1

---

## 17. GET SETTINGS

**Method**: GET  
**URL**: http://localhost:9091/api/attendance/settings/1

---

## 18. UPDATE SETTINGS

**Method**: PUT  
**URL**: http://localhost:9091/api/attendance/settings/1

**Headers**:
```
Content-Type: application/json
```

**Body (Raw JSON)**:
```json
{
  "minAttendancePercentage": 80,
  "lowAttendanceThreshold": 80,
  "maxRetroactiveDays": 5,
  "allowBulkMarking": true,
  "allowRetroactiveMarking": true,
  "notifyLowAttendance": true,
  "workingDaysPerWeek": 5
}
```

---

## 19. INITIALIZE SETTINGS

**Method**: POST  
**URL**: http://localhost:9091/api/attendance/settings/2/initialize

---

## 20. GET STUDENT ATTENDANCE RECORDS

**Method**: GET  
**URL**: http://localhost:9091/api/attendance/student/1?from=2026-03-01&to=2026-04-30&page=0&size=10

---

## 21. GET ATTENDANCE PERCENTAGE

**Method**: GET  
**URL**: http://localhost:9091/api/attendance/student/1/percentage?from=2026-04-01&to=2026-04-30

---

## 22. GET CLASS ATTENDANCE RECORDS

**Method**: GET  
**URL**: http://localhost:9091/api/attendance/class/10/A?from=2026-03-01&to=2026-04-30&page=0&size=10

---

## 📋 TEST FLOW EXAMPLE

### Step 1: Mark Attendance
```
POST http://localhost:9091/api/attendance/mark
Body: Individual attendance JSON
```

### Step 2: Get Student Summary
```
GET http://localhost:9091/api/attendance/student/1/summary?from=2026-04-01&to=2026-04-30
```

### Step 3: Get Class Report
```
GET http://localhost:9091/api/attendance/report/class/10/A?date=2026-04-01
```

### Step 4: Request Exception
```
POST http://localhost:9091/api/attendance/exception/request
Body: Exception JSON
```

### Step 5: Approve Exception
```
PUT http://localhost:9091/api/attendance/exception/1/approve?approverUserId=5
```

### Step 6: Check Pending Exceptions
```
GET http://localhost:9091/api/attendance/exception/pending?page=0&size=10
```

---

## 🔧 USEFUL POSTMAN VARIABLES

Add these as Environment or Collection variables for easier testing:

```
{{BASE_URL}} = http://localhost:9091/api/attendance
{{STUDENT_ID}} = 1
{{CLASS_NAME}} = 10
{{SECTION}} = A
{{SCHOOL_ID}} = 1
{{EXCEPTION_ID}} = 1
{{APPROVER_ID}} = 5
```

### Usage in URLs:
```
GET {{BASE_URL}}/student/{{STUDENT_ID}}/summary?from=2026-04-01&to=2026-04-30
PUT {{BASE_URL}}/exception/{{EXCEPTION_ID}}/approve?approverUserId={{APPROVER_ID}}
```

---

## ✅ STATUS CODES

| Code | Meaning |
|------|---------|
| 200 | OK - Request successful |
| 201 | Created - Resource created successfully |
| 400 | Bad Request - Invalid input |
| 404 | Not Found - Resource not found |
| 409 | Conflict - Resource already exists |
| 500 | Server Error |

---

## 🎯 QUICK TESTING CHECKLIST

- [ ] Mark individual attendance (POST)
- [ ] Bulk mark class attendance (POST)
- [ ] Get student summary (GET)
- [ ] Get class report (GET)
- [ ] Get school report (GET)
- [ ] Update attendance (PUT)
- [ ] Request exception (POST)
- [ ] Approve exception (PUT)
- [ ] Get pending exceptions (GET)
- [ ] Get student exceptions (GET)
- [ ] Update settings (PUT)
- [ ] Get settings (GET)

---

## 💡 TIPS FOR POSTMAN

1. **Save Requests** - Click Save to save each request
2. **Create Collection** - Group related requests
3. **Use Environment** - Create variables for dynamic values
4. **Tests Tab** - Add tests to validate responses
5. **Pre-request Script** - Set up data before requests
6. **Authorization** - Add auth if needed (JWT, OAuth)

---

**Last Updated**: April 1, 2026  
**Total Endpoints**: 30  
**Ready for Testing**: ✅

