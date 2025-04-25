package com.cs308.backend.config;

import com.cs308.backend.config.JwtAuthFilter;
import com.cs308.backend.services.SecureTokenService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class SecurityConfig {

    private final SecureTokenService tokenService;

    public SecurityConfig(SecureTokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Bean
    public JwtAuthFilter jwtAuthFilter() {
        return new JwtAuthFilter(tokenService);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/signup",
                                "/api/auth/login",
                                "/api/email/verify/**",
                                "/api/main/cart/**"        // guest‐cart endpoints
                        ).permitAll()
                        .requestMatchers(
                                "/api/order/**"
                        ).authenticated()
                        .anyRequest().permitAll()
                )
                // insert your JWT filter before Spring’s UsernamePasswordAuthenticationFilter
                .addFilterBefore(jwtAuthFilter(),
                        UsernamePasswordAuthenticationFilter.class)
                // if you still want HTTP Basic on any leftover endpoints
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
