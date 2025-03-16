package com.cs308.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // Disable CSRF for now
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/signup").permitAll() // Allow Sign-Up without authentication
                        .anyRequest().permitAll() // Allow all requests (for now)
                );

        return http.build();
    }
}
