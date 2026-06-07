package com.org.careerbuilder.controller;

import com.org.careerbuilder.dto.request.AdminQuickActionRequests;
import com.org.careerbuilder.dto.response.AdminDashboardDtos;
import com.org.careerbuilder.dto.response.AdminOperationResponses;
import com.org.careerbuilder.service.AdminApprovalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/approvals")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000", "http://localhost:5174"},
        allowedHeaders = "*",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.PATCH,
                RequestMethod.DELETE, RequestMethod.OPTIONS})
public class AdminApprovalController {

    private final AdminApprovalService adminApprovalService;

    @GetMapping("/pending")
    public ResponseEntity<AdminOperationResponses.ApprovalListResponse> listPending(
            @RequestParam Long schoolId) {
        return ResponseEntity.ok(adminApprovalService.listPending(schoolId));
    }

    /**
     * Approve or reject a pending item.
     * PATCH /api/admin/approvals/{compositeId}
     * compositeId format: TEACHER_LEAVE:12 | STUDENT_LEAVE:5 | ADMISSION_ENQUIRY:3
     * Body kind: APPROVE | REJECT
     */
    @PatchMapping("/{compositeId}")
    public ResponseEntity<AdminDashboardDtos.PendingApprovalRow> decide(
            @PathVariable String compositeId,
            @Valid @RequestBody AdminQuickActionRequests.ApprovalDecisionRequest request) {
        return ResponseEntity.ok(adminApprovalService.decide(compositeId, request));
    }
}
