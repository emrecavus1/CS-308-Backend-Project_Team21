package com.cs308.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1) turn off CSRF (we’ll call this an API, not a browser form)
                .csrf(AbstractHttpConfigurer::disable)

                // 2) stateless session (no cookies)
                .sessionManagement(sm -> sm
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 3) open these three endpoints to anyone
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/signup",
                                "/api/auth/login",
                                "/api/email/verify/**"
                        ).permitAll()
                        // everything else requires authentication
                        .anyRequest().permitAll()
                )

                // 4) enable HTTP‑Basic
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    /**
     * Use BCrypt so we never store plain‑text passwords.
     * Inject this into your UserDetailsService or wherever
     * you hash incoming signup passwords and compare at login.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
