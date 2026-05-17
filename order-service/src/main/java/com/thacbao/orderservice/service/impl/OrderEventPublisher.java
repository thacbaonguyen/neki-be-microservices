package com.thacbao.orderservice.service.impl;

import com.thacbao.common.event.OrderCancelledEvent;
import com.thacbao.common.event.OrderCreatedEvent;
import com.thacbao.common.event.OrderStatusUpdatedEvent;
import com.thacbao.orderservice.model.OutboxEvent;
import com.thacbao.orderservice.repository.OutboxEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static com.thacbao.common.constant.RabbitMQConstants.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderEventPublisher {

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public void publishOrderCreated(OrderCreatedEvent event) {
        log.info("Saving order.created to outbox for order: {}", event.getOrderNumber());
        saveToOutbox("Order", event.getOrderNumber(), "OrderCreatedEvent", event, ORDER_EXCHANGE, ORDER_CREATED_KEY);
    }

    public void publishOrderCancelled(OrderCancelledEvent event) {
        log.info("Saving order.cancelled to outbox for order: {}", event.getOrderNumber());
        saveToOutbox("Order", event.getOrderNumber(), "OrderCancelledEvent", event, ORDER_EXCHANGE, ORDER_CANCELLED_KEY);
    }

    public void publishOrderStatusUpdated(OrderStatusUpdatedEvent event) {
        log.info("Saving order.status.updated to outbox for order: {}", event.getOrderNumber());
        saveToOutbox("Order", event.getOrderNumber(), "OrderStatusUpdatedEvent", event, ORDER_EXCHANGE, ORDER_STATUS_UPDATED_KEY);
    }
    
    private void saveToOutbox(String aggregateType, String aggregateId, String type, Object eventPayload, String exchange, String routingKey) {
        try {
            OutboxEvent outboxEvent = OutboxEvent.builder()
                    .aggregateType(aggregateType)
                    .aggregateId(aggregateId)
                    .type(type)
                    .payload(objectMapper.writeValueAsString(eventPayload))
                    .status("PENDING")
                    .exchange(exchange)
                    .routingKey(routingKey)
                    .build();
            outboxEventRepository.save(outboxEvent);
        } catch (Exception e) {
            log.error("Failed to serialize event {} for aggregate {}", type, aggregateId, e);
            throw new RuntimeException("Failed to save event to outbox", e);
        }
    }
}
