# Attendance Management System - Complete API Documentation

## Overview
The Attendance Management System is a comprehensive module for tracking and managing student attendance, calculating attendance percentages, handling exceptions, and generating detailed reports. It integrates seamlessly with the Career Builder backend architecture.

## Database Schema

### Core Tables

#### 1. `attendance_records`
Main table for storing individual attendance records.

```sql
CREATE TABLE attendance_records (
    attendance_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    student_id BIGINT NOT NULL,
    att_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL,        -- PRESENT, ABSENT, LEAVE
    class_name VARCHAR(50),
    section VARCHAR(10),
    subject_id BIGINT,
    marked_by_id BIGINT,
    remarks VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    UNIQUE KEY uk_attendance_student_date (student_id, att_date),
    FOREIGN KEY (student_id) REFERENCES students(id),
    FOREIGN KEY (subject_id) REFERENCES subjects(id),
    FOREIGN KEY (marked_by_id) REFERENCES app_users(id),
    INDEX idx_att_student_date (student_id, att_date),
    INDEX idx_attendance_class_date (class_name, section, att_date),
    INDEX idx_attendance_status_date (status, att_date)
);
```

#### 2. `attendance_settings`
School-specific configuration for attendance rules.

```sql
CREATE TABLE attendance_settings (
    setting_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    school_id BIGINT NOT NULL UNIQUE,
    working_days_per_week INT DEFAULT 5,
    min_attendance_percentage INT DEFAULT 75,
    allow_bulk_marking BOOLEAN DEFAULT TRUE,
    allow_retroactive_marking BOOLEAN DEFAULT TRUE,
    max_retroactive_days INT DEFAULT 7,
    notify_low_attendance BOOLEAN DEFAULT TRUE,
    low_attendance_threshold INT DEFAULT 75,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    FOREIGN KEY (school_id) REFERENCES schools(school_id)
);
```

#### 3. `attendance_batch_uploads`
Tracks bulk attendance upload operations.

```sql
CREATE TABLE attendance_batch_uploads (
    batch_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    school_id BIGINT NOT NULL,
    class_name VARCHAR(50) NOT NULL,
    section VARCHAR(10) NOT NULL,
    upload_date DATE NOT NULL,
    uploaded_by_id BIGINT NOT NULL,
    total_records INT DEFAULT 0,
    successful_records INT DEFAULT 0,
    failed_records INT DEFAULT 0,
    upload_status VARCHAR(50) DEFAULT 'PENDING',
    remarks VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_batch_upload (school_id, class_name, section, upload_date),
    FOREIGN KEY (school_id) REFERENCES schools(school_id),
    FOREIGN KEY (uploaded_by_id) REFERENCES app_users(id)
);
```

#### 4. `attendance_exceptions`
Handles special cases like sick leave, medical absence, field trips.

```sql
CREATE TABLE attendance_exceptions (
    exception_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    student_id BIGINT NOT NULL,
    exception_date DATE NOT NULL,
    exception_type VARCHAR(50) NOT NULL,  -- SICK_LEAVE, MEDICAL, FIELD_TRIP, etc.
    status VARCHAR(50) DEFAULT 'PENDING',  -- PENDING, APPROVED, REJECTED
    requested_by_id BIGINT NOT NULL,
    approved_by_id BIGINT,
    reason VARCHAR(500) NOT NULL,
    attachment_url VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    approved_at TIMESTAMP,
    UNIQUE KEY uk_exception_date (student_id, exception_date),
    FOREIGN KEY (student_id) REFERENCES students(id),
    FOREIGN KEY (requested_by_id) REFERENCES app_users(id),
    FOREIGN KEY (approved_by_id) REFERENCES app_users(id)
);
```

## API Endpoints

### 1. Individual Attendance Operations

#### Mark Attendance
**POST** `/api/attendance/mark`

Mark attendance for a single student.

```json
Request Body:
{
    "studentId": 1,
    "date": "2026-04-01",
    "status": "PRESENT",
    "className": "10",
    "section": "A",
    "subjectId": 5,
    "remarks": "Regular class"
}

Response (201 Created):
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
    "remarks": "Regular class",
    "createdAt": "2026-04-01T09:30:00",
    "updatedAt": "2026-04-01T09:30:00"
}
```

#### Update Attendance
**PUT** `/api/attendance/{attendanceId}`

Update an existing attendance record.

```json
Request Body:
{
    "status": "LEAVE",
    "remarks": "Medical leave"
}

Response (200 OK):
{
    "attendanceId": 1,
    "studentId": 1,
    "studentName": "John Doe",
    "date": "2026-04-01",
    "status": "LEAVE",
    "remarks": "Medical leave",
    ...
}
```

#### Get Attendance Record
**GET** `/api/attendance/{attendanceId}`

Retrieve a specific attendance record.

```
Response (200 OK):
{
    "attendanceId": 1,
    "studentId": 1,
    "studentName": "John Doe",
    "rollNo": "101",
    "date": "2026-04-01",
    "status": "PRESENT",
    ...
}
```

#### Delete Attendance
**DELETE** `/api/attendance/{attendanceId}`

```
Response (200 OK):
"Attendance record deleted successfully"
```

### 2. Bulk Attendance Operations

#### Bulk Mark Attendance
**POST** `/api/attendance/bulk/mark`

Mark attendance for an entire class at once.

```json
Request Body:
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
            "remarks": "Sick leave"
        },
        {
            "studentId": 3,
            "status": "LEAVE",
            "remarks": "Court case"
        }
    ]
}

Response (201 Created):
{
    "batchId": 101,
    "className": "10",
    "section": "A",
    "totalRecords": 3,
    "successfulRecords": 3,
    "failedRecords": 0,
    "uploadStatus": "COMPLETED",
    "uploadedAt": "2026-04-01T09:30:00"
}
```

### 3. Student Attendance Retrieval

#### Get Student Attendance Records
**GET** `/api/attendance/student/{studentId}`

Retrieve paginated attendance records for a student.

```
Query Parameters:
- from: 2026-03-01 (optional, defaults to 1 month ago)
- to: 2026-04-01 (optional, defaults to today)
- page: 0 (optional)
- size: 10 (optional)
- sortBy: date (optional)

Response (200 OK):
{
    "content": [
        {
            "attendanceId": 1,
            "studentId": 1,
            "studentName": "John Doe",
            "date": "2026-04-01",
            "status": "PRESENT",
            ...
        }
    ],
    "totalElements": 21,
    "totalPages": 3,
    "currentPage": 0,
    "size": 10
}
```

#### Get Student Attendance Summary
**GET** `/api/attendance/student/{studentId}/summary`

Get attendance statistics for a student within a date range.

```
Query Parameters:
- from: 2026-04-01 (optional, defaults to 1st of current month)
- to: 2026-04-30 (optional, defaults to today)

Response (200 OK):
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

#### Get Attendance Percentage
**GET** `/api/attendance/student/{studentId}/percentage`

Get only the attendance percentage for quick reference.

```
Query Parameters:
- from: 2026-04-01 (optional)
- to: 2026-04-30 (optional)

Response (200 OK):
92
```

### 4. Class Attendance Operations

#### Get Class Attendance
**GET** `/api/attendance/class/{className}/{section}`

Retrieve all attendance records for a specific class.

```
Query Parameters:
- from: 2026-03-01 (optional)
- to: 2026-04-01 (optional)
- page: 0 (optional)
- size: 10 (optional)

Response (200 OK):
{
    "content": [
        {
            "attendanceId": 1,
            "studentId": 1,
            "studentName": "John Doe",
            "date": "2026-04-01",
            "status": "PRESENT",
            ...
        }
    ],
    "totalElements": 45,
    "totalPages": 5,
    "currentPage": 0,
    "size": 10
}
```

#### Get Class Attendance Report
**GET** `/api/attendance/report/class/{className}/{section}`

Get daily attendance summary for a class.

```
Query Parameters:
- date: 2026-04-01 (optional, defaults to today)

Response (200 OK):
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

#### Get School Attendance Report
**GET** `/api/attendance/report/school/{schoolId}`

Get attendance report for all classes in a school.

```
Query Parameters:
- date: 2026-04-01 (optional, defaults to today)

Response (200 OK):
[
    {
        "className": "10",
        "section": "A",
        "reportDate": "2026-04-01",
        "totalStudents": 45,
        "presentCount": 42,
        "absentCount": 2,
        "leaveCount": 1,
        "attendancePercentage": 93.33,
        ...
    },
    {
        "className": "10",
        "section": "B",
        "reportDate": "2026-04-01",
        "totalStudents": 48,
        "presentCount": 45,
        "absentCount": 2,
        "leaveCount": 1,
        "attendancePercentage": 93.75,
        ...
    }
]
```

### 5. Attendance Settings

#### Get Settings
**GET** `/api/attendance/settings/{schoolId}`

Retrieve attendance settings for a school.

```
Response (200 OK):
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

#### Update Settings
**PUT** `/api/attendance/settings/{schoolId}`

Update attendance settings for a school.

```json
Request Body:
{
    "minAttendancePercentage": 80,
    "lowAttendanceThreshold": 80,
    "maxRetroactiveDays": 5
}

Response (200 OK):
{
    "settingId": 1,
    "schoolId": 1,
    "minAttendancePercentage": 80,
    "lowAttendanceThreshold": 80,
    "maxRetroactiveDays": 5,
    ...
}
```

#### Initialize Settings
**POST** `/api/attendance/settings/{schoolId}/initialize`

Initialize default attendance settings for a new school.

```
Response (201 Created):
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

### 6. Attendance Exceptions

#### Request Exception
**POST** `/api/attendance/exception/request`

Request an exception for an absence (sick leave, medical, field trip, etc.).

```json
Request Body:
{
    "studentId": 1,
    "exceptionDate": "2026-04-02",
    "exceptionType": "SICK_LEAVE",
    "reason": "Fever and cough",
    "attachmentUrl": "https://example.com/medical-cert.pdf"
}

Response (201 Created):
{
    "exceptionId": 1,
    "studentId": 1,
    "studentName": "John Doe",
    "exceptionDate": "2026-04-02",
    "exceptionType": "SICK_LEAVE",
    "status": "PENDING",
    "reason": "Fever and cough",
    "attachmentUrl": "https://example.com/medical-cert.pdf",
    "requestedById": 1,
    "requestedByName": "john_doe",
    "createdAt": "2026-04-01T10:00:00"
}
```

#### Approve Exception
**PUT** `/api/attendance/exception/{exceptionId}/approve`

Approve a pending exception.

```
Query Parameters:
- approverUserId: 5 (required)
- remarks: "Medical certificate verified" (optional)

Response (200 OK):
{
    "exceptionId": 1,
    "studentId": 1,
    "studentName": "John Doe",
    "exceptionDate": "2026-04-02",
    "exceptionType": "SICK_LEAVE",
    "status": "APPROVED",
    "approvedById": 5,
    "approvedByName": "admin_user",
    "approvedAt": "2026-04-01T11:00:00",
    ...
}
```

#### Reject Exception
**PUT** `/api/attendance/exception/{exceptionId}/reject`

Reject a pending exception.

```
Query Parameters:
- rejecterUserId: 5 (required)
- remarks: "No supporting document" (optional)

Response (200 OK):
{
    "exceptionId": 1,
    "studentId": 1,
    "status": "REJECTED",
    "approvedById": 5,
    "approvedByName": "admin_user",
    ...
}
```

#### Get Pending Exceptions
**GET** `/api/attendance/exception/pending`

Retrieve all pending exceptions for review.

```
Query Parameters:
- page: 0 (optional)
- size: 10 (optional)

Response (200 OK):
{
    "content": [
        {
            "exceptionId": 1,
            "studentId": 1,
            "studentName": "John Doe",
            "exceptionDate": "2026-04-02",
            "exceptionType": "SICK_LEAVE",
            "status": "PENDING",
            ...
        }
    ],
    "totalElements": 5,
    "totalPages": 1
}
```

#### Get Student Exceptions
**GET** `/api/attendance/exception/student/{studentId}`

Retrieve all exceptions for a specific student.

```
Query Parameters:
- page: 0 (optional)
- size: 10 (optional)

Response (200 OK):
{
    "content": [
        {
            "exceptionId": 1,
            "studentId": 1,
            "studentName": "John Doe",
            "exceptionDate": "2026-04-02",
            "exceptionType": "SICK_LEAVE",
            "status": "APPROVED",
            ...
        }
    ],
    "totalElements": 3,
    "totalPages": 1
}
```

#### Get Exception by ID
**GET** `/api/attendance/exception/{exceptionId}`

Retrieve details of a specific exception.

```
Response (200 OK):
{
    "exceptionId": 1,
    "studentId": 1,
    "studentName": "John Doe",
    "exceptionDate": "2026-04-02",
    "exceptionType": "SICK_LEAVE",
    "status": "PENDING",
    ...
}
```

## Error Handling

All API endpoints follow standard HTTP status codes and include meaningful error messages:

```json
400 Bad Request:
{
    "error": "Validation failed",
    "message": "Attendance date cannot be in the future"
}

404 Not Found:
{
    "error": "Resource not found",
    "message": "Student not found with ID: 999"
}

409 Conflict:
{
    "error": "Conflict",
    "message": "Exception already exists for this student on this date"
}

500 Internal Server Error:
{
    "error": "Internal server error",
    "message": "An unexpected error occurred"
}
```

## Attendance Status Enums

- **PRESENT**: Student was present for the day
- **ABSENT**: Student was absent without authorization
- **LEAVE**: Student was absent with authorization

## Exception Types

- **SICK_LEAVE**: Student is sick
- **MEDICAL**: Medical appointment or treatment
- **FIELD_TRIP**: Authorized absence for school activity
- **FAMILY_EMERGENCY**: Family emergency
- **COURT_CASE**: Legal/court case related absence
- **OTHER**: Other authorized absences

## Usage Examples

### Example 1: Mark Attendance for a Class

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

### Example 2: Get Student Attendance Summary

```bash
curl -X GET "http://localhost:9091/api/attendance/student/1/summary?from=2026-04-01&to=2026-04-30"
```

### Example 3: Request a Sick Leave Exception

```bash
curl -X POST http://localhost:9091/api/attendance/exception/request \
  -H "Content-Type: application/json" \
  -d '{
    "studentId": 1,
    "exceptionDate": "2026-04-02",
    "exceptionType": "SICK_LEAVE",
    "reason": "Viral fever"
  }'
```

## Running the System

1. **Apply Database Migrations**: Execute `attendance_migration.sql` on your database
2. **Start the Backend**: Run the Spring Boot application
3. **Verify**: Test the endpoints using Postman or curl

## Features

✅ Individual attendance marking and tracking
✅ Bulk attendance operations for entire classes
✅ Attendance statistics and percentage calculations
✅ Class-wise attendance reports
✅ School-wide attendance reports
✅ Exception handling (sick leave, medical, field trips)
✅ Configurable attendance settings per school
✅ Attendance tracking with audit logs
✅ Support for subject-specific attendance
✅ Comprehensive error handling
✅ Pagination support for large datasets

## Future Enhancements

- SMS/Email notifications for low attendance
- Parent portal for attendance tracking
- Attendance trends analysis
- Automated absent marking based on timetable
- Integration with biometric systems
- Attendance forecasting
- Mobile app support


