package com.thacbao.orderservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.thacbao.common.constant.RabbitMQConstants.*;

@Configuration
public class RabbitMQConfig {

    // === ORDER EXCHANGE ===
    @Bean
    public TopicExchange orderExchange() {
        return new TopicExchange(ORDER_EXCHANGE);
    }

    // === Queues for Order events (consumed by other services) ===
    @Bean
    public Queue notificationOrderCreatedQueue() {
        return new Queue(NOTIFICATION_ORDER_CREATED_QUEUE, true);
    }

    @Bean
    public Queue notificationOrderCancelledQueue() {
        return new Queue(NOTIFICATION_ORDER_CANCELLED_QUEUE, true);
    }

    @Bean
    public Queue notificationOrderUpdatedQueue() {
        return new Queue(NOTIFICATION_ORDER_UPDATED_QUEUE, true);
    }

    @Bean
    public Queue productOrderCancelledQueue() {
        return new Queue(PRODUCT_ORDER_CANCELLED_QUEUE, true);
    }

    @Bean
    public Queue recommendationOrderCreatedQueue() {
        return new Queue(RECOMMENDATION_ORDER_CREATED_QUEUE, true);
    }

    // === Bindings ===
    @Bean
    public Binding notificationOrderCreatedBinding() {
        return BindingBuilder.bind(notificationOrderCreatedQueue())
                .to(orderExchange())
                .with(ORDER_CREATED_KEY);
    }

    @Bean
    public Binding notificationOrderCancelledBinding() {
        return BindingBuilder.bind(notificationOrderCancelledQueue())
                .to(orderExchange())
                .with(ORDER_CANCELLED_KEY);
    }

    @Bean
    public Binding notificationOrderUpdatedBinding() {
        return BindingBuilder.bind(notificationOrderUpdatedQueue())
                .to(orderExchange())
                .with(ORDER_STATUS_UPDATED_KEY);
    }

    @Bean
    public Binding productOrderCancelledBinding() {
        return BindingBuilder.bind(productOrderCancelledQueue())
                .to(orderExchange())
                .with(ORDER_CANCELLED_KEY);
    }

    @Bean
    public Binding recommendationOrderCreatedBinding() {
        return BindingBuilder.bind(recommendationOrderCreatedQueue())
                .to(orderExchange())
                .with(ORDER_CREATED_KEY);
    }

    // === Payment event queues (consumed by Order Service) ===
    @Bean
    public Queue paymentCompletedQueue() {
        return new Queue(ORDER_PAYMENT_COMPLETED_QUEUE, true);
    }

    @Bean
    public Queue paymentFailedQueue() {
        return new Queue(ORDER_PAYMENT_FAILED_QUEUE, true);
    }

    // === JSON Message Converter ===
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
}
