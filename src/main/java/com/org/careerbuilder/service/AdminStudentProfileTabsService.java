package com.org.careerbuilder.service;

import com.org.careerbuilder.dto.request.AdminStudentRequests;
import com.org.careerbuilder.dto.response.AdminStudentProfileDtos;
import com.org.careerbuilder.service.AdminStudentManagementService.AdminStudentExportResult;

import java.time.LocalDate;

public interface AdminStudentProfileTabsService {

    AdminStudentProfileDtos.AcademicPerformanceResponse getAcademicPerformance(
            Long schoolId, Long studentId, String category, String subjectSearch);

    AdminStudentProfileDtos.PerformanceExportCatalogResponse getPerformanceExportCatalog();

    AdminStudentExportResult exportPerformance(
            Long studentId, AdminStudentRequests.PerformanceExportRequest request);

    AdminStudentProfileDtos.AssignmentDetailResponse getAssignmentDetail(
            Long schoolId, Long studentId, Long assignmentId);

    AdminStudentProfileDtos.DailyActivityTabResponse getDailyActivityTab(
            Long schoolId, Long studentId, String period, LocalDate from, LocalDate to, String search);

    AdminStudentExportResult exportDailyActivityReport(
            Long studentId, AdminStudentRequests.ReportDownloadRequest request);

    AdminStudentProfileDtos.AttendanceSummaryTabResponse getAttendanceSummaryTab(Long schoolId, Long studentId);

    AdminStudentProfileDtos.SubjectAttendanceDetailResponse getSubjectAttendanceDetail(
            Long schoolId, Long studentId, Long subjectId,
            String academicYear, Integer month, String status, String search);

    AdminStudentExportResult exportAttendanceReport(
            Long studentId, Long subjectId, AdminStudentRequests.ReportDownloadRequest request);

    AdminStudentProfileDtos.FeesTabDetailResponse getFeesTabDetail(Long schoolId, Long studentId);

    AdminStudentProfileDtos.PaymentLinkResponse generatePaymentLink(
            Long studentId, AdminStudentRequests.GeneratePaymentLinkRequest request);

    AdminStudentProfileDtos.FeeReminderResponse sendFeeReminder(
            Long studentId, AdminStudentRequests.FeeReminderRequest request);

    AdminStudentExportResult downloadFeeStatement(Long schoolId, Long studentId, String format);

    AdminStudentExportResult downloadFeeReceipt(Long schoolId, Long studentId, Long feeId);
}
