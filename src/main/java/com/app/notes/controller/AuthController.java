package com.app.notes.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.notes.config.JwtUtil;
import com.app.notes.dto.AuthResponse;
import com.app.notes.dto.LoginRequest;
import com.app.notes.service.AuthService;

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
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        String role = auth.roleFor(req.email(), req.password());
        if (role == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid credentials"));
        }
        String token = jwt.generate(req.email(), role);
        return ResponseEntity.ok(new AuthResponse(token, ttl / 1000, role));
    }

    @GetMapping("/me")
    public Map<String, Object> me(java.security.Principal principal) {
        return Map.of("email", principal.getName());
    }
}
