package com.org.careerbuilder.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;
import java.util.Map;

public final class AdminTeacherProfileRequests {

    private AdminTeacherProfileRequests() {
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UploadDocumentRequest {
        @NotNull
        private Long schoolId;
        @NotBlank
        private String documentName;
        @NotBlank
        private String documentType;
        private String uploadedBy;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProfileExportRequest {
        @NotNull
        private Long schoolId;
        @NotBlank
        private String format;
        private Map<String, List<String>> fields;
        private String performedBy;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MarkLeaveRequest {
        @NotNull
        private Long schoolId;
        private String performedBy;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DeactivateRequest {
        @NotNull
        private Long schoolId;
        private String performedBy;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AssignSubjectRequest {
        @NotNull
        private Long schoolId;
        @NotNull
        private Long subjectId;
        private String performedBy;
    }

    /** Approve / reject a teacher leave request from the Attendance &amp; Leave tab. */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReviewLeaveRequest {
        @NotNull
        private Long schoolId;
        /** APPROVE or REJECT. */
        @NotBlank
        private String action;
        private String remarks;
        private String performedBy;
    }
}
