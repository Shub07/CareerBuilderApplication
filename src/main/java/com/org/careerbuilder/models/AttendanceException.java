package com.org.careerbuilder.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Exception handling for attendance (sick leave, medical, field trip, etc.)
 */
@Entity
@Table(
    name = "attendance_exceptions",
    indexes = {
        @Index(name = "idx_exception_student", columnList = "student_id"),
        @Index(name = "idx_exception_status", columnList = "status"),
        @Index(name = "idx_exception_date", columnList = "exception_date")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_exception_date", columnNames = {"student_id", "exception_date"})
    }
)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class AttendanceException {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "exception_id")
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @NotNull
    @Column(name = "exception_date", nullable = false)
    private LocalDate exceptionDate;

    @NotNull
    @Column(name = "exception_type", nullable = false, length = 50)
    private String exceptionType; // SICK_LEAVE, MEDICAL, FIELD_TRIP, etc.

    @Column(name = "status", nullable = false, length = 50)
    private String status = "PENDING"; // PENDING, APPROVED, REJECTED

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requested_by_id", nullable = false)
    private AppUser requestedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by_id")
    private AppUser approvedBy;

    @NotNull
    @Column(name = "reason", nullable = false, length = 500)
    private String reason;

    @Column(name = "attachment_url", length = 500)
    private String attachmentUrl;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

