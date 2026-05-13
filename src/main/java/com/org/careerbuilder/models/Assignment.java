package com.org.careerbuilder.models;

import com.org.careerbuilder.models.enums.AssignmentPublishStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

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
@NoArgsConstructor
@AllArgsConstructor
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

    @Column(name = "due_time")
    private LocalTime dueTime;

    @Column(name = "school_id")
    private Long schoolId;

    @Column(name = "class_name", length = 50)
    private String className;

    @Column(name = "section", length = 10)
    private String section;

    @Column(name = "given_date")
    private LocalDate givenDate;

    @Column(name = "total_marks")
    private Integer totalMarks;

    @Builder.Default
    @Column(name = "allow_late_submission", nullable = false)
    private boolean allowLateSubmission = false;

    @Builder.Default
    @Column(name = "allow_resubmission", nullable = false)
    private boolean allowResubmission = false;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "publish_status", nullable = false, length = 20)
    @Builder.Default
    private AssignmentPublishStatus publishStatus = AssignmentPublishStatus.PUBLISHED;

    /**
     * Audit fields
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "attachment_path", length = 500)
    private String attachmentPath;

    /** End-of-day deadline when {@link #dueTime} is null. */
    public LocalDateTime dueDateTime() {
        LocalTime t = dueTime != null ? dueTime : LocalTime.of(23, 59);
        return dueDate.atTime(t);
    }

    public boolean visibleToStudents() {
        return publishStatus == AssignmentPublishStatus.PUBLISHED;
    }

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (publishStatus == null) {
            publishStatus = AssignmentPublishStatus.PUBLISHED;
        }
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}