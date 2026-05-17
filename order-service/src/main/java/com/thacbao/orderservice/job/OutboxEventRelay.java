package com.thacbao.orderservice.job;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thacbao.common.event.OrderCancelledEvent;
import com.thacbao.common.event.OrderCreatedEvent;
import com.thacbao.common.event.OrderStatusUpdatedEvent;
import com.thacbao.orderservice.model.OutboxEvent;
import com.thacbao.orderservice.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxEventRelay {

    private final OutboxEventRepository outboxEventRepository;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelay = 5000)
    public void processOutboxEvents() {
        // Find top 50 pending events to avoid memory issues
        List<OutboxEvent> pendingEvents = outboxEventRepository.findByStatusOrderByCreatedAtAsc("PENDING", PageRequest.of(0, 50));
        
        if (pendingEvents.isEmpty()) {
            return;
        }

        log.info("Found {} pending outbox events. Processing...", pendingEvents.size());

        for (OutboxEvent event : pendingEvents) {
            try {
                Object payload = deserializePayload(event);
                if (payload != null) {
                    rabbitTemplate.convertAndSend(event.getExchange(), event.getRoutingKey(), payload);
                    event.setStatus("PROCESSED");
                    outboxEventRepository.save(event);
                    log.info("Successfully pushed outbox event {} to RabbitMQ", event.getId());
                } else {
                    event.setStatus("FAILED");
                    outboxEventRepository.save(event);
                    log.error("Failed to deserialize payload for outbox event {}", event.getId());
                }
            } catch (Exception e) {
                log.error("Failed to process outbox event {}. Will retry later.", event.getId(), e);
                // In production, we'd add logic to mark as FAILED after N retries, but for now we'll keep it PENDING.
            }
        }
    }

    private Object deserializePayload(OutboxEvent event) throws Exception {
        return switch (event.getType()) {
            case "OrderCreatedEvent" -> objectMapper.readValue(event.getPayload(), OrderCreatedEvent.class);
            case "OrderCancelledEvent" -> objectMapper.readValue(event.getPayload(), OrderCancelledEvent.class);
            case "OrderStatusUpdatedEvent" -> objectMapper.readValue(event.getPayload(), OrderStatusUpdatedEvent.class);
            default -> null; // Unknown event type
        };
    }
}
