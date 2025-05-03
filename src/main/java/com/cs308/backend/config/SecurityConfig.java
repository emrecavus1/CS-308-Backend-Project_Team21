package com.cs308.backend.config;

import com.cs308.backend.config.JwtAuthFilter;
import com.cs308.backend.services.SecureTokenService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.http.HttpMethod;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;


@Configuration
@EnableMethodSecurity
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

                        // public endpoints
                        .requestMatchers(
                                "/api/auth/signup",
                                "/api/auth/login",
                                "/api/email/verify/**",
                                "/api/main/cart/**"
                        ).permitAll()

                        // all the /api/order endpoints you want to protect:
                        .requestMatchers(
                                HttpMethod.PUT, "/api/order"                         // processPayment
                        ).authenticated()
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/order/viewPreviousOrders/**",
                                "/api/order/viewActiveOrders/**",
                                "/api/order/previous-products/**"
                        ).authenticated()
                        .requestMatchers(
                                HttpMethod.POST, "/api/order/record"
                        ).authenticated()

                        // everything else under /api/order (like markShipped, cancel, etc.) stays open:
                        .requestMatchers("/api/order/**").permitAll()

                        // and allow any other URL in your app
                        .anyRequest().permitAll()
                )
                // insert your JWT filter
                .addFilterBefore(jwtAuthFilter(),
                        UsernamePasswordAuthenticationFilter.class)
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

