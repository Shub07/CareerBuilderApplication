package com.org.careerbuilder.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "teacher_notice_class_targets",
        uniqueConstraints = @UniqueConstraint(name = "uk_tnct_notice_class_section",
                columnNames = {"teacher_notice_id", "class_name", "section"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeacherNoticeClassTarget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "teacher_notice_id", nullable = false)
    private TeacherNotice teacherNotice;

    @Column(name = "class_name", nullable = false, length = 30)
    private String className;

    @Column(name = "section", nullable = false, length = 10)
    private String section;
}
