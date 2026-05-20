package com.thacbao.paymentservice.service.impl;

import com.thacbao.common.event.PaymentCompletedEvent;
import com.thacbao.common.event.PaymentFailedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import static com.thacbao.common.constant.RabbitMQConstants.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishPaymentCompleted(PaymentCompletedEvent event) {
        log.info("Publishing payment.completed event for order: {}", event.getOrderNumber());
        rabbitTemplate.convertAndSend(PAYMENT_EXCHANGE, PAYMENT_COMPLETED_KEY, event);
    }

    public void publishPaymentFailed(PaymentFailedEvent event) {
        log.info("Publishing payment.failed event for order: {}", event.getOrderNumber());
        rabbitTemplate.convertAndSend(PAYMENT_EXCHANGE, PAYMENT_FAILED_KEY, event);
    }
}
