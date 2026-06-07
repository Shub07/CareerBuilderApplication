package com.org.careerbuilder.service;

import com.org.careerbuilder.dto.request.AdminQuickActionRequests;
import com.org.careerbuilder.dto.response.AdminDashboardDtos;
import com.org.careerbuilder.dto.response.AdminOperationResponses;

import java.util.List;

public interface AdminApprovalService {

    AdminOperationResponses.ApprovalListResponse listPending(Long schoolId);

    AdminDashboardDtos.PendingApprovalRow decide(String compositeId, AdminQuickActionRequests.ApprovalDecisionRequest request);
}
