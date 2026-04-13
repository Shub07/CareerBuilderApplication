package com.org.careerbuilder.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "career_results",
    indexes = {
        @Index(name = "idx_cr_student", columnList = "student_id"),
        @Index(name = "idx_cr_assessment", columnList = "assessment_id")
    }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CareerResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assessment_id", nullable = false)
    private CareerAssessment assessment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "progress_id", nullable = false)
    private CareerStudentProgress progress;

    /** JSON array string: ["Analytical Thinking","Verbal Communication"] */
    @Column(name = "strength_areas_json", columnDefinition = "TEXT")
    private String strengthAreasJson;

    /** JSON array string: ["Science","Technology"] */
    @Column(name = "interest_indicators_json", columnDefinition = "TEXT")
    private String interestIndicatorsJson;

    @Column(name = "score")
    private Integer score;

    @Column(name = "report_available", nullable = false)
    @Builder.Default
    private Boolean reportAvailable = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.reportAvailable == null) this.reportAvailable = true;
    }
}
