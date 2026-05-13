package com.org.careerbuilder.models;

import com.org.careerbuilder.models.enums.QuizLifecycleStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "teacher_quizzes", indexes = {
        @Index(name = "idx_teacher_quizzes_teacher", columnList = "teacher_id"),
        @Index(name = "idx_teacher_quizzes_lifecycle", columnList = "lifecycle_status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeacherQuiz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "quiz_id", nullable = false, updatable = false)
    private Long id;

    @NotNull
    @Column(name = "school_id", nullable = false)
    private Long schoolId;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "teacher_id", nullable = false)
    private Faculty teacher;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @NotBlank
    @Column(name = "title", nullable = false, length = 300)
    private String title;

    @Column(name = "instructions", columnDefinition = "TEXT")
    private String instructions;

    @NotBlank
    @Column(name = "class_name", nullable = false, length = 50)
    private String className;

    @NotBlank
    @Column(name = "section", nullable = false, length = 10)
    private String section;

    @NotNull
    @Column(name = "time_limit_minutes", nullable = false)
    @Builder.Default
    private Integer timeLimitMinutes = 30;

    @NotNull
    @Column(name = "total_marks", nullable = false)
    private Integer totalMarks;

    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

    @Column(name = "shuffle_questions", nullable = false)
    @Builder.Default
    private boolean shuffleQuestions = false;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "lifecycle_status", nullable = false, length = 20)
    @Builder.Default
    private QuizLifecycleStatus lifecycleStatus = QuizLifecycleStatus.DRAFT;

    @Column(name = "conducted_on")
    private LocalDate conductedOn;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC, id ASC")
    @Builder.Default
    private List<QuizQuestion> questions = new ArrayList<>();

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
