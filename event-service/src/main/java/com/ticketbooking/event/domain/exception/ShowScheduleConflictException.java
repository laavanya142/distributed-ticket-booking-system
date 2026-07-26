package com.ticketbooking.event.domain.exception;

import com.ticketbooking.common.exception.DomainException;
import java.time.Instant;
import java.util.UUID;

/**
 * Thrown when attempting to schedule a show that overlaps with an existing show on the same screen.
 */
public class ShowScheduleConflictException extends DomainException {
    public ShowScheduleConflictException(UUID screenId, Instant startTime, Instant endTime) {
        super(
                "SHOW_SCHEDULE_CONFLICT",
                String.format("Schedule conflict on screen %s between %s and %s", screenId, startTime, endTime));
    }
}
