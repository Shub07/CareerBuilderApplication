package com.org.careerbuilder.models;

import com.org.careerbuilder.models.enums.NoticeCategory;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "notices",
        indexes = {
                @Index(name = "idx_notice_school_created", columnList = "school_id, created_at"),
                @Index(name = "idx_notice_category", columnList = "category"),
                @Index(name = "idx_notice_pinned", columnList = "is_pinned")
        }
)
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notice_id", nullable = false, updatable = false)
    private Long id;

    @NotNull
    @Column(name = "school_id", nullable = false)
    private Long schoolId;

    @NotBlank
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "body", length = 3000)
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 50)
    private NoticeCategory category;

    @Column(name = "source", length = 150)
    private String source;

    @Column(name = "is_pinned", nullable = false)
    private Boolean isPinned;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.isPinned == null) {
            this.isPinned = false;
        }
        if (this.category == null) {
            this.category = NoticeCategory.OTHER;
        }
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
