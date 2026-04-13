# 🎫 Vacations Management System - Implementation Guide

## 📋 Overview

A complete **Vacations Management System** has been implemented for the Career Builder Platform with:
- **Admin controls** for creating and managing school vacations
- **Student visibility** - All students see vacations created for their school
- **Exception handling** using GlobalExceptionHandler and custom VacationException
- **Multiple vacation types** - Holiday, Summer, Winter, Spring, Exam breaks, etc.
- **Vacation status tracking** - Ongoing, Upcoming, Completed
- **Notice tracking** - Admin can mark when vacations are communicated to students

---

## 🏗️ Architecture

### System Design
```
┌─────────────────────────────────────────┐
│       ADMIN OPERATIONS                  │
├─────────────────────────────────────────┤
│ ├─ Create Vacation                     │
│ ├─ Update Vacation                     │
│ ├─ Delete Vacation                     │
│ └─ Mark Notice as Sent                 │
└──────────────┬──────────────────────────┘
               │ (Admin creates via API)
               ▼
    ┌──────────────────────┐
    │  VACATIONS TABLE     │
    │  (Database)          │
    └──────────────┬───────┘
                   │ (All data stored)
                   ▼
┌─────────────────────────────────────────┐
│   STUDENT OPERATIONS (Public)           │
├─────────────────────────────────────────┤
│ ├─ View All Vacations                  │
│ ├─ View Upcoming Vacations             │
│ ├─ View Ongoing Vacations              │
│ ├─ View Completed Vacations            │
│ └─ Check if School is on Vacation      │
└─────────────────────────────────────────┘
```

---

## 📁 Files Created

### 1. **Models** (1 file)
- `Vacation.java` - Complete vacation entity with enums and helper methods

### 2. **Repositories** (1 file)
- `VacationRepository.java` - 14 query methods for vacation data access

### 3. **Services** (1 file)
- `VacationService.java` - Business logic with proper exception handling

### 4. **Controllers** (1 file)
- `VacationController.java` - 10 REST endpoints (5 public + 5 admin)

### 5. **DTOs** (2 files)
- `VacationRequest.java` - Admin request for creating/updating vacations
- `VacationResponse.java` - Response object for all vacation endpoints

### 6. **Exceptions** (1 file)
- `VacationException.java` - Custom vacation-specific exception

### 7. **Database** (1 file)
- `vacations_migration.sql` - Complete DDL + sample data + views

### 8. **Documentation** (This file)

---

## 🚀 API Endpoints

### **Student Endpoints (Public)** - All students see vacations

#### 1. Get All School Vacations
```
GET /api/vacations/school/{schoolId}

Response:
{
  "success": true,
  "message": "Vacations retrieved successfully",
  "data": [
    {
      "vacation_id": 1,
      "vacation_name": "Spring Vacation 2026",
      "vacation_type": "SPRING",
      "vacation_type_label": "Spring Vacation",
      "start_date": "2026-04-01",
      "end_date": "2026-04-14",
      "duration_days": 14,
      "is_ongoing": false,
      "is_upcoming": true,
      "is_completed": false,
      "description": "Spring break for all students"
    }
  ],
  "total_count": 5
}
```

#### 2. Get Upcoming Vacations
```
GET /api/vacations/school/{schoolId}/upcoming

Returns vacations starting after today
```

#### 3. Get Ongoing Vacations
```
GET /api/vacations/school/{schoolId}/ongoing

Returns vacations happening today
```

#### 4. Get Completed Vacations
```
GET /api/vacations/school/{schoolId}/completed

Returns vacations that have ended
```

#### 5. Get Single Vacation
```
GET /api/vacations/{vacationId}

Returns detailed information for a specific vacation
```

#### 6. Check if School is on Vacation
```
GET /api/vacations/school/{schoolId}/on-vacation

Response:
{
  "success": true,
  "school_id": 1,
  "is_on_vacation": true,
  "message": "School is currently on vacation"
}
```

### **Admin Endpoints** - Only admins can use

#### 7. Create Vacation
```
POST /api/vacations/admin/create
Content-Type: application/json

Request:
{
  "school_id": 1,
  "vacation_name": "Summer Vacation 2026",
  "vacation_type": "SUMMER",
  "start_date": "2026-05-15",
  "end_date": "2026-07-15",
  "description": "Extended summer vacation",
  "is_active": true,
  "created_by": "admin_username"
}

Response (201 Created):
{
  "success": true,
  "message": "Vacation created successfully",
  "data": {
    "vacation_id": 10,
    "vacation_name": "Summer Vacation 2026",
    "vacation_type": "SUMMER",
    "start_date": "2026-05-15",
    "end_date": "2026-07-15",
    "duration_days": 62,
    ...
  }
}
```

#### 8. Update Vacation
```
PUT /api/vacations/admin/{vacationId}
Content-Type: application/json

Request: (Same as create, but updates existing)

Response: Updated vacation object
```

#### 9. Delete Vacation
```
DELETE /api/vacations/admin/{vacationId}

Response:
{
  "success": true,
  "message": "Vacation deleted successfully"
}
```

#### 10. Mark Notice as Sent
```
POST /api/vacations/admin/{vacationId}/mark-notice-sent

Response:
{
  "success": true,
  "message": "Vacation notice marked as sent",
  "data": {
    "vacation_id": 1,
    "notice_sent": true,
    "notice_sent_date": "2026-03-31T10:30:00"
  }
}
```

---

## 💾 Database Schema

### VACATIONS Table
```sql
CREATE TABLE vacations (
    vacation_id SERIAL PRIMARY KEY,
    school_id BIGINT NOT NULL,
    vacation_name VARCHAR(100),
    vacation_type VARCHAR(50),           -- HOLIDAY, SUMMER, WINTER, etc.
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    description VARCHAR(500),
    is_active BOOLEAN DEFAULT TRUE,
    notice_sent BOOLEAN DEFAULT FALSE,
    notice_sent_date TIMESTAMP,
    created_by VARCHAR(100),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    FOREIGN KEY (school_id) REFERENCES schools(id)
)
```

### Indexes (5)
- `idx_vacation_start_date` - Fast filtering by start date
- `idx_vacation_end_date` - Fast filtering by end date
- `idx_vacation_school_id` - Fast lookup by school
- `idx_vacation_type` - Fast filtering by vacation type
- `idx_vacation_active` - Fast filtering by active status

### Views (3)
- `v_ongoing_vacations` - Vacations happening today
- `v_upcoming_vacations` - Future vacations
- `v_vacation_schedule_summary` - School vacation summary

---

## 🔒 Exception Handling

### Custom Exception Class
```java
public class VacationException extends RuntimeException {
    private String errorCode;
    
    public VacationException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
}
```

### GlobalExceptionHandler Integration
```java
@ExceptionHandler(VacationException.class)
public ResponseEntity<Map<String, Object>> handleVacationException(
        VacationException ex) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
            "success", false,
            "message", ex.getMessage(),
            "error_code", ex.getErrorCode()
    ));
}
```

### Error Codes
- `VACATION_ERROR` - Generic vacation error
- `INVALID_DATES` - Invalid date range
- `INVALID_DATE_RANGE` - Start date after end date
- `INVALID_START_DATE` - Start date in the past
- `VACATION_SCHOOL_MISMATCH` - Vacation doesn't belong to school
- `VACATION_CREATE_ERROR` - Creation failed
- `VACATION_UPDATE_ERROR` - Update failed
- `VACATION_DELETE_ERROR` - Deletion failed
- `VACATION_FETCH_ERROR` - Fetch failed

---

## 🎯 Vacation Types

```java
enum VacationType {
    HOLIDAY("Holiday/Festival"),           // National holidays
    EXAM_BREAK("Exam Break"),              // No classes during exams
    SUMMER("Summer Vacation"),             // Long summer break
    WINTER("Winter Vacation"),             // Winter holidays
    SPRING("Spring Vacation"),             // Spring break
    AUTUMN("Autumn Vacation"),             // Autumn break
    SPECIAL("Special Leave"),              // Special occasions
    EMERGENCY("Emergency Closure")         // Unexpected closures
}
```

---

## 📊 Sample Data

The migration script inserts sample vacations:

```sql
-- Spring Vacation
INSERT INTO vacations 
VALUES (1, 'Spring Vacation 2026', 'SPRING', '2026-04-01', '2026-04-14', ...)

-- Summer Vacation
INSERT INTO vacations 
VALUES (1, 'Summer Vacation 2026', 'SUMMER', '2026-05-15', '2026-07-15', ...)

-- Independence Day
INSERT INTO vacations 
VALUES (1, 'Independence Day', 'HOLIDAY', '2026-08-15', '2026-08-15', ...)
```

---

## 🧪 Testing Examples

### 1. Admin Creates Vacation
```bash
curl -X POST http://localhost:9091/api/vacations/admin/create \
  -H "Content-Type: application/json" \
  -d '{
    "school_id": 1,
    "vacation_name": "Summer 2026",
    "vacation_type": "SUMMER",
    "start_date": "2026-05-15",
    "end_date": "2026-07-15",
    "description": "Extended summer break",
    "created_by": "admin1"
  }'
```

### 2. Student Views All Vacations
```bash
curl http://localhost:9091/api/vacations/school/1
```

### 3. Student Views Upcoming Vacations
```bash
curl http://localhost:9091/api/vacations/school/1/upcoming
```

### 4. Check if School is on Vacation
```bash
curl http://localhost:9091/api/vacations/school/1/on-vacation
```

### 5. Admin Updates Vacation
```bash
curl -X PUT http://localhost:9091/api/vacations/admin/1 \
  -H "Content-Type: application/json" \
  -d '{
    "school_id": 1,
    "vacation_name": "Extended Summer 2026",
    "vacation_type": "SUMMER",
    "start_date": "2026-05-10",
    "end_date": "2026-07-20",
    "description": "Extended by one week"
  }'
```

---

## 🔑 Key Features

✅ **Admin Control** - Only admins can create/update/delete vacations  
✅ **Student Visibility** - All students see vacations for their school  
✅ **Real-time Status** - Vacations show as Upcoming/Ongoing/Completed  
✅ **Date Validation** - Start date cannot be before end date  
✅ **Notice Tracking** - Admin can mark when notice is sent to students  
✅ **Exception Handling** - Comprehensive error handling with custom exceptions  
✅ **Query Performance** - 5 indexes for fast queries  
✅ **Reporting Views** - 3 views for vacation analytics  
✅ **Type Classification** - 8 vacation types for organization  

---

## 📈 Common Queries

### Get Active Vacations
```
/api/vacations/school/1
```

### Get Vacation Status
```
/api/vacations/1
Returns: is_ongoing, is_upcoming, is_completed
```

### Check School Availability
```
/api/vacations/school/1/on-vacation
Returns: true/false
```

---

## ⚙️ Implementation Steps

1. ✅ Created Vacation model with JPA annotations
2. ✅ Created VacationRepository with 14 query methods
3. ✅ Created VacationService with business logic
4. ✅ Created VacationController with 10 endpoints
5. ✅ Created DTOs for request/response
6. ✅ Created VacationException custom exception
7. ✅ Updated GlobalExceptionHandler
8. ✅ Created database migration script
9. ✅ Added sample vacation data

---

## 🚀 Deployment Steps

### 1. Build
```bash
mvn clean package -DskipTests
```

### 2. Apply Migration
```bash
psql -U admin -d admindb -f vacations_migration.sql
```

### 3. Start Backend
```bash
java -jar target/career-builder-0.0.1-SNAPSHOT.jar
```

### 4. Test Endpoints
```bash
# Get vacations
curl http://localhost:9091/api/vacations/school/1

# Create vacation (admin)
curl -X POST http://localhost:9091/api/vacations/admin/create \
  -H "Content-Type: application/json" \
  -d '{"school_id":1,"vacation_name":"Test","vacation_type":"HOLIDAY",...}'
```

---

## 📝 Notes

- All students in a school see the same vacations
- Vacations are school-specific (not student-specific)
- Admin must provide valid date ranges
- Notice sent date is automatically set to current timestamp
- All timestamps are in local timezone

---

**Status**: ✅ **PRODUCTION READY**  
**Created**: March 31, 2026  
**Last Updated**: March 31, 2026

