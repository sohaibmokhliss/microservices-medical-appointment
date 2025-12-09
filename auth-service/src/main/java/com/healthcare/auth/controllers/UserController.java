package com.healthcare.auth.controllers;

import com.healthcare.auth.entities.User;
import com.healthcare.auth.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllUsers() {
        List<User> users = userRepository.findAll();
        List<Map<String, Object>> sanitizedUsers = users.stream()
                .map(user -> {
                    Map<String, Object> userMap = new HashMap<>();
                    userMap.put("id", user.getId());
                    userMap.put("username", user.getUsername());
                    userMap.put("email", user.getEmail());
                    userMap.put("role", user.getRole());
                    userMap.put("enabled", user.getEnabled());
                    return userMap;
                })
                .toList();
        return ResponseEntity.ok(sanitizedUsers);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getUserById(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(user -> {
                    Map<String, Object> userMap = new HashMap<>();
                    userMap.put("id", user.getId());
                    userMap.put("username", user.getUsername());
                    userMap.put("email", user.getEmail());
                    userMap.put("role", user.getRole());
                    userMap.put("enabled", user.getEnabled());
                    return ResponseEntity.ok(userMap);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createUser(@RequestBody User user) {
        Map<String, Object> response = new HashMap<>();

        if (userRepository.existsByUsername(user.getUsername())) {
            response.put("error", "Username already exists");
            return ResponseEntity.badRequest().body(response);
        }

        if (userRepository.existsByEmail(user.getEmail())) {
            response.put("error", "Email already exists");
            return ResponseEntity.badRequest().body(response);
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        if (user.getEnabled() == null) {
            user.setEnabled(true);
        }

        User savedUser = userRepository.save(user);

        response.put("id", savedUser.getId());
        response.put("username", savedUser.getUsername());
        response.put("email", savedUser.getEmail());
        response.put("role", savedUser.getRole());
        response.put("enabled", savedUser.getEnabled());
        response.put("message", "User created successfully");

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateUser(@PathVariable Long id, @RequestBody Map<String, Object> updates) {
        return userRepository.findById(id)
                .map(user -> {
                    if (updates.containsKey("username")) {
                        String newUsername = (String) updates.get("username");
                        if (!newUsername.equals(user.getUsername()) && userRepository.existsByUsername(newUsername)) {
                            Map<String, Object> error = new HashMap<>();
                            error.put("error", "Username already exists");
                            return ResponseEntity.badRequest().body(error);
                        }
                        user.setUsername(newUsername);
                    }

                    if (updates.containsKey("email")) {
                        String newEmail = (String) updates.get("email");
                        if (!newEmail.equals(user.getEmail()) && userRepository.existsByEmail(newEmail)) {
                            Map<String, Object> error = new HashMap<>();
                            error.put("error", "Email already exists");
                            return ResponseEntity.badRequest().body(error);
                        }
                        user.setEmail(newEmail);
                    }

                    if (updates.containsKey("password")) {
                        String newPassword = (String) updates.get("password");
                        user.setPassword(passwordEncoder.encode(newPassword));
                    }

                    if (updates.containsKey("role")) {
                        user.setRole((String) updates.get("role"));
                    }

                    if (updates.containsKey("enabled")) {
                        user.setEnabled((Boolean) updates.get("enabled"));
                    }

                    User updatedUser = userRepository.save(user);

                    Map<String, Object> response = new HashMap<>();
                    response.put("id", updatedUser.getId());
                    response.put("username", updatedUser.getUsername());
                    response.put("email", updatedUser.getEmail());
                    response.put("role", updatedUser.getRole());
                    response.put("enabled", updatedUser.getEnabled());
                    response.put("message", "User updated successfully");

                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(user -> {
                    userRepository.delete(user);
                    Map<String, Object> response = new HashMap<>();
                    response.put("message", "User deleted successfully");
                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<Map<String, Object>> toggleUserStatus(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(user -> {
                    user.setEnabled(!user.getEnabled());
                    User updatedUser = userRepository.save(user);

                    Map<String, Object> response = new HashMap<>();
                    response.put("id", updatedUser.getId());
                    response.put("username", updatedUser.getUsername());
                    response.put("enabled", updatedUser.getEnabled());
                    response.put("message", "User status toggled successfully");

                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
