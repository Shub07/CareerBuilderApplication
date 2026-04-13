package com.org.careerbuilder.models;

import com.org.careerbuilder.models.enums.CareerAssessmentType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
    name = "career_assessments",
    indexes = {
        @Index(name = "idx_ca_school", columnList = "school_id"),
        @Index(name = "idx_ca_type", columnList = "type")
    }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CareerAssessment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "school_id", nullable = false)
    private Long schoolId;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description", length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50)
    private CareerAssessmentType type;

    @Column(name = "category", length = 100)
    private String category;

    @Column(name = "estimated_minutes")
    private Integer estimatedMinutes;

    @Column(name = "order_index")
    private Integer orderIndex;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @OneToMany(mappedBy = "assessment", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("questionOrder ASC")
    @Builder.Default
    private List<CareerQuestion> questions = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.isActive == null) this.isActive = true;
    }
}
