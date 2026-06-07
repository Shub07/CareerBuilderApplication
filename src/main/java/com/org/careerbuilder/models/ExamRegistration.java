package com.org.careerbuilder.models;

import com.org.careerbuilder.models.enums.ExamRegistrationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "exam_registrations",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_exam_registration", columnNames = {"exam_id", "student_id"}),
                @UniqueConstraint(name = "uk_exam_hall_ticket", columnNames = {"school_id", "hall_ticket_number"})
        },
        indexes = {
                @Index(name = "idx_exam_reg_exam", columnList = "exam_id"),
                @Index(name = "idx_exam_reg_student", columnList = "student_id"),
                @Index(name = "idx_exam_reg_school_status", columnList = "school_id, status")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamRegistration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "registration_id", nullable = false, updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;

    @Column(name = "school_id", nullable = false)
    private Long schoolId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(name = "hall_ticket_number", nullable = false, length = 40)
    private String hallTicketNumber;

    @Column(name = "primary_venue", length = 200)
    private String primaryVenue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venue_id")
    private ExamVenue venue;

    @Column(name = "document_hash", length = 40)
    private String documentHash;

    @Column(name = "hall_ticket_generated", nullable = false)
    @Builder.Default
    private boolean hallTicketGenerated = false;

    @Column(name = "portal_notified", nullable = false)
    @Builder.Default
    private boolean portalNotified = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private ExamRegistrationStatus status = ExamRegistrationStatus.PENDING;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
