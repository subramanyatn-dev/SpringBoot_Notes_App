package com.app.notes.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.app.notes.config.JwtUtil;
import com.app.notes.dto.AuthResponse;
import com.app.notes.dto.LoginRequest;
import com.app.notes.dto.RegisterRequest;
import com.app.notes.dto.UserResponse;  // New DTO for user info
import com.app.notes.model.User;
import com.app.notes.service.AuthService;

import jakarta.validation.Valid;  // Add validation

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService auth;
    private final JwtUtil jwt;

    @Value("${app.jwt.ttl}")
    private long ttl;

    public AuthController(AuthService auth, JwtUtil jwt) {
        this.auth = auth;
        this.jwt = jwt;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        try {
            String role = auth.roleFor(req.email(), req.password());
            if (role == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .build();
            }
            String token = jwt.generate(req.email(), role);
            return ResponseEntity.ok(new AuthResponse(token, ttl / 1000, role));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .build();
        }
    }
    
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest req) {
        try {
            User user = auth.register(
                req.name(), 
                req.email(), 
                req.password(), 
                req.role()
            );
            
            return ResponseEntity.ok(new UserResponse(
                user.getEmail(),
                user.getName(),
                user.getRole()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .build();
        }
    }
}
