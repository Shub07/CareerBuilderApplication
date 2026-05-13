package com.org.careerbuilder.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "class_attendance_sessions",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_class_attendance_session",
                columnNames = {"faculty_id", "class_name", "section", "subject_id", "session_date"}
        ),
        indexes = {
                @Index(name = "idx_cas_faculty_date", columnList = "faculty_id,session_date"),
                @Index(name = "idx_cas_school_date", columnList = "school_id,session_date")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassAttendanceSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "faculty_id", nullable = false)
    private Faculty faculty;

    @NotNull
    @Column(name = "school_id", nullable = false)
    private Long schoolId;

    @NotBlank
    @Column(name = "class_name", nullable = false, length = 50)
    private String className;

    @NotBlank
    @Column(name = "section", nullable = false, length = 10)
    private String section;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @NotNull
    @Column(name = "session_date", nullable = false)
    private LocalDate sessionDate;

    @Column(name = "locked", nullable = false)
    private boolean locked;

    @Column(name = "locked_at")
    private LocalDateTime lockedAt;

    @Column(name = "proof_attachment_path", length = 500)
    private String proofAttachmentPath;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
