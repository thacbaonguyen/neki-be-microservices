package com.thacbao.userservice.config;

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

    // Exchange
    @Bean
    public TopicExchange userExchange() {
        return new TopicExchange(USER_EXCHANGE);
    }

    // Queue for user registration events (consumed by notification-service)
    @Bean
    public Queue userRegisteredQueue() {
        return QueueBuilder.durable(NOTIFICATION_USER_REGISTERED_QUEUE).build();
    }

    // Queue for password reset events (consumed by notification-service)
    @Bean
    public Queue passwordResetQueue() {
        return QueueBuilder.durable(NOTIFICATION_USER_PASSWORD_QUEUE).build();
    }

    // Bindings
    @Bean
    public Binding userRegisteredBinding(Queue userRegisteredQueue, TopicExchange userExchange) {
        return BindingBuilder.bind(userRegisteredQueue).to(userExchange).with(USER_REGISTERED_KEY);
    }

    @Bean
    public Binding passwordResetBinding(Queue passwordResetQueue, TopicExchange userExchange) {
        return BindingBuilder.bind(passwordResetQueue).to(userExchange).with(USER_FORGOT_PASSWORD_KEY);
    }

    // JSON message converter
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}
