package com.org.careerbuilder.models;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Table(name = "my_classes", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"student_id", "subject_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MyClass {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @Column(name = "class_name", nullable = false, length = 50)
    private String className;

    @Column(name = "section", nullable = false, length = 10)
    private String section;

    // Transient fields for request deserialization
    @Transient
    @JsonProperty("studentId")
    private Long studentId;

    @Transient
    @JsonProperty("subjectId")
    private Long subjectId;
}

