package com.ticketbooking.notification.domain.provider;

/**
 * Interface contract for external multi-channel notification provider dispatchers.
 */
public interface NotificationProvider {

    /**
     * Dispatches an email notification.
     *
     * @param recipient Destination email address.
     * @param subject Subject line.
     * @param body Email content body.
     * @return NotificationProviderResult detailing delivery outcome.
     */
    NotificationProviderResult sendEmail(String recipient, String subject, String body);

    /**
     * Dispatches an SMS notification.
     *
     * @param recipient Phone number recipient.
     * @param message Text message content.
     * @return NotificationProviderResult detailing delivery outcome.
     */
    NotificationProviderResult sendSms(String recipient, String message);

    /**
     * Dispatches a Push notification.
     *
     * @param recipientToken Device push token.
     * @param title Push notification title.
     * @param message Push notification body.
     * @return NotificationProviderResult detailing delivery outcome.
     */
    NotificationProviderResult sendPush(String recipientToken, String title, String message);
}
