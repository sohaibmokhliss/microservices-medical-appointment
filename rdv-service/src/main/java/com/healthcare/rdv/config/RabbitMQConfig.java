package com.healthcare.rdv.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.exchange.appointments}")
    private String exchangeName;

    @Value("${rabbitmq.queue.notifications}")
    private String queueName;

    @Value("${rabbitmq.routing.key.created}")
    private String routingKeyCreated;

    @Value("${rabbitmq.routing.key.updated}")
    private String routingKeyUpdated;

    @Value("${rabbitmq.routing.key.cancelled}")
    private String routingKeyCancelled;

    @Bean
    public TopicExchange appointmentsExchange() {
        return new TopicExchange(exchangeName);
    }

    @Bean
    public Queue notificationsQueue() {
        return new Queue(queueName, true); // durable queue
    }

    @Bean
    public Binding bindingCreated() {
        return BindingBuilder
                .bind(notificationsQueue())
                .to(appointmentsExchange())
                .with(routingKeyCreated);
    }

    @Bean
    public Binding bindingUpdated() {
        return BindingBuilder
                .bind(notificationsQueue())
                .to(appointmentsExchange())
                .with(routingKeyUpdated);
    }

    @Bean
    public Binding bindingCancelled() {
        return BindingBuilder
                .bind(notificationsQueue())
                .to(appointmentsExchange())
                .with(routingKeyCancelled);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
}
