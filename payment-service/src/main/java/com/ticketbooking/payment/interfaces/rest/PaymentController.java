package com.ticketbooking.payment.interfaces.rest;

import com.ticketbooking.common.constants.HeaderConstants;
import com.ticketbooking.common.dto.ApiResponse;
import com.ticketbooking.payment.application.model.ChargePaymentCommand;
import com.ticketbooking.payment.application.model.RefundPaymentCommand;
import com.ticketbooking.payment.application.service.PaymentService;
import com.ticketbooking.payment.domain.entity.Payment;
import com.ticketbooking.payment.interfaces.rest.dto.ChargePaymentRequest;
import com.ticketbooking.payment.interfaces.rest.dto.PaymentResponse;
import com.ticketbooking.payment.interfaces.rest.dto.RefundPaymentRequest;
import com.ticketbooking.payment.interfaces.rest.mapper.PaymentMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller exposing payment gateway charge, refund, and payment audit query endpoints.
 */
@RestController
@RequestMapping("/api/v1/payments")
@Validated
@Slf4j
@RequiredArgsConstructor
@Tag(name = "Payment Management", description = "APIs for synchronous payment processing and refund handling")
public class PaymentController {

    private final PaymentService paymentService;
    private final PaymentMapper paymentMapper;

    /**
     * Charges a payment synchronously via gateway provider.
     *
     * @param userId Authenticated user ID injected by API Gateway.
     * @param idempotencyKey Client idempotency deduplication header.
     * @param request Payment charge request payload.
     * @return ResponseEntity with created PaymentResponse.
     */
    @PostMapping("/charge")
    @Operation(
            summary = "Charge payment",
            description = "Synchronously processes a payment charge via payment gateway provider")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "201",
                description = "Payment successfully charged and captured"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "Invalid request parameters"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "402",
                description = "Payment charge declined or failed"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "409",
                description = "Duplicate payment already captured for booking")
    })
    public ResponseEntity<ApiResponse<PaymentResponse>> chargePayment(
            @Parameter(hidden = true) @RequestHeader(value = HeaderConstants.USER_ID) UUID userId,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody ChargePaymentRequest request) {
        log.info("Received charge request from user ID: {} for booking ID: {}", userId, request.getBookingId());

        ChargePaymentCommand command = paymentMapper.toChargeCommand(request, userId, idempotencyKey);
        Payment payment = paymentService.charge(command);
        PaymentResponse response = paymentMapper.toPaymentResponse(payment);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Payment charged successfully", response));
    }

    /**
     * Refunds an existing captured payment synchronously via gateway provider.
     *
     * @param paymentId Payment identifier.
     * @param userId Authenticated user ID.
     * @param idempotencyKey Optional client idempotency header.
     * @param request Optional refund request payload.
     * @return ResponseEntity with updated PaymentResponse payload.
     */
    @PostMapping("/{paymentId}/refund")
    @Operation(
            summary = "Refund payment",
            description = "Synchronously refunds an existing captured payment transaction")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "Payment successfully refunded"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Payment not found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "409",
                description = "Refund declined or invalid payment state")
    })
    public ResponseEntity<ApiResponse<PaymentResponse>> refundPayment(
            @PathVariable UUID paymentId,
            @Parameter(hidden = true) @RequestHeader(value = HeaderConstants.USER_ID) UUID userId,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody(required = false) RefundPaymentRequest request) {
        log.info("Received refund request for payment ID: {} from user ID: {}", paymentId, userId);

        Payment existingPayment = paymentService.findById(paymentId);
        String reason = request != null ? request.getReason() : "USER_REQUESTED";

        RefundPaymentCommand command = paymentMapper.toRefundCommand(
                paymentId,
                existingPayment.getBookingId(),
                userId,
                existingPayment.getAmount(),
                existingPayment.getCurrency(),
                reason,
                idempotencyKey);

        Payment refundedPayment = paymentService.refund(command);
        PaymentResponse response = paymentMapper.toPaymentResponse(refundedPayment);

        return ResponseEntity.ok(ApiResponse.success("Payment refunded successfully", response));
    }

    /**
     * Retrieves details of a specific payment by ID.
     *
     * @param paymentId Payment identifier.
     * @return ResponseEntity with PaymentResponse payload.
     */
    @GetMapping("/{paymentId}")
    @Operation(summary = "Get payment details", description = "Fetches details of a specific payment by ID")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "Payment details retrieved"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Payment not found")
    })
    public ResponseEntity<ApiResponse<PaymentResponse>> getPayment(@PathVariable UUID paymentId) {
        log.debug("Received request to fetch payment ID: {}", paymentId);

        Payment payment = paymentService.findById(paymentId);
        PaymentResponse response = paymentMapper.toPaymentResponse(payment);

        return ResponseEntity.ok(ApiResponse.success("Payment details retrieved successfully", response));
    }

    /**
     * Retrieves all payments associated with a booking ID.
     *
     * @param bookingId Booking identifier.
     * @return ResponseEntity with List of PaymentResponse.
     */
    @GetMapping("/booking/{bookingId}")
    @Operation(
            summary = "List booking payments",
            description = "Retrieves all payment transactions associated with a booking ID")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "List of payments retrieved")
    })
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getBookingPayments(@PathVariable UUID bookingId) {
        log.debug("Received request to fetch payments for booking ID: {}", bookingId);

        List<Payment> payments = paymentService.findByBookingId(bookingId);
        List<PaymentResponse> responses = paymentMapper.toPaymentResponseList(payments);

        return ResponseEntity.ok(ApiResponse.success("Booking payments retrieved successfully", responses));
    }
}
