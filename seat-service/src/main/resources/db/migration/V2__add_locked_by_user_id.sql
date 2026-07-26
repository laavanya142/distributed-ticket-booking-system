-- Add locked_by_user_id column to show_seats table
ALTER TABLE show_seats ADD COLUMN locked_by_user_id UUID NULL;

CREATE INDEX idx_show_seats_locked_by ON show_seats(locked_by_user_id);
