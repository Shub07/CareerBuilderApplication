package com.org.careerbuilder.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

public final class AdminQuickActionRequests {

    private AdminQuickActionRequests() {
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AddTeacherRequest {
        @NotNull
        private Long schoolId;
        @NotBlank
        private String teacherName;
        @NotBlank
        private String subjectName;
        @NotBlank
        @Pattern(regexp = "^[0-9]{10,15}$")
        private String phone;
        @NotBlank
        @Email
        private String email;
        private String photoUrl;
        private String performedBy;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateNoticeRequest {
        @NotNull
        private Long schoolId;
        @NotBlank
        private String title;
        @NotBlank
        private String description;
        private String audience;
        private String category;
        private String attachmentUrl;
        private String performedBy;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateExamRequest {
        @NotNull
        private Long schoolId;
        @NotBlank
        private String examName;
        @NotBlank
        private String className;
        @NotBlank
        private String examType;
        @NotNull
        private LocalDate startDate;
        @NotNull
        private LocalDate endDate;
        private String performedBy;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AddHolidayRequest {
        @NotNull
        private Long schoolId;
        @NotBlank
        private String holidayName;
        @NotNull
        private LocalDate date;
        private String description;
        private String performedBy;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AdmissionEnquiryRequest {
        @NotNull
        private Long schoolId;
        @NotBlank
        private String studentName;
        @NotBlank
        private String classApplying;
        private String parentName;
        @NotBlank
        @Pattern(regexp = "^[0-9]{10,15}$")
        private String phone;
        private String notes;
        private String performedBy;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ApprovalDecisionRequest {
        @NotBlank
        private String kind;
        private String rejectionReason;
        private String performedBy;
    }
}
