package com.org.careerbuilder.service;

import com.org.careerbuilder.dto.request.AdminStudentRequests;
import com.org.careerbuilder.dto.response.AdminStudentProfileDtos;
import com.org.careerbuilder.service.AdminStudentManagementService.AdminStudentExportResult;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

public interface AdminStudentProfileService {

    AdminStudentProfileDtos.FullProfileResponse getFullProfile(Long schoolId, Long studentId);

    AdminStudentProfileDtos.PerformanceTabResponse getPerformanceTab(
            Long schoolId, Long studentId, String examType, LocalDate from, LocalDate to);

    AdminStudentProfileDtos.AttendanceTabResponse getAttendanceTab(
            Long schoolId, Long studentId, String period, LocalDate from, LocalDate to);

    AdminStudentProfileDtos.FeesTabResponse getFeesTab(Long schoolId, Long studentId);

    AdminStudentProfileDtos.CertificatesTabResponse getCertificatesTab(
            Long schoolId, Long studentId, String academicYear);

    AdminStudentProfileDtos.ActivityLogTabResponse getActivityLogTab(
            Long schoolId, Long studentId, String period, LocalDate from, LocalDate to);

    AdminStudentProfileDtos.StudyActivityTabResponse getStudyActivityTab(
            Long schoolId, Long studentId, String period, LocalDate from, LocalDate to);

    AdminStudentProfileDtos.MessageSentResponse sendMessage(
            Long studentId, AdminStudentRequests.SendMessageRequest request);

    AdminStudentProfileDtos.ActionResponse flagStudent(
            Long studentId, AdminStudentRequests.FlagStudentRequest request);

    AdminStudentProfileDtos.ActionResponse promoteStudent(Long schoolId, Long studentId, String performedBy);

    AdminStudentProfileDtos.ActionResponse transferStudent(
            Long studentId, AdminStudentRequests.TransferStudentRequest request);

    AdminStudentProfileDtos.ActionResponse deactivateStudent(Long schoolId, Long studentId, String performedBy);

    AdminStudentProfileDtos.ActionResponse reactivateStudent(Long schoolId, Long studentId, String performedBy);

    AdminStudentProfileDtos.ActionResponse quickEdit(
            Long studentId, AdminStudentRequests.QuickEditRequest request);

    AdminStudentProfileDtos.DocumentRow uploadDocument(
            Long schoolId, Long studentId, MultipartFile file,
            AdminStudentRequests.UploadStudentDocumentRequest meta);

    void deleteDocument(Long schoolId, Long studentId, Long documentId);

    byte[] downloadDocument(Long schoolId, Long studentId, Long documentId);

    AdminStudentProfileDtos.ProfileExportCatalogResponse getExportCatalog();

    AdminStudentExportResult exportProfile(Long studentId, AdminStudentRequests.ProfileExportRequest request);
}
