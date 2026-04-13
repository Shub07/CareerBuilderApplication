package com.org.careerbuilder.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "career_student_answers",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_csa_progress_question",
            columnNames = {"progress_id", "question_id"})
    },
    indexes = {
        @Index(name = "idx_csa_progress", columnList = "progress_id")
    }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CareerStudentAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "progress_id", nullable = false)
    private CareerStudentProgress progress;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private CareerQuestion question;

    @Column(name = "selected_option_index", nullable = false)
    private Integer selectedOptionIndex;

    @Column(name = "answered_at", nullable = false)
    private LocalDateTime answeredAt;

    @PrePersist
    void onCreate() {
        if (this.answeredAt == null) this.answeredAt = LocalDateTime.now();
    }
}
