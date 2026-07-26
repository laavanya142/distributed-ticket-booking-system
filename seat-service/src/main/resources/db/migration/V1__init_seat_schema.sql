-- Physical Seats Table
CREATE TABLE seats (
    id UUID PRIMARY KEY,
    screen_id UUID NOT NULL,
    row_number VARCHAR(5) NOT NULL,
    seat_number INT NOT NULL,
    category VARCHAR(20) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_screen_row_seat UNIQUE (screen_id, row_number, seat_number)
);

CREATE INDEX idx_seats_screen ON seats(screen_id);

-- Show Seats Table
CREATE TABLE show_seats (
    id UUID PRIMARY KEY,
    show_id UUID NOT NULL,
    seat_id UUID NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    price NUMERIC(10, 2) NOT NULL,
    locked_at TIMESTAMP WITH TIME ZONE NULL,
    lock_token UUID NULL,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_show_seat UNIQUE (show_id, seat_id),
    CONSTRAINT fk_show_seats_seat FOREIGN KEY (seat_id) REFERENCES seats(id) ON DELETE RESTRICT
);

CREATE INDEX idx_show_seats_show_status ON show_seats(show_id, status);
CREATE INDEX idx_show_seats_seat ON show_seats(seat_id);
