package com.cs308.backend;

import com.cs308.backend.config.SecurityConfig;
import com.cs308.backend.services.SecureTokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootTest
class ConfigLoadsTest {
    @Autowired PasswordEncoder pe;
    @Autowired SecureTokenService sts;
    @Test void contextLoads() { }
}