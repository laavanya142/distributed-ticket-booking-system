package com.ticketbooking.booking.application.model;

import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Command object encapsulating inputs required to initiate a booking checkout.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateBookingCommand {

    private UUID userId;
    private UUID showId;
    private List<UUID> showSeatIds;
    private UUID lockToken;
    private UUID paymentMethodId;
    private String currency;
    private String idempotencyKey;
}
