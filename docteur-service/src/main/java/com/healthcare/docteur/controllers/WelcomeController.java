package com.healthcare.docteur.controllers;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/")
public class WelcomeController {

    @GetMapping
    public Map<String, Object> welcome() {
        Map<String, Object> response = new HashMap<>();
        response.put("service", "Docteur Service");
        response.put("status", "Running");
        response.put("port", 8081);
        response.put("endpoints", Map.of(
            "docteurs", "/api/docteurs",
            "h2-console", "/h2-console"
        ));
        response.put("description", "Service de gestion des docteurs");
        return response;
    }
}
