package com.healthcare.auth.controllers;

import com.healthcare.auth.entities.User;
import com.healthcare.auth.models.LoginRequest;
import com.healthcare.auth.models.LoginResponse;
import com.healthcare.auth.models.RegisterRequest;
import com.healthcare.auth.services.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    // Registration disabled - only admin can create users through admin panel
    // @PostMapping("/register")
    // public ResponseEntity<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
    //     LoginResponse response = authService.registerUser(request);
    //
    //     if (response.getToken() == null) {
    //         return ResponseEntity.badRequest().body(response);
    //     }
    //
    //     return ResponseEntity.status(HttpStatus.CREATED).body(response);
    // }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.loginUser(request);

        if (response.getToken() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateToken(@RequestHeader("Authorization") String authHeader) {
        Map<String, Object> response = new HashMap<>();

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.put("valid", false);
            response.put("message", "Invalid authorization header");
            return ResponseEntity.badRequest().body(response);
        }

        String token = authHeader.substring(7);
        Boolean isValid = authService.validateToken(token);

        if (isValid) {
            String username = authService.getUsernameFromToken(token);
            String role = authService.getRoleFromToken(token);

            response.put("valid", true);
            response.put("username", username);
            response.put("role", role);
            return ResponseEntity.ok(response);
        } else {
            response.put("valid", false);
            response.put("message", "Invalid or expired token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        Map<String, Object> response = new HashMap<>();

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.put("error", "Invalid authorization header");
            return ResponseEntity.badRequest().body(response);
        }

        String token = authHeader.substring(7);

        if (!authService.validateToken(token)) {
            response.put("error", "Invalid or expired token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        String username = authService.getUsernameFromToken(token);
        User user = authService.getUserByUsername(username);

        if (user == null) {
            response.put("error", "User not found");
            return ResponseEntity.notFound().build();
        }

        response.put("id", user.getId());
        response.put("username", user.getUsername());
        response.put("email", user.getEmail());
        response.put("role", user.getRole());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/welcome")
    public String welcome() {
        return "Welcome to Authentication Service";
    }
}
