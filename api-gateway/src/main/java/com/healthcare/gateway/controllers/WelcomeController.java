package com.healthcare.gateway.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class WelcomeController {

    @GetMapping("/")
    public Map<String, Object> welcome() {
        Map<String, Object> response = new HashMap<>();
        response.put("service", "API Gateway");
        response.put("status", "UP");
        response.put("message", "Healthcare System API Gateway is running");
        response.put("routes", Map.of(
            "docteurs", "/api/docteurs",
            "rdv", "/api/rdv",
            "notifications", "/api/notifications"
        ));
        return response;
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("gateway", "healthy");
        return response;
    }
}
