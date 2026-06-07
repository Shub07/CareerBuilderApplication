package com.org.careerbuilder.models;

import com.org.careerbuilder.models.enums.FacultyAccountStatus;
import com.org.careerbuilder.models.enums.FacultyEmploymentType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "faculty_profiles", indexes = {
        @Index(name = "idx_faculty_profile_status", columnList = "account_status"),
        @Index(name = "idx_faculty_profile_deleted", columnList = "deleted")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FacultyProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "faculty_id", nullable = false, unique = true)
    private Faculty faculty;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_status", nullable = false, length = 20)
    @Builder.Default
    private FacultyAccountStatus accountStatus = FacultyAccountStatus.ACTIVE;

    @Column(name = "photo_url", length = 500)
    private String photoUrl;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "joining_date")
    private LocalDate joiningDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "employment_type", length = 30)
    private FacultyEmploymentType employmentType;

    @Column(name = "department", length = 100)
    private String department;

    @Column(name = "designation", length = 100)
    private String designation;

    @Column(name = "skills", length = 500)
    private String skills;

    @Column(name = "certifications", length = 1000)
    private String certifications;

    @Column(name = "resume_path", length = 500)
    private String resumePath;

    @Column(name = "id_proof_path", length = 500)
    private String idProofPath;

    @Column(name = "certificates_path", length = 500)
    private String certificatesPath;

    @Column(name = "experience_letters_path", length = 500)
    private String experienceLettersPath;

    @Column(name = "contract_path", length = 500)
    private String contractPath;

    @Column(name = "basic_salary", precision = 12, scale = 2)
    private java.math.BigDecimal basicSalary;

    @Column(name = "contract_type", length = 40)
    private String contractType;

    @Column(name = "deleted", nullable = false)
    @Builder.Default
    private boolean deleted = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
