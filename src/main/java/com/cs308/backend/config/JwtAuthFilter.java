package com.cs308.backend.config;  // or .config

import com.cs308.backend.services.SecureTokenService;
import com.cs308.backend.models.User;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

public class JwtAuthFilter extends OncePerRequestFilter {
    private final SecureTokenService tokenService;

    public JwtAuthFilter(SecureTokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest  request,
            HttpServletResponse response,
            FilterChain         chain
    ) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String rawToken = header.substring(7);
            tokenService.getToken(rawToken).ifPresent(secToken -> {
                User user = secToken.getUser();
                // Build an Authentication and store it in the SecurityContext
                var auth = new UsernamePasswordAuthenticationToken(
                        user.getUserId(),
                        null,
                        List.of()   // ← you can load actual GrantedAuthorities here
                );
                SecurityContextHolder.getContext().setAuthentication(auth);
            });
        }
        // continue down the filter chain
        chain.doFilter(request, response);
    }
}
