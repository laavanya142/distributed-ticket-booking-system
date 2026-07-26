package com.ticketbooking.notification.domain.provider;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Result model returned by notification providers.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationProviderResult {

    private boolean successful;
    private String providerMessageId;
    private String failureReason;
}
