package com.org.careerbuilder.controller;

import com.org.careerbuilder.dto.request.TeacherSelfAttendanceDtos;
import com.org.careerbuilder.models.enums.TeacherLeaveStatus;
import com.org.careerbuilder.service.TeacherSelfAttendanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
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

    @PostMapping(value = "/{facultyId}/leave/apply", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> applyLeaveJson(
            @PathVariable Long facultyId,
            @Valid @RequestBody TeacherSelfAttendanceDtos.LeaveApplyRequest request) {
        teacherSelfAttendanceService.applyLeave(facultyId, request, null);
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/{facultyId}/leave/apply", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> applyLeaveMultipart(
            @PathVariable Long facultyId,
            @RequestParam com.org.careerbuilder.models.enums.TeacherLeaveType leaveType,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam String reason,
            @RequestParam(required = false) Long substituteFacultyId,
            @RequestPart(value = "document", required = false) MultipartFile document) {
        TeacherSelfAttendanceDtos.LeaveApplyRequest request = new TeacherSelfAttendanceDtos.LeaveApplyRequest(
                leaveType, fromDate, toDate, reason, substituteFacultyId);
        teacherSelfAttendanceService.applyLeave(facultyId, request, document);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{facultyId}/leave/substitutes")
    public ResponseEntity<List<TeacherSelfAttendanceDtos.SubstituteTeacherOption>> substituteTeachers(
            @PathVariable Long facultyId) {
        return ResponseEntity.ok(teacherSelfAttendanceService.listSubstituteTeachers(facultyId));
    }

    @GetMapping("/{facultyId}/leave/{leaveId}/document")
    public ResponseEntity<Resource> downloadLeaveDocument(
            @PathVariable Long facultyId,
            @PathVariable Long leaveId) {
        Resource resource = teacherSelfAttendanceService.downloadLeaveDocument(facultyId, leaveId);
        String filename = resource.getFilename() != null ? resource.getFilename() : "leave_document";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(resource);
    }

    @GetMapping("/{facultyId}/attendance/history")
    public ResponseEntity<List<TeacherSelfAttendanceDtos.AttendanceHistoryRow>> attendanceHistory(
            @PathVariable Long facultyId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(teacherSelfAttendanceService.getAttendanceHistory(facultyId, fromDate, toDate, status));
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
