# Attendance Management System - Quick Start Guide

## 📋 Overview

The Attendance Management System provides a complete solution for tracking student attendance, managing exceptions, and generating reports. It's designed to integrate seamlessly with the Career Builder backend.

## 🚀 Quick Setup (5 minutes)

### Step 1: Database Migration

Run the migration script to create all necessary tables:

```bash
# Execute in your MySQL database
mysql -u root -p your_database < attendance_migration.sql
```

### Step 2: Start the Backend

The application will automatically initialize with the new controllers and services.

### Step 3: Test with Postman

Import the provided Postman collection:
- **File**: `Attendance_API.postman_collection.json`
- **Method**: In Postman → Import → Select the JSON file

## 📱 Common Operations

### 1️⃣ Mark Attendance for One Student

```bash
curl -X POST http://localhost:9091/api/attendance/mark \
  -H "Content-Type: application/json" \
  -d '{
    "studentId": 1,
    "date": "2026-04-01",
    "status": "PRESENT",
    "className": "10",
    "section": "A",
    "remarks": "Regular class"
  }'
```

**Response:**
```json
{
  "attendanceId": 1,
  "studentId": 1,
  "studentName": "John Doe",
  "date": "2026-04-01",
  "status": "PRESENT",
  "className": "10",
  "section": "A"
}
```

### 2️⃣ Mark Attendance for Entire Class (Bulk)

```bash
curl -X POST http://localhost:9091/api/attendance/bulk/mark \
  -H "Content-Type: application/json" \
  -d '{
    "schoolId": 1,
    "date": "2026-04-01",
    "className": "10",
    "section": "A",
    "records": [
      {"studentId": 1, "status": "PRESENT"},
      {"studentId": 2, "status": "ABSENT"},
      {"studentId": 3, "status": "LEAVE"}
    ]
  }'
```

**Response:**
```json
{
  "batchId": 101,
  "className": "10",
  "section": "A",
  "totalRecords": 3,
  "successfulRecords": 3,
  "failedRecords": 0,
  "uploadStatus": "COMPLETED"
}
```

### 3️⃣ Get Student Attendance Summary

```bash
curl -X GET "http://localhost:9091/api/attendance/student/1/summary?from=2026-04-01&to=2026-04-30"
```

**Response:**
```json
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
  "status": "GOOD"
}
```

### 4️⃣ Get Class Attendance Report

```bash
curl -X GET "http://localhost:9091/api/attendance/report/class/10/A?date=2026-04-01"
```

**Response:**
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

### 5️⃣ Request Exception (Sick Leave)

```bash
curl -X POST http://localhost:9091/api/attendance/exception/request \
  -H "Content-Type: application/json" \
  -d '{
    "studentId": 1,
    "exceptionDate": "2026-04-02",
    "exceptionType": "SICK_LEAVE",
    "reason": "Viral fever",
    "attachmentUrl": "https://example.com/medical-cert.pdf"
  }'
```

**Response:**
```json
{
  "exceptionId": 1,
  "studentId": 1,
  "studentName": "John Doe",
  "exceptionDate": "2026-04-02",
  "exceptionType": "SICK_LEAVE",
  "status": "PENDING",
  "reason": "Viral fever"
}
```

### 6️⃣ Approve Exception

```bash
curl -X PUT "http://localhost:9091/api/attendance/exception/1/approve?approverUserId=5"
```

**Response:**
```json
{
  "exceptionId": 1,
  "status": "APPROVED",
  "approvedById": 5,
  "approvedByName": "admin_user",
  "approvedAt": "2026-04-01T11:00:00"
}
```

## 📊 Key Features

| Feature | Endpoint | Method |
|---------|----------|--------|
| Mark Attendance | `/api/attendance/mark` | POST |
| Bulk Mark | `/api/attendance/bulk/mark` | POST |
| Student Summary | `/api/attendance/student/{id}/summary` | GET |
| Class Report | `/api/attendance/report/class/{class}/{section}` | GET |
| School Report | `/api/attendance/report/school/{schoolId}` | GET |
| Request Exception | `/api/attendance/exception/request` | POST |
| Approve Exception | `/api/attendance/exception/{id}/approve` | PUT |
| Pending Exceptions | `/api/attendance/exception/pending` | GET |

## 🎯 Attendance Status

| Status | Description |
|--------|-------------|
| PRESENT | Student attended the class |
| ABSENT | Student was absent without authorization |
| LEAVE | Student was absent with authorization |

## 📝 Exception Types

- **SICK_LEAVE**: Student is sick
- **MEDICAL**: Medical appointment
- **FIELD_TRIP**: School activity
- **FAMILY_EMERGENCY**: Family emergency
- **COURT_CASE**: Legal matter
- **OTHER**: Other authorized absences

## ⚙️ Configuration

### Get Current Settings

```bash
curl -X GET http://localhost:9091/api/attendance/settings/1
```

### Update Settings

```bash
curl -X PUT http://localhost:9091/api/attendance/settings/1 \
  -H "Content-Type: application/json" \
  -d '{
    "minAttendancePercentage": 80,
    "lowAttendanceThreshold": 80,
    "allowBulkMarking": true,
    "allowRetroactiveMarking": true,
    "maxRetroactiveDays": 7
  }'
```

## 🗄️ Database Tables

1. **attendance_records** - Individual attendance records
2. **attendance_settings** - School-wise configuration
3. **attendance_batch_uploads** - Bulk upload tracking
4. **attendance_exceptions** - Exception management
5. **attendance_notifications** - Notification tracking
6. **attendance_audit_log** - Audit trail

## 🔍 Troubleshooting

### Issue: "Student not found"
- **Solution**: Ensure the student ID exists in the students table

### Issue: "Attendance already exists"
- **Solution**: Update the existing record instead of creating a new one

### Issue: "School settings not found"
- **Solution**: Initialize settings with `/api/attendance/settings/{schoolId}/initialize`

### Issue: Bulk upload partial failure
- **Response**: Returns both successful and failed counts

## 📈 API Response Patterns

### Successful Response (201 Created)
```json
{
  "attendanceId": 1,
  "studentId": 1,
  "status": "PRESENT",
  ...
}
```

### Error Response (400 Bad Request)
```json
{
  "error": "Validation failed",
  "message": "Attendance date cannot be in the future"
}
```

### Paginated Response (200 OK)
```json
{
  "content": [...],
  "totalElements": 100,
  "totalPages": 10,
  "currentPage": 0,
  "size": 10
}
```

## 🔐 Best Practices

1. **Use Bulk Operations**: For marking entire class, use bulk API instead of individual calls
2. **Set Reasonable Thresholds**: Configure attendance percentage based on school policy
3. **Handle Exceptions Promptly**: Review and approve/reject exceptions regularly
4. **Backup Regularly**: Keep database backups for compliance
5. **Monitor Reports**: Regularly check class and school attendance reports
6. **Use Audit Logs**: Review audit logs for compliance and troubleshooting

## 🎓 Example Workflow

### Daily Attendance Marking Workflow

```
1. Teacher opens class attendance page at 9:00 AM
   ↓
2. Marks attendance for all students (bulk operation)
   POST /api/attendance/bulk/mark
   ↓
3. System processes and returns success rate
   ↓
4. Teacher can view class report
   GET /api/attendance/report/class/10/A?date=today
   ↓
5. Any exceptions (sick leave) are marked separately
   POST /api/attendance/exception/request
   ↓
6. Principal reviews exceptions and approves/rejects
   PUT /api/attendance/exception/{id}/approve
```

### End-of-Month Report Workflow

```
1. Generate school attendance report
   GET /api/attendance/report/school/1?date=last_day_of_month
   ↓
2. View each class performance
   GET /api/attendance/report/class/{class}/{section}
   ↓
3. Identify students below threshold
   GET /api/attendance/student/{id}/summary
   ↓
4. Generate alerts for parents
   (Integration with notification system)
```

## 📞 Support

For issues or feature requests, please refer to:
- **Documentation**: `ATTENDANCE_API_DOCUMENTATION.md`
- **Database Schema**: `attendance_migration.sql`
- **Postman Collection**: `Attendance_API.postman_collection.json`

## 🎉 You're Ready!

The Attendance Management System is now ready to use. Start marking attendance and tracking student progress!

---

**Last Updated**: April 1, 2026  
**Version**: 1.0  
**Status**: Production Ready ✅

