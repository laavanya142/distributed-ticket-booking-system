-- Core notifications table
CREATE TABLE notifications (
    id                  UUID PRIMARY KEY,
    event_id            UUID NOT NULL,
    aggregate_id        UUID NOT NULL,
    aggregate_type      VARCHAR(50) NOT NULL,
    event_type          VARCHAR(100) NOT NULL,
    user_id             UUID NOT NULL,
    channel             VARCHAR(20) NOT NULL,
    status              VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    recipient           VARCHAR(255) NOT NULL,
    subject             VARCHAR(255) NULL,
    title               VARCHAR(255) NULL,
    message             TEXT NOT NULL,
    provider_message_id VARCHAR(128) NULL,
    failure_reason      VARCHAR(255) NULL,
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    sent_at             TIMESTAMP WITH TIME ZONE NULL,
    updated_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version             BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_notifications_event_id UNIQUE (event_id)
);

CREATE INDEX idx_notifications_user_id    ON notifications(user_id);
CREATE INDEX idx_notifications_status     ON notifications(status);
CREATE INDEX idx_notifications_channel    ON notifications(channel);
CREATE INDEX idx_notifications_created_at ON notifications(created_at);
