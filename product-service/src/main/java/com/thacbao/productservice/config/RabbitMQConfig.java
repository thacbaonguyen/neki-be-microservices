package com.thacbao.productservice.config;

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

    // Product service consumes order.cancelled events to restore inventory
    @Bean
    public TopicExchange orderExchange() {
        return new TopicExchange(ORDER_EXCHANGE);
    }

    @Bean
    public Queue productOrderCancelledQueue() {
        return QueueBuilder.durable(PRODUCT_ORDER_CANCELLED_QUEUE).build();
    }

    @Bean
    public Binding productOrderCancelledBinding() {
        return BindingBuilder
                .bind(productOrderCancelledQueue())
                .to(orderExchange())
                .with(ORDER_CANCELLED_KEY);
    }
    
    @Bean
    public Queue productOrderCreatedQueue() {
        return QueueBuilder.durable(PRODUCT_ORDER_CREATED_QUEUE).build();
    }

    @Bean
    public Binding productOrderCreatedBinding() {
        return BindingBuilder
                .bind(productOrderCreatedQueue())
                .to(orderExchange())
                .with(ORDER_CREATED_KEY);
    }

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
