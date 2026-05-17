package com.thacbao.orderservice.listener;

import com.thacbao.common.event.PaymentCompletedEvent;
import com.thacbao.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import static com.thacbao.common.constant.RabbitMQConstants.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventListener {

    private final OrderService orderService;

    @RabbitListener(queues = ORDER_PAYMENT_COMPLETED_QUEUE)
    public void handlePaymentCompleted(PaymentCompletedEvent event) {
        log.info("Received payment.completed event for order: {}", event.getOrderNumber());
        try {
            orderService.updateOrderStatus(event.getOrderNumber(), "CONFIRMED");
            log.info("Order {} status updated to CONFIRMED", event.getOrderNumber());
        } catch (Exception e) {
            log.error("Failed to update order status for {}: {}", event.getOrderNumber(), e.getMessage());
        }
    }
}
