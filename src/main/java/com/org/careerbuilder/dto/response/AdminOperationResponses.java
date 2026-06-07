package com.org.careerbuilder.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public final class AdminOperationResponses {

    private AdminOperationResponses() {
    }

    public record StudentRegistrationResponse(
            Long studentId,
            String admissionNumber,
            String fullName,
            String className,
            String section,
            Integer rollNumber,
            Long initialFeeId,
            List<String> documentUrls
    ) {
    }

    public record RegistrationMetadataResponse(
            List<String> genders,
            List<String> bloodGroups,
            List<String> religions,
            List<String> categories,
            List<String> studentTypes,
            List<String> documentTypes,
            List<ClassSectionOption> classes
    ) {
    }

    public record ClassSectionOption(
            String className,
            List<String> sections
    ) {
    }

    public record TeacherCreatedResponse(
            Long facultyId,
            String facultyCode,
            String fullName,
            String email
    ) {
    }

    public record GlobalSearchResponse(
            List<SearchHit> results
    ) {
    }

    public record SearchHit(
            String type,
            Long id,
            String title,
            String subtitle,
            String linkHint
    ) {
    }

    public record ApprovalListResponse(
            List<AdminDashboardDtos.PendingApprovalRow> items
    ) {
    }

    public record StudentSectionResponse(
            StudentStats stats,
            List<StudentRow> rows,
            int page,
            int size,
            long totalElements,
            int totalPages,
            List<String> visibleColumns
    ) {
    }

    public record StudentStats(
            long totalStudents,
            long activeStudents,
            long inactiveStudents,
            long newAdmissions
    ) {
    }

    /** Dropdown values for Students list filters (class, section, gender, status, fee). */
    public record StudentFilterOptionsResponse(
            List<ClassSectionOption> classes,
            List<String> genders,
            List<String> accountStatuses,
            List<String> feeStatuses
    ) {
    }

    public record StudentRow(
            Long studentId,
            String fullName,
            String email,
            String photoUrl,
            String registrationNumber,
            Integer rollNumber,
            String className,
            String section,
            String gender,
            String parentName,
            String parentPhone,
            String address,
            String feeStatus,
            String status,
            Double attendancePercent,
            String performanceLevel
    ) {
    }

    public record AcademicYearsResponse(List<String> academicYears, String currentAcademicYear) {
    }

    public record AdvancedFilterOptionsResponse(
            List<TableColumnOption> availableColumns,
            List<String> performanceLevels,
            List<String> feeStatuses,
            int attendanceWindowDays
    ) {
    }

    public record TableColumnOption(String key, String label, boolean defaultVisible) {
    }

    public record TablePreferencesResponse(
            Long schoolId,
            List<String> visibleColumns,
            List<TableColumnOption> availableColumns
    ) {
    }

    public record StudentDetailResponse(
            Long studentId,
            String fullName,
            String email,
            String phone,
            String registrationNumber,
            Integer rollNumber,
            String className,
            String section,
            String gender,
            LocalDateFields dates,
            ParentInfo parent,
            ContactInfo contact,
            String feeStatus,
            String status,
            String academicYear
    ) {
    }

    public record LocalDateFields(
            String dateOfBirth,
            String admissionDate
    ) {
    }

    public record ParentInfo(
            String fatherName,
            String fatherPhone,
            String motherName,
            String motherPhone,
            String fatherOccupation
    ) {
    }

    public record ContactInfo(
            String addressLine1,
            String addressLine2,
            String city,
            String state,
            String pincode,
            String country
    ) {
    }

    public record ExportFieldCatalogResponse(
            Map<String, List<String>> availableFields
    ) {
    }

    public record BulkActionResponse(
            int affectedCount,
            String message
    ) {
    }

    public record BulkTemplateInfoResponse(
            String templateVersion,
            List<String> headers,
            String notes
    ) {
    }

    public record BulkPreviewResponse(
            String previewId,
            int detectedRows,
            int validRows,
            int errorRows,
            List<BulkStudentRowResult> rows
    ) {
    }

    public record BulkStudentRowResult(
            int rowNumber,
            String fullName,
            String className,
            String status,
            String remarks
    ) {
    }

    public record BulkCommitResponse(
            String previewId,
            int registeredCount,
            int skippedCount
    ) {
    }
}
