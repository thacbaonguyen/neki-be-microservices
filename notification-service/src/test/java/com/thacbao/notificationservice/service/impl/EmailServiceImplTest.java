package com.thacbao.notificationservice.service.impl;

import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

    @Mock private JavaMailSender mailSender;
    @InjectMocks private EmailServiceImpl emailService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailService, "fromEmail", "noreply@neki.com");
        ReflectionTestUtils.setField(emailService, "appName", "NEKI E-Commerce");
        ReflectionTestUtils.setField(emailService, "appUrl", "http://localhost:3000");
    }

    private void setupMailSender() {
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
    }

    @Test
    void sendOtpEmail_success() {
        setupMailSender();

        emailService.sendOtpEmail("test@test.com", "123456");

        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void sendOtpEmail_failure_logsError() {
        when(mailSender.createMimeMessage()).thenThrow(new RuntimeException("Mail error"));

        assertDoesNotThrow(() -> emailService.sendOtpEmail("test@test.com", "123456"));
    }

    @Test
    void sendPasswordResetEmail_success() {
        setupMailSender();

        emailService.sendPasswordResetEmail("test@test.com", "654321");

        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void sendPasswordResetEmail_failure() {
        when(mailSender.createMimeMessage()).thenThrow(new RuntimeException("Mail error"));

        assertDoesNotThrow(() -> emailService.sendPasswordResetEmail("test@test.com", "654321"));
    }

    @Test
    void sendWelcomeEmail_success() {
        setupMailSender();

        emailService.sendWelcomeEmail("test@test.com", "Test User");

        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void sendWelcomeEmail_failure() {
        when(mailSender.createMimeMessage()).thenThrow(new RuntimeException("Mail error"));

        assertDoesNotThrow(() -> emailService.sendWelcomeEmail("test@test.com", "Test User"));
    }

    @Test
    void sendPasswordChangedEmail_success() {
        setupMailSender();

        emailService.sendPasswordChangedEmail("test@test.com", "Test User");

        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void sendPasswordChangedEmail_failure() {
        when(mailSender.createMimeMessage()).thenThrow(new RuntimeException("Mail error"));

        assertDoesNotThrow(() -> emailService.sendPasswordChangedEmail("test@test.com", "Test User"));
    }

    @Test
    void sendAccountBlockedEmail_success() {
        setupMailSender();

        emailService.sendAccountBlockedEmail("test@test.com", "Test User");

        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void sendAccountBlockedEmail_failure() {
        when(mailSender.createMimeMessage()).thenThrow(new RuntimeException("Mail error"));

        assertDoesNotThrow(() -> emailService.sendAccountBlockedEmail("test@test.com", "Test User"));
    }

    @Test
    void sendOrderConfirmationEmail_success() {
        setupMailSender();

        emailService.sendOrderConfirmationEmail("test@test.com", "NEKI-001", "150000");

        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void sendOrderConfirmationEmail_failure() {
        when(mailSender.createMimeMessage()).thenThrow(new RuntimeException("Mail error"));

        assertDoesNotThrow(() -> emailService.sendOrderConfirmationEmail("test@test.com", "NEKI-001", "150000"));
    }

    @Test
    void sendOrderCancelledEmail_success() {
        setupMailSender();

        emailService.sendOrderCancelledEmail("test@test.com", "Test User", "NEKI-001", "Changed mind");

        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void sendOrderCancelledEmail_failure() {
        when(mailSender.createMimeMessage()).thenThrow(new RuntimeException("Mail error"));

        assertDoesNotThrow(() -> emailService.sendOrderCancelledEmail("test@test.com", "Test User", "NEKI-001", "reason"));
    }

    @Test
    void sendOrderStatusUpdatedEmail_success() {
        setupMailSender();

        emailService.sendOrderStatusUpdatedEmail("test@test.com", "Test User", "NEKI-001", "PENDING", "CONFIRMED");

        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void sendOrderStatusUpdatedEmail_failure() {
        when(mailSender.createMimeMessage()).thenThrow(new RuntimeException("Mail error"));

        assertDoesNotThrow(() -> emailService.sendOrderStatusUpdatedEmail("test@test.com", "Test User", "NEKI-001", "PENDING", "CONFIRMED"));
    }

    @Test
    void sendPaymentConfirmationEmail_success() {
        setupMailSender();

        emailService.sendPaymentConfirmationEmail("test@test.com", "NEKI-001", "200000", "PayOS");

        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void sendPaymentConfirmationEmail_failure() {
        when(mailSender.createMimeMessage()).thenThrow(new RuntimeException("Mail error"));

        assertDoesNotThrow(() -> emailService.sendPaymentConfirmationEmail("test@test.com", "NEKI-001", "200000", "PayOS"));
    }
}
