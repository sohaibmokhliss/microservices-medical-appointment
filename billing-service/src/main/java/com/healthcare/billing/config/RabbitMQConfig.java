package com.healthcare.billing.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Billing Exchange and Queue (for publishing billing events)
    public static final String BILLING_EXCHANGE = "billing.exchange";
    public static final String BILLING_NOTIFICATIONS_QUEUE = "billing.notifications.queue";
    public static final String BILLING_ROUTING_KEY = "billing.#";

    // Appointment Queue (for consuming appointment events from RDV service)
    public static final String APPOINTMENT_EXCHANGE = "appointments.exchange";
    public static final String APPOINTMENT_BILLING_QUEUE = "appointment.billing.queue";
    public static final String APPOINTMENT_CREATED_ROUTING_KEY = "appointment.created";

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

    // Billing Exchange for publishing payment/invoice events
    @Bean
    public TopicExchange billingExchange() {
        return new TopicExchange(BILLING_EXCHANGE);
    }

    @Bean
    public Queue billingNotificationsQueue() {
        return new Queue(BILLING_NOTIFICATIONS_QUEUE, true);
    }

    @Bean
    public Binding billingNotificationsBinding() {
        return BindingBuilder
                .bind(billingNotificationsQueue())
                .to(billingExchange())
                .with(BILLING_ROUTING_KEY);
    }

    // Appointment Exchange and Queue for consuming appointment events
    @Bean
    public Queue appointmentBillingQueue() {
        return new Queue(APPOINTMENT_BILLING_QUEUE, true);
    }

    @Bean
    public Binding appointmentBillingBinding() {
        return BindingBuilder
                .bind(appointmentBillingQueue())
                .to(new TopicExchange(APPOINTMENT_EXCHANGE))
                .with(APPOINTMENT_CREATED_ROUTING_KEY);
    }
}
