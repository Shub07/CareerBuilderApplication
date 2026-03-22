package com.org.careerbuilder.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(
        name = "subjects",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_subject_name", columnNames = "subject_name")
        }
)
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Subject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "subject_id", nullable = false, updatable = false)
    private Long id;

    @NotBlank @Size(min = 2, max = 100)
    @Column(name = "subject_name", nullable = false, length = 100)
    private String name;
}
