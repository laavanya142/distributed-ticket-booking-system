package com.ticketbooking.payment.application.service;

import com.ticketbooking.payment.application.model.ChargePaymentCommand;
import com.ticketbooking.payment.application.model.RefundPaymentCommand;
import com.ticketbooking.payment.domain.entity.Payment;
import java.util.List;
import java.util.UUID;

/**
 * Service interface for payment processing, refund handling, and audit query operations.
 */
public interface PaymentService {

    /**
     * Charges a payment synchronously via gateway, updating database state and writing outbox events.
     *
     * @param command Charge parameters.
     * @return Processed Payment entity.
     */
    Payment charge(ChargePaymentCommand command);

    /**
     * Refunds an existing payment transaction synchronously via gateway.
     *
     * @param command Refund parameters.
     * @return Updated Payment entity.
     */
    Payment refund(RefundPaymentCommand command);

    /**
     * Retrieves a payment by primary key ID.
     *
     * @param paymentId Payment entity identifier.
     * @return Payment aggregate.
     */
    Payment findById(UUID paymentId);

    /**
     * Retrieves all payments for a specific booking.
     *
     * @param bookingId Booking identifier.
     * @return List of Payment aggregates.
     */
    List<Payment> findByBookingId(UUID bookingId);
}
