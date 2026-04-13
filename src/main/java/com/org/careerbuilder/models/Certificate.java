package com.org.careerbuilder.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "certificates", indexes = {
        @Index(name = "idx_cert_student", columnList = "student_id"),
        @Index(name = "idx_cert_academic_year", columnList = "academic_year")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Certificate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 30)
    private CertificateCategory category;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private CertificateStatus status;

    @Column(name = "issued_on")
    private LocalDate issuedOn;

    @Column(name = "academic_year", length = 20)
    private String academicYear;

    @Column(name = "file_path", length = 500)
    private String filePath;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    public enum CertificateCategory {
        ACADEMIC, MISCELLANEOUS
    }

    public enum CertificateStatus {
        AVAILABLE, UNAVAILABLE
    }
}
