package com.thacbao.notificationservice.config;

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

    // ===== Exchanges =====
    @Bean
    public TopicExchange orderExchange() {
        return new TopicExchange(ORDER_EXCHANGE);
    }

    @Bean
    public TopicExchange paymentExchange() {
        return new TopicExchange(PAYMENT_EXCHANGE);
    }

    @Bean
    public TopicExchange userExchange() {
        return new TopicExchange(USER_EXCHANGE);
    }

    // ===== Queues =====
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
    public Queue notificationPaymentQueue() {
        return new Queue(NOTIFICATION_PAYMENT_QUEUE, true);
    }

    @Bean
    public Queue notificationUserRegisteredQueue() {
        return new Queue(NOTIFICATION_USER_REGISTERED_QUEUE, true);
    }

    @Bean
    public Queue notificationUserPasswordQueue() {
        return new Queue(NOTIFICATION_USER_PASSWORD_QUEUE, true);
    }

    // ===== Bindings =====
    @Bean
    public Binding orderCreatedBinding() {
        return BindingBuilder.bind(notificationOrderCreatedQueue())
                .to(orderExchange()).with(ORDER_CREATED_KEY);
    }

    @Bean
    public Binding orderCancelledBinding() {
        return BindingBuilder.bind(notificationOrderCancelledQueue())
                .to(orderExchange()).with(ORDER_CANCELLED_KEY);
    }

    @Bean
    public Binding orderUpdatedBinding() {
        return BindingBuilder.bind(notificationOrderUpdatedQueue())
                .to(orderExchange()).with(ORDER_STATUS_UPDATED_KEY);
    }

    @Bean
    public Binding paymentCompletedBinding() {
        return BindingBuilder.bind(notificationPaymentQueue())
                .to(paymentExchange()).with(PAYMENT_COMPLETED_KEY);
    }

    @Bean
    public Binding userRegisteredBinding() {
        return BindingBuilder.bind(notificationUserRegisteredQueue())
                .to(userExchange()).with(USER_REGISTERED_KEY);
    }

    @Bean
    public Binding userPasswordBinding() {
        return BindingBuilder.bind(notificationUserPasswordQueue())
                .to(userExchange()).with(USER_FORGOT_PASSWORD_KEY);
    }

    // ===== Message Converter =====
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
