package com.healthcare.rdv.services;

import com.healthcare.rdv.entities.Rdv;
import com.healthcare.rdv.events.AppointmentEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AppointmentEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(AppointmentEventPublisher.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.appointments}")
    private String exchangeName;

    @Value("${rabbitmq.routing.key.created}")
    private String routingKeyCreated;

    @Value("${rabbitmq.routing.key.updated}")
    private String routingKeyUpdated;

    @Value("${rabbitmq.routing.key.cancelled}")
    private String routingKeyCancelled;

    public void publishAppointmentCreated(Rdv rdv) {
        AppointmentEvent event = createEventFromRdv("CREATED", rdv);
        log.info("Publishing CREATED event for appointment ID: {}", rdv.getId());
        rabbitTemplate.convertAndSend(exchangeName, routingKeyCreated, event);
        log.info("CREATED event published successfully for appointment ID: {}", rdv.getId());
    }

    public void publishAppointmentUpdated(Rdv rdv) {
        AppointmentEvent event = createEventFromRdv("UPDATED", rdv);
        log.info("Publishing UPDATED event for appointment ID: {}", rdv.getId());
        rabbitTemplate.convertAndSend(exchangeName, routingKeyUpdated, event);
        log.info("UPDATED event published successfully for appointment ID: {}", rdv.getId());
    }

    public void publishAppointmentCancelled(Rdv rdv) {
        AppointmentEvent event = createEventFromRdv("CANCELLED", rdv);
        log.info("Publishing CANCELLED event for appointment ID: {}", rdv.getId());
        rabbitTemplate.convertAndSend(exchangeName, routingKeyCancelled, event);
        log.info("CANCELLED event published successfully for appointment ID: {}", rdv.getId());
    }

    private AppointmentEvent createEventFromRdv(String eventType, Rdv rdv) {
        return new AppointmentEvent(
                eventType,
                rdv.getId(),
                rdv.getDocteurId(),
                rdv.getPatientNom(),
                rdv.getPatientPrenom(),
                rdv.getPatientEmail(),
                rdv.getPatientTelephone(),
                rdv.getDateHeure(),
                rdv.getMotif(),
                rdv.getStatut()
        );
    }
}
