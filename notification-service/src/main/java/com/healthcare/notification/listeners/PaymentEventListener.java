package com.healthcare.notification.listeners;

import com.healthcare.notification.events.PaymentEvent;
import com.healthcare.notification.services.NotificationService;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;

@Component
public class PaymentEventListener {

    @Autowired
    private NotificationService notificationService;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DecimalFormat AMOUNT_FORMATTER = new DecimalFormat("#,##0.00");

    @RabbitListener(queues = "billing.notifications.queue")
    @Retry(name = "notificationService")
    public void handlePaymentEvent(PaymentEvent event) {
        System.out.println("Received payment event: " + event.getEventType());

        try {
            String emailSubject;
            String emailMessage;
            String smsMessage;

            switch (event.getEventType()) {
                case "invoice.created":
                    emailSubject = "Nouvelle facture - Système de santé";
                    emailMessage = String.format(
                            "Bonjour %s,\n\n" +
                            "Une nouvelle facture a été générée pour votre consultation.\n\n" +
                            "Numéro de facture: #%d\n" +
                            "Montant: %s MAD\n" +
                            "Statut: En attente de paiement\n\n" +
                            "Vous pouvez consulter et régler votre facture en ligne.\n\n" +
                            "Cordialement,\n" +
                            "Système de santé en ligne - 2025",
                            event.getPatientName(),
                            event.getInvoiceId(),
                            AMOUNT_FORMATTER.format(event.getAmount())
                    );
                    smsMessage = String.format(
                            "Nouvelle facture #%d de %s MAD générée. Consultez vos factures en ligne.",
                            event.getInvoiceId(),
                            AMOUNT_FORMATTER.format(event.getAmount())
                    );
                    break;

                case "payment.received":
                    emailSubject = "Paiement reçu - Système de santé";
                    emailMessage = String.format(
                            "Bonjour %s,\n\n" +
                            "Nous avons bien reçu votre paiement.\n\n" +
                            "Facture: #%d\n" +
                            "Montant payé: %s MAD\n" +
                            "Statut: %s\n\n" +
                            "Merci pour votre paiement.\n\n" +
                            "Cordialement,\n" +
                            "Système de santé en ligne - 2025",
                            event.getPatientName(),
                            event.getInvoiceId(),
                            AMOUNT_FORMATTER.format(event.getAmount()),
                            event.getStatus()
                    );
                    smsMessage = String.format(
                            "Paiement de %s MAD reçu pour la facture #%d. Merci!",
                            AMOUNT_FORMATTER.format(event.getAmount()),
                            event.getInvoiceId()
                    );
                    break;

                case "payment.overdue":
                    emailSubject = "Rappel de paiement - Système de santé";
                    emailMessage = String.format(
                            "Bonjour %s,\n\n" +
                            "Nous vous rappelons que votre facture est en attente de paiement.\n\n" +
                            "Facture: #%d\n" +
                            "Montant dû: %s MAD\n\n" +
                            "Veuillez effectuer le paiement dès que possible.\n\n" +
                            "Cordialement,\n" +
                            "Système de santé en ligne - 2025",
                            event.getPatientName(),
                            event.getInvoiceId(),
                            AMOUNT_FORMATTER.format(event.getAmount())
                    );
                    smsMessage = String.format(
                            "Rappel: Facture #%d de %s MAD en attente de paiement.",
                            event.getInvoiceId(),
                            AMOUNT_FORMATTER.format(event.getAmount())
                    );
                    break;

                default:
                    System.out.println("Unknown payment event type: " + event.getEventType());
                    return;
            }

            // Send SMS notification
            notificationService.sendSMS(event.getPatientEmail(), smsMessage);

            // Send email notification
            notificationService.sendEmail(event.getPatientEmail(), emailSubject, emailMessage);

            System.out.println("Payment notification sent successfully to " + event.getPatientEmail());

        } catch (Exception e) {
            System.err.println("Error handling payment event: " + e.getMessage());
            throw e; // Rethrow to trigger retry
        }
    }
}
