package com.thacbao.notificationservice.listener;

import com.thacbao.common.event.PaymentCompletedEvent;
import com.thacbao.notificationservice.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import static com.thacbao.common.constant.RabbitMQConstants.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventListener {

    private final EmailService emailService;

    @RabbitListener(queues = NOTIFICATION_PAYMENT_QUEUE)
    public void handlePaymentCompleted(PaymentCompletedEvent event) {
        log.info("Received payment.completed event for order: {}", event.getOrderNumber());
        try {
            emailService.sendPaymentConfirmationEmail(
                    event.getUserEmail(),
                    event.getOrderNumber(),
                    event.getAmount().toString(),
                    event.getPaymentMethod()
            );
        } catch (Exception e) {
            log.error("Error handling payment.completed event: {}", e.getMessage());
        }
    }
}
