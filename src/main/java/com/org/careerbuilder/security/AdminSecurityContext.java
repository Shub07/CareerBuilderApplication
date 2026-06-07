package com.org.careerbuilder.security;

import com.org.careerbuilder.dto.request.AdminExamRequests;
import com.org.careerbuilder.dto.request.AdminFeeRequests;
import com.org.careerbuilder.dto.request.AdminStudentRegistrationRequest;
import com.org.careerbuilder.dto.request.AdminStudentRequests;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Resolves the authenticated admin's tenant ({@code schoolId}) and audit identity
 * from the JWT-backed {@link UserPrincipal}.
 */
public final class AdminSecurityContext {

    private AdminSecurityContext() {
    }

    public static UserPrincipal requirePrincipal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserPrincipal principal)) {
            throw new AccessDeniedException("Authentication required");
        }
        return principal;
    }

    public static Long requireSchoolId() {
        UserPrincipal principal = requirePrincipal();
        if (principal.getSchoolId() == null) {
            throw new AccessDeniedException("Admin account is not linked to a school");
        }
        return principal.getSchoolId();
    }

    public static String performerName() {
        UserPrincipal principal = requirePrincipal();
        if (principal.getEmail() != null && !principal.getEmail().isBlank()) {
            return principal.getEmail();
        }
        if (principal.getUserId() != null) {
            return "user-" + principal.getUserId();
        }
        return "admin";
    }

    public static void stamp(AdminFeeRequests.CreateFeeStructureRequest request) {
        request.setSchoolId(requireSchoolId());
        if (request.getPerformedBy() == null || request.getPerformedBy().isBlank()) {
            request.setPerformedBy(performerName());
        }
    }

    public static void stamp(AdminFeeRequests.UpdateFeeStructureRequest request) {
        request.setSchoolId(requireSchoolId());
        if (request.getPerformedBy() == null || request.getPerformedBy().isBlank()) {
            request.setPerformedBy(performerName());
        }
    }

    public static void stamp(AdminFeeRequests.CollectFeeRequest request) {
        request.setSchoolId(requireSchoolId());
        if (request.getPerformedBy() == null || request.getPerformedBy().isBlank()) {
            request.setPerformedBy(performerName());
        }
    }

    public static void stamp(AdminFeeRequests.RefundFeeRequest request) {
        request.setSchoolId(requireSchoolId());
        if (request.getPerformedBy() == null || request.getPerformedBy().isBlank()) {
            request.setPerformedBy(performerName());
        }
    }

    public static void stamp(AdminFeeRequests.ExportFeeReportRequest request) {
        request.setSchoolId(requireSchoolId());
    }

    public static void stamp(AdminExamRequests.CreateExamRequest request) {
        request.setSchoolId(requireSchoolId());
        if (request.getPerformedBy() == null || request.getPerformedBy().isBlank()) {
            request.setPerformedBy(performerName());
        }
    }

    public static void stamp(AdminExamRequests.UpdateExamRequest request) {
        request.setSchoolId(requireSchoolId());
        if (request.getPerformedBy() == null || request.getPerformedBy().isBlank()) {
            request.setPerformedBy(performerName());
        }
    }

    public static void stamp(AdminExamRequests.CreateScheduleRequest request) {
        request.setSchoolId(requireSchoolId());
        if (request.getPerformedBy() == null || request.getPerformedBy().isBlank()) {
            request.setPerformedBy(performerName());
        }
    }

    public static void stamp(AdminExamRequests.UpdateScheduleRequest request) {
        request.setSchoolId(requireSchoolId());
        if (request.getPerformedBy() == null || request.getPerformedBy().isBlank()) {
            request.setPerformedBy(performerName());
        }
    }

    public static void stamp(AdminExamRequests.BulkAssignStudentsRequest request) {
        request.setSchoolId(requireSchoolId());
        if (request.getPerformedBy() == null || request.getPerformedBy().isBlank()) {
            request.setPerformedBy(performerName());
        }
    }

    public static void stamp(AdminExamRequests.PublishResultsRequest request) {
        request.setSchoolId(requireSchoolId());
        if (request.getPerformedBy() == null || request.getPerformedBy().isBlank()) {
            request.setPerformedBy(performerName());
        }
    }

    public static void stamp(AdminExamRequests.GenerateHallTicketsRequest request) {
        request.setSchoolId(requireSchoolId());
        if (request.getPerformedBy() == null || request.getPerformedBy().isBlank()) {
            request.setPerformedBy(performerName());
        }
    }

    public static void stamp(AdminExamRequests.AssignVenueAllocationRequest request) {
        request.setSchoolId(requireSchoolId());
        if (request.getPerformedBy() == null || request.getPerformedBy().isBlank()) {
            request.setPerformedBy(performerName());
        }
    }

    public static void stamp(AdminExamRequests.UpdateVenueAllocationRequest request) {
        request.setSchoolId(requireSchoolId());
        if (request.getPerformedBy() == null || request.getPerformedBy().isBlank()) {
            request.setPerformedBy(performerName());
        }
    }

    public static void stamp(AdminExamRequests.ApproveSubjectMarksRequest request) {
        request.setSchoolId(requireSchoolId());
        if (request.getPerformedBy() == null || request.getPerformedBy().isBlank()) {
            request.setPerformedBy(performerName());
        }
    }

    public static void stamp(AdminExamRequests.RejectSubjectMarksRequest request) {
        request.setSchoolId(requireSchoolId());
        if (request.getPerformedBy() == null || request.getPerformedBy().isBlank()) {
            request.setPerformedBy(performerName());
        }
    }

    public static void stamp(AdminExamRequests.SaveMarksDraftRequest request) {
        request.setSchoolId(requireSchoolId());
        if (request.getPerformedBy() == null || request.getPerformedBy().isBlank()) {
            request.setPerformedBy(performerName());
        }
    }

    // ─── Admin Students module ─────────────────────────────────────────────────

    public static void stamp(AdminStudentRegistrationRequest request) {
        request.setSchoolId(requireSchoolId());
        if (request.getPerformedBy() == null || request.getPerformedBy().isBlank()) {
            request.setPerformedBy(performerName());
        }
    }

    public static void stamp(AdminStudentRequests.StudentListQuery query) {
        query.setSchoolId(requireSchoolId());
    }

    public static void stamp(AdminStudentRequests.SaveTablePreferencesRequest request) {
        request.setSchoolId(requireSchoolId());
        if (request.getAdminUserId() == null) {
            request.setAdminUserId(requirePrincipal().getUserId());
        }
        if (request.getAdminEmail() == null || request.getAdminEmail().isBlank()) {
            request.setAdminEmail(requirePrincipal().getEmail());
        }
    }

    public static void stamp(AdminStudentRequests.ExportRequest request) {
        request.setSchoolId(requireSchoolId());
        if (request.getPerformedBy() == null || request.getPerformedBy().isBlank()) {
            request.setPerformedBy(performerName());
        }
    }

    public static void stamp(AdminStudentRequests.BulkStatusRequest request) {
        request.setSchoolId(requireSchoolId());
        if (request.getPerformedBy() == null || request.getPerformedBy().isBlank()) {
            request.setPerformedBy(performerName());
        }
    }

    public static void stamp(AdminStudentRequests.BulkPromoteRequest request) {
        request.setSchoolId(requireSchoolId());
        if (request.getPerformedBy() == null || request.getPerformedBy().isBlank()) {
            request.setPerformedBy(performerName());
        }
    }

    public static void stamp(AdminStudentRequests.BulkDeleteRequest request) {
        request.setSchoolId(requireSchoolId());
        if (request.getPerformedBy() == null || request.getPerformedBy().isBlank()) {
            request.setPerformedBy(performerName());
        }
    }

    public static void stamp(AdminStudentRequests.UpdateStatusRequest request) {
        if (request.getPerformedBy() == null || request.getPerformedBy().isBlank()) {
            request.setPerformedBy(performerName());
        }
    }

    public static void stamp(AdminStudentRequests.UpdateStudentRequest request) {
        if (request.getPerformedBy() == null || request.getPerformedBy().isBlank()) {
            request.setPerformedBy(performerName());
        }
    }

    public static void stamp(AdminStudentRequests.SendMessageRequest request) {
        request.setSchoolId(requireSchoolId());
        if (request.getSentBy() == null || request.getSentBy().isBlank()) {
            request.setSentBy(performerName());
        }
    }

    public static void stamp(AdminStudentRequests.FlagStudentRequest request) {
        request.setSchoolId(requireSchoolId());
        if (request.getPerformedBy() == null || request.getPerformedBy().isBlank()) {
            request.setPerformedBy(performerName());
        }
    }

    public static void stamp(AdminStudentRequests.TransferStudentRequest request) {
        request.setSchoolId(requireSchoolId());
        if (request.getPerformedBy() == null || request.getPerformedBy().isBlank()) {
            request.setPerformedBy(performerName());
        }
    }

    public static void stamp(AdminStudentRequests.ProfileExportRequest request) {
        request.setSchoolId(requireSchoolId());
    }

    public static void stamp(AdminStudentRequests.PerformanceExportRequest request) {
        request.setSchoolId(requireSchoolId());
    }

    public static void stamp(AdminStudentRequests.ReportDownloadRequest request) {
        request.setSchoolId(requireSchoolId());
    }

    public static void stamp(AdminStudentRequests.GeneratePaymentLinkRequest request) {
        request.setSchoolId(requireSchoolId());
        if (request.getCreatedBy() == null || request.getCreatedBy().isBlank()) {
            request.setCreatedBy(performerName());
        }
    }

    public static void stamp(AdminStudentRequests.FeeReminderRequest request) {
        request.setSchoolId(requireSchoolId());
        if (request.getSentBy() == null || request.getSentBy().isBlank()) {
            request.setSentBy(performerName());
        }
    }

    public static void stamp(AdminStudentRequests.UploadCertificateRequest request) {
        request.setSchoolId(requireSchoolId());
        if (request.getUploadedBy() == null || request.getUploadedBy().isBlank()) {
            request.setUploadedBy(performerName());
        }
    }

    public static void stamp(AdminStudentRequests.QuickEditRequest request) {
        if (request.getPerformedBy() == null || request.getPerformedBy().isBlank()) {
            request.setPerformedBy(performerName());
        }
    }

    public static void stamp(AdminStudentRequests.UploadStudentDocumentRequest request) {
        if (request.getUploadedBy() == null || request.getUploadedBy().isBlank()) {
            request.setUploadedBy(performerName());
        }
    }

    /** Resolves admin user id for table preferences (JWT user id). */
    public static Long resolveAdminUserId(Long requested) {
        return requested != null ? requested : requirePrincipal().getUserId();
    }

    /** Resolves admin email for table preferences (JWT subject). */
    public static String resolveAdminEmail(String requested) {
        if (requested != null && !requested.isBlank()) {
            return requested;
        }
        return requirePrincipal().getEmail();
    }
}
