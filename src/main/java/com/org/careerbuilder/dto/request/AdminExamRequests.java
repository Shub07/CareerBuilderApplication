package com.org.careerbuilder.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public final class AdminExamRequests {

    private AdminExamRequests() {
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateExamRequest {
        /** Set server-side from JWT. */
        private Long schoolId;
        @NotBlank
        private String examName;
        @NotBlank
        private String examType;
        @NotBlank
        private String className;
        private String section;
        @NotBlank
        private String academicYear;
        @NotNull
        private LocalDate startDate;
        @NotNull
        private LocalDate endDate;
        private String description;
        private String instructions;
        private Long coordinatorId;
        /** When true, status stays DRAFT regardless of dates. */
        @Builder.Default
        private boolean draft = false;
        private String performedBy;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateExamRequest {
        private Long schoolId;
        private String examName;
        private String examType;
        private String className;
        private String section;
        private String academicYear;
        private LocalDate startDate;
        private LocalDate endDate;
        private String description;
        private String instructions;
        private Long coordinatorId;
        private Boolean draft;
        private String performedBy;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateScheduleRequest {
        private Long schoolId;
        @NotNull
        private Long subjectId;
        @NotNull
        private LocalDate scheduledDate;
        @NotNull
        private LocalTime startTime;
        @NotNull
        private LocalTime endTime;
        private String venue;
        private Long venueId;
        private Long invigilatorId;
        private Long assistantInvigilatorId;
        private Integer durationMinutes;
        private LocalTime reportingTime;
        private Integer bufferMinutes;
        private String instructions;
        private String performedBy;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateScheduleRequest {
        private Long schoolId;
        private Long subjectId;
        private LocalDate scheduledDate;
        private LocalTime startTime;
        private LocalTime endTime;
        private String venue;
        private Long venueId;
        private Long invigilatorId;
        private Long assistantInvigilatorId;
        private Integer durationMinutes;
        private LocalTime reportingTime;
        private Integer bufferMinutes;
        private String instructions;
        private String performedBy;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AssignmentPreviewRequest {
        @NotBlank
        private String method;
        private List<String> sections;
        private List<Long> studentIds;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BulkAssignStudentsRequest {
        private Long schoolId;
        @NotBlank
        private String method;
        private List<String> sections;
        private List<Long> studentIds;
        private String defaultPrimaryVenue;
        private String performedBy;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PublishResultsRequest {
        private Long schoolId;
        @Builder.Default
        private boolean notifyStudents = true;
        private String performedBy;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ExportExamReportRequest {
        private List<String> reportTypes;
        private List<String> dataFields;
        private List<String> sections;
        private List<Long> subjectIds;
        private String format;
        private Integer minScore;
        private Integer maxScore;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class HallTicketGeneratePreviewRequest {
        private String prefix;
        @NotNull
        private Integer startingNumber;
        @Builder.Default
        private boolean autoIncrement = true;
        /** ALL | UNASSIGNED_ONLY */
        @NotBlank
        private String studentFilter;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GenerateHallTicketsRequest {
        private Long schoolId;
        private String prefix;
        @NotNull
        private Integer startingNumber;
        @Builder.Default
        private boolean autoIncrement = true;
        @NotBlank
        private String studentFilter;
        @Builder.Default
        private boolean generatePdf = true;
        @Builder.Default
        private boolean sendToPortal = false;
        private String performedBy;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AssignVenueAllocationRequest {
        private Long schoolId;
        @NotNull
        private Long venueId;
        private Integer capacityLimit;
        @NotNull
        private Long administratorId;
        @NotNull
        private Long primaryInvigilatorId;
        private Long assistantInvigilatorId;
        /** When true, fills venue with students not yet assigned to any venue (up to capacity). */
        @Builder.Default
        private boolean distributeStudents = false;
        private String performedBy;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ApproveSubjectMarksRequest {
        private Long schoolId;
        @Builder.Default
        private boolean confirmCorrect = true;
        private String performedBy;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RejectSubjectMarksRequest {
        private Long schoolId;
        private String reason;
        private String performedBy;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SaveMarksDraftRequest {
        private Long schoolId;
        private List<MarkVerificationUpdate> updates;
        private String performedBy;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MarkVerificationUpdate {
        @NotNull
        private Long resultId;
        private Boolean adminVerified;
        private Integer obtainedMarks;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PrintResultsRequest {
        /** ALL | SELECTED */
        @Builder.Default
        private String studentScope = "ALL";
        private List<Long> studentIds;
        /** SUMMARY | DETAILED */
        @Builder.Default
        private String format = "SUMMARY";
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateVenueAllocationRequest {
        private Long schoolId;
        private Integer capacityLimit;
        private Long administratorId;
        private Long primaryInvigilatorId;
        private Long assistantInvigilatorId;
        private Boolean distributeStudents;
        private String performedBy;
    }
}
