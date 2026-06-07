package com.org.careerbuilder.service;

import com.org.careerbuilder.models.NotificationOutbox;
import com.org.careerbuilder.models.enums.NotificationChannel;

/**
 * Strategy for delivering an outbox message over a specific channel.
 * Swap in SES/Twilio implementations later without touching the dispatcher.
 */
public interface NotificationSender {

    boolean supports(NotificationChannel channel);

    void send(NotificationOutbox message) throws Exception;
}
