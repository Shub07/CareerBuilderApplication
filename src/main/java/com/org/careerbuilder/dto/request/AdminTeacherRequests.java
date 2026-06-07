package com.org.careerbuilder.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public final class AdminTeacherRequests {

    private AdminTeacherRequests() {
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateTeacherRequest {
        @NotNull
        private Long schoolId;
        private String employeeId;
        @NotBlank
        private String firstName;
        @NotBlank
        private String lastName;
        @NotBlank
        private String gender;
        private LocalDate dateOfBirth;
        @NotBlank
        @Pattern(regexp = "^[0-9]{10,15}$")
        private String phone;
        @NotBlank
        @Email
        private String email;
        @NotBlank
        private String address;
        @NotBlank
        private String qualification;
        @NotBlank
        private String specialization;
        private Long subjectId;
        @NotNull
        @Min(0)
        private Integer experienceYears;
        private String skills;
        private String certifications;
        private LocalDate joiningDate;
        private String employmentType;
        private String department;
        private String performedBy;
        private List<ClassAssignmentInput> initialAssignments;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateTeacherRequest {
        @NotNull
        private Long schoolId;
        private String employeeId;
        @NotBlank
        private String firstName;
        @NotBlank
        private String lastName;
        @NotBlank
        private String gender;
        private LocalDate dateOfBirth;
        @NotBlank
        @Pattern(regexp = "^[0-9]{10,15}$")
        private String phone;
        @NotBlank
        @Email
        private String email;
        @NotBlank
        private String address;
        @NotBlank
        private String qualification;
        @NotBlank
        private String specialization;
        private Long subjectId;
        @NotNull
        @Min(0)
        private Integer experienceYears;
        private String skills;
        private String certifications;
        private LocalDate joiningDate;
        private String employmentType;
        private String department;
        private String performedBy;
        /** Sync class assignments on save (Edit Teacher modal). */
        private List<ClassAssignmentInput> classAssignments;
        /** When true, replaces all active assignments with classAssignments. */
        private Boolean replaceClassAssignments;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ConfirmDeleteRequest {
        @NotNull
        private Long schoolId;
        private String performedBy;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AllocateClassesRequest {
        @NotNull
        private Long schoolId;
        @NotNull
        private Long subjectId;
        @NotEmpty
        private List<ClassAssignmentInput> classSections;
        /** Role for the allocation: CLASS_TEACHER / SUBJECT_TEACHER / ASSISTANT_TEACHER. */
        private String role;
        /** If true, deactivates existing assignments before creating new ones. */
        @Builder.Default
        private boolean replaceExisting = false;
        private String performedBy;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateStatusRequest {
        @NotNull
        private Long schoolId;
        @NotBlank
        private String status;
        private String performedBy;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ClassAssignmentInput {
        @NotBlank
        private String className;
        @NotBlank
        private String section;
        @NotNull
        private Long subjectId;
        /** Optional per-row role override. Falls back to the request-level role. */
        private String role;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ExportRequest {
        @NotNull
        private Long schoolId;
        /** Selected row IDs from bulk checkbox export; omit for filter-based export. */
        private List<Long> facultyIds;
        private String academicYear;
        private String q;
        private Long subjectId;
        private String status;
        private String statsFilter;
        @NotBlank
        private String format;
        /** Category → field keys, e.g. basic → [teacherName, employeeId, ...] */
        private Map<String, List<String>> fields;
        private String performedBy;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TeacherListQuery {
        @NotNull
        private Long schoolId;
        private String academicYear;
        private String q;
        private Long subjectId;
        /** Dropdown: ACTIVE, ON_LEAVE, INACTIVE, or ALL */
        private String status;
        /** Stat card click: ALL, ACTIVE, ON_LEAVE, INACTIVE, UNASSIGNED */
        private String statsFilter;
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
    public static class BulkStatusRequest {
        @NotNull
        private Long schoolId;
        @NotEmpty
        private List<Long> facultyIds;
        @NotBlank
        private String status;
        private String performedBy;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BulkDeleteRequest {
        @NotNull
        private Long schoolId;
        @NotEmpty
        private List<Long> facultyIds;
        private String performedBy;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AddAssignmentRequest {
        @NotNull
        private Long schoolId;
        @NotBlank
        private String className;
        @NotBlank
        private String section;
        @NotNull
        private Long subjectId;
        private String performedBy;
    }
}
