package com.org.careerbuilder.controller;

import com.org.careerbuilder.dto.request.AttendanceRequest;
import com.org.careerbuilder.dto.request.BulkAttendanceRequest;
import com.org.careerbuilder.dto.response.AttendanceRecordResponse;
import com.org.careerbuilder.dto.response.AttendanceSummaryResponse;
import com.org.careerbuilder.dto.response.ClassAttendanceReportResponse;
import com.org.careerbuilder.dto.response.BulkAttendanceResponse;
import com.org.careerbuilder.service.AttendanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 📋 Attendance Controller - Manages all attendance operations
 * Endpoints for marking, updating, retrieving attendance records
 * and generating attendance reports
 */
@Slf4j
@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    // ===== INDIVIDUAL ATTENDANCE OPERATIONS =====

    /**
     * Mark attendance for a student
     * POST /api/attendance/mark
     */
    @PostMapping("/mark")
    public ResponseEntity<Map<String, Object>> markAttendance(
            @Valid @RequestBody AttendanceRequest request
    ) {
        log.info("Marking attendance for student: {} on date: {}", 
                 request.getStudentId(), request.getDate());

        try {
            AttendanceRecordResponse response = attendanceService.markAttendance(request);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Attendance marked successfully");
            result.put("data", response);

            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (Exception ex) {
            log.error("Error marking attendance", ex);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Failed to mark attendance");
            error.put("error", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * Update existing attendance record
     * PUT /api/attendance/{attendanceId}
     */
    @PutMapping("/{attendanceId}")
    public ResponseEntity<Map<String, Object>> updateAttendance(
            @PathVariable Long attendanceId,
            @Valid @RequestBody AttendanceRequest request
    ) {
        log.info("Updating attendance record: {}", attendanceId);

        try {
            AttendanceRecordResponse response = attendanceService.updateAttendance(attendanceId, request);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Attendance updated successfully");
            result.put("data", response);

            return ResponseEntity.ok(result);
        } catch (Exception ex) {
            log.error("Error updating attendance", ex);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Failed to update attendance");
            error.put("error", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * Get attendance record by ID
     * GET /api/attendance/{attendanceId}
     */
    @GetMapping("/{attendanceId}")
    public ResponseEntity<Map<String, Object>> getAttendanceById(
            @PathVariable Long attendanceId
    ) {
        log.info("Fetching attendance record: {}", attendanceId);

        try {
            AttendanceRecordResponse response = attendanceService.getAttendanceById(attendanceId);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Attendance record retrieved successfully");
            result.put("data", response);

            return ResponseEntity.ok(result);
        } catch (Exception ex) {
            log.error("Error fetching attendance", ex);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Failed to fetch attendance record");
            error.put("error", ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    /**
     * Delete attendance record
     * DELETE /api/attendance/{attendanceId}
     */
    @DeleteMapping("/{attendanceId}")
    public ResponseEntity<Map<String, Object>> deleteAttendance(
            @PathVariable Long attendanceId
    ) {
        log.info("Deleting attendance record: {}", attendanceId);

        try {
            attendanceService.deleteAttendance(attendanceId);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Attendance record deleted successfully");

            return ResponseEntity.ok(result);
        } catch (Exception ex) {
            log.error("Error deleting attendance", ex);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Failed to delete attendance record");
            error.put("error", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    // ===== BULK OPERATIONS =====

    /**
     * Mark attendance for multiple students in bulk
     * POST /api/attendance/bulk-mark
     */
    @PostMapping("/bulk-mark")
    public ResponseEntity<Map<String, Object>> bulkMarkAttendance(
            @Valid @RequestBody BulkAttendanceRequest request
    ) {
        log.info("Bulk marking attendance for {} students", request.getRecords().size());

        try {
            BulkAttendanceResponse response = attendanceService.bulkMarkAttendance(request);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Bulk attendance marked successfully");
            result.put("data", response);

            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (Exception ex) {
            log.error("Error in bulk attendance marking", ex);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Failed to mark bulk attendance");
            error.put("error", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    // ===== STUDENT ATTENDANCE RETRIEVAL =====

    /**
     * Get attendance records for a student
     * GET /api/attendance/student/{studentId}
     */
    @GetMapping("/student/{studentId}")
    public ResponseEntity<Map<String, Object>> getStudentAttendance(
            @PathVariable Long studentId,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        log.info("Fetching attendance for student: {}", studentId);

        try {
            LocalDate from = fromDate != null ? LocalDate.parse(fromDate) : LocalDate.now().minusMonths(1);
            LocalDate to = toDate != null ? LocalDate.parse(toDate) : LocalDate.now();
            
            Pageable pageable = PageRequest.of(page, size);
            Page<AttendanceRecordResponse> records = attendanceService.getStudentAttendance(
                    studentId, from, to, pageable
            );

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Student attendance retrieved successfully");
            result.put("data", records.getContent());
            result.put("page_info", Map.of(
                    "current_page", records.getNumber(),
                    "page_size", records.getSize(),
                    "total_elements", records.getTotalElements(),
                    "total_pages", records.getTotalPages()
            ));

            return ResponseEntity.ok(result);
        } catch (Exception ex) {
            log.error("Error fetching student attendance", ex);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Failed to fetch student attendance");
            error.put("error", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * Get attendance summary for a student
     * GET /api/attendance/student/{studentId}/summary
     */
    @GetMapping("/student/{studentId}/summary")
    public ResponseEntity<Map<String, Object>> getStudentAttendanceSummary(
            @PathVariable Long studentId,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate
    ) {
        log.info("Fetching attendance summary for student: {}", studentId);

        try {
            LocalDate from = fromDate != null ? LocalDate.parse(fromDate) : LocalDate.now().withDayOfMonth(1);
            LocalDate to = toDate != null ? LocalDate.parse(toDate) : LocalDate.now();
            
            AttendanceSummaryResponse summary = attendanceService.getStudentAttendanceSummary(
                    studentId, from, to
            );

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Attendance summary retrieved successfully");
            result.put("data", summary);

            return ResponseEntity.ok(result);
        } catch (Exception ex) {
            log.error("Error fetching attendance summary", ex);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Failed to fetch attendance summary");
            error.put("error", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    // ===== CLASS ATTENDANCE OPERATIONS =====

    /**
     * Get attendance records for a class
     * GET /api/attendance/class
     */
    @GetMapping("/class")
    public ResponseEntity<Map<String, Object>> getClassAttendance(
            @RequestParam String className,
            @RequestParam String section,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        log.info("Fetching attendance for class: {} section: {}", className, section);

        try {
            LocalDate from = fromDate != null ? LocalDate.parse(fromDate) : LocalDate.now().minusMonths(1);
            LocalDate to = toDate != null ? LocalDate.parse(toDate) : LocalDate.now();
            
            Pageable pageable = PageRequest.of(page, size);
            Page<AttendanceRecordResponse> records = attendanceService.getClassAttendance(
                    className, section, from, to, pageable
            );

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Class attendance retrieved successfully");
            result.put("data", records.getContent());
            result.put("page_info", Map.of(
                    "current_page", records.getNumber(),
                    "page_size", records.getSize(),
                    "total_elements", records.getTotalElements(),
                    "total_pages", records.getTotalPages()
            ));

            return ResponseEntity.ok(result);
        } catch (Exception ex) {
            log.error("Error fetching class attendance", ex);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Failed to fetch class attendance");
            error.put("error", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * Get attendance report for a class on a specific date
     * GET /api/attendance/class/report
     */
    @GetMapping("/class/report")
    public ResponseEntity<Map<String, Object>> getClassAttendanceReport(
            @RequestParam String className,
            @RequestParam String section,
            @RequestParam String date
    ) {
        log.info("Generating attendance report for class: {} section: {} on date: {}", 
                 className, section, date);

        try {
            LocalDate reportDate = LocalDate.parse(date);
            ClassAttendanceReportResponse report = attendanceService.getClassAttendanceReport(
                    className, section, reportDate
            );

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Class attendance report retrieved successfully");
            result.put("data", report);

            return ResponseEntity.ok(result);
        } catch (Exception ex) {
            log.error("Error generating class attendance report", ex);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Failed to generate class attendance report");
            error.put("error", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * Get school-wide attendance report for a specific date
     * GET /api/attendance/school/report
     */
    @GetMapping("/school/report")
    public ResponseEntity<Map<String, Object>> getSchoolAttendanceReport(
            @RequestParam Long schoolId,
            @RequestParam String date
    ) {
        log.info("Generating school attendance report for date: {}", date);

        try {
            LocalDate reportDate = LocalDate.parse(date);
            List<ClassAttendanceReportResponse> reports = attendanceService.getSchoolAttendanceReport(
                    schoolId, reportDate
            );

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "School attendance report retrieved successfully");
            result.put("data", reports);
            result.put("total_classes", reports.size());

            return ResponseEntity.ok(result);
        } catch (Exception ex) {
            log.error("Error generating school attendance report", ex);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Failed to generate school attendance report");
            error.put("error", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    // ===== STATISTICS AND ANALYTICS =====

    /**
     * Get attendance percentage for a student
     * GET /api/attendance/student/{studentId}/percentage
     */
    @GetMapping("/student/{studentId}/percentage")
    public ResponseEntity<Map<String, Object>> getAttendancePercentage(
            @PathVariable Long studentId,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate
    ) {
        log.info("Fetching attendance percentage for student: {}", studentId);

        try {
            LocalDate from = fromDate != null ? LocalDate.parse(fromDate) : LocalDate.now().withDayOfMonth(1);
            LocalDate to = toDate != null ? LocalDate.parse(toDate) : LocalDate.now();
            
            int percentage = attendanceService.getAttendancePercentage(studentId, from, to);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Attendance percentage retrieved successfully");
            result.put("data", Map.of(
                    "student_id", studentId,
                    "attendance_percentage", percentage,
                    "from_date", from,
                    "to_date", to
            ));

            return ResponseEntity.ok(result);
        } catch (Exception ex) {
            log.error("Error fetching attendance percentage", ex);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Failed to fetch attendance percentage");
            error.put("error", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * Get attendance statistics for a student
     * GET /api/attendance/student/{studentId}/statistics
     */
    @GetMapping("/student/{studentId}/statistics")
    public ResponseEntity<Map<String, Object>> getAttendanceStatistics(
            @PathVariable Long studentId,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate
    ) {
        log.info("Fetching attendance statistics for student: {}", studentId);

        try {
            LocalDate from = fromDate != null ? LocalDate.parse(fromDate) : LocalDate.now().withDayOfMonth(1);
            LocalDate to = toDate != null ? LocalDate.parse(toDate) : LocalDate.now();
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("student_id", studentId);
            stats.put("total_present", attendanceService.getTotalPresent(studentId, from, to));
            stats.put("total_absent", attendanceService.getTotalAbsent(studentId, from, to));
            stats.put("total_leave", attendanceService.getTotalLeave(studentId, from, to));
            stats.put("attendance_percentage", attendanceService.getAttendancePercentage(studentId, from, to));
            stats.put("from_date", from);
            stats.put("to_date", to);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Attendance statistics retrieved successfully");
            result.put("data", stats);

            return ResponseEntity.ok(result);
        } catch (Exception ex) {
            log.error("Error fetching attendance statistics", ex);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Failed to fetch attendance statistics");
            error.put("error", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
}

