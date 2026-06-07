package com.org.careerbuilder.models;

import com.org.careerbuilder.models.enums.StudentAccountStatus;
import com.org.careerbuilder.models.enums.StudentAdmissionType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "student_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false, unique = true)
    private Student student;

    @Column(name = "admission_number", nullable = false, unique = true, length = 50)
    private String admissionNumber;

    @Column(name = "admission_date", nullable = false)
    private LocalDate admissionDate;

    @Column(name = "photo_url", length = 500)
    private String photoUrl;

    @Column(name = "gender", nullable = false, length = 20)
    private String gender;

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Column(name = "blood_group", length = 10)
    private String bloodGroup;

    @Column(name = "aadhar_number", length = 20)
    private String aadharNumber;

    @Column(name = "religion", length = 50)
    private String religion;

    @Column(name = "category", length = 30)
    private String category;

    @Column(name = "academic_year", length = 20)
    private String academicYear;

    @Column(name = "previous_school_details", length = 1000)
    private String previousSchoolDetails;

    @Enumerated(EnumType.STRING)
    @Column(name = "student_type", nullable = false, length = 20)
    private StudentAdmissionType studentType;

    @Column(name = "father_name", length = 100)
    private String fatherName;

    @Column(name = "father_phone", length = 20)
    private String fatherPhone;

    @Column(name = "mother_name", length = 100)
    private String motherName;

    @Column(name = "mother_phone", length = 20)
    private String motherPhone;

    @Column(name = "father_occupation", length = 100)
    private String fatherOccupation;

    @Column(name = "guardian_name", length = 100)
    private String guardianName;

    @Column(name = "guardian_phone", length = 20)
    private String guardianPhone;

    @Column(name = "address_line1", length = 300)
    private String addressLine1;

    @Column(name = "address_line2", length = 300)
    private String addressLine2;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "state_name", length = 100)
    private String stateName;

    @Column(name = "pincode", length = 10)
    private String pincode;

    @Column(name = "country", length = 50)
    private String country;

    @Column(name = "transport_required", nullable = false)
    private Boolean transportRequired;

    @Column(name = "hostel_required", nullable = false)
    private Boolean hostelRequired;

    @Column(name = "medical_conditions", columnDefinition = "TEXT")
    private String medicalConditions;

    @Column(name = "additional_notes", columnDefinition = "TEXT")
    private String additionalNotes;

    @Column(name = "transport_route", length = 200)
    private String transportRoute;

    @Column(name = "nationality", length = 50)
    private String nationality;

    @Column(name = "flagged", nullable = false)
    private Boolean flagged;

    @Column(name = "flag_reason", length = 1000)
    private String flagReason;

    @Column(name = "flagged_at")
    private LocalDateTime flaggedAt;

    @Column(name = "flagged_by", length = 100)
    private String flaggedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_status", nullable = false, length = 30)
    private StudentAccountStatus accountStatus;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (transportRequired == null) {
            transportRequired = false;
        }
        if (hostelRequired == null) {
            hostelRequired = false;
        }
        if (studentType == null) {
            studentType = StudentAdmissionType.NEW_ADMISSION;
        }
        if (country == null || country.isBlank()) {
            country = "India";
        }
        if (accountStatus == null) {
            accountStatus = StudentAccountStatus.NEW_ADMISSION;
        }
        if (flagged == null) {
            flagged = false;
        }
        if (nationality == null || nationality.isBlank()) {
            nationality = "Indian";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
