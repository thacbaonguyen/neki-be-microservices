package com.thacbao.productservice.listener;

import com.thacbao.common.event.OrderCancelledEvent;
import com.thacbao.productservice.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import static com.thacbao.common.constant.RabbitMQConstants.PRODUCT_ORDER_CANCELLED_QUEUE;
import static com.thacbao.common.constant.RabbitMQConstants.PRODUCT_ORDER_CREATED_QUEUE;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventListener {

    private final ProductService productService;

    @RabbitListener(queues = PRODUCT_ORDER_CANCELLED_QUEUE)
    public void handleOrderCancelled(OrderCancelledEvent event) {
        log.info("Received order cancelled event for order: {}", event.getOrderNumber());
        try {
            if (event.getItems() != null) {
                event.getItems().forEach(item -> {
                    productService.restoreInventory(item.getVariantId(), item.getQuantity());
                    log.info("Restored inventory for variant {} by {}", item.getVariantId(), item.getQuantity());
                });
            }
        } catch (Exception e) {
            log.error("Error processing order cancelled event: {}", event.getOrderNumber(), e);
        }
    }

    @RabbitListener(queues = PRODUCT_ORDER_CREATED_QUEUE)
    public void handleOrderCreated(com.thacbao.common.event.OrderCreatedEvent event) {
        log.info("Received order created event for order {}, confirming inventory.", event.getOrderNumber());
        try {
            if (event.getItems() != null) {
                event.getItems().forEach(item -> {
                    productService.confirmInventory(item.getVariantId(), item.getQuantity());
                    log.info("Confirmed inventory lock for variant {} by {}", item.getVariantId(), item.getQuantity());
                });
            }
        } catch (Exception e) {
            log.error("Error processing order created inventory confirmation: {}", event.getOrderNumber(), e);
        }
    }
}
