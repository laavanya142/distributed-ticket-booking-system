package com.ticketbooking.common.constants;

/**
 * Shared HTTP header constants for correlation ID and gateway authentication propagation.
 */
public final class HeaderConstants {

    /**
     * W3C Standard or Custom Correlation ID header name.
     */
    public static final String CORRELATION_ID = "X-Correlation-ID";

    /**
     * W3C Trace Parent header name for OpenTelemetry propagation.
     */
    public static final String TRACE_PARENT = "traceparent";

    /**
     * Authenticated User ID header injected by API Gateway.
     */
    public static final String USER_ID = "X-User-ID";

    /**
     * Authenticated User Roles header injected by API Gateway.
     */
    public static final String USER_ROLES = "X-User-Roles";

    private HeaderConstants() {
        // Prevent instantiation of constant utility class
    }
}
