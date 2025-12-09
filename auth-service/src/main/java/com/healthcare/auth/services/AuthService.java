package com.healthcare.auth.services;

import com.healthcare.auth.entities.User;
import com.healthcare.auth.models.LoginRequest;
import com.healthcare.auth.models.LoginResponse;
import com.healthcare.auth.models.RegisterRequest;
import com.healthcare.auth.repositories.UserRepository;
import com.healthcare.auth.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    public LoginResponse registerUser(RegisterRequest request) {
        // Check if username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            return new LoginResponse(null, null, null, "Username already exists");
        }

        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            return new LoginResponse(null, null, null, "Email already exists");
        }

        // Create new user with hashed password
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setRole(request.getRole());
        user.setEnabled(true);

        userRepository.save(user);

        // Generate JWT token
        String token = jwtUtil.generateToken(user.getUsername(), user.getRole());

        return new LoginResponse(token, user.getUsername(), user.getRole(), "User registered successfully");
    }

    public LoginResponse loginUser(LoginRequest request) {
        // Find user by username
        User user = userRepository.findByUsername(request.getUsername())
                .orElse(null);

        if (user == null) {
            return new LoginResponse(null, null, null, "Invalid username or password");
        }

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return new LoginResponse(null, null, null, "Invalid username or password");
        }

        // Check if user is enabled
        if (!user.getEnabled()) {
            return new LoginResponse(null, null, null, "Account is disabled");
        }

        // Generate JWT token
        String token = jwtUtil.generateToken(user.getUsername(), user.getRole());

        return new LoginResponse(token, user.getUsername(), user.getRole(), "Login successful");
    }

    public Boolean validateToken(String token) {
        return jwtUtil.validateToken(token);
    }

    public String getUsernameFromToken(String token) {
        return jwtUtil.extractUsername(token);
    }

    public String getRoleFromToken(String token) {
        return jwtUtil.extractRole(token);
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }
}
