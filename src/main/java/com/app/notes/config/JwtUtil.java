package com.app.notes.config;

import java.util.Date;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Component
public class JwtUtil {
    @Value("${app.jwt.secret}")
    private String secret;

    public String generate(String email, String role) {
        return Jwts.builder()
            .setSubject(email)
            .addClaims(Map.of("role", role))
            .setExpiration(new Date(System.currentTimeMillis() + 86400000)) // 24 hours
            .signWith(SignatureAlgorithm.HS256, secret)
            .compact();
    }

    public Claims validate(String token) {
        return Jwts.parser()
            .setSigningKey(secret)
            .parseClaimsJws(token)
            .getBody();
    }
}
