package com.org.careerbuilder.service.impl;

import com.org.careerbuilder.dto.request.LeaveRequestDTO;
import com.org.careerbuilder.dto.response.LeaveRequestResponse;
import com.org.careerbuilder.exceptions.ResourceNotFoundException;
import com.org.careerbuilder.models.LeaveRequest;
import com.org.careerbuilder.models.Student;
import com.org.careerbuilder.models.enums.LeaveStatus;
import com.org.careerbuilder.repository.LeaveRequestRepository;
import com.org.careerbuilder.repository.StudentRepository;
import com.org.careerbuilder.service.LeaveRequestService;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service implementation for Leave Request operations
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class LeaveRequestServiceImpl implements LeaveRequestService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final StudentRepository studentRepository;

    @Override
    public LeaveRequestResponse applyForLeave(LeaveRequestDTO requestDTO) {
        log.info("Applying for leave for student: {} from {} to {}", 
                 requestDTO.getStudentId(), requestDTO.getFromDate(), requestDTO.getToDate());

        Student student = studentRepository.findById(requestDTO.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + requestDTO.getStudentId()));

        LeaveRequest leaveRequest = LeaveRequest.builder()
                .student(student)
                .fromDate(requestDTO.getFromDate())
                .toDate(requestDTO.getToDate())
                .leaveType(requestDTO.getLeaveType())
                .reason(requestDTO.getReason())
                .isHalfDay(requestDTO.getIsHalfDay() != null ? requestDTO.getIsHalfDay() : false)
                .halfDayPeriod(requestDTO.getHalfDayPeriod())
                .status(LeaveStatus.APPLIED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        LeaveRequest saved = leaveRequestRepository.save(leaveRequest);
        log.info("Leave request created with ID: {}", saved.getId());

        return convertToResponse(saved, student);
    }

    @Override
    public LeaveRequestResponse getLeaveRequestById(Long leaveId) {
        log.info("Fetching leave request: {}", leaveId);

        LeaveRequest leaveRequest = leaveRequestRepository.findById(leaveId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found with ID: " + leaveId));

        return convertToResponse(leaveRequest, leaveRequest.getStudent());
    }

    @Override
    public Page<LeaveRequestResponse> getStudentLeaveRequests(Long studentId, Pageable pageable) {
        log.info("Fetching leave requests for student: {}", studentId);

        if (!studentRepository.existsById(studentId)) {
            throw new ResourceNotFoundException("Student not found with ID: " + studentId);
        }

        Page<LeaveRequest> leaveRequests = leaveRequestRepository.findByStudent_IdOrderByFromDateDesc(studentId, pageable);
        return leaveRequests.map(lr -> convertToResponse(lr, lr.getStudent()));
    }

    @Override
    public Page<LeaveRequestResponse> getStudentLeaveRequestsByStatus(Long studentId, LeaveStatus status, Pageable pageable) {
        log.info("Fetching leave requests for student: {} with status: {}", studentId, status);

        if (!studentRepository.existsById(studentId)) {
            throw new ResourceNotFoundException("Student not found with ID: " + studentId);
        }

        Page<LeaveRequest> leaveRequests = leaveRequestRepository.findByStudent_IdAndStatusOrderByFromDateDesc(studentId, status, pageable);
        return leaveRequests.map(lr -> convertToResponse(lr, lr.getStudent()));
    }

    @Override
    public List<LeaveRequestResponse> getUpcomingLeaveRequests(Long studentId) {
        log.info("Fetching upcoming leave requests for student: {}", studentId);

        List<LeaveRequest> upcomingLeaves = leaveRequestRepository.findUpcomingLeaveRequests(studentId);
        return upcomingLeaves.stream()
                .map(lr -> convertToResponse(lr, lr.getStudent()))
                .collect(Collectors.toList());
    }

    @Override
    public LeaveBalanceResponse getLeaveBalance(Long studentId) {
        log.info("Calculating leave balance for student: {}", studentId);

        if (!studentRepository.existsById(studentId)) {
            throw new ResourceNotFoundException("Student not found with ID: " + studentId);
        }

        // Use direct DB count queries instead of loading all records into memory
        long approvedLeaves = leaveRequestRepository.countByStudent_IdAndStatus(studentId, LeaveStatus.APPROVED);
        long pendingLeaves  = leaveRequestRepository.countByStudent_IdAndStatus(studentId, LeaveStatus.APPLIED);

        // Default total leave balance (assuming standard school policy)
        int totalLeaveBalance = 20;
        int usedLeaves = (int) approvedLeaves;
        int remainingLeaves = totalLeaveBalance - usedLeaves;
        int pending = (int) pendingLeaves;

        return new LeaveBalanceResponse() {
            @Override public Integer getTotalLeaveBalance() { return totalLeaveBalance; }
            @Override public Integer getUsedLeaves()        { return usedLeaves; }
            @Override public Integer getRemainingLeaves()   { return remainingLeaves; }
            @Override public Integer getPendingRequests()   { return pending; }
        };
    }

    @Override
    public LeaveRequestResponse cancelLeaveRequest(Long leaveId) {
        log.info("Cancelling leave request: {}", leaveId);

        LeaveRequest leaveRequest = leaveRequestRepository.findById(leaveId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found with ID: " + leaveId));

        if (leaveRequest.getStatus() != LeaveStatus.APPLIED) {
            throw new IllegalStateException("Can only cancel leave requests with APPLIED status");
        }

        leaveRequest.setStatus(LeaveStatus.CANCELLED);
        leaveRequest.setUpdatedAt(LocalDateTime.now());

        LeaveRequest updated = leaveRequestRepository.save(leaveRequest);
        return convertToResponse(updated, updated.getStudent());
    }

    @Override
    public LeaveRequestResponse updateLeaveStatus(Long leaveId, LeaveStatus status, String rejectionReason) {
        log.info("Updating leave request {} to status: {}", leaveId, status);

        LeaveRequest leaveRequest = leaveRequestRepository.findById(leaveId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found with ID: " + leaveId));

        leaveRequest.setStatus(status);
        leaveRequest.setApprovedOn(LocalDateTime.now());
        leaveRequest.setApprovedBy("Admin"); // This should come from context

        if (status == LeaveStatus.REJECTED) {
            leaveRequest.setRejectionReason(rejectionReason);
        }

        leaveRequest.setUpdatedAt(LocalDateTime.now());

        LeaveRequest updated = leaveRequestRepository.save(leaveRequest);
        return convertToResponse(updated, updated.getStudent());
    }

    @Override
    public void deleteLeaveRequest(Long leaveId) {
        log.info("Deleting leave request: {}", leaveId);

        if (!leaveRequestRepository.existsById(leaveId)) {
            throw new ResourceNotFoundException("Leave request not found with ID: " + leaveId);
        }
        leaveRequestRepository.deleteById(leaveId);
    }

    @Override
    public LeavePreviewResponse getLeavePreview(Long studentId) {
        log.info("Fetching leave preview for student: {}", studentId);

        if (!studentRepository.existsById(studentId)) {
            throw new ResourceNotFoundException("Student not found with ID: " + studentId);
        }

        List<LeaveRequest> upcomingLeaves = leaveRequestRepository.findUpcomingLeaveRequests(studentId);
        int upcomingCount = (int) upcomingLeaves.stream()
                .filter(lr -> lr.getStatus() == LeaveStatus.APPROVED || lr.getStatus() == LeaveStatus.APPLIED)
                .count();

        String nextLeaveText = "No upcoming leave";
        if (!upcomingLeaves.isEmpty()) {
            LeaveRequest nextLeave = upcomingLeaves.get(0);
            nextLeaveText = nextLeave.getLeaveType() + " from " + nextLeave.getFromDate() + " to " + nextLeave.getToDate();
        }

        List<LeaveRequest> recentLeaves = leaveRequestRepository
                .findByStudent_IdOrderByFromDateDesc(studentId, PageRequest.of(0, 3))
                .getContent();

        List<LeaveRequestResponse> recentResponses = recentLeaves.stream()
                .map(lr -> convertToResponse(lr, lr.getStudent()))
                .collect(Collectors.toList());

        final String finalNextLeaveText = nextLeaveText;
        return new LeavePreviewResponse() {
            @Override
            public Integer getUpcomingLeaves() {
                return upcomingCount;
            }

            @Override
            public String getNextLeaveText() {
                return finalNextLeaveText;
            }

            @Override
            public List<LeaveRequestResponse> getRecentLeaves() {
                return recentResponses;
            }
        };
    }

    private LeaveRequestResponse convertToResponse(LeaveRequest leaveRequest, Student student) {
        int durationDays = calculateDurationInDays(leaveRequest);

        return LeaveRequestResponse.builder()
                .id(leaveRequest.getId())
                .studentId(student.getId())
                .studentName(student.getFirstName() + " " + student.getLastName())
                .leaveType(leaveRequest.getLeaveType())
                .fromDate(leaveRequest.getFromDate())
                .toDate(leaveRequest.getToDate())
                .reason(leaveRequest.getReason())
                .status(leaveRequest.getStatus())
                .isHalfDay(leaveRequest.getIsHalfDay())
                .halfDayPeriod(leaveRequest.getHalfDayPeriod())
                .durationDays(durationDays)
                .className(student.getClassName())
                .section(student.getSection())
                .appliedOn(leaveRequest.getCreatedAt())
                .approvedOn(leaveRequest.getApprovedOn())
                .approvedBy(leaveRequest.getApprovedBy())
                .rejectionReason(leaveRequest.getRejectionReason())
                .createdAt(leaveRequest.getCreatedAt())
                .updatedAt(leaveRequest.getUpdatedAt())
                .build();
    }

    private int calculateDurationInDays(LeaveRequest leaveRequest) {
        if (leaveRequest.getIsHalfDay()) {
            return 1;  // Half day counts as 0.5 but we'll show as 1 for simplicity
        }
        return (int) java.time.temporal.ChronoUnit.DAYS.between(
                leaveRequest.getFromDate(),
                leaveRequest.getToDate()
        ) + 1;
    }
}
