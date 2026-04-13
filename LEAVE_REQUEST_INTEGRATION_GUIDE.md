# LeaveRequest System - Backend & Frontend Integration Guide

## Overview
This document provides complete instructions for deploying the LeaveRequest system integration between the backend Java Spring Boot API and the frontend React application.

---

## Backend Setup

### 1. Database Migration

Execute the database migration script to create the `leave_requests` table:

```bash
# Windows using MySQL client
mysql -u your_username -p your_database < leaveRequest_migration.sql

# Or via your database GUI (MySQL Workbench, PhpMyAdmin, etc.)
# Execute the SQL script: C:\Users\Admin\Downloads\career-builder-backend-public-apis\career-builder-backend-main\leaveRequest_migration.sql
```

**Expected Output:**
- Table `leave_requests` created with columns:
  - `leave_id` (Primary Key)
  - `student_id` (Foreign Key to students table)
  - `from_date`, `to_date`
  - `leave_type`, `reason`
  - `status` (APPLIED, APPROVED, REJECTED, CANCELLED)
  - `is_half_day`, `half_day_period`
  - `approved_on`, `approved_by`, `rejection_reason`
  - `created_at`, `updated_at`

### 2. Insert Test Data

Insert sample leave requests to test the API:

```bash
# Windows using MySQL client
mysql -u your_username -p your_database < leaveRequest_seed_data.sql

# Verify data insertion
mysql -u your_username -p your_database -e "SELECT COUNT(*) FROM leave_requests;"
```

**Expected Result:** 10 leave requests across 4 students with various statuses

### 3. Rebuild Backend

```bash
cd C:\Users\Admin\Downloads\career-builder-backend-public-apis\career-builder-backend-main

# Using Maven
mvn clean compile install

# Or if using Maven wrapper
mvnw clean compile install

# Expected: BUILD SUCCESS
```

**Files Created/Modified:**
- ✅ `src/main/java/com/org/careerbuilder/controller/LeaveRequestController.java` (NEW)
- ✅ `src/main/java/com/org/careerbuilder/service/LeaveRequestService.java` (MODIFIED)
- ✅ `src/main/java/com/org/careerbuilder/service/impl/LeaveRequestServiceImpl.java` (NEW)
- ✅ `src/main/java/com/org/careerbuilder/dto/request/LeaveRequestDTO.java` (NEW)
- ✅ `src/main/java/com/org/careerbuilder/dto/response/LeaveRequestResponse.java` (NEW)
- ✅ `src/main/java/com/org/careerbuilder/models/LeaveRequest.java` (MODIFIED)
- ✅ `src/main/java/com/org/careerbuilder/repository/LeaveRequestRepository.java` (MODIFIED)

### 4. Start Backend Server

```bash
cd C:\Users\Admin\Downloads\career-builder-backend-public-apis\career-builder-backend-main

# Run with Spring Boot
mvn spring-boot:run

# Or using JAR file
java -jar target/career-builder-backend-main.jar

# Expected: Application starts on port 9091
```

**Verify Backend Health:**
```bash
# Test the API endpoint in PowerShell
$response = Invoke-RestMethod -Uri "http://localhost:9091/api/leave-requests/student/1?page=0&size=10" -Method Get
$response | ConvertTo-Json
```

Expected response should include leave request data from database.

---

## Frontend Setup

### 1. Frontend API Service Already Created

The file has been created at:
- `src/api/leaveService.js`

This file contains all necessary API functions:
- `applyForLeave()` - Submit new leave request
- `getStudentLeaveRequests()` - Get all leave requests (paginated)
- `getLeaveBalance()` - Get leave balance summary
- `cancelLeaveRequest()` - Cancel pending request
- `updateLeaveStatus()` - Update leave status (admin)
- And more...

### 2. Frontend Components Updated

The following components have been updated with live API integration:

**LeavePreview.jsx** - Dashboard preview with real data
- Fetches leave balance from backend
- Displays pending, used, and remaining leave counts
- Auto-loads on component mount

**YourRequests.jsx** - List of leave requests
- Fetches leave requests from backend with pagination
- Shows status badges (APPLIED, APPROVED, REJECTED, CANCELLED)
- Supports cancel functionality for pending requests
- Shows formatted dates and duration calculations

**ApplyForLeave.jsx** - Form to apply for leave
- Form validation before submission
- Sends request to backend API
- Shows success/error messages
- Resets form after successful submission
- Support for half-day leave selection

### 3. Verify Frontend Build

```bash
cd C:\Users\Admin\OneDrive\Desktop\Career_Builder_Frontend\finalCareer

# Check for any build errors
npm run build

# Expected: Build completes successfully
```

### 4. Start Frontend Dev Server

```bash
cd C:\Users\Admin\OneDrive\Desktop\Career_Builder_Frontend\finalCareer

npm run dev

# Expected: Frontend runs on port 5175 (or next available port)
```

---

## Testing the Integration

### 1. Test Leave Balance API (Backend)

```powershell
# Measure response time and validate structure
$response = Invoke-RestMethod -Uri "http://localhost:9091/api/leave-requests/student/1/balance" -Method Get

$response | ConvertTo-Json | Out-String
```

**Expected Response:**
```json
{
  "success": true,
  "message": "Leave balance retrieved successfully",
  "data": {
    "totalLeaveBalance": 20,
    "usedLeaves": 3,
    "remainingLeaves": 17,
    "pendingRequests": 1
  }
}
```

### 2. Test Get Student Leave Requests (Backend)

```powershell
$response = Invoke-RestMethod -Uri "http://localhost:9091/api/leave-requests/student/1?page=0&size=10" -Method Get

$response | ConvertTo-Json | Out-String | Select-Object -First 100
```

**Expected Response:** Array of leave requests with all details

### 3. Test Apply for Leave (Backend)

```powershell
$leaveData = @{
    studentId = 1
    leaveType = "Sick Leave"
    fromDate = "2026-04-15"
    toDate = "2026-04-17"
    reason = "Testing the API"
    isHalfDay = $false
    halfDayPeriod = $null
} | ConvertTo-Json

$response = Invoke-RestMethod -Uri "http://localhost:9091/api/leave-requests/apply" -Method Post -Body $leaveData -ContentType "application/json"

$response | ConvertTo-Json | Out-String
```

**Expected Response:** Newly created leave request with APPLIED status

### 4. Test Frontend UI

1. **Navigate to Student Portal:**
   - Go to `http://localhost:5175` in your browser
   - Login with valid student credentials
   - Click on "LeaveRequest" in the sidebar

2. **Verify LeavePreview Component:**
   - Should display leave balance cards
   - Cards should show real data from backend
   - If API fails, it should show fallback data

3. **Verify YourRequests Component:**
   - Should display your leave requests in a list
   - Each request shows: title, status badge, date range, duration
   - Click "Apply Leave" button at the top right

4. **Test Apply for Leave Modal:**
   - Fill in the form:
     - Leave Type: Select "Sick Leave"
     - Dates: Pick any future dates
     - Reason: Enter a reason
   - Click "Submit Request"
   - Should show success message
   - Modal should close
   - New request should appear in YourRequests list

### 5. Test Cancel Functionality

1. In YourRequests, find a "Pending" leave request
2. Click the "Cancel" button
3. Request status should change to "CANCELLED"
4. Button should disappear after cancellation

---

## Common Issues & Troubleshooting

### Issue 1: Backend API Returns 404

**Symptom:** `Error 404: Endpoint not found`

**Solution:**
1. Verify that the backend has been rebuilt after adding new files
2. Check that the port is 9091 (or correct port in your setup)
3. Restart the Spring Boot server

```bash
# Rebuild and restart
mvn clean compile install
mvn spring-boot:run
```

### Issue 2: Frontend Can't Connect to Backend

**Symptom:** Network errors, CORS errors, or timeouts

**Solution:**
1. Ensure backend is running on `http://localhost:9091`
2. Check if CORS is enabled in backend configuration
3. Update axios configuration in `src/api/axios.js` if needed

```javascript
// Check axios base URL in src/api/axios.js
const API_BASE_URL = process.env.REACT_APP_API_URL || "http://localhost:9091";
```

### Issue 3: Student ID Returns 0 or Null

**Symptom:** Leave requests show as undefined student

**Solution:**
1. Ensure you're logged in with a valid student account
2. Check Redux store has user data: `useSelector((state) => state?.user?.userId)`
3. Verify student record exists in database

```bash
# Check in MySQL
SELECT * FROM students WHERE student_id = 1;
```

### Issue 4: Leave Types Not Matching

**Symptom:** Leave type shows incorrectly in UI

**Solution:**
1. Ensure enum values match between backend and frontend
2. **Backend:** `LeaveStatus` enum in `models/enums/LeaveStatus.java`
   - Values: APPLIED, APPROVED, REJECTED, CANCELLED
3. **Frontend:** Status config in `YourRequests.jsx`
   - Must match backend values exactly

### Issue 5: Dates Not Formatting Correctly

**Solution:**
1. Check date format consistency (YYYY-MM-DD)
2. Verify timezone settings if dates appear off
3. Use `formatDate()` utility function in components

---

## Database Schema Verification

Run this query to verify the schema is correct:

```sql
-- Check leave_requests table structure
DESCRIBE leave_requests;

-- Check indexes
SHOW INDEXES FROM leave_requests;

-- Verify foreign key relationship
SELECT CONSTRAINT_NAME, TABLE_NAME, REFERENCED_TABLE_NAME 
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE 
WHERE TABLE_NAME = 'leave_requests';

-- Check sample data
SELECT * FROM leave_requests LIMIT 5;
```

---

## API Endpoints Summary

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/leave-requests/apply` | Apply for new leave |
| GET | `/api/leave-requests/{id}` | Get leave request by ID |
| GET | `/api/leave-requests/student/{id}` | Get all requests (paginated) |
| GET | `/api/leave-requests/student/{id}/status/{status}` | Get by status |
| GET | `/api/leave-requests/student/{id}/upcoming` | Get upcoming leaves |
| GET | `/api/leave-requests/student/{id}/balance` | Get leave balance |
| GET | `/api/leave-requests/student/{id}/preview` | Get dashboard preview |
| PUT | `/api/leave-requests/{id}/cancel` | Cancel request |
| PUT | `/api/leave-requests/{id}/status` | Update status (Admin) |
| DELETE | `/api/leave-requests/{id}` | Delete request (Admin) |

---

## Performance Optimization

### Recommended Indexes (Already Included)
- `idx_leave_student_from` on (student_id, from_date)
- `idx_leave_student_status` on (student_id, status)
- `idx_leave_from_date` on (from_date)
- `idx_leave_status` on (status)

### Pagination Settings
- Default page size: 10 requests per page
- Maximum recommended: 50 requests per page

---

## Next Steps

1. ✅ Deploy backend code (compile and run)
2. ✅ Run database migrations
3. ✅ Insert test data
4. ✅ Verify API endpoints
5. ✅ Start frontend dev server
6. ✅ Test complete flow end-to-end
7. Optional: Set up admin approval workflow
8. Optional: Add email notifications for leave status changes
9. Optional: Implement leave policy enforcement

---

## Support & Debugging

### Enable Debug Logging

**Backend (application.properties):**
```properties
logging.level.com.org.careerbuilder=DEBUG
logging.level.com.org.careerbuilder.controller.LeaveRequestController=DEBUG
logging.level.com.org.careerbuilder.service.impl.LeaveRequestServiceImpl=DEBUG
```

**Frontend (browser console):**
```javascript
// Enable API call logging
localStorage.setItem('debug', 'api:*');
```

### Check Logs

```bash
# Backend logs (if running with Spring Boot)
tail -f target/backend.log

# Browser console
# Press F12 in browser to open Developer Tools
# Check Network tab for API calls
```

---

## Verification Checklist

- [ ] Backend compiles successfully
- [ ] Database migration script executed
- [ ] Test data inserted (10 leave requests)
- [ ] Backend API endpoints respond with correct data
- [ ] Frontend fetches leave balance data
- [ ] Frontend displays leave requests list
- [ ] Can apply for new leave via modal
- [ ] Cancel button works for pending requests
- [ ] Status changes are reflected immediately
- [ ] No console errors in browser
- [ ] No errors in backend logs

---

**Document Version:** 1.0  
**Last Updated:** April 2, 2026  
**Status:** ✅ Complete and Ready for Testing
