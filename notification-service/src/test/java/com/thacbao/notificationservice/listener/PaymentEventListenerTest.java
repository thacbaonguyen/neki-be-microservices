package com.thacbao.notificationservice.listener;

import com.thacbao.common.event.PaymentCompletedEvent;
import com.thacbao.notificationservice.service.EmailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentEventListenerTest {

    @Mock private EmailService emailService;
    @InjectMocks private PaymentEventListener listener;

    @Test
    void handlePaymentCompleted_sendsEmail() {
        PaymentCompletedEvent event = PaymentCompletedEvent.builder()
                .orderNumber("NEKI-001").userEmail("test@test.com")
                .amount(BigDecimal.valueOf(200000)).paymentMethod("PayOS").build();

        listener.handlePaymentCompleted(event);

        verify(emailService).sendPaymentConfirmationEmail("test@test.com", "NEKI-001", "200000", "PayOS");
    }

    @Test
    void handlePaymentCompleted_emailFails_logsError() {
        PaymentCompletedEvent event = PaymentCompletedEvent.builder()
                .orderNumber("NEKI-001").userEmail("test@test.com")
                .amount(BigDecimal.valueOf(200000)).paymentMethod("PayOS").build();
        doThrow(new RuntimeException("Mail error")).when(emailService)
                .sendPaymentConfirmationEmail(anyString(), anyString(), anyString(), anyString());

        assertDoesNotThrow(() -> listener.handlePaymentCompleted(event));
    }
}
