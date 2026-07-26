package com.ticketbooking.common.exception;

/**
 * Exception thrown when a requested domain resource or entity is not found in the persistence store.
 */
public class ResourceNotFoundException extends DomainException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a ResourceNotFoundException with resource name and identifier.
     *
     * @param resourceName The entity name (e.g., "Booking", "Show").
     * @param identifier The unique ID that was searched for.
     */
    public ResourceNotFoundException(String resourceName, Object identifier) {
        super("RESOURCE_NOT_FOUND", String.format("%s not found with identifier: %s", resourceName, identifier));
    }

    /**
     * Constructs a ResourceNotFoundException with a custom message.
     *
     * @param message Custom explanation.
     */
    public ResourceNotFoundException(String message) {
        super("RESOURCE_NOT_FOUND", message);
    }
}
