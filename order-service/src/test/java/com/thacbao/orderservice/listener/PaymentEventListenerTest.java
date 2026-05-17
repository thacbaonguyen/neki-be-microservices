package com.thacbao.orderservice.listener;

import com.thacbao.common.event.PaymentCompletedEvent;
import com.thacbao.orderservice.service.OrderService;
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

    @Mock private OrderService orderService;
    @InjectMocks private PaymentEventListener listener;

    @Test
    void handlePaymentCompleted_updatesOrderStatus() {
        PaymentCompletedEvent event = PaymentCompletedEvent.builder()
                .orderNumber("NEKI-001").userId(1).amount(BigDecimal.valueOf(100000)).build();

        listener.handlePaymentCompleted(event);

        verify(orderService).updateOrderStatus("NEKI-001", "CONFIRMED");
    }

    @Test
    void handlePaymentCompleted_orderServiceFails_logsError() {
        PaymentCompletedEvent event = PaymentCompletedEvent.builder()
                .orderNumber("NEKI-001").userId(1).build();
        doThrow(new RuntimeException("DB error")).when(orderService).updateOrderStatus(anyString(), anyString());

        assertDoesNotThrow(() -> listener.handlePaymentCompleted(event));
    }
}
