package com.org.careerbuilder.models;

import com.org.careerbuilder.models.enums.ClassStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * A grade level for a given academic year (e.g. "Grade 10" — 2025-2026).
 * Sections, subjects and teacher allocations hang off this entity.
 */
@Entity
@Table(name = "academic_classes",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_academic_class_school_name_year",
                columnNames = {"school_id", "name", "academic_year"}),
        indexes = {
                @Index(name = "idx_academic_class_school", columnList = "school_id"),
                @Index(name = "idx_academic_class_status", columnList = "status"),
                @Index(name = "idx_academic_class_deleted", columnList = "deleted")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AcademicClass {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    /** Grade level label, e.g. "Grade 10". */
    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "academic_year", nullable = false, length = 20)
    private String academicYear;

    @Column(name = "max_capacity")
    private Integer maxCapacity;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private ClassStatus status = ClassStatus.ACTIVE;

    /** Primary class teacher; null means "Unassigned". */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_teacher_id")
    private Faculty classTeacher;

    @Column(name = "deleted", nullable = false)
    @Builder.Default
    private boolean deleted = false;

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
