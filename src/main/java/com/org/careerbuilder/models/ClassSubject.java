package com.org.careerbuilder.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * A subject offered within an {@link AcademicClass} (the curriculum structure),
 * carrying a class-specific subject code and an optional assigned teacher.
 */
@Entity
@Table(name = "class_subjects",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_class_subject_class_subject",
                columnNames = {"class_id", "subject_id"}),
        indexes = {
                @Index(name = "idx_class_subject_class", columnList = "class_id"),
                @Index(name = "idx_class_subject_school", columnList = "school_id")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassSubject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "class_id", nullable = false)
    private AcademicClass academicClass;

    @Column(name = "school_id", nullable = false)
    private Long schoolId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @Column(name = "subject_code", length = 30)
    private String subjectCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_teacher_id")
    private Faculty assignedTeacher;

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
