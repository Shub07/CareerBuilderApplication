package com.org.careerbuilder.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 📌 Exam Questions Model
 */
@Entity
@Table(name = "exam_questions")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamQuestion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "question_id")
    private Long id;
    
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;
    
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id", nullable = false)
    private ExamSection section;
    
    @NotNull
    @Column(name = "question_number", nullable = false)
    private Integer questionNumber;
    
    @Column(name = "question_text", length = 500)
    private String questionText;
    
    @NotNull
    @Column(name = "marks", nullable = false)
    private Integer marks;
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

