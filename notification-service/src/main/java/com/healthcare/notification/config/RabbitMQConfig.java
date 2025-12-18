package com.healthcare.notification.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableRabbit
public class RabbitMQConfig {

    @Value("${rabbitmq.queue.notifications}")
    private String appointmentQueueName;

    @Value("${rabbitmq.queue.billing}")
    private String billingQueueName;

    @Bean
    public Queue notificationsQueue() {
        return new Queue(appointmentQueueName, true);
    }

    @Bean
    public Queue billingNotificationsQueue() {
        return new Queue(billingQueueName, true);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
