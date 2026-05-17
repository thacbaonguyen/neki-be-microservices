package com.thacbao.orderservice.job;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thacbao.common.event.OrderCreatedEvent;
import com.thacbao.common.event.OrderCancelledEvent;
import com.thacbao.common.event.OrderStatusUpdatedEvent;
import com.thacbao.orderservice.model.OutboxEvent;
import com.thacbao.orderservice.repository.OutboxEventRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.PageRequest;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OutboxEventRelayTest {

    @Mock private OutboxEventRepository outboxEventRepository;
    @Mock private RabbitTemplate rabbitTemplate;
    @Mock private ObjectMapper objectMapper;

    @InjectMocks
    private OutboxEventRelay relay;

    @Test
    void processOutboxEvents_empty_doesNothing() {
        when(outboxEventRepository.findByStatusOrderByCreatedAtAsc(eq("PENDING"), any(PageRequest.class)))
                .thenReturn(Collections.emptyList());

        relay.processOutboxEvents();

        verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), any(Object.class));
    }

    @Test
    void processOutboxEvents_orderCreatedEvent_publishes() throws Exception {
        OutboxEvent event = OutboxEvent.builder()
                .id(1).type("OrderCreatedEvent").payload("{}")
                .exchange("order.exchange").routingKey("order.created").status("PENDING").build();
        when(outboxEventRepository.findByStatusOrderByCreatedAtAsc(eq("PENDING"), any(PageRequest.class)))
                .thenReturn(List.of(event));
        OrderCreatedEvent parsed = OrderCreatedEvent.builder().orderNumber("NEKI-001").build();
        when(objectMapper.readValue("{}", OrderCreatedEvent.class)).thenReturn(parsed);

        relay.processOutboxEvents();

        verify(rabbitTemplate).convertAndSend("order.exchange", "order.created", parsed);
        assertEquals("PROCESSED", event.getStatus());
    }

    @Test
    void processOutboxEvents_orderCancelledEvent_publishes() throws Exception {
        OutboxEvent event = OutboxEvent.builder()
                .id(2).type("OrderCancelledEvent").payload("{}")
                .exchange("order.exchange").routingKey("order.cancelled").status("PENDING").build();
        when(outboxEventRepository.findByStatusOrderByCreatedAtAsc(eq("PENDING"), any(PageRequest.class)))
                .thenReturn(List.of(event));
        OrderCancelledEvent parsed = OrderCancelledEvent.builder().orderNumber("NEKI-002").build();
        when(objectMapper.readValue("{}", OrderCancelledEvent.class)).thenReturn(parsed);

        relay.processOutboxEvents();

        verify(rabbitTemplate).convertAndSend("order.exchange", "order.cancelled", parsed);
        assertEquals("PROCESSED", event.getStatus());
    }

    @Test
    void processOutboxEvents_unknownType_marksFailed() throws Exception {
        OutboxEvent event = OutboxEvent.builder()
                .id(3).type("UnknownEvent").payload("{}")
                .exchange("x").routingKey("y").status("PENDING").build();
        when(outboxEventRepository.findByStatusOrderByCreatedAtAsc(eq("PENDING"), any(PageRequest.class)))
                .thenReturn(List.of(event));

        relay.processOutboxEvents();

        assertEquals("FAILED", event.getStatus());
        verify(outboxEventRepository).save(event);
    }

    @Test
    void processOutboxEvents_publishFails_keepsStatusPending() throws Exception {
        OutboxEvent event = OutboxEvent.builder()
                .id(4).type("OrderCreatedEvent").payload("{}")
                .exchange("order.exchange").routingKey("order.created").status("PENDING").build();
        when(outboxEventRepository.findByStatusOrderByCreatedAtAsc(eq("PENDING"), any(PageRequest.class)))
                .thenReturn(List.of(event));
        OrderCreatedEvent parsed = OrderCreatedEvent.builder().orderNumber("NEKI-003").build();
        when(objectMapper.readValue("{}", OrderCreatedEvent.class)).thenReturn(parsed);
        doThrow(new RuntimeException("RabbitMQ down")).when(rabbitTemplate)
                .convertAndSend(anyString(), anyString(), any(Object.class));

        relay.processOutboxEvents();

        assertEquals("PENDING", event.getStatus());
    }

    @Test
    void processOutboxEvents_statusUpdatedEvent_publishes() throws Exception {
        OutboxEvent event = OutboxEvent.builder()
                .id(5).type("OrderStatusUpdatedEvent").payload("{}")
                .exchange("order.exchange").routingKey("order.status.updated").status("PENDING").build();
        when(outboxEventRepository.findByStatusOrderByCreatedAtAsc(eq("PENDING"), any(PageRequest.class)))
                .thenReturn(List.of(event));
        OrderStatusUpdatedEvent parsed = OrderStatusUpdatedEvent.builder()
                .orderNumber("NEKI-004").oldStatus("PENDING").newStatus("CONFIRMED").build();
        when(objectMapper.readValue("{}", OrderStatusUpdatedEvent.class)).thenReturn(parsed);

        relay.processOutboxEvents();

        verify(rabbitTemplate).convertAndSend("order.exchange", "order.status.updated", parsed);
    }
}
