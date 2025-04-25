// 2. UserChecksTest.java
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

public class UserChecksTest {

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
    public void testEmailChecks_ValidEmail() {
        // Arrange
        String validEmail = "test@gmail.com";
        when(userService.isEmailTaken(validEmail)).thenReturn(false);

        // Act
        ResponseEntity<String> response = userChecks.emailChecks(validEmail);

        // Assert
        assertEquals("", response.getBody());
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    public void testEmailChecks_InvalidEmailFormat() {
        // Arrange
        String invalidEmail = "testinvalid";

        // Act
        ResponseEntity<String> response = userChecks.emailChecks(invalidEmail);

        // Assert
        assertEquals("Invalid email format", response.getBody());
        assertEquals(400, response.getStatusCodeValue());
    }

    @Test
    public void testPasswordChecks_ValidPassword() {
        // Arrange
        String validPassword = "Password1!";

        // Act
        ResponseEntity<String> response = userChecks.passwordChecks(validPassword);

        // Assert
        assertEquals("", response.getBody());
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    public void testCityChecks_ValidCity() {
        // Arrange
        String validCity = "İSTANBUL";

        // Act
        ResponseEntity<String> response = userChecks.cityChecks(validCity);

        // Assert
        assertEquals("", response.getBody());
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    public void testCityChecks_InvalidCity() {
        // Arrange
        String invalidCity = "INVALIDCITY";

        // Act
        ResponseEntity<String> response = userChecks.cityChecks(invalidCity);

        // Assert
        assertTrue(response.getBody().contains("Invalid city"));
        assertEquals(400, response.getStatusCodeValue());
    }
}