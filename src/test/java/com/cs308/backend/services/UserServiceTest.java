// 1. UserServiceTest.java
package com.cs308.backend.services;

import com.cs308.backend.exception.UserAlreadyExistsException;
import com.cs308.backend.mailing.EmailService;
import com.cs308.backend.models.Product;
import com.cs308.backend.models.User;
import com.cs308.backend.repositories.ProductRepository;
import com.cs308.backend.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private SecureTokenService secureTokenService;

    @Mock
    private Environment env;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private String testProductId;

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
        testUser.setWishList(new ArrayList<>());

        testProductId = UUID.randomUUID().toString();
    }

    @Test
    public void testRegisterUser_Successful() {
        // Arrange
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act & Assert
        assertDoesNotThrow(() -> userService.registerUser(testUser));
        verify(userRepository, times(1)).save(testUser);
        verify(passwordEncoder, times(1)).encode(anyString());
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
    public void testAuthenticate_Successful() {
        // Arrange
        String rawPassword = "Password1!";
        String encodedPassword = "encodedPassword";
        testUser.setPassword(encodedPassword);

        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(true);

        // Act
        User result = userService.authenticate(testUser.getEmail(), rawPassword);

        // Assert
        assertNotNull(result);
        assertEquals(testUser, result);
    }

    @Test
    public void testAuthenticate_InvalidCredentials() {
        // Arrange
        String rawPassword = "WrongPassword";
        String encodedPassword = "encodedPassword";
        testUser.setPassword(encodedPassword);

        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(false);

        // Act
        User result = userService.authenticate(testUser.getEmail(), rawPassword);

        // Assert
        assertNull(result);
    }

    @Test
    public void testAddToWishlist_Successful() {
        // Arrange
        when(userRepository.findById(testUser.getUserId())).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        ResponseEntity<String> response = userService.addToWishlist(testUser.getUserId(), testProductId);

        // Assert
        assertEquals("Product added to wishlist.", response.getBody());
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(testUser.getWishList().contains(testProductId));
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    public void testRemoveFromWishlist_Successful() {
        // Arrange
        testUser.getWishList().add(testProductId);
        when(userRepository.findById(testUser.getUserId())).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        ResponseEntity<String> response = userService.removeFromWishlist(testUser.getUserId(), testProductId);

        // Assert
        assertEquals("Product removed from wishlist.", response.getBody());
        assertEquals(200, response.getStatusCodeValue());
        assertFalse(testUser.getWishList().contains(testProductId));
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    public void testGetWishlist_Successful() {
        // Arrange
        testUser.getWishList().add(testProductId);
        Product testProduct = new Product();
        testProduct.setProductId(testProductId);

        when(userRepository.findById(testUser.getUserId())).thenReturn(Optional.of(testUser));
        when(productRepository.findAllById(Collections.singletonList(testProductId))).thenReturn(Collections.singletonList(testProduct));

        // Act
        ResponseEntity<List<Product>> response = userService.getWishlist(testUser.getUserId());

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().size());
        assertEquals(testProductId, response.getBody().get(0).getProductId());
    }
}