package com.org.careerbuilder.models;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
    name = "career_questions",
    indexes = {
        @Index(name = "idx_cq_assessment", columnList = "assessment_id"),
        @Index(name = "idx_cq_order", columnList = "assessment_id, question_order")
    }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CareerQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assessment_id", nullable = false)
    private CareerAssessment assessment;

    @Column(name = "question_text", nullable = false, length = 500)
    private String questionText;

    @Column(name = "question_order", nullable = false)
    private Integer questionOrder;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("optionIndex ASC")
    @Builder.Default
    private List<CareerQuestionOption> options = new ArrayList<>();
}
