package com.thacbao.paymentservice.config;

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

    // === PAYMENT EXCHANGE ===
    @Bean
    public TopicExchange paymentExchange() {
        return new TopicExchange(PAYMENT_EXCHANGE);
    }

    // === Notification queue for payment events ===
    @Bean
    public Queue notificationPaymentQueue() {
        return new Queue(NOTIFICATION_PAYMENT_QUEUE, true);
    }

    // === Order queue for payment completed ===
    @Bean
    public Queue orderPaymentCompletedQueue() {
        return new Queue(ORDER_PAYMENT_COMPLETED_QUEUE, true);
    }

    // === Order queue for payment failed ===
    @Bean
    public Queue orderPaymentFailedQueue() {
        return new Queue(ORDER_PAYMENT_FAILED_QUEUE, true);
    }

    // === Bindings ===
    @Bean
    public Binding notificationPaymentCompletedBinding() {
        return BindingBuilder.bind(notificationPaymentQueue())
                .to(paymentExchange())
                .with(PAYMENT_COMPLETED_KEY);
    }

    @Bean
    public Binding orderPaymentCompletedBinding() {
        return BindingBuilder.bind(orderPaymentCompletedQueue())
                .to(paymentExchange())
                .with(PAYMENT_COMPLETED_KEY);
    }

    @Bean
    public Binding orderPaymentFailedBinding() {
        return BindingBuilder.bind(orderPaymentFailedQueue())
                .to(paymentExchange())
                .with(PAYMENT_FAILED_KEY);
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
