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
            .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/login").permitAll()
                
                // Stream endpoints
                .requestMatchers(HttpMethod.GET, "/streams/**").hasAnyRole("USER", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/streams").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/streams/**").hasRole("ADMIN")
                
                // Semester endpoints
                .requestMatchers(HttpMethod.GET, "/streams/*/semesters/**").hasAnyRole("USER", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/streams/*/semesters").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/streams/*/semesters/**").hasRole("ADMIN")
                
                // Subject endpoints
                .requestMatchers(HttpMethod.GET, "/semesters/*/subjects/**").hasAnyRole("USER", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/semesters/*/subjects").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/semesters/*/subjects/**").hasRole("ADMIN")
                
                // Note endpoints
                .requestMatchers(HttpMethod.GET, "/subjects/*/notes/**").hasAnyRole("USER", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/subjects/*/notes").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/subjects/*/notes/**").hasRole("ADMIN")
                
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
