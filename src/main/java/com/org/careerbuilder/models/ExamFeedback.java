package com.org.careerbuilder.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 📌 Exam Feedback Model
 */
@Entity
@Table(name = "exam_feedback")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamFeedback {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feedback_id")
    private Long id;
    
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_result_id", nullable = false)
    private ExamResult examResult;
    
    @Column(name = "teacher_id")
    private Long teacherId;
    
    @Column(name = "teacher_name", length = 100)
    private String teacherName;
    
    @Column(name = "feedback_text", length = 500)
    private String feedbackText;
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

