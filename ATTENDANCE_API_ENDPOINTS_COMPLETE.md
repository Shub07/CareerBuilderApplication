# 📡 ATTENDANCE API - COMPLETE ENDPOINT REFERENCE WITH JSON PAYLOADS

## Base URL
```
http://localhost:9091/api/attendance
```

---

## 📋 TABLE OF CONTENTS

1. Individual Attendance Operations (4 endpoints)
2. Bulk Attendance Operations (1 endpoint)
3. Student Attendance Retrieval (3 endpoints)
4. Class Attendance Operations (3 endpoints)
5. Attendance Settings (3 endpoints)
6. Attendance Exceptions (6 endpoints)

---

## 1️⃣ INDIVIDUAL ATTENDANCE OPERATIONS

### 1.1 Mark Attendance
```
POST /api/attendance/mark
Content-Type: application/json

REQUEST PAYLOAD:
{
  "studentId": 1,
  "date": "2026-04-01",
  "status": "PRESENT",
  "className": "10",
  "section": "A",
  "subjectId": 5,
  "remarks": "Present for regular class"
}

RESPONSE (201 Created):
{
  "attendanceId": 1,
  "studentId": 1,
  "studentName": "John Doe",
  "rollNo": "101",
  "date": "2026-04-01",
  "status": "PRESENT",
  "className": "10",
  "section": "A",
  "subjectId": 5,
  "remarks": "Present for regular class",
  "createdAt": "2026-04-01T09:30:00",
  "updatedAt": "2026-04-01T09:30:00"
}
```

### 1.2 Update Attendance
```
PUT /api/attendance/{attendanceId}
Content-Type: application/json

URL PARAMETER:
attendanceId = 1

REQUEST PAYLOAD:
{
  "status": "LEAVE",
  "remarks": "Medical emergency"
}

RESPONSE (200 OK):
{
  "attendanceId": 1,
  "studentId": 1,
  "studentName": "John Doe",
  "date": "2026-04-01",
  "status": "LEAVE",
  "remarks": "Medical emergency",
  "createdAt": "2026-04-01T09:30:00",
  "updatedAt": "2026-04-01T10:00:00"
}
```

### 1.3 Get Attendance Record
```
GET /api/attendance/{attendanceId}

URL PARAMETER:
attendanceId = 1

RESPONSE (200 OK):
{
  "attendanceId": 1,
  "studentId": 1,
  "studentName": "John Doe",
  "rollNo": "101",
  "date": "2026-04-01",
  "status": "PRESENT",
  "className": "10",
  "section": "A",
  "remarks": "Present",
  "createdAt": "2026-04-01T09:30:00"
}
```

### 1.4 Delete Attendance
```
DELETE /api/attendance/{attendanceId}

URL PARAMETER:
attendanceId = 1

RESPONSE (200 OK):
"Attendance record deleted successfully"
```

---

## 2️⃣ BULK ATTENDANCE OPERATIONS

### 2.1 Bulk Mark Attendance
```
POST /api/attendance/bulk/mark
Content-Type: application/json

REQUEST PAYLOAD:
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
      "remarks": "Not feeling well"
    },
    {
      "studentId": 3,
      "status": "LEAVE",
      "remarks": "Medical appointment"
    },
    {
      "studentId": 4,
      "status": "PRESENT",
      "remarks": "Present"
    },
    {
      "studentId": 5,
      "status": "ABSENT",
      "remarks": "Personal work"
    }
  ]
}

RESPONSE (201 Created):
{
  "batchId": 101,
  "className": "10",
  "section": "A",
  "totalRecords": 5,
  "successfulRecords": 5,
  "failedRecords": 0,
  "uploadStatus": "COMPLETED",
  "uploadedAt": "2026-04-01T09:30:00"
}
```

---

## 3️⃣ STUDENT ATTENDANCE RETRIEVAL

### 3.1 Get Student Attendance Records
```
GET /api/attendance/student/{studentId}

URL PARAMETER:
studentId = 1

QUERY PARAMETERS:
?from=2026-03-01&to=2026-04-30&page=0&size=10&sortBy=date

RESPONSE (200 OK):
{
  "content": [
    {
      "attendanceId": 1,
      "studentId": 1,
      "studentName": "John Doe",
      "date": "2026-04-01",
      "status": "PRESENT",
      "className": "10",
      "section": "A",
      "remarks": "Present"
    },
    {
      "attendanceId": 2,
      "studentId": 1,
      "studentName": "John Doe",
      "date": "2026-03-31",
      "status": "ABSENT",
      "className": "10",
      "section": "A",
      "remarks": "Sick"
    }
  ],
  "totalElements": 25,
  "totalPages": 3,
  "currentPage": 0,
  "size": 10
}
```

### 3.2 Get Student Attendance Summary
```
GET /api/attendance/student/{studentId}/summary

URL PARAMETER:
studentId = 1

QUERY PARAMETERS:
?from=2026-04-01&to=2026-04-30

RESPONSE (200 OK):
{
  "studentId": 1,
  "studentName": "John Doe",
  "className": "10",
  "section": "A",
  "attendancePercentage": 92,
  "totalDaysExpected": 24,
  "totalDaysPresent": 22,
  "totalDaysAbsent": 1,
  "totalDaysLeave": 1,
  "periodStartDate": "2026-04-01",
  "periodEndDate": "2026-04-30",
  "status": "GOOD",
  "lastUpdated": "2026-04-01T15:30:00"
}
```

### 3.3 Get Attendance Percentage
```
GET /api/attendance/student/{studentId}/percentage

URL PARAMETER:
studentId = 1

QUERY PARAMETERS:
?from=2026-04-01&to=2026-04-30

RESPONSE (200 OK):
92
```

---

## 4️⃣ CLASS ATTENDANCE OPERATIONS

### 4.1 Get Class Attendance Records
```
GET /api/attendance/class/{className}/{section}

URL PARAMETERS:
className = 10
section = A

QUERY PARAMETERS:
?from=2026-03-01&to=2026-04-30&page=0&size=10

RESPONSE (200 OK):
{
  "content": [
    {
      "attendanceId": 1,
      "studentId": 1,
      "studentName": "John Doe",
      "date": "2026-04-01",
      "status": "PRESENT",
      "className": "10",
      "section": "A"
    },
    {
      "attendanceId": 2,
      "studentId": 2,
      "studentName": "Jane Smith",
      "date": "2026-04-01",
      "status": "ABSENT",
      "className": "10",
      "section": "A"
    }
  ],
  "totalElements": 45,
  "totalPages": 5,
  "currentPage": 0
}
```

### 4.2 Get Class Attendance Report
```
GET /api/attendance/report/class/{className}/{section}

URL PARAMETERS:
className = 10
section = A

QUERY PARAMETERS:
?date=2026-04-01

RESPONSE (200 OK):
{
  "className": "10",
  "section": "A",
  "reportDate": "2026-04-01",
  "totalStudents": 45,
  "presentCount": 42,
  "absentCount": 2,
  "leaveCount": 1,
  "attendancePercentage": 93.33,
  "classTeacherName": "Ms. Smith",
  "generatedAt": "2026-04-01"
}
```

### 4.3 Get School Attendance Report
```
GET /api/attendance/report/school/{schoolId}

URL PARAMETER:
schoolId = 1

QUERY PARAMETERS:
?date=2026-04-01

RESPONSE (200 OK):
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
  },
  {
    "className": "9",
    "section": "A",
    "reportDate": "2026-04-01",
    "totalStudents": 42,
    "presentCount": 40,
    "absentCount": 2,
    "leaveCount": 0,
    "attendancePercentage": 95.24
  }
]
```

---

## 5️⃣ ATTENDANCE SETTINGS

### 5.1 Get Settings
```
GET /api/attendance/settings/{schoolId}

URL PARAMETER:
schoolId = 1

RESPONSE (200 OK):
{
  "settingId": 1,
  "schoolId": 1,
  "workingDaysPerWeek": 5,
  "minAttendancePercentage": 75,
  "allowBulkMarking": true,
  "allowRetroactiveMarking": true,
  "maxRetroactiveDays": 7,
  "notifyLowAttendance": true,
  "lowAttendanceThreshold": 75,
  "createdAt": "2026-01-01T10:00:00",
  "updatedAt": "2026-04-01T10:00:00"
}
```

### 5.2 Update Settings
```
PUT /api/attendance/settings/{schoolId}
Content-Type: application/json

URL PARAMETER:
schoolId = 1

REQUEST PAYLOAD:
{
  "minAttendancePercentage": 80,
  "lowAttendanceThreshold": 80,
  "maxRetroactiveDays": 5,
  "allowBulkMarking": true,
  "allowRetroactiveMarking": true,
  "notifyLowAttendance": true,
  "workingDaysPerWeek": 5
}

RESPONSE (200 OK):
{
  "settingId": 1,
  "schoolId": 1,
  "workingDaysPerWeek": 5,
  "minAttendancePercentage": 80,
  "allowBulkMarking": true,
  "allowRetroactiveMarking": true,
  "maxRetroactiveDays": 5,
  "notifyLowAttendance": true,
  "lowAttendanceThreshold": 80,
  "updatedAt": "2026-04-01T11:00:00"
}
```

### 5.3 Initialize Settings
```
POST /api/attendance/settings/{schoolId}/initialize

URL PARAMETER:
schoolId = 2

RESPONSE (201 Created):
{
  "settingId": 2,
  "schoolId": 2,
  "workingDaysPerWeek": 5,
  "minAttendancePercentage": 75,
  "allowBulkMarking": true,
  "allowRetroactiveMarking": true,
  "maxRetroactiveDays": 7,
  "notifyLowAttendance": true,
  "lowAttendanceThreshold": 75,
  "createdAt": "2026-04-01T10:00:00"
}
```

---

## 6️⃣ ATTENDANCE EXCEPTIONS

### 6.1 Request Exception
```
POST /api/attendance/exception/request
Content-Type: application/json

REQUEST PAYLOAD:
{
  "studentId": 1,
  "exceptionDate": "2026-04-02",
  "exceptionType": "SICK_LEAVE",
  "reason": "Viral fever and cough",
  "attachmentUrl": "https://example.com/medical-certificate.pdf"
}

RESPONSE (201 Created):
{
  "exceptionId": 1,
  "studentId": 1,
  "studentName": "John Doe",
  "exceptionDate": "2026-04-02",
  "exceptionType": "SICK_LEAVE",
  "status": "PENDING",
  "reason": "Viral fever and cough",
  "attachmentUrl": "https://example.com/medical-certificate.pdf",
  "requestedById": 1,
  "requestedByName": "john_doe",
  "createdAt": "2026-04-01T10:00:00"
}
```

### 6.2 Approve Exception
```
PUT /api/attendance/exception/{exceptionId}/approve

URL PARAMETER:
exceptionId = 1

QUERY PARAMETERS:
?approverUserId=5&remarks=Medical%20certificate%20verified

RESPONSE (200 OK):
{
  "exceptionId": 1,
  "studentId": 1,
  "studentName": "John Doe",
  "exceptionDate": "2026-04-02",
  "exceptionType": "SICK_LEAVE",
  "status": "APPROVED",
  "reason": "Viral fever and cough",
  "approvedById": 5,
  "approvedByName": "admin_user",
  "approvedAt": "2026-04-01T11:00:00"
}
```

### 6.3 Reject Exception
```
PUT /api/attendance/exception/{exceptionId}/reject

URL PARAMETER:
exceptionId = 1

QUERY PARAMETERS:
?rejecterUserId=5&remarks=No%20supporting%20document

RESPONSE (200 OK):
{
  "exceptionId": 1,
  "studentId": 1,
  "studentName": "John Doe",
  "exceptionDate": "2026-04-02",
  "exceptionType": "SICK_LEAVE",
  "status": "REJECTED",
  "reason": "Viral fever and cough",
  "approvedById": 5,
  "approvedByName": "admin_user"
}
```

### 6.4 Get Pending Exceptions
```
GET /api/attendance/exception/pending

QUERY PARAMETERS:
?page=0&size=10

RESPONSE (200 OK):
{
  "content": [
    {
      "exceptionId": 1,
      "studentId": 1,
      "studentName": "John Doe",
      "exceptionDate": "2026-04-02",
      "exceptionType": "SICK_LEAVE",
      "status": "PENDING",
      "reason": "Fever",
      "createdAt": "2026-04-01T10:00:00"
    },
    {
      "exceptionId": 2,
      "studentId": 2,
      "studentName": "Jane Smith",
      "exceptionDate": "2026-04-02",
      "exceptionType": "MEDICAL",
      "status": "PENDING",
      "reason": "Doctor appointment",
      "createdAt": "2026-04-01T10:15:00"
    }
  ],
  "totalElements": 5,
  "totalPages": 1
}
```

### 6.5 Get Student Exceptions
```
GET /api/attendance/exception/student/{studentId}

URL PARAMETER:
studentId = 1

QUERY PARAMETERS:
?page=0&size=10

RESPONSE (200 OK):
{
  "content": [
    {
      "exceptionId": 1,
      "studentId": 1,
      "studentName": "John Doe",
      "exceptionDate": "2026-04-02",
      "exceptionType": "SICK_LEAVE",
      "status": "APPROVED",
      "reason": "Fever",
      "createdAt": "2026-04-01T10:00:00"
    },
    {
      "exceptionId": 3,
      "studentId": 1,
      "studentName": "John Doe",
      "exceptionDate": "2026-03-25",
      "exceptionType": "FIELD_TRIP",
      "status": "APPROVED",
      "reason": "Science museum visit",
      "createdAt": "2026-03-20T10:00:00"
    }
  ],
  "totalElements": 3,
  "totalPages": 1
}
```

### 6.6 Get Exception by ID
```
GET /api/attendance/exception/{exceptionId}

URL PARAMETER:
exceptionId = 1

RESPONSE (200 OK):
{
  "exceptionId": 1,
  "studentId": 1,
  "studentName": "John Doe",
  "exceptionDate": "2026-04-02",
  "exceptionType": "SICK_LEAVE",
  "status": "PENDING",
  "reason": "Viral fever and cough",
  "attachmentUrl": "https://example.com/medical-certificate.pdf",
  "requestedById": 1,
  "requestedByName": "john_doe",
  "createdAt": "2026-04-01T10:00:00"
}
```

---

## 📊 ATTENDANCE STATUS VALUES

| Value | Description |
|-------|-------------|
| PRESENT | Student attended the class |
| ABSENT | Student was absent without authorization |
| LEAVE | Student was absent with authorization |

---

## 📝 EXCEPTION TYPES

| Type | Description |
|------|-------------|
| SICK_LEAVE | Student is sick |
| MEDICAL | Medical appointment or treatment |
| FIELD_TRIP | School-organized field trip |
| FAMILY_EMERGENCY | Family emergency |
| COURT_CASE | Court/legal case |
| OTHER | Other authorized reason |

---

## 🔍 QUERY PARAMETERS

### Common Parameters
- `page`: Page number (default: 0)
- `size`: Records per page (default: 10)
- `sortBy`: Sort field (default: id)
- `from`: Start date (format: YYYY-MM-DD)
- `to`: End date (format: YYYY-MM-DD)
- `date`: Specific date (format: YYYY-MM-DD)

---

## 📌 POSTMAN SETUP INSTRUCTIONS

1. **Create New Request**
   - Method: SELECT from dropdown
   - URL: Copy from endpoint definition
   - Headers: Set `Content-Type: application/json`
   - Body: Copy JSON payload
   - Send

2. **Test Collection**
   - Import `Attendance_API.postman_collection.json`
   - All 30 requests are pre-configured
   - Just update IDs and dates as needed

3. **Variables (Optional)**
```
BASE_URL: http://localhost:9091/api/attendance
STUDENT_ID: 1
CLASS_NAME: 10
SECTION: A
SCHOOL_ID: 1
```

---

## ✅ ERROR RESPONSES

### 400 Bad Request
```json
{
  "error": "Validation failed",
  "message": "Attendance date cannot be in the future"
}
```

### 404 Not Found
```json
{
  "error": "Resource not found",
  "message": "Student not found with ID: 999"
}
```

### 409 Conflict
```json
{
  "error": "Conflict",
  "message": "Exception already exists for this student on this date"
}
```

### 500 Internal Server Error
```json
{
  "error": "Internal server error",
  "message": "An unexpected error occurred"
}
```

---

## 🧪 TESTING CHECKLIST

- [ ] Test individual attendance marking
- [ ] Test bulk attendance for class
- [ ] Test getting attendance summary
- [ ] Test generating class report
- [ ] Test generating school report
- [ ] Test requesting exception
- [ ] Test approving exception
- [ ] Test getting pending exceptions
- [ ] Test updating attendance
- [ ] Test deleting attendance

---

**Total Endpoints**: 30  
**All Methods Documented**: ✅  
**Ready for Testing**: ✅

