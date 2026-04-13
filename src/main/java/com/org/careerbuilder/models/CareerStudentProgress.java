package com.org.careerbuilder.models;

import com.org.careerbuilder.models.enums.CareerAssessmentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "career_student_progress",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_csp_student_assessment",
            columnNames = {"student_id", "assessment_id"})
    },
    indexes = {
        @Index(name = "idx_csp_student", columnList = "student_id"),
        @Index(name = "idx_csp_status", columnList = "student_id, status")
    }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CareerStudentProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assessment_id", nullable = false)
    private CareerAssessment assessment;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private CareerAssessmentStatus status = CareerAssessmentStatus.IN_PROGRESS;

    @Column(name = "current_question_index", nullable = false)
    @Builder.Default
    private Integer currentQuestionIndex = 0;

    @Column(name = "started_at", nullable = false, updatable = false)
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "last_saved_at")
    private LocalDateTime lastSavedAt;

    @PrePersist
    void onCreate() {
        this.startedAt = LocalDateTime.now();
        this.lastSavedAt = LocalDateTime.now();
        if (this.status == null) this.status = CareerAssessmentStatus.IN_PROGRESS;
    }
}
