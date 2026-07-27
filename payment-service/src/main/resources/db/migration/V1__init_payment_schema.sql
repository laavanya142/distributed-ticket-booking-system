-- Core payment table
CREATE TABLE payments (
    id                      UUID PRIMARY KEY,
    booking_id              UUID NOT NULL,
    user_id                 UUID NOT NULL,
    amount                  NUMERIC(12, 2) NOT NULL,
    currency                VARCHAR(3) NOT NULL DEFAULT 'USD',
    status                  VARCHAR(30) NOT NULL DEFAULT 'INITIATED',
    payment_method_id       UUID NOT NULL,
    provider_transaction_id VARCHAR(128) NULL,
    failure_reason          VARCHAR(255) NULL,
    version                 BIGINT NOT NULL DEFAULT 0,
    created_at              TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_payments_booking_id    ON payments(booking_id);
CREATE INDEX idx_payments_user_id       ON payments(user_id);
CREATE INDEX idx_payments_status        ON payments(status);
CREATE INDEX idx_payments_provider_tx   ON payments(provider_transaction_id);

-- Transactional outbox table
CREATE TABLE outbox_events (
    id             UUID PRIMARY KEY,
    aggregate_id   UUID NOT NULL,
    aggregate_type VARCHAR(50) NOT NULL,
    event_type     VARCHAR(100) NOT NULL,
    payload        TEXT NOT NULL,
    status         VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    retry_count    INT NOT NULL DEFAULT 0,
    created_at     TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    published_at   TIMESTAMP WITH TIME ZONE NULL
);

CREATE INDEX idx_outbox_status_created ON outbox_events(status, created_at);

-- Client idempotency store table
CREATE TABLE idempotency_keys (
    idempotency_key VARCHAR(128) NOT NULL,
    user_id       UUID NOT NULL,
    payment_id    UUID NULL,
    status_code   INT NOT NULL,
    response_body TEXT NOT NULL,
    created_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at    TIMESTAMP WITH TIME ZONE NOT NULL,
    PRIMARY KEY (idempotency_key, user_id)
);

CREATE INDEX idx_idempotency_expires ON idempotency_keys(expires_at);
