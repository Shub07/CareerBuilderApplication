package com.org.careerbuilder.service.impl;

import com.org.careerbuilder.models.NotificationOutbox;
import com.org.careerbuilder.models.enums.NotificationChannel;
import com.org.careerbuilder.service.NotificationSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Default sender used until a real provider (SES/Twilio) is wired in. Logs the
 * message so the outbox flow is fully exercisable end-to-end in any environment.
 */
@Slf4j
@Component
public class LoggingNotificationSender implements NotificationSender {

    @Override
    public boolean supports(NotificationChannel channel) {
        return true;
    }

    @Override
    public void send(NotificationOutbox message) {
        log.info("[notification:{}] to={} template={} subject={} body={}",
                message.getChannel(), message.getRecipient(), message.getTemplate(),
                message.getSubject(), message.getBody());
    }
}
