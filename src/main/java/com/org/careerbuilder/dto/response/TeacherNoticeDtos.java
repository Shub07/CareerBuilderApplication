package com.org.careerbuilder.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public final class TeacherNoticeDtos {

    private TeacherNoticeDtos() {}

    public record ClassTargetRef(String className, String section) {}

    public record TeacherNoticeResponse(
            Long id,
            String noticeType,
            String status,
            String title,
            String description,
            String sentToLabel,
            int recipientCount,
            List<ClassTargetRef> classTargets,
            List<Long> studentIds,
            String attachmentUrl,
            String attachmentFileName,
            String attachmentFileType,
            Long attachmentFileSize,
            Long publicNoticeId,
            LocalDateTime publishedAt,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {}

    public record ClassOption(
            String className,
            String section,
            String label
    ) {}

    public record StudentOption(
            Long studentId,
            String fullName,
            Integer rollNo,
            String className,
            String section,
            String label
    ) {}

    public record TeacherNoticeFiltersResponse(
            List<ClassOption> classes,
            List<StudentOption> students
    ) {}

    public record AttachmentUploadResponse(
            String fileUrl,
            String fileName,
            String fileType,
            Long fileSize
    ) {}
}
