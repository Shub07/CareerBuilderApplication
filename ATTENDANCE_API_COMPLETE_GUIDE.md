# Career Builder Backend - Complete API Response Guide

## Overview
This guide provides information on all API endpoints and their expected request/response formats.

---

## Authentication
All endpoints accept JSON requests and return JSON responses. The API runs on **http://localhost:9091**

### Error Response Format
```json
{
  "success": false,
  "message": "Error description",
  "error": "Detailed error information"
}
```

### Success Response Format
```json
{
  "success": true,
  "message": "Success description",
  "data": { /* response data */ }
}
```

---

## Attendance API Endpoints

### 1. Mark Attendance
**Endpoint:** `POST /api/attendance/mark`  
**Description:** Mark attendance for a single student

**Request Body:**
```json
{
  "studentId": 1,
  "date": "2026-04-01",
  "status": "PRESENT",
  "className": "10A",
  "remarks": "Present and attentive"
}
```

**Success Response (201):**
```json
{
  "success": true,
  "message": "Attendance marked successfully",
  "data": {
    "attendanceId": 1,
    "studentId": 1,
    "studentName": "John Doe",
    "rollNo": "001",
    "date": "2026-04-01",
    "status": "PRESENT",
    "className": "10A",
    "remarks": "Present and attentive",
    "createdAt": "2026-04-01T22:19:37",
    "updatedAt": "2026-04-01T22:19:37"
  }
}
```

**Status Values:** PRESENT, ABSENT, LEAVE

---

### 2. Update Attendance
**Endpoint:** `PUT /api/attendance/{attendanceId}`  
**Description:** Update an existing attendance record

**Request Body:**
```json
{
  "status": "LEAVE",
  "remarks": "Leave due to medical reasons"
}
```

**Success Response (200):**
```json
{
  "success": true,
  "message": "Attendance updated successfully",
  "data": { /* updated record */ }
}
```

---

### 3. Get Attendance Record by ID
**Endpoint:** `GET /api/attendance/{attendanceId}`  
**Description:** Retrieve a specific attendance record

**Success Response (200):**
```json
{
  "success": true,
  "message": "Attendance record retrieved successfully",
  "data": { /* attendance record */ }
}
```

---

### 4. Delete Attendance
**Endpoint:** `DELETE /api/attendance/{attendanceId}`  
**Description:** Delete an attendance record

**Success Response (200):**
```json
{
  "success": true,
  "message": "Attendance record deleted successfully"
}
```

---

### 5. Bulk Mark Attendance
**Endpoint:** `POST /api/attendance/bulk-mark`  
**Description:** Mark attendance for multiple students at once

**Request Body:**
```json
{
  "schoolId": 1,
  "date": "2026-04-01",
  "className": "10A",
  "section": "A",
  "subjectId": null,
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
      "remarks": "Approved leave"
    }
  ]
}
```

**Success Response (201):**
```json
{
  "success": true,
  "message": "Bulk attendance marked successfully",
  "data": {
    "batchId": 1,
    "className": "10A",
    "section": "A",
    "totalRecords": 3,
    "successfulRecords": 3,
    "failedRecords": 0,
    "uploadStatus": "SUCCESS",
    "remarks": "All records processed successfully",
    "uploadedAt": "2026-04-01T22:19:37"
  }
}
```

---

### 6. Get Student Attendance
**Endpoint:** `GET /api/attendance/student/{studentId}`  
**Query Parameters:**
- `fromDate` (optional): YYYY-MM-DD format
- `toDate` (optional): YYYY-MM-DD format
- `page` (optional, default: 0)
- `size` (optional, default: 10)

**Example:** `GET /api/attendance/student/1?fromDate=2026-03-01&toDate=2026-04-01&page=0&size=10`

**Success Response (200):**
```json
{
  "success": true,
  "message": "Student attendance retrieved successfully",
  "data": [
    { /* attendance record 1 */ },
    { /* attendance record 2 */ }
  ],
  "page_info": {
    "current_page": 0,
    "page_size": 10,
    "total_elements": 25,
    "total_pages": 3
  }
}
```

---

### 7. Get Student Attendance Summary
**Endpoint:** `GET /api/attendance/student/{studentId}/summary`  
**Query Parameters:**
- `fromDate` (optional): YYYY-MM-DD format
- `toDate` (optional): YYYY-MM-DD format

**Example:** `GET /api/attendance/student/1/summary?fromDate=2026-04-01&toDate=2026-04-30`

**Success Response (200):**
```json
{
  "success": true,
  "message": "Attendance summary retrieved successfully",
  "data": {
    "studentId": 1,
    "studentName": "John Doe",
    "className": "10A",
    "section": "A",
    "attendancePercentage": 95,
    "totalDaysExpected": 20,
    "totalDaysPresent": 19,
    "totalDaysAbsent": 1,
    "totalDaysLeave": 0,
    "periodStartDate": "2026-04-01",
    "periodEndDate": "2026-04-30",
    "status": "EXCELLENT",
    "lastUpdated": "2026-04-01T22:19:37"
  }
}
```

---

### 8. Get Class Attendance
**Endpoint:** `GET /api/attendance/class`  
**Query Parameters:**
- `className` (required): e.g., "10A"
- `section` (required): e.g., "A"
- `fromDate` (optional): YYYY-MM-DD format
- `toDate` (optional): YYYY-MM-DD format
- `page` (optional, default: 0)
- `size` (optional, default: 10)

**Example:** `GET /api/attendance/class?className=10A&section=A&fromDate=2026-04-01&toDate=2026-04-30`

**Success Response (200):**
```json
{
  "success": true,
  "message": "Class attendance retrieved successfully",
  "data": [
    { /* student 1 attendance */ },
    { /* student 2 attendance */ }
  ],
  "page_info": {
    "current_page": 0,
    "page_size": 10,
    "total_elements": 45,
    "total_pages": 5
  }
}
```

---

### 9. Get Class Attendance Report
**Endpoint:** `GET /api/attendance/class/report`  
**Query Parameters:**
- `className` (required): e.g., "10A"
- `section` (required): e.g., "A"
- `date` (required): YYYY-MM-DD format

**Example:** `GET /api/attendance/class/report?className=10A&section=A&date=2026-04-01`

**Success Response (200):**
```json
{
  "success": true,
  "message": "Class attendance report retrieved successfully",
  "data": {
    "className": "10A",
    "section": "A",
    "reportDate": "2026-04-01",
    "totalStudents": 45,
    "presentCount": 42,
    "absentCount": 2,
    "leaveCount": 1,
    "attendancePercentage": 93.33,
    "generatedAt": "2026-04-01"
  }
}
```

---

### 10. Get School Attendance Report
**Endpoint:** `GET /api/attendance/school/report`  
**Query Parameters:**
- `schoolId` (required)
- `date` (required): YYYY-MM-DD format

**Example:** `GET /api/attendance/school/report?schoolId=1&date=2026-04-01`

**Success Response (200):**
```json
{
  "success": true,
  "message": "School attendance report retrieved successfully",
  "data": [
    {
      "className": "10A",
      "section": "A",
      "reportDate": "2026-04-01",
      "totalStudents": 45,
      "presentCount": 42,
      "absentCount": 2,
      "leaveCount": 1,
      "attendancePercentage": 93.33,
      "generatedAt": "2026-04-01"
    },
    {
      "className": "10B",
      "section": "B",
      "reportDate": "2026-04-01",
      "totalStudents": 48,
      "presentCount": 45,
      "absentCount": 2,
      "leaveCount": 1,
      "attendancePercentage": 93.75,
      "generatedAt": "2026-04-01"
    }
  ],
  "total_classes": 2
}
```

---

### 11. Get Attendance Percentage
**Endpoint:** `GET /api/attendance/student/{studentId}/percentage`  
**Query Parameters:**
- `fromDate` (optional): YYYY-MM-DD format
- `toDate` (optional): YYYY-MM-DD format

**Example:** `GET /api/attendance/student/1/percentage?fromDate=2026-04-01&toDate=2026-04-30`

**Success Response (200):**
```json
{
  "success": true,
  "message": "Attendance percentage retrieved successfully",
  "data": {
    "student_id": 1,
    "attendance_percentage": 92,
    "from_date": "2026-04-01",
    "to_date": "2026-04-30"
  }
}
```

---

### 12. Get Attendance Statistics
**Endpoint:** `GET /api/attendance/student/{studentId}/statistics`  
**Query Parameters:**
- `fromDate` (optional): YYYY-MM-DD format
- `toDate` (optional): YYYY-MM-DD format

**Example:** `GET /api/attendance/student/1/statistics?fromDate=2026-04-01&toDate=2026-04-30`

**Success Response (200):**
```json
{
  "success": true,
  "message": "Attendance statistics retrieved successfully",
  "data": {
    "student_id": 1,
    "total_present": 18,
    "total_absent": 1,
    "total_leave": 1,
    "attendance_percentage": 90,
    "from_date": "2026-04-01",
    "to_date": "2026-04-30"
  }
}
```

---

## Common Error Scenarios

### 400 Bad Request
```json
{
  "success": false,
  "message": "Validation failed",
  "errors": {
    "studentId": "Student ID is required",
    "date": "Date is required",
    "status": "Invalid status value"
  }
}
```

### 404 Not Found
```json
{
  "success": false,
  "message": "Failed to fetch attendance record",
  "error": "Attendance record not found with ID: 999"
}
```

### 409 Conflict (Duplicate)
```json
{
  "success": false,
  "message": "Duplicate or constraint violation",
  "detail": "An attendance record already exists for this student on this date"
}
```

---

## Testing Tips

1. **Always include proper dates** in YYYY-MM-DD format
2. **Use valid status values**: PRESENT, ABSENT, or LEAVE
3. **Check student ID exists** before marking attendance
4. **Ensure class name and section** are valid
5. **Use pagination** for large datasets
6. **Check query parameters** are properly URL encoded

---

## Example Postman Request

### Mark Attendance
```
POST http://localhost:9091/api/attendance/mark
Content-Type: application/json

{
  "studentId": 1,
  "date": "2026-04-01",
  "status": "PRESENT",
  "className": "10A",
  "remarks": "Present in class"
}
```

**Expected Response:**
- Status Code: 201 Created
- Body: { "success": true, "message": "Attendance marked successfully", ... }

---

## Database Schema Notes

### attendance_records table
- `attendance_id` (PK): Auto-generated ID
- `student_id` (FK): References students.id
- `att_date`: Date of attendance
- `status`: PRESENT/ABSENT/LEAVE (Enum)
- `class_name`: Class name
- `section`: Section of class
- `remarks`: Additional notes
- `created_at`: Timestamp of creation
- `updated_at`: Timestamp of last update

---

Generated: April 1, 2026

