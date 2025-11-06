package com.app.notes.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {
    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sess -> 
                sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers(
                    "/auth/login", 
                    "/auth/register",
                    "/",
                    "/index.html",
                    "/styles.css",
                    "/app.js"
                ).permitAll()
                
                // User & Admin endpoints
                .requestMatchers(HttpMethod.GET, "/**")
                    .hasAnyRole("USER", "ADMIN")
                
                // Admin-only endpoints
                .requestMatchers(HttpMethod.POST, "/**")
                    .hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/**")
                    .hasRole("ADMIN")
                
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
