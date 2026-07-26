package com.ticketbooking.notification.infrastructure.provider;

import com.ticketbooking.notification.domain.exception.NotificationProcessingException;
import com.ticketbooking.notification.domain.provider.NotificationProvider;
import com.ticketbooking.notification.domain.provider.NotificationProviderResult;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Mock multi-channel notification provider simulating Email, SMS, and Push dispatch.
 */
@Component
@Slf4j
public class FakeNotificationProvider implements NotificationProvider {

    public static final String FAIL_RECIPIENT = "fail@example.com";
    public static final String TIMEOUT_RECIPIENT = "timeout@example.com";

    @Override
    public NotificationProviderResult sendEmail(String recipient, String subject, String body) {
        log.info("Simulating Email send to recipient: {}, subject: {}", recipient, subject);
        return processSimulatedDispatch(recipient, "Email");
    }

    @Override
    public NotificationProviderResult sendSms(String recipient, String message) {
        log.info(
                "Simulating SMS send to recipient: {}, message length: {}",
                recipient,
                message != null ? message.length() : 0);
        return processSimulatedDispatch(recipient, "SMS");
    }

    @Override
    public NotificationProviderResult sendPush(String recipientToken, String title, String message) {
        log.info("Simulating Push send to token: {}, title: {}", recipientToken, title);
        return processSimulatedDispatch(recipientToken, "Push");
    }

    private NotificationProviderResult processSimulatedDispatch(String recipient, String channelName) {
        if (TIMEOUT_RECIPIENT.equalsIgnoreCase(recipient)) {
            log.warn(
                    "Chaos simulation triggered: Provider timeout for channel {} to recipient: {}",
                    channelName,
                    recipient);
            throw new NotificationProcessingException("Provider connection timeout");
        }

        if (FAIL_RECIPIENT.equalsIgnoreCase(recipient) || (recipient != null && recipient.startsWith("+0000"))) {
            log.warn("Simulating provider delivery failure for channel {} to recipient: {}", channelName, recipient);
            return NotificationProviderResult.builder()
                    .successful(false)
                    .failureReason("RECIPIENT_UNREACHABLE")
                    .build();
        }

        String msgId = "MSG-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        log.info(
                "Successfully dispatched {} notification to recipient: {}. Provider Message ID: {}",
                channelName,
                recipient,
                msgId);

        return NotificationProviderResult.builder()
                .successful(true)
                .providerMessageId(msgId)
                .build();
    }
}
