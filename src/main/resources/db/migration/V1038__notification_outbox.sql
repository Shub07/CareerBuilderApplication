-- Durable outbox for fee notifications (reminders). A poller drains PENDING rows
-- to the configured channel and records delivery, so a reminder is never lost
-- if the notification provider is briefly unavailable.
CREATE TABLE IF NOT EXISTS notification_outbox (
    id                 BIGSERIAL PRIMARY KEY,
    school_id          BIGINT NOT NULL,
    channel            VARCHAR(10) NOT NULL,          -- EMAIL | SMS
    recipient          VARCHAR(200) NOT NULL,
    template           VARCHAR(60) NOT NULL,
    subject            VARCHAR(200),
    body               TEXT NOT NULL,
    status             VARCHAR(12) NOT NULL DEFAULT 'PENDING', -- PENDING|SENT|FAILED
    attempts           INT NOT NULL DEFAULT 0,
    last_error         VARCHAR(500),
    related_student_id BIGINT,
    created_at         TIMESTAMP NOT NULL DEFAULT now(),
    sent_at            TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_outbox_status ON notification_outbox (status, created_at);
