package com.cs308.backend.config;

import jakarta.servlet.Filter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig {
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("http://localhost:5173")
                        .allowedMethods("*")
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }
        };
    }

    @Bean
    public FilterRegistrationBean<TabAwareRequestFilter> tabAwareFilter() {
        FilterRegistrationBean<TabAwareRequestFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new TabAwareRequestFilter());
        registrationBean.addUrlPatterns("/*");
        registrationBean.setOrder(1); // Run before JWT filter
        return registrationBean;
    }
}