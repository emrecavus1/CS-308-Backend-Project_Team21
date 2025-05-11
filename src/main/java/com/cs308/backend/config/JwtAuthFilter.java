package com.cs308.backend.config;  // or .config

import com.cs308.backend.services.SecureTokenService;
import com.cs308.backend.models.*;
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
            try {
                SecureToken secToken = tokenService.getToken(rawToken)
                        .orElseThrow(() -> new IllegalArgumentException("Invalid or expired token"));

                User user = secToken.getUser();

                // Use user's role or authorities here if needed
                var auth = new UsernamePasswordAuthenticationToken(
                        user.getUserId(),  // principal
                        null,
                        List.of()          // authorities
                );

                SecurityContextHolder.getContext().setAuthentication(auth);

            } catch (IllegalArgumentException ex) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"" + ex.getMessage() + "\"}");
                return;
            }
        }

        chain.doFilter(request, response);
    }
}
