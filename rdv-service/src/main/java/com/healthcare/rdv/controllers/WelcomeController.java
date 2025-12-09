package com.healthcare.rdv.controllers;

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
        response.put("service", "RDV Service");
        response.put("status", "Running");
        response.put("port", 8082);
        response.put("endpoints", Map.of(
            "rdv", "/api/rdv",
            "rdv-by-docteur", "/api/rdv/docteur/{docteurId}",
            "h2-console", "/h2-console"
        ));
        response.put("description", "Service de gestion des rendez-vous");
        return response;
    }
}
