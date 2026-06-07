package com.org.careerbuilder.service.impl;

import com.org.careerbuilder.models.NotificationOutbox;
import com.org.careerbuilder.models.enums.NotificationChannel;
import com.org.careerbuilder.models.enums.NotificationStatus;
import com.org.careerbuilder.repository.NotificationOutboxRepository;
import com.org.careerbuilder.service.NotificationSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Drains PENDING {@link NotificationOutbox} rows on a fixed delay, delivering
 * each via the matching {@link NotificationSender} with bounded retries.
 */
@Slf4j
@Component
public class OutboxDispatcher {

    private static final int BATCH_SIZE = 50;
    private static final int MAX_ATTEMPTS = 5;

    private final NotificationOutboxRepository outboxRepository;
    private final List<NotificationSender> senders;

    public OutboxDispatcher(NotificationOutboxRepository outboxRepository, List<NotificationSender> senders) {
        this.outboxRepository = outboxRepository;
        this.senders = senders;
    }

    @Scheduled(fixedDelayString = "${fee.outbox.poll-ms:15000}")
    @Transactional
    public void dispatch() {
        List<NotificationOutbox> batch = outboxRepository.findByStatusOrderByCreatedAtAsc(
                NotificationStatus.PENDING, PageRequest.of(0, BATCH_SIZE));
        if (batch.isEmpty()) {
            return;
        }
        for (NotificationOutbox msg : batch) {
            try {
                resolveSender(msg.getChannel()).send(msg);
                msg.setStatus(NotificationStatus.SENT);
                msg.setSentAt(LocalDateTime.now());
            } catch (Exception e) {
                msg.setAttempts(msg.getAttempts() + 1);
                String err = e.getMessage();
                msg.setLastError(err != null && err.length() > 480 ? err.substring(0, 480) : err);
                if (msg.getAttempts() >= MAX_ATTEMPTS) {
                    msg.setStatus(NotificationStatus.FAILED);
                    log.warn("Outbox message {} permanently failed after {} attempts",
                            msg.getId(), msg.getAttempts());
                }
            }
        }
        outboxRepository.saveAll(batch);
    }

    private NotificationSender resolveSender(NotificationChannel channel) {
        return senders.stream()
                .filter(s -> s.supports(channel))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No sender for channel " + channel));
    }
}
