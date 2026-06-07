package com.org.careerbuilder.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "student_quiz_results", indexes = {
        @Index(name = "idx_quiz_student", columnList = "student_id"),
        @Index(name = "idx_quiz_subject", columnList = "subject_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentQuizResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "faculty_id")
    private Faculty faculty;

    @NotBlank
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @NotNull
    @Column(name = "quiz_date", nullable = false)
    private LocalDate quizDate;

    @NotNull
    @Min(0)
    @Column(name = "obtained_marks", nullable = false)
    private Integer obtainedMarks;

    @NotNull
    @Min(1)
    @Column(name = "total_marks", nullable = false)
    private Integer totalMarks;

    @Column(name = "passed", nullable = false)
    private boolean passed = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
