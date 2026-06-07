package com.org.careerbuilder.service;

import com.org.careerbuilder.dto.request.AdminStudentRequests;
import com.org.careerbuilder.dto.response.AdminOperationResponses;

public interface AdminStudentManagementService {

    AdminOperationResponses.StudentStats getStats(Long schoolId, String academicYear);

    AdminOperationResponses.AcademicYearsResponse getAcademicYears(Long schoolId);

    AdminOperationResponses.StudentFilterOptionsResponse getFilterOptions(Long schoolId);

    AdminOperationResponses.AdvancedFilterOptionsResponse getAdvancedFilterOptions();

    AdminOperationResponses.TablePreferencesResponse getTablePreferences(
            Long schoolId, Long adminUserId, String adminEmail);

    AdminOperationResponses.TablePreferencesResponse saveTablePreferences(
            AdminStudentRequests.SaveTablePreferencesRequest request);

    AdminOperationResponses.StudentSectionResponse listStudents(AdminStudentRequests.StudentListQuery query);

    AdminOperationResponses.StudentDetailResponse getStudentDetail(Long schoolId, Long studentId);

    AdminOperationResponses.StudentDetailResponse updateStudent(
            Long schoolId, Long studentId, AdminStudentRequests.UpdateStudentRequest request);

    AdminOperationResponses.ExportFieldCatalogResponse getExportFieldCatalog();

    AdminStudentExportResult exportStudents(AdminStudentRequests.ExportRequest request);

    AdminOperationResponses.BulkActionResponse bulkChangeStatus(AdminStudentRequests.BulkStatusRequest request);

    AdminOperationResponses.BulkActionResponse bulkPromote(AdminStudentRequests.BulkPromoteRequest request);

    AdminOperationResponses.BulkActionResponse bulkDelete(AdminStudentRequests.BulkDeleteRequest request);

    AdminOperationResponses.BulkActionResponse updateStudentStatus(
            Long schoolId, Long studentId, AdminStudentRequests.UpdateStatusRequest request);

    void deleteStudent(Long schoolId, Long studentId, String performedBy);

    record AdminStudentExportResult(byte[] content, String contentType, String fileName) {
    }
}
