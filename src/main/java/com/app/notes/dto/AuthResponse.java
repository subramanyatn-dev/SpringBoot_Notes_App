package com.app.notes.dto;

public record AuthResponse(String accessToken, long expiresIn, String role) {}
