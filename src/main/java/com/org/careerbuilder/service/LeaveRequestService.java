package com.org.careerbuilder.service;

import com.org.careerbuilder.dto.request.LeaveRequestDTO;
import com.org.careerbuilder.dto.response.LeaveRequestResponse;
import com.org.careerbuilder.models.enums.LeaveStatus;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service interface for Leave Request operations
 */
public interface LeaveRequestService {

    /**
     * Apply for a new leave request
     */
    LeaveRequestResponse applyForLeave(LeaveRequestDTO requestDTO);

    /**
     * Get leave request by ID
     */
    LeaveRequestResponse getLeaveRequestById(Long leaveId);

    /**
     * Get all leave requests for a student with pagination
     */
    Page<LeaveRequestResponse> getStudentLeaveRequests(Long studentId, Pageable pageable);

    /**
     * Get leave requests by status for a student
     */
    Page<LeaveRequestResponse> getStudentLeaveRequestsByStatus(Long studentId, LeaveStatus status, Pageable pageable);

    /**
     * Get upcoming leave requests for a student
     */
    List<LeaveRequestResponse> getUpcomingLeaveRequests(Long studentId);

    /**
     * Get leave balance/summary for a student
     */
    LeaveBalanceResponse getLeaveBalance(Long studentId);

    /**
     * Cancel a leave request
     */
    LeaveRequestResponse cancelLeaveRequest(Long leaveId);

    /**
     * Update leave request status (Admin only)
     */
    LeaveRequestResponse updateLeaveStatus(Long leaveId, LeaveStatus status, String rejectionReason);

    /**
     * Delete leave request (Admin only)
     */
    void deleteLeaveRequest(Long leaveId);

    /**
     * Get leave preview data for dashboard
     */
    LeavePreviewResponse getLeavePreview(Long studentId);

    /**
     * Nested DTO for leave balance summary
     */
    interface LeaveBalanceResponse {
        Integer getTotalLeaveBalance();
        Integer getUsedLeaves();
        Integer getRemainingLeaves();
        Integer getPendingRequests();
    }

    /**
     * Nested DTO for leave preview
     */
    interface LeavePreviewResponse {
        Integer getUpcomingLeaves();
        String getNextLeaveText();
        List<LeaveRequestResponse> getRecentLeaves();
    }
}
