package com.org.careerbuilder.controller;

import com.org.careerbuilder.dto.request.LeaveRequestDTO;
import com.org.careerbuilder.dto.response.LeaveRequestResponse;
import com.org.careerbuilder.models.enums.LeaveStatus;
import com.org.careerbuilder.service.LeaveRequestService;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 📋 Leave Request Controller - Manages all leave request operations
 * Endpoints for applying, tracking, and managing leave requests
 */
@Slf4j
@RestController
@RequestMapping("/api/leave-requests")
@RequiredArgsConstructor
public class LeaveRequestController {

    private final LeaveRequestService leaveRequestService;

    // ===== LEAVE REQUEST OPERATIONS =====

    /**
     * Apply for a new leave request
     * POST /api/leave-requests/apply
     */
    @PostMapping("/apply")
    public ResponseEntity<Map<String, Object>> applyForLeave(
            @Valid @RequestBody LeaveRequestDTO requestDTO
    ) {
        log.info("Applying for leave for student: {}", requestDTO.getStudentId());

        try {
            LeaveRequestResponse response = leaveRequestService.applyForLeave(requestDTO);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Leave request submitted successfully");
            result.put("data", response);

            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (Exception ex) {
            log.error("Error applying for leave", ex);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Failed to apply for leave");
            error.put("error", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * Get leave request by ID
     * GET /api/leave-requests/{leaveId}
     */
    @GetMapping("/{leaveId}")
    public ResponseEntity<Map<String, Object>> getLeaveRequestById(
            @PathVariable Long leaveId
    ) {
        log.info("Fetching leave request: {}", leaveId);

        try {
            LeaveRequestResponse response = leaveRequestService.getLeaveRequestById(leaveId);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Leave request retrieved successfully");
            result.put("data", response);

            return ResponseEntity.ok(result);
        } catch (Exception ex) {
            log.error("Error fetching leave request", ex);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Failed to fetch leave request");
            error.put("error", ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    /**
     * Get all leave requests for a student (paginated)
     * GET /api/leave-requests/student/{studentId}?page=0&size=10
     */
    @GetMapping("/student/{studentId}")
    public ResponseEntity<Map<String, Object>> getStudentLeaveRequests(
            @PathVariable Long studentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        log.info("Fetching leave requests for student: {}", studentId);

        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<LeaveRequestResponse> responses = leaveRequestService.getStudentLeaveRequests(studentId, pageable);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Leave requests retrieved successfully");
            result.put("data", responses.getContent());
            result.put("currentPage", responses.getNumber());
            result.put("totalPages", responses.getTotalPages());
            result.put("totalElements", responses.getTotalElements());

            return ResponseEntity.ok(result);
        } catch (Exception ex) {
            log.error("Error fetching leave requests", ex);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Failed to fetch leave requests");
            error.put("error", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * Get leave requests by status for a student
     * GET /api/leave-requests/student/{studentId}/status/{status}?page=0&size=10
     */
    @GetMapping("/student/{studentId}/status/{status}")
    public ResponseEntity<Map<String, Object>> getStudentLeaveRequestsByStatus(
            @PathVariable Long studentId,
            @PathVariable LeaveStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        log.info("Fetching leave requests for student: {} with status: {}", studentId, status);

        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<LeaveRequestResponse> responses = leaveRequestService.getStudentLeaveRequestsByStatus(studentId, status, pageable);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Leave requests retrieved successfully");
            result.put("data", responses.getContent());
            result.put("currentPage", responses.getNumber());
            result.put("totalPages", responses.getTotalPages());
            result.put("totalElements", responses.getTotalElements());

            return ResponseEntity.ok(result);
        } catch (Exception ex) {
            log.error("Error fetching leave requests by status", ex);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Failed to fetch leave requests");
            error.put("error", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * Get upcoming leave requests for a student
     * GET /api/leave-requests/student/{studentId}/upcoming
     */
    @GetMapping("/student/{studentId}/upcoming")
    public ResponseEntity<Map<String, Object>> getUpcomingLeaveRequests(
            @PathVariable Long studentId
    ) {
        log.info("Fetching upcoming leave requests for student: {}", studentId);

        try {
            List<LeaveRequestResponse> responses = leaveRequestService.getUpcomingLeaveRequests(studentId);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Upcoming leave requests retrieved successfully");
            result.put("data", responses);

            return ResponseEntity.ok(result);
        } catch (Exception ex) {
            log.error("Error fetching upcoming leave requests", ex);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Failed to fetch upcoming leave requests");
            error.put("error", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * Get leave balance/summary for a student
     * GET /api/leave-requests/student/{studentId}/balance
     */
    @GetMapping("/student/{studentId}/balance")
    public ResponseEntity<Map<String, Object>> getLeaveBalance(
            @PathVariable Long studentId
    ) {
        log.info("Fetching leave balance for student: {}", studentId);

        try {
            LeaveRequestService.LeaveBalanceResponse balance = leaveRequestService.getLeaveBalance(studentId);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Leave balance retrieved successfully");
            result.put("data", new HashMap<String, Object>() {{
                put("totalLeaveBalance", balance.getTotalLeaveBalance());
                put("usedLeaves", balance.getUsedLeaves());
                put("remainingLeaves", balance.getRemainingLeaves());
                put("pendingRequests", balance.getPendingRequests());
            }});

            return ResponseEntity.ok(result);
        } catch (Exception ex) {
            log.error("Error fetching leave balance", ex);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Failed to fetch leave balance");
            error.put("error", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * Get leave preview data for dashboard
     * GET /api/leave-requests/student/{studentId}/preview
     */
    @GetMapping("/student/{studentId}/preview")
    public ResponseEntity<Map<String, Object>> getLeavePreview(
            @PathVariable Long studentId
    ) {
        log.info("Fetching leave preview for student: {}", studentId);

        try {
            LeaveRequestService.LeavePreviewResponse preview = leaveRequestService.getLeavePreview(studentId);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Leave preview retrieved successfully");
            result.put("data", preview);

            return ResponseEntity.ok(result);
        } catch (Exception ex) {
            log.error("Error fetching leave preview", ex);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Failed to fetch leave preview");
            error.put("error", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * Cancel a leave request
     * PUT /api/leave-requests/{leaveId}/cancel
     */
    @PutMapping("/{leaveId}/cancel")
    public ResponseEntity<Map<String, Object>> cancelLeaveRequest(
            @PathVariable Long leaveId
    ) {
        log.info("Cancelling leave request: {}", leaveId);

        try {
            LeaveRequestResponse response = leaveRequestService.cancelLeaveRequest(leaveId);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Leave request cancelled successfully");
            result.put("data", response);

            return ResponseEntity.ok(result);
        } catch (Exception ex) {
            log.error("Error cancelling leave request", ex);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Failed to cancel leave request");
            error.put("error", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * Update leave request status (Admin only)
     * PUT /api/leave-requests/{leaveId}/status
     */
    @PutMapping("/{leaveId}/status")
    public ResponseEntity<Map<String, Object>> updateLeaveStatus(
            @PathVariable Long leaveId,
            @RequestParam LeaveStatus status,
            @RequestParam(required = false) String rejectionReason
    ) {
        log.info("Updating leave request {} to status: {}", leaveId, status);

        try {
            LeaveRequestResponse response = leaveRequestService.updateLeaveStatus(leaveId, status, rejectionReason);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Leave request status updated successfully");
            result.put("data", response);

            return ResponseEntity.ok(result);
        } catch (Exception ex) {
            log.error("Error updating leave status", ex);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Failed to update leave status");
            error.put("error", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * Delete leave request (Admin only)
     * DELETE /api/leave-requests/{leaveId}
     */
    @DeleteMapping("/{leaveId}")
    public ResponseEntity<Map<String, Object>> deleteLeaveRequest(
            @PathVariable Long leaveId
    ) {
        log.info("Deleting leave request: {}", leaveId);

        try {
            leaveRequestService.deleteLeaveRequest(leaveId);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Leave request deleted successfully");

            return ResponseEntity.ok(result);
        } catch (Exception ex) {
            log.error("Error deleting leave request", ex);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Failed to delete leave request");
            error.put("error", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
}
