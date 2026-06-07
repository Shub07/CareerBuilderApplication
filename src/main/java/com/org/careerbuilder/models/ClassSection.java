package com.org.careerbuilder.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * A division within an {@link AcademicClass}, e.g. "Section A".
 */
@Entity
@Table(name = "class_sections",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_class_section_class_name",
                columnNames = {"class_id", "name"}),
        indexes = {
                @Index(name = "idx_class_section_class", columnList = "class_id"),
                @Index(name = "idx_class_section_school", columnList = "school_id")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassSection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "class_id", nullable = false)
    private AcademicClass academicClass;

    @Column(name = "school_id", nullable = false)
    private Long schoolId;

    /** Section label, e.g. "A". */
    @Column(name = "name", nullable = false, length = 20)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_teacher_id")
    private Faculty sectionTeacher;

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
