package com.healthcare.notification.controllers;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/")
@CrossOrigin(origins = "*")
public class WelcomeController {

    @GetMapping
    public Map<String, Object> welcome() {
        Map<String, Object> response = new HashMap<>();
        response.put("service", "Notification Service");
        response.put("status", "Running");
        response.put("port", 8083);
        response.put("endpoints", Map.of(
            "send-notification", "/api/notifications/send"
        ));
        response.put("description", "Service de gestion des notifications (SMS et Email)");
        return response;
    }
}
