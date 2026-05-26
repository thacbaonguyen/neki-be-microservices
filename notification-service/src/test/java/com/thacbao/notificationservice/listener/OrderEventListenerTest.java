package com.thacbao.notificationservice.listener;

import com.thacbao.common.event.OrderCancelledEvent;
import com.thacbao.common.event.OrderCreatedEvent;
import com.thacbao.common.event.OrderStatusUpdatedEvent;
import com.thacbao.notificationservice.service.EmailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderEventListenerTest {

    @Mock private EmailService emailService;
    @InjectMocks private OrderEventListener listener;

    @Test
    void handleOrderCreated_sendsConfirmationEmail() {
        OrderCreatedEvent event = OrderCreatedEvent.builder()
                .orderNumber("NEKI-001").userEmail("test@test.com")
                .finalAmount(BigDecimal.valueOf(200000)).items(List.of()).build();

        listener.handleOrderCreated(event);

        verify(emailService).sendOrderConfirmationEmail("test@test.com", "NEKI-001", "200000");
    }

    @Test
    void handleOrderCreated_emailFails_logsError() {
        OrderCreatedEvent event = OrderCreatedEvent.builder()
                .orderNumber("NEKI-001").userEmail("test@test.com")
                .finalAmount(BigDecimal.valueOf(200000)).items(List.of()).build();
        doThrow(new RuntimeException("Mail error")).when(emailService)
                .sendOrderConfirmationEmail(anyString(), anyString(), anyString());

        assertDoesNotThrow(() -> listener.handleOrderCreated(event));
    }

    @Test
    void handleOrderCancelled_sendsCancelledEmail() {
        OrderCancelledEvent event = OrderCancelledEvent.builder()
                .orderNumber("NEKI-001").userEmail("test@test.com")
                .userFullName("Test User").reason("Changed mind").items(List.of()).build();

        listener.handleOrderCancelled(event);

        verify(emailService).sendOrderCancelledEmail("test@test.com", "Test User", "NEKI-001", "Changed mind");
    }

    @Test
    void handleOrderCancelled_emailFails_logsError() {
        OrderCancelledEvent event = OrderCancelledEvent.builder()
                .orderNumber("NEKI-001").userEmail("test@test.com")
                .userFullName("Test User").reason("reason").items(List.of()).build();
        doThrow(new RuntimeException("Mail error")).when(emailService)
                .sendOrderCancelledEmail(anyString(), anyString(), anyString(), anyString());

        assertDoesNotThrow(() -> listener.handleOrderCancelled(event));
    }

    @Test
    void handleOrderStatusUpdated_sendsUpdateEmail() {
        OrderStatusUpdatedEvent event = OrderStatusUpdatedEvent.builder()
                .orderNumber("NEKI-001").userEmail("test@test.com")
                .userFullName("Test User").oldStatus("PENDING").newStatus("CONFIRMED").build();

        listener.handleOrderStatusUpdated(event);

        verify(emailService).sendOrderStatusUpdatedEmail("test@test.com", "Test User", "NEKI-001", "PENDING", "CONFIRMED");
    }

    @Test
    void handleOrderStatusUpdated_emailFails_logsError() {
        OrderStatusUpdatedEvent event = OrderStatusUpdatedEvent.builder()
                .orderNumber("NEKI-001").userEmail("test@test.com")
                .userFullName("Test User").oldStatus("PENDING").newStatus("CONFIRMED").build();
        doThrow(new RuntimeException("Mail error")).when(emailService)
                .sendOrderStatusUpdatedEmail(anyString(), anyString(), anyString(), anyString(), anyString());

        assertDoesNotThrow(() -> listener.handleOrderStatusUpdated(event));
    }
}
