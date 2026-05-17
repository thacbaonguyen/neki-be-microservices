package com.thacbao.orderservice.service.impl;

import com.thacbao.common.event.OrderCancelledEvent;
import com.thacbao.common.event.OrderCreatedEvent;
import com.thacbao.common.event.OrderStatusUpdatedEvent;
import com.thacbao.orderservice.model.OutboxEvent;
import com.thacbao.orderservice.repository.OutboxEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderEventPublisherTest {

    @Mock private OutboxEventRepository outboxEventRepository;
    @Mock private ObjectMapper objectMapper;

    @InjectMocks
    private OrderEventPublisher publisher;

    @Test
    void publishOrderCreated_savesToOutbox() throws Exception {
        OrderCreatedEvent event = OrderCreatedEvent.builder()
                .orderNumber("NEKI-001").userId(1).userEmail("test@test.com")
                .totalAmount(BigDecimal.valueOf(100000)).finalAmount(BigDecimal.valueOf(130000))
                .items(List.of()).build();
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        publisher.publishOrderCreated(event);

        verify(outboxEventRepository).save(any(OutboxEvent.class));
    }

    @Test
    void publishOrderCancelled_savesToOutbox() throws Exception {
        OrderCancelledEvent event = OrderCancelledEvent.builder()
                .orderNumber("NEKI-001").userId(1).reason("Changed mind")
                .items(List.of()).build();
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        publisher.publishOrderCancelled(event);

        verify(outboxEventRepository).save(any(OutboxEvent.class));
    }

    @Test
    void publishOrderStatusUpdated_savesToOutbox() throws Exception {
        OrderStatusUpdatedEvent event = OrderStatusUpdatedEvent.builder()
                .orderNumber("NEKI-001").userId(1).oldStatus("PENDING").newStatus("CONFIRMED").build();
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        publisher.publishOrderStatusUpdated(event);

        verify(outboxEventRepository).save(any(OutboxEvent.class));
    }

    @Test
    void publishOrderCreated_serializationFails_throws() throws Exception {
        OrderCreatedEvent event = OrderCreatedEvent.builder()
                .orderNumber("NEKI-001").userId(1).items(List.of()).build();
        when(objectMapper.writeValueAsString(any())).thenThrow(new RuntimeException("Serialization error"));

        assertThrows(RuntimeException.class, () -> publisher.publishOrderCreated(event));
    }
}
