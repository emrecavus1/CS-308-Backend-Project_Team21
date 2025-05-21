
package com.cs308.backend.services;

import com.cs308.backend.models.SecureToken;
import com.cs308.backend.models.User;
import com.cs308.backend.repositories.SecureTokenRepository;
import com.cs308.backend.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class SecureTokenServiceTest {

    @Mock
    private SecureTokenRepository tokenRepo;

    @Mock
    private UserRepository userRepo;

    @InjectMocks
    private SecureTokenServiceImpl secureTokenService;

    private User testUser;
    private SecureToken testToken;
    private String tokenStr;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        tokenStr = UUID.randomUUID().toString();

        testUser = new User();
        testUser.setEmail("test@example.com");

        testToken = new SecureToken(tokenStr, LocalDateTime.now().plusMinutes(30), testUser);
    }

    @Test
    public void testCreateToken_SuccessfulSave() {
        when(tokenRepo.save(testToken)).thenReturn(testToken);

        SecureToken result = secureTokenService.createToken(testToken);

        assertNotNull(result);
        assertEquals(tokenStr, result.getToken());
        verify(tokenRepo, times(1)).save(testToken);
    }

    @Test
    public void testGetToken_ValidToken_ReturnsToken() {
        when(tokenRepo.findByToken(tokenStr)).thenReturn(Optional.of(testToken));

        Optional<SecureToken> result = secureTokenService.getToken(tokenStr);

        assertTrue(result.isPresent());
        assertEquals(tokenStr, result.get().getToken());
    }

    @Test
    public void testGetToken_ExpiredToken_RemovedAndReturnsEmpty() {
        SecureToken expiredToken = new SecureToken(tokenStr, LocalDateTime.now().minusMinutes(1), testUser);
        when(tokenRepo.findByToken(tokenStr)).thenReturn(Optional.of(expiredToken));

        Optional<SecureToken> result = secureTokenService.getToken(tokenStr);

        assertFalse(result.isPresent());
        verify(tokenRepo, times(1)).removeByToken(tokenStr);
    }

    @Test
    public void testRemoveToken_CallsRepository() {
        doNothing().when(tokenRepo).removeByToken(tokenStr);

        secureTokenService.removeToken(tokenStr);

        verify(tokenRepo, times(1)).removeByToken(tokenStr);
    }

    @Test
    public void testGenerateForUser_ValidUser_GeneratesAndSavesToken() {
        String userId = UUID.randomUUID().toString();
        testUser.setToken(null);
        when(userRepo.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepo.save(any(User.class))).thenReturn(testUser);
        when(tokenRepo.save(any(SecureToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SecureToken generatedToken = secureTokenService.generateForUser(userId, 15);

        assertNotNull(generatedToken.getToken());
        assertTrue(generatedToken.getExpiredAt().isAfter(LocalDateTime.now()));
        assertEquals(testUser, generatedToken.getUser());

        verify(userRepo, times(1)).save(testUser);
        verify(tokenRepo, times(1)).save(any(SecureToken.class));
    }
}
