package com.thacbao.paymentservice.service.impl;

import com.thacbao.common.event.PaymentCompletedEvent;
import com.thacbao.common.event.PaymentFailedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentEventPublisherTest {

    @Mock private RabbitTemplate rabbitTemplate;
    @InjectMocks private PaymentEventPublisher publisher;

    @Test
    void publishPaymentCompleted() {
        PaymentCompletedEvent event = PaymentCompletedEvent.builder()
                .orderNumber("NEKI-001").userId(1).amount(BigDecimal.valueOf(100000))
                .paymentMethod("PayOS").build();

        publisher.publishPaymentCompleted(event);

        verify(rabbitTemplate).convertAndSend(anyString(), anyString(), eq(event));
    }

    @Test
    void publishPaymentFailed() {
        PaymentFailedEvent event = PaymentFailedEvent.builder()
                .orderNumber("NEKI-001").userId(1).reason("Insufficient funds").build();

        publisher.publishPaymentFailed(event);

        verify(rabbitTemplate).convertAndSend(anyString(), anyString(), eq(event));
    }
}
