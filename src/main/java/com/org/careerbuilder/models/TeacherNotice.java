package com.org.careerbuilder.models;

import com.org.careerbuilder.models.enums.TeacherNoticeStatus;
import com.org.careerbuilder.models.enums.TeacherNoticeType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "teacher_notices", indexes = {
        @Index(name = "idx_teacher_notices_faculty", columnList = "faculty_id"),
        @Index(name = "idx_teacher_notices_school", columnList = "school_id"),
        @Index(name = "idx_teacher_notices_type_status", columnList = "notice_type,status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeacherNotice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "faculty_id", nullable = false)
    private Faculty faculty;

    @Column(name = "school_id", nullable = false)
    private Long schoolId;

    @Enumerated(EnumType.STRING)
    @Column(name = "notice_type", nullable = false, length = 40)
    private TeacherNoticeType noticeType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TeacherNoticeStatus status;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description", nullable = false, length = 1000)
    private String description;

    @Column(name = "attachment_url", length = 500)
    private String attachmentUrl;

    @Column(name = "attachment_file_name", length = 250)
    private String attachmentFileName;

    @Column(name = "attachment_file_type", length = 120)
    private String attachmentFileType;

    @Column(name = "attachment_file_size")
    private Long attachmentFileSize;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "public_notice_id")
    private Notice publicNotice;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
        if (status == null) {
            status = TeacherNoticeStatus.DRAFT;
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
