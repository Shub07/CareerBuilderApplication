# Teacher Exam Marks Management API - Complete Guide

## Overview
This document provides complete details for the **Teacher Exam Marks Management** backend service, enabling teachers to manage exam marks including:
- Listing exams for marks entry
- Entering and saving marks (draft mode)
- Submitting and locking marks
- Importing marks from sheets (Excel/CSV)
- Editing locked marks (with mandatory reason)
- Exporting marks as Excel
- Viewing marks history

---

## Base URL
```
/api/teacher/exams
```

---

## Core API Endpoints

### 1. List Exams for Marks Entry
**Endpoint:** `GET /{facultyId}/marks-entry`

**Description:** List all exams awaiting marks entry with filters

**Query Parameters:**
- `facultyId` (path) - Faculty/Teacher ID [REQUIRED]
- `className` (query) - Filter by class name [OPTIONAL]
- `section` (query) - Filter by section [OPTIONAL]
- `examType` (query) - Filter by exam type (Unit Test, Midterm, Final) [OPTIONAL]
- `status` (query) - Filter by status (MARKS_PENDING, UPCOMING, SUBMITTED) [OPTIONAL]

**Response:**
```json
[
  {
    "id": 1,
    "title": "Unit Test 1",
    "examType": "Unit Test",
    "className": "Grade 10A",
    "section": "A",
    "subjectName": "Mathematics",
    "examDate": "2026-02-12",
    "time": "10:00",
    "location": "Hall A",
    "status": "MARKS_PENDING",
    "totalMarks": 20,
    "passingMarks": 8
  }
]
```

**Example:**
```bash
curl -X GET "http://localhost:8080/api/teacher/exams/1/marks-entry?className=Grade%2010A&section=A&status=MARKS_PENDING" \
  -H "Authorization: Bearer TOKEN"
```

---

### 2. Get Marks Entry Board
**Endpoint:** `GET /{facultyId}/marks-entry/{examId}`

**Description:** Get the marks entry board for a specific exam with all students and their current marks

**Path Parameters:**
- `facultyId` - Faculty ID [REQUIRED]
- `examId` - Exam ID [REQUIRED]

**Response:**
```json
{
  "examId": 1,
  "examTitle": "Unit Test 1",
  "className": "Grade 10A",
  "section": "A",
  "subjectName": "Mathematics",
  "examDate": "2026-02-12",
  "totalMarks": 20,
  "passingMarks": 8,
  "totalStudents": 32,
  "presentCount": 30,
  "isLocked": false,
  "status": "IN_PROGRESS",
  "students": [
    {
      "studentId": 101,
      "rollNo": 101,
      "studentName": "Aarav Sharma",
      "marks": 18,
      "remark": "Good work",
      "status": "PASS"
    },
    {
      "studentId": 102,
      "rollNo": 102,
      "studentName": "Riya Patel",
      "marks": 12,
      "remark": "Needs improvement",
      "status": "PASS"
    }
  ]
}
```

---

### 3. Save Marks (Draft Mode)
**Endpoint:** `POST /{facultyId}/marks-entry/{examId}/save`

**Description:** Save marks in draft mode (not locked yet)

**Request Body:**
```json
{
  "examId": 1,
  "marks": [
    {
      "studentId": 101,
      "marks": 18,
      "remark": "Good work"
    },
    {
      "studentId": 102,
      "marks": 12,
      "remark": "Needs improvement"
    }
  ]
}
```

**Response:** Returns updated mark entry board (same as GET endpoint)

**Example:**
```bash
curl -X POST "http://localhost:8080/api/teacher/exams/1/marks-entry/1/save" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer TOKEN" \
  -d '{
    "examId": 1,
    "marks": [
      {"studentId": 101, "marks": 18, "remark": "Good work"},
      {"studentId": 102, "marks": 12, "remark": "Needs improvement"}
    ]
  }'
```

---

### 4. Update Individual Mark
**Endpoint:** `PATCH /{facultyId}/marks-entry/{examId}/{resultId}`

**Description:** Update a single student''s mark

**Request Body:**
```json
{
  "studentId": 101,
  "marks": 19,
  "remark": "Excellent work"
}
```

**Response:**
```json
{
  "studentId": 101,
  "rollNo": 101,
  "studentName": "Aarav Sharma",
  "marks": 19,
  "remark": "Excellent work",
  "status": "PASS"
}
```

---

### 5. Submit and Lock Marks
**Endpoint:** `POST /{facultyId}/marks-entry/{examId}/submit`

**Description:** Submit all marks for an exam (locks them, no casual editing allowed)

**Request Body:** Empty

**Warning:** Once submitted, marks cannot be edited except with a mandatory reason

**Response:** Returns updated mark entry board with `isLocked: true`

**Example:**
```bash
curl -X POST "http://localhost:8080/api/teacher/exams/1/marks-entry/1/submit" \
  -H "Authorization: Bearer TOKEN"
```

---

### 6. Get Marks History
**Endpoint:** `GET /{facultyId}/marks-history`

**Description:** Get all submitted/locked exams (marks history)

**Query Parameters:**
- `facultyId` (path) - Faculty ID [REQUIRED]
- `className` (query) - Filter by class [OPTIONAL]
- `section` (query) - Filter by section [OPTIONAL]

**Response:**
```json
[
  {
    "examId": 1,
    "examTitle": "Calculus Final",
    "className": "Grade 12C",
    "section": "C",
    "subjectName": "Mathematics",
    "examDate": "2026-01-20",
    "submittedOn": "2026-01-20T15:30:00",
    "totalStudents": 32,
    "presentCount": 30,
    "averageMarks": 65.5,
    "passCount": 28,
    "failCount": 2,
    "marksStatus": "LOCKED"
  }
]
```

---

### 7. View Marks Detail (Locked Marks)
**Endpoint:** `GET /{facultyId}/marks-detail/{examId}`

**Description:** View the final submitted marks (locked state)

**Response:** Same as marks entry board, but with `isLocked: true`

---

### 8. Edit Locked Marks
**Endpoint:** `PUT /{facultyId}/marks-detail/{examId}/edit`

**Description:** Edit previously locked marks (mandatory reason required for audit)

**Request Body:**
```json
{
  "resultId": 101,
  "marks": 20,
  "remark": "Rechecked, student deserves extra marks",
  "reasonForEdit": "Rechecked answer paper, marks were undercounted on question 3"
}
```

**Response:**
```json
{
  "studentId": 101,
  "rollNo": 101,
  "studentName": "Aarav Sharma",
  "marks": 20,
  "remark": "Rechecked, student deserves extra marks",
  "status": "PASS"
}
```

**Important:** The `reasonForEdit` field is mandatory and will be logged for audit trail.

---

### 9. Import Marks from Sheet - Preview
**Endpoint:** `POST /{facultyId}/marks-entry/{examId}/import-preview`

**Description:** Upload a marks sheet and preview the results (validation check)

**Request Type:** Multipart Form Data
- `file` - Excel/CSV file with columns: Roll No, Marks, [Optional: Remark]

**Response:**
```json
{
  "totalRows": 32,
  "validRows": 30,
  "invalidRows": 2,
  "errors": [
    "Row 5: Roll number 105 not found in class",
    "Row 10: Invalid marks value 'ABC'"
  ],
  "rows": [
    {
      "lineNumber": 1,
      "rollNo": 101,
      "studentName": "Aarav Sharma",
      "marks": 18,
      "remark": null,
      "isValid": true,
      "errorMessage": null
    },
    {
      "lineNumber": 5,
      "rollNo": 105,
      "marks": null,
      "isValid": false,
      "errorMessage": "Roll number 105 not found in class"
    }
  ]
}
```

**File Format Example:**
```
Roll No | Marks | Remark
--------|-------|--------
101     | 18    | Good work
102     | 12    | Needs improvement
103     | 8     | Failed
```

---

### 10. Import Marks from Sheet - Confirm
**Endpoint:** `POST /{facultyId}/marks-entry/{examId}/import-confirm`

**Description:** Confirm and apply the imported marks to the exam

**Request Body:**
```json
[
  {
    "lineNumber": 1,
    "rollNo": 101,
    "studentName": "Aarav Sharma",
    "marks": 18,
    "remark": null,
    "isValid": true
  },
  {
    "lineNumber": 2,
    "rollNo": 102,
    "studentName": "Riya Patel",
    "marks": 12,
    "remark": "Needs improvement",
    "isValid": true
  }
]
```

**Response:** Returns updated mark entry board with imported marks applied

---

### 11. Export Marks as Excel
**Endpoint:** `GET /{facultyId}/marks-detail/{examId}/export`

**Description:** Download exam marks as Excel file

**Response:** Binary Excel file with columns:
- Roll No
- Student Name
- Marks
- Remark
- Status

**Example:**
```bash
curl -X GET "http://localhost:8080/api/teacher/exams/1/marks-detail/1/export" \
  -H "Authorization: Bearer TOKEN" \
  -o exam_marks_1.xlsx
```

---

## Data Models

### TeacherExamCardResponse
```json
{
  "id": 1,
  "title": "Unit Test 1",
  "examType": "Unit Test",
  "className": "Grade 10A",
  "section": "A",
  "subjectName": "Mathematics",
  "examDate": "2026-02-12",
  "time": "10:00",
  "location": "Hall A",
  "status": "MARKS_PENDING|UPCOMING|SUBMITTED",
  "totalMarks": 20,
  "passingMarks": 8
}
```

### ExamMarkEntryRowResponse
```json
{
  "studentId": 101,
  "rollNo": 101,
  "studentName": "Aarav Sharma",
  "marks": 18,
  "remark": "Good work",
  "status": "PASS|FAIL"
}
```

### ExamMarkEntryBoardResponse
```json
{
  "examId": 1,
  "examTitle": "Unit Test 1",
  "className": "Grade 10A",
  "section": "A",
  "subjectName": "Mathematics",
  "examDate": "2026-02-12",
  "totalMarks": 20,
  "passingMarks": 8,
  "totalStudents": 32,
  "presentCount": 30,
  "isLocked": false,
  "status": "IN_PROGRESS|SUBMITTED",
  "students": [...]
}
```

### ExamMarksHistoryResponse
```json
{
  "examId": 1,
  "examTitle": "Calculus Final",
  "className": "Grade 12C",
  "section": "C",
  "subjectName": "Mathematics",
  "examDate": "2026-01-20",
  "submittedOn": "2026-01-20T15:30:00",
  "totalStudents": 32,
  "presentCount": 30,
  "averageMarks": 65.5,
  "passCount": 28,
  "failCount": 2,
  "marksStatus": "SUBMITTED|LOCKED"
}
```

---

## Error Handling

### Common Errors

**404 - Not Found**
```json
{
  "error": "Faculty not found",
  "message": "Faculty not found with id: 999"
}
```

**400 - Bad Request (Missing required field)**
```json
{
  "error": "Validation failed",
  "message": "Reason for editing is mandatory"
}
```

**409 - Conflict (Cannot edit non-locked marks)**
```json
{
  "error": "Invalid state",
  "message": "Marks are not locked for this result"
}
```

---

## Workflow Example

### Complete Flow: From Entry to Submission

```
1. List Exams → GET /marks-entry
2. Get Marks Board → GET /marks-entry/{examId}
3. Save Draft → POST /marks-entry/{examId}/save
4. Update Individual Mark → PATCH /marks-entry/{examId}/{resultId}
5. Submit & Lock → POST /marks-entry/{examId}/submit
6. View History → GET /marks-history
7. View Locked Marks → GET /marks-detail/{examId}
8. Export → GET /marks-detail/{examId}/export
```

### Import Workflow

```
1. Preview Import → POST /marks-entry/{examId}/import-preview (Upload file)
2. Review errors/results
3. Confirm Import → POST /marks-entry/{examId}/import-confirm
4. Marks are saved
```

### Edit Locked Marks Workflow

```
1. View Marks Detail → GET /marks-detail/{examId}
2. Edit Locked → PUT /marks-detail/{examId}/edit (with mandatory reason)
3. Marks updated with audit trail
```

---

## Database Schema (SQL Migration)

```sql
ALTER TABLE exam_results ADD COLUMN remarks TEXT;
ALTER TABLE exam_results ADD COLUMN marks_locked BOOLEAN DEFAULT FALSE;
ALTER TABLE exam_results ADD COLUMN marks_locked_at TIMESTAMP;
ALTER TABLE exam_results ADD COLUMN marks_edited_at TIMESTAMP;
ALTER TABLE exam_results ADD COLUMN marks_edit_reason TEXT;
ALTER TABLE exam_results ADD COLUMN edited_by_teacher_id BIGINT;
```

---

## Security & Permissions

- Only assigned teachers can mark exams for their classes
- Once locked, marks require audit trail reason for edits
- All edits are logged with timestamp and teacher ID
- Marks history is immutable for compliance

---

## Integration Notes

- Uses Apache POI for Excel file parsing and export
- File size limit: 10MB per upload
- Supported formats: .xlsx, .xls, .csv
- Auto-mapping: Roll Number and Marks columns
- Transactional: All operations are ACID-compliant

---

## Testing

### Test Case 1: Save Draft Marks
```bash
POST /api/teacher/exams/1/marks-entry/1/save
Body: { "examId": 1, "marks": [...] }
Expected: 200 OK with updated board
```

### Test Case 2: Submit & Lock
```bash
POST /api/teacher/exams/1/marks-entry/1/submit
Expected: 200 OK, isLocked = true
```

### Test Case 3: Edit Locked Marks
```bash
PUT /api/teacher/exams/1/marks-detail/1/edit
Body: { "resultId": 1, "marks": 20, "reasonForEdit": "Rechecked" }
Expected: 200 OK with new marks
```

### Test Case 4: Import Sheet
```bash
POST /api/teacher/exams/1/marks-entry/1/import-preview
File: marks.xlsx
Expected: 200 OK with preview data
```

---

## Summary

This Exam Marks Management API provides complete marking workflow:
✅ List exams  
✅ Entry/Edit marks  
✅ Draft save  
✅ Submit & lock  
✅ Import from sheet  
✅ Edit with audit  
✅ Export marks  
✅ View history  

All with proper authorization, validation, and audit trails.

