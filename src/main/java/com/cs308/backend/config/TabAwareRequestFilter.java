package com.cs308.backend.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class TabAwareRequestFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String tabId = httpRequest.getParameter("tabId");

        if (tabId != null) {
            // Store tab ID in request attribute for use in controllers
            request.setAttribute("tabId", tabId);

            // Create a tab-specific attribute in the security context
            // This works with your JWT authentication
            if (httpRequest.getSession(false) != null) {
                HttpSession session = httpRequest.getSession();
                session.setAttribute("tabId", tabId);
            }
        }

        chain.doFilter(request, response);
    }
}