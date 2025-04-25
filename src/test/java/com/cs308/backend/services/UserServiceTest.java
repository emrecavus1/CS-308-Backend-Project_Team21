// 1. UserServiceTest.java
package com.cs308.backend.services;

import com.cs308.backend.exception.InvalidTokenException;
import com.cs308.backend.exception.UnknownIdentifierException;
import com.cs308.backend.exception.UserAlreadyExistsException;
import com.cs308.backend.mailing.AccountVerificationEmailContext;
import com.cs308.backend.mailing.EmailService;
import com.cs308.backend.models.SecureToken;
import com.cs308.backend.models.User;
import com.cs308.backend.repositories.UserRepository;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.env.Environment;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private SecureTokenService secureTokenService;

    @Mock
    private Environment env;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        testUser = new User();
        testUser.setUserId(UUID.randomUUID().toString());
        testUser.setEmail("test@gmail.com");
        testUser.setPassword("Password1!");
        testUser.setName("Test");
        testUser.setSurname("User");
        testUser.setRole("Customer");
        testUser.setCity("ISTANBUL");
        testUser.setSpecificAddress("123 Test Street, Neighborhood, District");
        testUser.setPhoneNumber("05551234567");
    }

    @Test
    public void testRegisterUser_Successful() throws MessagingException {
        // Arrange
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        doNothing().when(emailService).sendMail(any(AccountVerificationEmailContext.class));
        when(env.getProperty(eq("site.base.url.https"), anyString())).thenReturn("https://localhost:8080");
        when(secureTokenService.createToken(any(SecureToken.class))).thenReturn(new SecureToken("token", LocalDateTime.now().plusMinutes(15), testUser));

        // Act & Assert
        assertDoesNotThrow(() -> userService.registerUser(testUser));
        verify(userRepository, times(1)).save(testUser);
        verify(secureTokenService, times(1)).createToken(any(SecureToken.class));
        verify(emailService, times(1)).sendMail(any(AccountVerificationEmailContext.class));
    }

    @Test
    public void testRegisterUser_EmailAlreadyExists() {
        // Arrange
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));

        // Act & Assert
        UserAlreadyExistsException exception = assertThrows(
                UserAlreadyExistsException.class,
                () -> userService.registerUser(testUser)
        );
        assertEquals("User with email " + testUser.getEmail() + " already exists", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void testVerifyUser_Successful() {
        // Arrange
        String token = "valid-token";
        SecureToken secureToken = new SecureToken(token, LocalDateTime.now().plusMinutes(10), testUser);

        when(secureTokenService.getToken(token)).thenReturn(Optional.of(secureToken));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        assertDoesNotThrow(() -> userService.verifyUser(token));

        // Assert
        assertTrue(testUser.isAccountVerified());
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    public void testVerifyUser_ExpiredToken() {
        // Arrange
        String token = "expired-token";
        SecureToken secureToken = new SecureToken(token, LocalDateTime.now().minusMinutes(10), testUser);

        when(secureTokenService.getToken(token)).thenReturn(Optional.of(secureToken));

        // Act & Assert
        assertThrows(InvalidTokenException.class, () -> userService.verifyUser(token));
        assertFalse(testUser.isAccountVerified());
        verify(userRepository, never()).save(any(User.class));
    }
}