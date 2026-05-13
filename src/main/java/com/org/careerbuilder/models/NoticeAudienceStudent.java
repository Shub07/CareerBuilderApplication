package com.org.careerbuilder.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "notice_audience_students",
        uniqueConstraints = @UniqueConstraint(name = "uk_notice_audience_student",
                columnNames = {"notice_id", "student_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NoticeAudienceStudent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "notice_id", nullable = false)
    private Notice notice;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;
}
