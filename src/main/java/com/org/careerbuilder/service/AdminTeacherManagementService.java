package com.org.careerbuilder.service;

import com.org.careerbuilder.dto.request.AdminQuickActionRequests;
import com.org.careerbuilder.dto.request.AdminTeacherRequests;
import com.org.careerbuilder.dto.response.AdminOperationResponses;
import com.org.careerbuilder.dto.response.AdminTeacherDtos;
import org.springframework.web.multipart.MultipartFile;

public interface AdminTeacherManagementService {

    AdminTeacherDtos.TeacherStats getStats(Long schoolId);

    AdminTeacherDtos.AcademicYearsResponse getAcademicYears(Long schoolId);

    AdminTeacherDtos.TeacherFilterOptions getFilterOptions(Long schoolId);

    AdminTeacherDtos.TeacherSectionResponse listTeachers(AdminTeacherRequests.TeacherListQuery query);

    AdminTeacherDtos.TeacherSectionResponse listTeachers(
            Long schoolId, String academicYear, String q, Long subjectId,
            String status, String statsFilter, int page, int size);

    AdminOperationResponses.BulkActionResponse bulkChangeStatus(AdminTeacherRequests.BulkStatusRequest request);

    AdminOperationResponses.BulkActionResponse bulkDelete(AdminTeacherRequests.BulkDeleteRequest request);

    AdminTeacherDtos.ExportFieldCatalogResponse getExportFieldCatalog();

    AdminTeacherDtos.ExportPreviewResponse previewExport(AdminTeacherRequests.ExportRequest request);

    AdminTeacherExportResult exportTeachers(AdminTeacherRequests.ExportRequest request);

    AdminTeacherDtos.TeacherDetailResponse getTeacherDetail(Long schoolId, Long facultyId);

    AdminTeacherDtos.TeacherProfileSummaryResponse getProfileSummary(Long schoolId, Long facultyId);

    AdminTeacherDtos.EditFormResponse getEditForm(Long schoolId, Long facultyId);

    AdminTeacherDtos.DeletePreviewResponse getDeletePreview(Long schoolId, Long facultyId);

    AdminTeacherDtos.DeleteTeacherResponse deleteTeacher(
            Long schoolId, Long facultyId, String performedBy);

    AdminTeacherDtos.AllocateClassesFormResponse getAllocateClassesForm(Long schoolId, Long facultyId);

    AdminTeacherDtos.AllocateClassesResponse allocateClasses(
            Long facultyId, AdminTeacherRequests.AllocateClassesRequest request);

    AdminTeacherDtos.TeacherCreatedResponse createTeacher(
            AdminTeacherRequests.CreateTeacherRequest request,
            MultipartFile photo,
            MultipartFile resume,
            MultipartFile idProof);

    AdminTeacherDtos.TeacherDetailResponse updateTeacher(
            Long facultyId,
            AdminTeacherRequests.UpdateTeacherRequest request,
            MultipartFile photo,
            MultipartFile resume,
            MultipartFile idProof,
            MultipartFile certificates,
            MultipartFile experienceLetters);

    AdminTeacherDtos.ActionResponse updateStatus(Long facultyId, AdminTeacherRequests.UpdateStatusRequest request);

    AdminTeacherDtos.AssignmentCreatedResponse addAssignment(
            Long facultyId, AdminTeacherRequests.AddAssignmentRequest request);

    AdminTeacherDtos.ActionResponse removeAssignment(Long schoolId, Long facultyId, Long assignmentId, String performedBy);

    /** Dashboard quick-add — backward compatible with AdminOperationsController. */
    AdminOperationResponses.TeacherCreatedResponse quickCreate(AdminQuickActionRequests.AddTeacherRequest request);

    record AdminTeacherExportResult(byte[] content, String contentType, String fileName) {
    }
}
