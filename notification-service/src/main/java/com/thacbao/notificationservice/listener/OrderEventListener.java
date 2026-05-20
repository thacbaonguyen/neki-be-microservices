package com.thacbao.notificationservice.listener;

import com.thacbao.common.event.OrderCancelledEvent;
import com.thacbao.common.event.OrderCreatedEvent;
import com.thacbao.common.event.OrderStatusUpdatedEvent;
import com.thacbao.notificationservice.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import static com.thacbao.common.constant.RabbitMQConstants.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventListener {

    private final EmailService emailService;

    @RabbitListener(queues = NOTIFICATION_ORDER_CREATED_QUEUE)
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("Received order.created event for order: {}", event.getOrderNumber());
        try {
            emailService.sendOrderConfirmationEmail(
                    event.getUserEmail(),
                    event.getOrderNumber(),
                    event.getFinalAmount().toString()
            );
        } catch (Exception e) {
            log.error("Error handling order.created event: {}", e.getMessage());
        }
    }

    @RabbitListener(queues = NOTIFICATION_ORDER_CANCELLED_QUEUE)
    public void handleOrderCancelled(OrderCancelledEvent event) {
        log.info("Received order.cancelled event for order: {}", event.getOrderNumber());
        try {
            emailService.sendOrderCancelledEmail(
                    event.getUserEmail(),
                    event.getUserFullName(),
                    event.getOrderNumber(),
                    event.getReason()
            );
        } catch (Exception e) {
            log.error("Error handling order.cancelled event: {}", e.getMessage());
        }
    }

    @RabbitListener(queues = NOTIFICATION_ORDER_UPDATED_QUEUE)
    public void handleOrderStatusUpdated(OrderStatusUpdatedEvent event) {
        log.info("Received order.status.updated event for order: {}", event.getOrderNumber());
        try {
            emailService.sendOrderStatusUpdatedEmail(
                    event.getUserEmail(),
                    event.getUserFullName(),
                    event.getOrderNumber(),
                    event.getOldStatus(),
                    event.getNewStatus()
            );
        } catch (Exception e) {
            log.error("Error handling order.status.updated event: {}", e.getMessage());
        }
    }
}
