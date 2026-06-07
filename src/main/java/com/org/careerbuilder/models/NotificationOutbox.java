package com.org.careerbuilder.models;

import com.org.careerbuilder.models.enums.NotificationChannel;
import com.org.careerbuilder.models.enums.NotificationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Transactional outbox entry for an outbound notification (e.g. a fee reminder).
 * Written in the same transaction as the triggering action, then drained
 * asynchronously by {@code OutboxDispatcher}.
 */
@Entity
@Table(name = "notification_outbox", indexes = {
        @Index(name = "idx_outbox_status", columnList = "status,created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationOutbox {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "school_id", nullable = false)
    private Long schoolId;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false, length = 10)
    private NotificationChannel channel;

    @Column(name = "recipient", nullable = false, length = 200)
    private String recipient;

    @Column(name = "template", nullable = false, length = 60)
    private String template;

    @Column(name = "subject", length = 200)
    private String subject;

    @Column(name = "body", nullable = false, columnDefinition = "text")
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 12)
    private NotificationStatus status;

    @Column(name = "attempts", nullable = false)
    private Integer attempts;

    @Column(name = "last_error", length = 500)
    private String lastError;

    @Column(name = "related_student_id")
    private Long relatedStudentId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = NotificationStatus.PENDING;
        }
        if (attempts == null) {
            attempts = 0;
        }
    }
}
