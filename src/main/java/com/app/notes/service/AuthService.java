package com.app.notes.service;

import org.springframework.stereotype.Service;

@Service
public class AuthService {

    public String roleFor(String email, String password) {
        if ("admin@example.com".equalsIgnoreCase(email) && "1234".equals(password))
            return "ADMIN";
        if ("user@example.com".equalsIgnoreCase(email) && "1234".equals(password))
            return "USER";
        return null;
    }
}
