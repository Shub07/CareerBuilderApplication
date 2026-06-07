package com.org.careerbuilder.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminStudentRegistrationRequest {

    /** Set server-side from JWT. */
    private Long schoolId;

    @Valid
    @NotNull
    private BasicDetails basic;

    @Valid
    @NotNull
    private AcademicDetails academic;

    @Valid
    private ParentDetails parent;

    @Valid
    @NotNull
    private ContactDetails contact;

    private AdditionalDetails additional;

    private List<DocumentUploadRef> documents;

    private String performedBy;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BasicDetails {
        @NotBlank
        private String fullName;
        @NotBlank
        private String admissionNumber;
        @NotNull
        private LocalDate admissionDate;
        @NotBlank
        private String gender;
        @NotNull
        private LocalDate dateOfBirth;
        private String bloodGroup;
        private String aadharNumber;
        private String religion;
        private String category;
        private String photoUrl;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AcademicDetails {
        @NotBlank
        private String className;
        @NotBlank
        private String section;
        private Integer rollNumber;
        private String academicYear;
        private String previousSchoolDetails;
        @NotBlank
        private String studentType;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ParentDetails {
        @NotBlank
        private String fatherName;
        @NotBlank
        private String fatherPhone;
        private String motherName;
        private String motherPhone;
        private String fatherOccupation;
        private String guardianName;
        private String guardianPhone;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ContactDetails {
        @NotBlank
        @Pattern(regexp = "^[0-9]{10,15}$")
        private String phone;
        @Email
        private String email;
        @NotBlank
        private String addressLine1;
        private String addressLine2;
        @NotBlank
        private String city;
        @NotBlank
        private String state;
        @NotBlank
        @Pattern(regexp = "^[0-9]{6}$")
        private String pincode;
        private String country;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AdditionalDetails {
        private Boolean transportRequired;
        private Boolean hostelRequired;
        private String medicalConditions;
        private String additionalNotes;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DocumentUploadRef {
        @NotBlank
        private String documentType;
        private String fileName;
        @NotBlank
        private String fileUrl;
    }
}
