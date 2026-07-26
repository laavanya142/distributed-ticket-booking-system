-- Core booking aggregate
CREATE TABLE bookings (
    id                UUID PRIMARY KEY,
    user_id           UUID NOT NULL,
    show_id           UUID NOT NULL,
    status            VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    lock_token        UUID NOT NULL,
    total_amount      NUMERIC(12, 2) NOT NULL,
    currency          VARCHAR(3) NOT NULL DEFAULT 'USD',
    payment_id        UUID NULL,
    idempotency_key   VARCHAR(128) NOT NULL,
    confirmation_code VARCHAR(20) NULL,
    expires_at        TIMESTAMP WITH TIME ZONE NOT NULL,
    version           BIGINT NOT NULL DEFAULT 0,
    created_at        TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_booking_idempotency UNIQUE (idempotency_key)
);

CREATE INDEX idx_bookings_user_id     ON bookings(user_id);
CREATE INDEX idx_bookings_show_id     ON bookings(show_id);
CREATE INDEX idx_bookings_status      ON bookings(status);
CREATE INDEX idx_bookings_expires_at  ON bookings(expires_at, status);

-- Per-seat price snapshot
CREATE TABLE booking_items (
    id               UUID PRIMARY KEY,
    booking_id       UUID NOT NULL REFERENCES bookings(id) ON DELETE CASCADE,
    show_seat_id     UUID NOT NULL,
    seat_label       VARCHAR(20) NOT NULL,
    category         VARCHAR(20) NOT NULL,
    price_at_booking NUMERIC(10, 2) NOT NULL,
    created_at       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_booking_items_booking_id ON booking_items(booking_id);

-- Transactional outbox (Kafka guarantee)
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

-- Kafka inbox deduplication
CREATE TABLE inbox_messages (
    event_id       UUID NOT NULL,
    consumer_group VARCHAR(100) NOT NULL,
    topic          VARCHAR(200) NOT NULL,
    processed_at   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (event_id, consumer_group)
);

-- Client idempotency store
CREATE TABLE idempotency_keys (
    key           VARCHAR(128) NOT NULL,
    user_id       UUID NOT NULL,
    booking_id    UUID NULL,
    status_code   INT NOT NULL,
    response_body TEXT NOT NULL,
    created_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at    TIMESTAMP WITH TIME ZONE NOT NULL,
    PRIMARY KEY (key, user_id)
);

CREATE INDEX idx_idempotency_expires ON idempotency_keys(expires_at);
