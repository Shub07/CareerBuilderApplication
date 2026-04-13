package com.org.careerbuilder.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "career_question_options",
    indexes = {
        @Index(name = "idx_cqo_question", columnList = "question_id")
    }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CareerQuestionOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private CareerQuestion question;

    @Column(name = "option_text", nullable = false, length = 300)
    private String optionText;

    @Column(name = "option_index", nullable = false)
    private Integer optionIndex;

    /** Tag used to compute strengths: e.g., Analytical, Creative, Leadership, Research */
    @Column(name = "trait_tag", length = 100)
    private String traitTag;
}
