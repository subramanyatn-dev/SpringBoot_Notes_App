package com.app.notes.service;

import org.springframework.stereotype.Service;

import com.app.notes.model.User;
import com.app.notes.repository.UserRepository;

@Service
public class AuthService {
    
    private final UserRepository userRepository;
    
    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public String roleFor(String email, String password) {
        // Check hardcoded demo users first
        if ("admin@example.com".equalsIgnoreCase(email) && "1234".equals(password))
            return "ADMIN";
        if ("user@example.com".equalsIgnoreCase(email) && "1234".equals(password))
            return "USER";
        
        // Check database users
        return userRepository.findByEmail(email)
                .filter(user -> user.getPassword().equals(password))
                .map(User::getRole)
                .orElse(null);
    }
    
    public User register(String name, String email, String password, String role) {
        System.out.println("Starting registration process for: " + email);
        
        if (email == null || email.trim().isEmpty()) {
            throw new RuntimeException("Email cannot be empty");
        }
        
        if (password == null || password.trim().isEmpty()) {
            throw new RuntimeException("Password cannot be empty");
        }
        
        if (name == null || name.trim().isEmpty()) {
            throw new RuntimeException("Name cannot be empty");
        }
        
        if (userRepository.existsByEmail(email)) {
            System.out.println("Registration failed: Email already exists: " + email);
            throw new RuntimeException("Email already exists");
        }
        
        // Force all registrations to be USER role only
        // Admins must be created manually via database or curl
        if (!"USER".equals(role)) {
            System.out.println("Registration failed: Invalid role: " + role);
            throw new RuntimeException("Only USER role can be registered. Contact admin for ADMIN access.");
        }
        
        try {
            User user = new User(name, email, password, "USER");
            User savedUser = userRepository.save(user);
            System.out.println("Registration successful for: " + email);
            return savedUser;
        } catch (Exception e) {
            System.err.println("Database error during registration: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Registration failed: Database error");
        }
    }
}

