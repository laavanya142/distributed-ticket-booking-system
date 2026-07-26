package com.ticketbooking.payment.interfaces.rest.mapper;

import com.ticketbooking.payment.application.model.ChargePaymentCommand;
import com.ticketbooking.payment.application.model.RefundPaymentCommand;
import com.ticketbooking.payment.domain.entity.Payment;
import com.ticketbooking.payment.interfaces.rest.dto.ChargePaymentRequest;
import com.ticketbooking.payment.interfaces.rest.dto.PaymentResponse;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper converting between REST DTOs, domain models, and entities for payment processing.
 */
@Mapper(componentModel = "spring")
public interface PaymentMapper {

    /**
     * Maps ChargePaymentRequest, authenticated userId, and idempotencyKey to ChargePaymentCommand.
     *
     * @param request Client charge request DTO.
     * @param userId Authenticated user ID.
     * @param idempotencyKey Client idempotency key.
     * @return ChargePaymentCommand object.
     */
    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "idempotencyKey", source = "idempotencyKey")
    ChargePaymentCommand toChargeCommand(ChargePaymentRequest request, UUID userId, String idempotencyKey);

    /**
     * Maps refund parameters to RefundPaymentCommand.
     *
     * @param paymentId Payment entity ID.
     * @param bookingId Booking ID.
     * @param userId User ID.
     * @param amount Refund amount.
     * @param currency Currency.
     * @param reason Refund reason.
     * @param idempotencyKey Idempotency key.
     * @return RefundPaymentCommand object.
     */
    @Mapping(target = "paymentId", source = "paymentId")
    @Mapping(target = "bookingId", source = "bookingId")
    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "amount", source = "amount")
    @Mapping(target = "currency", source = "currency")
    @Mapping(target = "reason", source = "reason")
    @Mapping(target = "idempotencyKey", source = "idempotencyKey")
    RefundPaymentCommand toRefundCommand(
            UUID paymentId,
            UUID bookingId,
            UUID userId,
            BigDecimal amount,
            String currency,
            String reason,
            String idempotencyKey);

    /**
     * Maps Payment aggregate entity to PaymentResponse DTO.
     *
     * @param payment Payment entity.
     * @return PaymentResponse DTO.
     */
    PaymentResponse toPaymentResponse(Payment payment);

    /**
     * Maps list of Payment aggregate entities to list of PaymentResponse DTOs.
     *
     * @param payments List of Payment entities.
     * @return List of PaymentResponse DTOs.
     */
    List<PaymentResponse> toPaymentResponseList(List<Payment> payments);
}
