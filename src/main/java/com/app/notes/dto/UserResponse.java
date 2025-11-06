package com.app.notes.dto;

public record UserResponse(
    String email,
    String name,
    String role
) {}
