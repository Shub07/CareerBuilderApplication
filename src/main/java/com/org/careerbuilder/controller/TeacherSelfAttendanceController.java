package com.org.careerbuilder.controller;

import com.org.careerbuilder.dto.request.TeacherSelfAttendanceDtos;
import com.org.careerbuilder.models.enums.TeacherLeaveStatus;
import com.org.careerbuilder.service.TeacherSelfAttendanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teacher/self-attendance")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000", "http://localhost:5174"},
        allowedHeaders = "*",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.PATCH, RequestMethod.DELETE, RequestMethod.OPTIONS})
public class TeacherSelfAttendanceController {

    private final TeacherSelfAttendanceService teacherSelfAttendanceService;

    @GetMapping("/{facultyId}/dashboard")
    public ResponseEntity<TeacherSelfAttendanceDtos.DashboardResponse> dashboard(@PathVariable Long facultyId) {
        return ResponseEntity.ok(teacherSelfAttendanceService.getDashboard(facultyId));
    }

    @PostMapping("/{facultyId}/check-in")
    public ResponseEntity<TeacherSelfAttendanceDtos.TodayStatusCard> checkIn(
            @PathVariable Long facultyId,
            @RequestBody(required = false) TeacherSelfAttendanceDtos.CheckInRequest request) {
        return ResponseEntity.ok(teacherSelfAttendanceService.checkIn(facultyId, request));
    }

    @PostMapping("/{facultyId}/check-out")
    public ResponseEntity<TeacherSelfAttendanceDtos.TodayStatusCard> checkOut(
            @PathVariable Long facultyId,
            @RequestBody(required = false) TeacherSelfAttendanceDtos.CheckOutRequest request) {
        return ResponseEntity.ok(teacherSelfAttendanceService.checkOut(facultyId, request));
    }

    @PostMapping("/{facultyId}/leave/apply")
    public ResponseEntity<Void> applyLeave(
            @PathVariable Long facultyId,
            @Valid @RequestBody TeacherSelfAttendanceDtos.LeaveApplyRequest request) {
        teacherSelfAttendanceService.applyLeave(facultyId, request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{facultyId}/leave/history")
    public ResponseEntity<List<TeacherSelfAttendanceDtos.LeaveHistoryItem>> leaveHistory(
            @PathVariable Long facultyId,
            @RequestParam(required = false) TeacherLeaveStatus status) {
        return ResponseEntity.ok(teacherSelfAttendanceService.getLeaveHistory(facultyId, status));
    }

    @GetMapping("/admin/leave-requests")
    public ResponseEntity<List<TeacherSelfAttendanceDtos.AdminLeaveItem>> adminLeaveRequests(
            @RequestParam Long schoolId,
            @RequestParam(required = false) TeacherLeaveStatus status) {
        return ResponseEntity.ok(teacherSelfAttendanceService.getAdminLeaveRequests(schoolId, status));
    }

    @PatchMapping("/admin/leave-requests/{leaveId}/status")
    public ResponseEntity<TeacherSelfAttendanceDtos.AdminLeaveItem> updateLeaveStatus(
            @PathVariable Long leaveId,
            @Valid @RequestBody TeacherSelfAttendanceDtos.LeaveActionRequest request) {
        return ResponseEntity.ok(teacherSelfAttendanceService.updateLeaveStatus(leaveId, request));
    }
}
