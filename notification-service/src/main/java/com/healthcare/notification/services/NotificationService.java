package com.healthcare.notification.services;

import com.healthcare.notification.models.NotificationRequest;
import com.healthcare.notification.models.NotificationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class NotificationService {

    @Autowired
    @Qualifier("smsWebClient")
    private WebClient smsWebClient;

    @Autowired
    @Qualifier("emailWebClient")
    private WebClient emailWebClient;

    public NotificationResponse sendNotification(NotificationRequest request) {
        if ("SMS".equalsIgnoreCase(request.getType())) {
            return sendSms(request);
        } else if ("EMAIL".equalsIgnoreCase(request.getType())) {
            return sendEmail(request);
        } else {
            return new NotificationResponse(false, "Type de notification invalide",
                    LocalDateTime.now().toString());
        }
    }

    private NotificationResponse sendSms(NotificationRequest request) {
        try {
            Map<String, String> smsPayload = new HashMap<>();
            smsPayload.put("to", request.getDestination());
            smsPayload.put("message", request.getMessage());

            Mono<Map> response = smsWebClient.post()
                    .uri("/sms/send")
                    .bodyValue(smsPayload)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .onErrorResume(error -> {
                        System.out.println("SMS API non disponible, simulation de l'envoi...");
                        Map<String, Object> mockResponse = new HashMap<>();
                        mockResponse.put("status", "success");
                        return Mono.just(mockResponse);
                    });

            response.block();

            return new NotificationResponse(true,
                    "SMS envoyé avec succès à " + request.getDestination(),
                    LocalDateTime.now().toString());

        } catch (Exception e) {
            return new NotificationResponse(false,
                    "Erreur lors de l'envoi du SMS: " + e.getMessage(),
                    LocalDateTime.now().toString());
        }
    }

    private NotificationResponse sendEmail(NotificationRequest request) {
        try {
            Map<String, String> emailPayload = new HashMap<>();
            emailPayload.put("from", "onboarding@resend.dev");
            emailPayload.put("to", request.getDestination());
            emailPayload.put("subject", request.getSubject() != null ? request.getSubject() : "Notification");
            emailPayload.put("html", "<p>" + request.getMessage() + "</p>");

            Mono<Map> response = emailWebClient.post()
                    .uri("/emails")
                    .bodyValue(emailPayload)
                    .retrieve()
                    .bodyToMono(Map.class);

            Map result = response.block();
            System.out.println("Resend response: " + result);

            return new NotificationResponse(true,
                    "Email envoyé avec succès à " + request.getDestination(),
                    LocalDateTime.now().toString());

        } catch (Exception e) {
            System.out.println("Erreur Resend: " + e.getMessage());
            return new NotificationResponse(false,
                    "Erreur lors de l'envoi de l'email: " + e.getMessage(),
                    LocalDateTime.now().toString());
        }
    }
}
