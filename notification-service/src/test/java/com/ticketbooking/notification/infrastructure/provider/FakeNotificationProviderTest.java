package com.ticketbooking.notification.infrastructure.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ticketbooking.notification.domain.exception.NotificationProcessingException;
import com.ticketbooking.notification.domain.provider.NotificationProviderResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FakeNotificationProviderTest {

    private FakeNotificationProvider fakeNotificationProvider;

    @BeforeEach
    void setUp() {
        fakeNotificationProvider = new FakeNotificationProvider();
    }

    @Test
    @DisplayName("Successful EMAIL: Returns provider message ID with MSG- prefix")
    void testSuccessfulEmail() {
        NotificationProviderResult result = fakeNotificationProvider.sendEmail(
                "user@example.com", "Booking Confirmed", "Your booking is confirmed");

        assertThat(result.isSuccessful()).isTrue();
        assertThat(result.getProviderMessageId()).startsWith("MSG-");
    }

    @Test
    @DisplayName("Successful SMS: Returns provider message ID with MSG- prefix")
    void testSuccessfulSms() {
        NotificationProviderResult result = fakeNotificationProvider.sendSms("+1234567890", "Your OTP is 1234");

        assertThat(result.isSuccessful()).isTrue();
        assertThat(result.getProviderMessageId()).startsWith("MSG-");
    }

    @Test
    @DisplayName("Successful PUSH: Returns provider message ID with MSG- prefix")
    void testSuccessfulPush() {
        NotificationProviderResult result =
                fakeNotificationProvider.sendPush("device-token-xyz", "Booking Update", "Seat assigned");

        assertThat(result.isSuccessful()).isTrue();
        assertThat(result.getProviderMessageId()).startsWith("MSG-");
    }

    @Test
    @DisplayName("Simulated Failure: Returns failed result for fail@example.com or +0000 prefix")
    void testSimulatedFailure() {
        NotificationProviderResult emailResult =
                fakeNotificationProvider.sendEmail(FakeNotificationProvider.FAIL_RECIPIENT, "Subject", "Body");

        assertThat(emailResult.isSuccessful()).isFalse();
        assertThat(emailResult.getFailureReason()).isEqualTo("RECIPIENT_UNREACHABLE");

        NotificationProviderResult smsResult = fakeNotificationProvider.sendSms("+0000111222", "Message");

        assertThat(smsResult.isSuccessful()).isFalse();
        assertThat(smsResult.getFailureReason()).isEqualTo("RECIPIENT_UNREACHABLE");
    }

    @Test
    @DisplayName("Simulated Timeout: Throws NotificationProcessingException for timeout@example.com")
    void testSimulatedTimeout() {
        assertThatThrownBy(() -> fakeNotificationProvider.sendEmail(
                        FakeNotificationProvider.TIMEOUT_RECIPIENT, "Subject", "Body"))
                .isInstanceOf(NotificationProcessingException.class)
                .hasMessageContaining("Provider connection timeout");
    }
}
