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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final SecureTokenService secureTokenService;
    private final Environment env;

    // Temporary storage for unverified users
    private final Map<String, User> unverifiedUsers = new HashMap<>();

    @Autowired
    public UserService(UserRepository userRepository,
                       EmailService emailService,
                       SecureTokenService secureTokenService,
                       Environment env) {
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.secureTokenService = secureTokenService;
        this.env = env;
    }

    // Check if an email is already registered
    public boolean isEmailTaken(String email) {
        return userRepository.findByEmail(email).isPresent() ||
                unverifiedUsers.values().stream().anyMatch(user -> user.getEmail().equals(email));
    }

    // Register a new user: check if email is taken, save the user, and send a confirmation email.
    public void registerUser(User user) {
        if (isEmailTaken(user.getEmail())) {
            throw new UserAlreadyExistsException("User with email " + user.getEmail() + " already exists");
        }
        User savedUser = userRepository.save(user);
        sendRegistrationConfirmationEmail(savedUser);
    }

    // Sends the registration confirmation email
    public void sendRegistrationConfirmationEmail(User user) {
        // Generate a unique token using UUID
        String token = UUID.randomUUID().toString();

        // Set token expiration to 15 minutes from now
        LocalDateTime expirationTime = LocalDateTime.now().plusMinutes(15);

        // Create a SecureToken valid for 15 minutes
        SecureToken secureToken = new SecureToken(token, expirationTime, user);
        secureTokenService.createToken(secureToken);

        // Create and initialize the email context
        AccountVerificationEmailContext emailContext = new AccountVerificationEmailContext();
        emailContext.init(user);   // Sets fullName, subject, to, etc.
        emailContext.setToken(token);
        emailContext.setExpirationTime(expirationTime);

        // Retrieve base URL from properties (default to "https://localhost:8080")
        String baseURL = env.getProperty("site.base.url.https", "https://localhost:8080");
        emailContext.buildVerificationUrl(baseURL, token);

        // Send the registration email
        try {
            emailService.sendMail(emailContext);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send registration confirmation email", e);
        }
    }


    public void verifyUser(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("Token is empty.");
        }
        // Retrieve and verify the token using your SecureTokenService.
        SecureToken secureToken = secureTokenService.getToken(token)
                .orElseThrow(() -> new UnknownIdentifierException("Token not found."));

        // If needed, check the token's expiration date.
        if (secureToken.getExpiredAt().isBefore(LocalDateTime.now())) {
            throw new InvalidTokenException("Token has expired.");
        }

        // Retrieve the user associated with the token.
        User user = secureToken.getUser();
        if (user == null) {
            throw new UnknownIdentifierException("User not found for token.");
        }

        // Mark the user's account as verified.
        user.setAccountVerified(true);
        userRepository.save(user);

        // Optionally, remove the token from the database.
        // secureTokenService.removeToken(token);
    }

    public boolean isEnabled(String userid) {
        User user = userRepository.findById(userid).orElse(null);
        if (user == null) {
            return false; // User not found, therefore "not enabled"
        }
        return user.isAccountVerified() && !user.isLoginDisabled();
    }

}
