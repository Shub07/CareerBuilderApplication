package com.org.careerbuilder.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "teacher_notice_student_targets",
        uniqueConstraints = @UniqueConstraint(name = "uk_tnst_notice_student",
                columnNames = {"teacher_notice_id", "student_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeacherNoticeStudentTarget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "teacher_notice_id", nullable = false)
    private TeacherNotice teacherNotice;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;
}
