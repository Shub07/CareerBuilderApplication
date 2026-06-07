package com.org.careerbuilder.service;

import com.org.careerbuilder.dto.request.AdminTeacherProfileRequests;
import com.org.careerbuilder.dto.response.AdminTeacherDtos;
import com.org.careerbuilder.dto.response.AdminTeacherProfileDtos;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AdminTeacherProfileService {

    AdminTeacherProfileDtos.FullProfileResponse getFullProfile(Long schoolId, Long facultyId);

    AdminTeacherProfileDtos.BasicDetailsTab getBasicDetailsTab(Long schoolId, Long facultyId);

    AdminTeacherProfileDtos.AllocationsTabResponse getAllocationsTab(Long schoolId, Long facultyId);

    AdminTeacherProfileDtos.AssignmentsTabResponse getAssignmentsTab(Long schoolId, Long facultyId);

    AdminTeacherProfileDtos.AssignmentDetailResponse getAssignmentDetail(
            Long schoolId, Long facultyId, Long assignmentId);

    AdminTeacherProfileDtos.AttendanceLeaveTabResponse getAttendanceLeaveTab(Long schoolId, Long facultyId);

    AdminTeacherDtos.ActionResponse reviewLeave(
            Long facultyId, Long leaveId, AdminTeacherProfileRequests.ReviewLeaveRequest request);

    AdminTeacherProfileDtos.WorkloadTabResponse getWorkloadTab(Long schoolId, Long facultyId);

    AdminTeacherProfileDtos.EmploymentTabResponse getEmploymentTab(Long schoolId, Long facultyId);

    AdminTeacherProfileDtos.ActivityLogTabResponse getActivityLogTab(
            Long schoolId, Long facultyId, int page, int size);

    AdminTeacherProfileDtos.MoreActionsMenuResponse getMoreActionsMenu(Long schoolId, Long facultyId);

    List<AdminTeacherProfileDtos.DocumentTypeOption> getDocumentTypes();

    List<AdminTeacherProfileDtos.DocumentRow> listDocuments(Long schoolId, Long facultyId);

    AdminTeacherProfileDtos.DocumentRow uploadDocument(
            Long facultyId, MultipartFile file, AdminTeacherProfileRequests.UploadDocumentRequest meta);

    void deleteDocument(Long schoolId, Long facultyId, Long documentId);

    byte[] downloadDocument(Long schoolId, Long facultyId, Long documentId);

    /** Download an employment document ("resume" or "contract") from the Employment tab. */
    byte[] downloadEmploymentDocument(Long schoolId, Long facultyId, String kind);

    AdminTeacherProfileDtos.ProfileExportCatalogResponse getProfileExportCatalog();

    AdminTeacherManagementService.AdminTeacherExportResult exportProfile(
            Long facultyId, AdminTeacherProfileRequests.ProfileExportRequest request);

    AdminTeacherDtos.ActionResponse markLeave(Long facultyId, AdminTeacherProfileRequests.MarkLeaveRequest request);

    AdminTeacherDtos.ActionResponse deactivate(Long facultyId, AdminTeacherProfileRequests.DeactivateRequest request);

    AdminTeacherDtos.ActionResponse assignSubject(Long facultyId, AdminTeacherProfileRequests.AssignSubjectRequest request);
}
