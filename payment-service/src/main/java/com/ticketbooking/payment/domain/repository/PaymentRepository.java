package com.ticketbooking.payment.domain.repository;

import com.ticketbooking.payment.domain.entity.Payment;
import com.ticketbooking.payment.domain.entity.PaymentStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing Payment aggregate persistence.
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    List<Payment> findByBookingId(UUID bookingId);

    Optional<Payment> findByBookingIdAndStatus(UUID bookingId, PaymentStatus status);

    boolean existsByBookingIdAndStatus(UUID bookingId, PaymentStatus status);
}
