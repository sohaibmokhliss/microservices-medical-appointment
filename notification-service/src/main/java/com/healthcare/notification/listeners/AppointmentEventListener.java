package com.healthcare.notification.listeners;

import com.healthcare.notification.events.AppointmentEvent;
import com.healthcare.notification.models.NotificationRequest;
import com.healthcare.notification.services.NotificationService;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

@Component
public class AppointmentEventListener {

    private static final Logger log = LoggerFactory.getLogger(AppointmentEventListener.class);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Autowired
    private NotificationService notificationService;

    @RabbitListener(queues = "${rabbitmq.queue.notifications}")
    public void handleAppointmentEvent(AppointmentEvent event) {
        log.info("Received {} event for appointment ID: {}", event.getEventType(), event.getAppointmentId());

        try {
            switch (event.getEventType()) {
                case "CREATED":
                    handleAppointmentCreated(event);
                    break;
                case "UPDATED":
                    handleAppointmentUpdated(event);
                    break;
                case "CANCELLED":
                    handleAppointmentCancelled(event);
                    break;
                default:
                    log.warn("Unknown event type: {}", event.getEventType());
            }
        } catch (Exception e) {
            log.error("Error processing appointment event: {}", e.getMessage(), e);
        }
    }

    @Retry(name = "notificationService")
    private void handleAppointmentCreated(AppointmentEvent event) {
        log.info("Processing CREATED event for appointment ID: {}", event.getAppointmentId());

        String message = String.format(
                "Bonjour %s %s, votre rendez-vous a été confirmé pour le %s. Motif: %s",
                event.getPatientPrenom(),
                event.getPatientNom(),
                event.getDateHeure().format(formatter),
                event.getMotif()
        );

        // Send SMS
        NotificationRequest smsRequest = new NotificationRequest();
        smsRequest.setType("SMS");
        smsRequest.setDestination(event.getPatientTelephone());
        smsRequest.setMessage(message);
        notificationService.sendNotification(smsRequest);

        // Send Email
        String emailSubject = "Confirmation de rendez-vous";
        NotificationRequest emailRequest = new NotificationRequest();
        emailRequest.setType("EMAIL");
        emailRequest.setDestination(event.getPatientEmail());
        emailRequest.setSubject(emailSubject);
        emailRequest.setMessage(message);
        notificationService.sendNotification(emailRequest);

        log.info("Notifications sent successfully for appointment ID: {}", event.getAppointmentId());
    }

    @Retry(name = "notificationService")
    private void handleAppointmentUpdated(AppointmentEvent event) {
        log.info("Processing UPDATED event for appointment ID: {}", event.getAppointmentId());

        String message = String.format(
                "Bonjour %s %s, votre rendez-vous a été modifié. Nouvelle date: %s. Motif: %s",
                event.getPatientPrenom(),
                event.getPatientNom(),
                event.getDateHeure().format(formatter),
                event.getMotif()
        );

        // Send SMS
        NotificationRequest smsRequest = new NotificationRequest();
        smsRequest.setType("SMS");
        smsRequest.setDestination(event.getPatientTelephone());
        smsRequest.setMessage(message);
        notificationService.sendNotification(smsRequest);

        // Send Email
        String emailSubject = "Modification de rendez-vous";
        NotificationRequest emailRequest = new NotificationRequest();
        emailRequest.setType("EMAIL");
        emailRequest.setDestination(event.getPatientEmail());
        emailRequest.setSubject(emailSubject);
        emailRequest.setMessage(message);
        notificationService.sendNotification(emailRequest);

        log.info("Update notifications sent successfully for appointment ID: {}", event.getAppointmentId());
    }

    @Retry(name = "notificationService")
    private void handleAppointmentCancelled(AppointmentEvent event) {
        log.info("Processing CANCELLED event for appointment ID: {}", event.getAppointmentId());

        String message = String.format(
                "Bonjour %s %s, votre rendez-vous du %s a été annulé.",
                event.getPatientPrenom(),
                event.getPatientNom(),
                event.getDateHeure().format(formatter)
        );

        // Send SMS
        NotificationRequest smsRequest = new NotificationRequest();
        smsRequest.setType("SMS");
        smsRequest.setDestination(event.getPatientTelephone());
        smsRequest.setMessage(message);
        notificationService.sendNotification(smsRequest);

        // Send Email
        String emailSubject = "Annulation de rendez-vous";
        NotificationRequest emailRequest = new NotificationRequest();
        emailRequest.setType("EMAIL");
        emailRequest.setDestination(event.getPatientEmail());
        emailRequest.setSubject(emailSubject);
        emailRequest.setMessage(message);
        notificationService.sendNotification(emailRequest);

        log.info("Cancellation notifications sent successfully for appointment ID: {}", event.getAppointmentId());
    }
}
