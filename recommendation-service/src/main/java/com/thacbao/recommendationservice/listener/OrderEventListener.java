package com.thacbao.recommendationservice.listener;

import com.thacbao.common.event.OrderCreatedEvent;
import com.thacbao.recommendationservice.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventListener {

    private final RedisService redisService;

    @RabbitListener(queues = "recommendation.order.created.queue")
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("Received order.created event for user: {}", event.getUserId());
        try {
            // Invalidate user's recommendation cache when they place a new order
            String cacheKey = "recommendation:user:" + event.getUserId();
            redisService.delete(cacheKey);
            log.debug("Invalidated recommendation cache for user {}", event.getUserId());
        } catch (Exception e) {
            log.error("Error handling order.created event: {}", e.getMessage());
        }
    }
}
