package com.org.careerbuilder.models;

import com.org.careerbuilder.models.enums.ClassAssignmentRole;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(
        name = "class_subject_teachers",
        uniqueConstraints = @UniqueConstraint(columnNames = {"school_id","class_name","section","subject_id"})
)
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassSubjectTeacher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="school_id", nullable=false)
    private Long schoolId;

    @Column(name="class_name", nullable=false, length=20)
    private String className;

    @Column(name="section", nullable=false, length=5)
    private String section;

    @ManyToOne(fetch=FetchType.LAZY, optional=false)
    @JoinColumn(name="subject_id", nullable=false)
    private Subject subject;

    @ManyToOne(fetch=FetchType.LAZY, optional=false)
    @JoinColumn(name="faculty_id", nullable=false)
    private Faculty faculty;

    @Column(name="is_active", nullable=false)
    private boolean active = true;

    @Enumerated(EnumType.STRING)
    @Column(name="role", length=30)
    @Builder.Default
    private ClassAssignmentRole role = ClassAssignmentRole.SUBJECT_TEACHER;

    @Column(name="assigned_date")
    private LocalDate assignedDate;
}