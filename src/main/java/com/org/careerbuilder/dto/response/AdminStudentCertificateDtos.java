package com.org.careerbuilder.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public final class AdminStudentCertificateDtos {

    private AdminStudentCertificateDtos() {
    }

    public record CertificatesListResponse(
            List<CertificateListRow> certificates,
            List<String> categories,
            int totalElements
    ) {
    }

    public record CertificateListRow(
            Long certificateId,
            String name,
            String category,
            String issueDate,
            String fileSizeLabel,
            String fileName,
            String uploadedBy,
            String status
    ) {
    }

    public record CertificateDetailResponse(
            Long certificateId,
            String name,
            String category,
            String issueDate,
            String fileSizeLabel,
            String fileName,
            String fileUrl,
            String studentName,
            String uploadedBy,
            String status
    ) {
    }

    public record CertificateUploadResponse(Long certificateId, String message) {
    }

    public record BulkCertificatePreviewResponse(
            String previewId,
            int totalRows,
            int validRows,
            int errorRows,
            List<BulkCertificatePreviewRow> rows
    ) {
    }

    public record BulkCertificatePreviewRow(
            int rowNumber,
            String studentName,
            String certificateName,
            String fileName,
            String status,
            String remarks
    ) {
    }

    public record BulkCertificateCommitResponse(
            String previewId,
            int registeredCount,
            int skippedCount,
            String message
    ) {
    }

    public record ActivityLogFeedResponse(
            List<ActivityLogEntry> entries,
            int page,
            int size,
            long totalElements,
            int totalPages,
            boolean hasMore
    ) {
    }

    public record ActivityLogEntry(
            Long logId,
            String logType,
            String title,
            String description,
            String performedBy,
            String actorRole,
            LocalDateTime createdAt,
            String displayTimestamp
    ) {
    }

    public record ActivityLogFilterOptions(List<String> logTypes) {
    }
}
