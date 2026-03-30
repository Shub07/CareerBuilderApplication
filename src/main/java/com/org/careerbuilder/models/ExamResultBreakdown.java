package com.org.careerbuilder.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 📌 Exam Result Breakdown Model
 */
@Entity
@Table(name = "exam_result_breakdown")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamResultBreakdown {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "breakdown_id")
    private Long id;
    
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_result_id", nullable = false)
    private ExamResult examResult;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id")
    private ExamSection section;
    
    @Column(name = "section_name", nullable = false, length = 100)
    private String sectionName;
    
    @NotNull
    @Column(name = "obtained_marks", nullable = false)
    private Integer obtainedMarks;
    
    @NotNull
    @Column(name = "total_marks", nullable = false)
    private Integer totalMarks;
    
    @Column(name = "percentage")
    private Double percentage;
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (percentage == null && totalMarks > 0) {
            percentage = (obtainedMarks * 100.0) / totalMarks;
        }
    }
}

