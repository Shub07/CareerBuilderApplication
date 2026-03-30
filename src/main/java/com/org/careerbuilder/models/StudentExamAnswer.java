package com.org.careerbuilder.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 📌 Student Exam Answers Model (Question-wise analysis)
 */
@Entity
@Table(name = "student_exam_answers")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentExamAnswer {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "answer_id")
    private Long id;
    
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_result_id", nullable = false)
    private ExamResult examResult;
    
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private ExamQuestion question;
    
    @NotNull
    @Column(name = "obtained_marks", nullable = false)
    private Integer obtainedMarks;
    
    @Column(name = "status", length = 50)
    private String status;  // CORRECT, WRONG, PARTIAL
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

