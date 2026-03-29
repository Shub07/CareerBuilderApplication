package com.org.careerbuilder.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 📌 Exam Sections Model (For marks breakdown)
 */
@Entity
@Table(name = "exam_sections")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamSection {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "section_id")
    private Long id;
    
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;
    
    @NotBlank
    @Column(name = "section_name", nullable = false, length = 100)
    private String name;
    
    @Column(name = "section_type", length = 50)
    private String type;  // MCQ, SHORT_ANSWER, LONG_ANSWER, PRACTICAL
    
    @NotNull
    @Column(name = "total_marks", nullable = false)
    private Integer totalMarks;
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

