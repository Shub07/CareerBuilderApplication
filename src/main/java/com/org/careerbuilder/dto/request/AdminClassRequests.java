package com.org.careerbuilder.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

/**
 * Request payloads for the Admin Class Details module.
 */
public final class AdminClassRequests {

    private AdminClassRequests() {
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateClassRequest {
        @NotNull
        private Long schoolId;
        @NotBlank
        private String name;
        @Positive
        private Integer maxCapacity;
        /** Optional; defaults to the current academic year when omitted. */
        private String academicYear;
        private String performedBy;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateClassRequest {
        @NotNull
        private Long schoolId;
        @NotBlank
        private String name;
        private String academicYear;
        @Positive
        private Integer maxCapacity;
        /** ACTIVE / INACTIVE. */
        private String status;
        private String performedBy;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AddSectionRequest {
        @NotNull
        private Long schoolId;
        @NotBlank
        private String name;
        private Long classTeacherId;
        private String performedBy;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateSectionRequest {
        @NotNull
        private Long schoolId;
        private String name;
        private Long classTeacherId;
        private String performedBy;
    }

    /** "Assign Class Teacher" header modal (teacher + subject + section). */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AssignClassTeacherRequest {
        @NotNull
        private Long schoolId;
        @NotNull
        private Long teacherId;
        private Long subjectId;
        /** Optional; null/blank means the allocation spans all sections. */
        private Long sectionId;
        private String performedBy;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AddSubjectRequest {
        @NotNull
        private Long schoolId;
        @NotBlank
        private String subjectName;
        private String subjectCode;
        private Long assignedTeacherId;
        private String performedBy;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateSubjectRequest {
        @NotNull
        private Long schoolId;
        private String subjectName;
        private String subjectCode;
        private Long assignedTeacherId;
        private String performedBy;
    }

    /** Students tab "Add Student" modal (existing student + section). */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AddStudentRequest {
        @NotNull
        private Long schoolId;
        @NotNull
        private Long studentId;
        /** Target section within this class; null/blank keeps the student's current section. */
        private Long sectionId;
        private String performedBy;
    }

    /** Students tab "Transfer Student" action (move to another section / class). */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TransferStudentRequest {
        @NotNull
        private Long schoolId;
        /** Target class; null means transfer within the current class. */
        private Long targetClassId;
        /** Target section id within the resolved class. */
        private Long sectionId;
        private String performedBy;
    }

    /** Teachers tab "Assign Teacher" modal (teacher + subject + section + role). */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AssignTeacherRequest {
        @NotNull
        private Long schoolId;
        @NotNull
        private Long teacherId;
        @NotNull
        private Long subjectId;
        /** Optional; null means all sections (rendered as "ALL"). */
        private Long sectionId;
        /** CLASS_TEACHER / SUBJECT_TEACHER / ASSISTANT_TEACHER. */
        private String role;
        private String performedBy;
    }
}
