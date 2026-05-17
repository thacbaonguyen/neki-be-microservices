package com.thacbao.notificationservice.listener;

import com.thacbao.common.event.PasswordResetEvent;
import com.thacbao.common.event.UserRegisteredEvent;
import com.thacbao.notificationservice.service.EmailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserEventListenerTest {

    @Mock private EmailService emailService;
    @InjectMocks private UserEventListener listener;

    @Test
    void handleUserRegistered_otpType_sendsOtpEmail() {
        UserRegisteredEvent event = UserRegisteredEvent.builder()
                .email("test@test.com").fullName("Test User")
                .otpCode("123456").type("OTP_VERIFICATION").build();

        listener.handleUserRegistered(event);

        verify(emailService).sendOtpEmail("test@test.com", "123456");
        verify(emailService, never()).sendWelcomeEmail(anyString(), anyString());
    }

    @Test
    void handleUserRegistered_welcomeType_sendsWelcomeEmail() {
        UserRegisteredEvent event = UserRegisteredEvent.builder()
                .email("test@test.com").fullName("Test User")
                .type("WELCOME").build();

        listener.handleUserRegistered(event);

        verify(emailService).sendWelcomeEmail("test@test.com", "Test User");
        verify(emailService, never()).sendOtpEmail(anyString(), anyString());
    }

    @Test
    void handleUserRegistered_unknownType_doesNothing() {
        UserRegisteredEvent event = UserRegisteredEvent.builder()
                .email("test@test.com").fullName("Test User")
                .type("UNKNOWN").build();

        listener.handleUserRegistered(event);

        verify(emailService, never()).sendOtpEmail(anyString(), anyString());
        verify(emailService, never()).sendWelcomeEmail(anyString(), anyString());
    }

    @Test
    void handleUserRegistered_emailFails_logsError() {
        UserRegisteredEvent event = UserRegisteredEvent.builder()
                .email("test@test.com").otpCode("123456").type("OTP_VERIFICATION").build();
        doThrow(new RuntimeException("Mail error")).when(emailService).sendOtpEmail(anyString(), anyString());

        assertDoesNotThrow(() -> listener.handleUserRegistered(event));
    }

    @Test
    void handlePasswordReset_sendsEmail() {
        PasswordResetEvent event = PasswordResetEvent.builder()
                .email("test@test.com").otpCode("654321").build();

        listener.handlePasswordReset(event);

        verify(emailService).sendPasswordResetEmail("test@test.com", "654321");
    }

    @Test
    void handlePasswordReset_emailFails_logsError() {
        PasswordResetEvent event = PasswordResetEvent.builder()
                .email("test@test.com").otpCode("654321").build();
        doThrow(new RuntimeException("Mail error")).when(emailService)
                .sendPasswordResetEmail(anyString(), anyString());

        assertDoesNotThrow(() -> listener.handlePasswordReset(event));
    }
}
