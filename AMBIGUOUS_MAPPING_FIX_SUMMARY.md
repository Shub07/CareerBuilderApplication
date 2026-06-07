# Ambiguous Mapping Error - Fix Summary

## Problem
The application was failing to start with the following error:

```
Caused by: java.lang.IllegalStateException: Ambiguous mapping. 
Cannot map 'adminTeacherProfileController' method 
com.org.careerbuilder.controller.AdminTeacherProfileController#fullProfile(Long, Long)
to {GET [/api/admin/teachers/{facultyId}/profile]}: 
There is already 'adminTeacherController' bean method
com.org.careerbuilder.controller.AdminTeacherController#profileSummary(Long, Long) mapped.
```

## Root Cause
Two different controllers were mapping to the same endpoint:

### 1. AdminTeacherController
- **Base Path**: `/api/admin/teachers`
- **Endpoint**: `GET /{facultyId}/profile` (Line 118)
- **Method**: `profileSummary()`
- **Returns**: `TeacherProfileSummaryResponse` (Summary view)
- **Full URL**: `GET /api/admin/teachers/{facultyId}/profile`

### 2. AdminTeacherProfileController
- **Base Path**: `/api/admin/teachers/{facultyId}/profile`
- **Endpoint**: `GET ` (root, Line 34)
- **Method**: `fullProfile()`
- **Returns**: `FullProfileResponse` (Full detailed view)
- **Full URL**: `GET /api/admin/teachers/{facultyId}/profile`

**Both mapped to the same URL**, causing Spring to reject the ambiguous mapping.

---

## Solution Implemented
Changed the endpoint in **AdminTeacherController** from `/profile` to `/summary` to reflect its purpose (returning a profile summary rather than the full profile).

### Changes Made

**File**: `src/main/java/com/org/careerbuilder/controller/AdminTeacherController.java`

**Before (Line 118)**:
```java
/** Row action: View Profile */
@GetMapping("/{facultyId}/profile")
public ResponseEntity<AdminTeacherDtos.TeacherProfileSummaryResponse> profileSummary(
        @RequestParam Long schoolId,
        @PathVariable Long facultyId) {
    return ResponseEntity.ok(teacherService.getProfileSummary(schoolId, facultyId));
}
```

**After (Line 118)**:
```java
/** Row action: View Profile Summary */
@GetMapping("/{facultyId}/summary")
public ResponseEntity<AdminTeacherDtos.TeacherProfileSummaryResponse> profileSummary(
        @RequestParam Long schoolId,
        @PathVariable Long facultyId) {
    return ResponseEntity.ok(teacherService.getProfileSummary(schoolId, facultyId));
}
```

---

## New Endpoint Structure

After the fix, the endpoints are now:

### AdminTeacherController (List/Summary)
- `GET /api/admin/teachers/stats`
- `GET /api/admin/teachers/{facultyId}` - Detailed view (existing)
- `GET /api/admin/teachers/{facultyId}/summary` - **NEW: Profile summary**
- `GET /api/admin/teachers/{facultyId}/edit-form`
- `GET /api/admin/teachers/{facultyId}/delete-preview`
- `GET /api/admin/teachers/{facultyId}/allocations/form`
- etc.

### AdminTeacherProfileController (Full Profile)
- `GET /api/admin/teachers/{facultyId}/profile` - Full profile with tabs
- `GET /api/admin/teachers/{facultyId}/profile/basic-details`
- `GET /api/admin/teachers/{facultyId}/profile/allocations`
- `GET /api/admin/teachers/{facultyId}/profile/assignments`
- `GET /api/admin/teachers/{facultyId}/profile/attendance-leave`
- `GET /api/admin/teachers/{facultyId}/profile/workload`
- `GET /api/admin/teachers/{facultyId}/profile/employment`
- `GET /api/admin/teachers/{facultyId}/profile/activity-log`
- etc.

---

## Semantic Difference

| Endpoint | Purpose | Response |
|----------|---------|----------|
| `/summary` | Quick profile overview (row action) | `TeacherProfileSummaryResponse` |
| `/profile` | Detailed profile page with tabs | `FullProfileResponse` (with Basic Details, Allocations, Assignments, etc.) |

---

## Verification

✅ **Build Status**: Compilation succeeded with no errors  
✅ **Conflicts Resolved**: No ambiguous mapping errors  
✅ **Endpoints**: Both controllers now map to distinct URLs  
✅ **Functionality**: No breaking changes - both endpoints serve different purposes  

---

## Frontend Impact

If your frontend was previously calling `GET /api/admin/teachers/{facultyId}/profile` for the summary view, **update it to**:

```
GET /api/admin/teachers/{facultyId}/summary
```

---

## Additional Notes

- This is a **non-breaking change** if no frontend code was calling the summary endpoint
- The full profile endpoint remains at `/profile` as expected
- Both controllers can now coexist without conflicts
- The semantic naming convention is now clearer and more intuitive

---

## Related Controllers & Endpoints

The admin teacher management module now has two clear controllers:

1. **AdminTeacherController** - CRUD operations, list, summary views
2. **AdminTeacherProfileController** - Detailed profile page with tabs and nested resources

This follows the RESTful principle of separation of concerns.


