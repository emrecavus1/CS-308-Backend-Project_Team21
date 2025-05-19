
package com.cs308.backend.services;

import com.cs308.backend.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UserChecksExtendedTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserChecks userChecks;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }



    @Test
    public void testEmailChecks_EmptyEmail() {
        String email = "";

        ResponseEntity<String> response = userChecks.emailChecks(email);

        assertEquals("Invalid email format", response.getBody());
        assertEquals(400, response.getStatusCodeValue());
    }

    @Test
    public void testPasswordChecks_TooShortPassword() {
        String shortPassword = "P1!";

        ResponseEntity<String> response = userChecks.passwordChecks(shortPassword);

        assertTrue(response.getBody().toLowerCase().contains("password"));
        assertEquals(400, response.getStatusCodeValue());
    }

    @Test
    public void testPasswordChecks_MissingSpecialCharacter() {
        String noSpecial = "Password1";

        ResponseEntity<String> response = userChecks.passwordChecks(noSpecial);

        assertTrue(response.getBody().toLowerCase().contains("password"));
        assertEquals(400, response.getStatusCodeValue());
    }


}
