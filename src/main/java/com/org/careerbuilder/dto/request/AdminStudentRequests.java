package com.org.careerbuilder.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public final class AdminStudentRequests {

    private AdminStudentRequests() {
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ExportRequest {
        /** Set server-side from JWT. */
        private Long schoolId;
        private List<Long> studentIds;
        private String query;
        private String className;
        private String section;
        private String gender;
        private String status;
        private String feeStatus;
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
    public static class BulkStatusRequest {
        /** Set server-side from JWT. */
        private Long schoolId;
        @NotEmpty
        private List<Long> studentIds;
        @NotBlank
        private String status;
        private String performedBy;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BulkPromoteRequest {
        /** Set server-side from JWT. */
        private Long schoolId;
        @NotEmpty
        private List<Long> studentIds;
        private String performedBy;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BulkDeleteRequest {
        /** Set server-side from JWT. */
        private Long schoolId;
        @NotEmpty
        private List<Long> studentIds;
        private String performedBy;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateStatusRequest {
        @NotBlank
        private String status;
        private String performedBy;
    }

    /** Query for list + advanced filters (also used as POST body). */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StudentListQuery {
        /** Set server-side from JWT. */
        private Long schoolId;
        private String academicYear;
        private String query;
        private String className;
        private String section;
        private String gender;
        private String status;
        /** Single fee status (legacy). */
        private String feeStatus;
        /** Multi-select from advanced filter modal. */
        private List<String> feeStatuses;
        private LocalDate admissionDateFrom;
        private LocalDate admissionDateTo;
        private String performanceLevel;
        private Integer minAttendance;
        private Integer maxAttendance;
        private Long adminUserId;
        private String adminEmail;
        @Builder.Default
        private int page = 0;
        @Builder.Default
        private int size = 10;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SaveTablePreferencesRequest {
        /** Set server-side from JWT. */
        private Long schoolId;
        private Long adminUserId;
        private String adminEmail;
        @NotEmpty
        private List<String> visibleColumns;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SendMessageRequest {
        /** Set server-side from JWT. */
        private Long schoolId;
        @NotBlank
        private String message;
        private String sentBy;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FlagStudentRequest {
        /** Set server-side from JWT. */
        private Long schoolId;
        @NotBlank
        private String reason;
        private String performedBy;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TransferStudentRequest {
        /** Set server-side from JWT. */
        private Long schoolId;
        @NotBlank
        private String targetClassName;
        @NotBlank
        private String targetSection;
        private Integer targetRollNo;
        private String performedBy;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class QuickEditRequest {
        private String fullName;
        private String admissionNumber;
        private String className;
        private String section;
        private String performedBy;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProfileExportRequest {
        /** Set server-side from JWT. */
        private Long schoolId;
        @NotBlank
        private String format;
        private Map<String, List<String>> sections;
        private String examType;
        private LocalDate performanceFrom;
        private LocalDate performanceTo;
        private String activityPeriod;
        private LocalDate activityFrom;
        private LocalDate activityTo;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UploadStudentDocumentRequest {
        private String documentType;
        private String uploadedBy;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateStudentRequest {
        private String firstName;
        private String lastName;
        private String email;
        private String phone;
        private String address;
        private String parentName;
        private String className;
        private String section;
        private Integer rollNo;
        private String gender;
        private String photoUrl;
        private String academicYear;
        private String status;
        private String performedBy;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PerformanceExportRequest {
        /** Set server-side from JWT. */
        private Long schoolId;
        @NotBlank
        private String format;
        private List<String> categories;
        private List<String> fields;
        private String subjectSearch;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReportDownloadRequest {
        /** Set server-side from JWT. */
        private Long schoolId;
        @NotBlank
        private String format;
        private String duration;
        private LocalDate from;
        private LocalDate to;
        private String reportType;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GeneratePaymentLinkRequest {
        /** Set server-side from JWT. */
        private Long schoolId;
        private List<Long> feeIds;
        private String createdBy;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FeeReminderRequest {
        /** Set server-side from JWT. */
        private Long schoolId;
        private List<Long> feeIds;
        private String sentBy;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UploadCertificateRequest {
        /** Set server-side from JWT. */
        private Long schoolId;
        @NotBlank
        private String certificateName;
        @NotBlank
        private String category;
        private String issueDate;
        private String academicYear;
        private String uploadedBy;
    }
}
