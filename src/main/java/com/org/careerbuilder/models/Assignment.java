package com.org.careerbuilder.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "assignments",
        indexes = {
                @Index(name = "idx_assignment_subject", columnList = "subject_id"),
                @Index(name = "idx_assignment_due_date", columnList = "due_date")
        }
)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Assignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "assignment_id", nullable = false, updatable = false)
    private Long id;

    /**
     * Subject relationship (Better than subjectId)
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    /**
     * Teacher relationship
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "teacher_id", nullable = false)
    private Faculty teacher;

    /**
     * Assignment title
     */
    @NotBlank
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    /**
     * Assignment instructions
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * Submission deadline
     */
    @NotNull
    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    /**
     * Audit fields
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}