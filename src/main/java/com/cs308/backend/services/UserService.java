package com.cs308.backend.services;

import com.cs308.backend.exception.InvalidTokenException;
import com.cs308.backend.exception.UnknownIdentifierException;
import com.cs308.backend.exception.UserAlreadyExistsException;
import com.cs308.backend.mailing.AccountVerificationEmailContext;
import com.cs308.backend.mailing.EmailService;
import com.cs308.backend.models.SecureToken;
import com.cs308.backend.models.User;
import com.cs308.backend.models.Product;
import com.cs308.backend.repositories.*;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.*;


@Service
public class UserService {
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final EmailService emailService;
    private final SecureTokenService secureTokenService;
    private final Environment env;
    private final PasswordEncoder passwordEncoder;

    // Temporary storage for unverified users
    private final Map<String, User> unverifiedUsers = new HashMap<>();

    @Autowired
    public UserService(UserRepository userRepository,
                       EmailService emailService,
                       SecureTokenService secureTokenService,
                       Environment env,
                       ProductRepository productRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.secureTokenService = secureTokenService;
        this.env = env;
        this.productRepository = productRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Check if an email is already registered
    public boolean isEmailTaken(String email) {
        return userRepository.findByEmail(email).isPresent() ||
                unverifiedUsers.values().stream().anyMatch(user -> user.getEmail().equals(email));
    }

    public void registerUser(User user) {
        if (isEmailTaken(user.getEmail())) {
            throw new UserAlreadyExistsException("User with email " + user.getEmail() + " already exists");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
    }

    public User authenticate(String email, String rawPassword) {
        return userRepository.findByEmail(email)
                .filter(u -> passwordEncoder.matches(rawPassword, u.getPassword()))
                .orElse(null);
    }


    public ResponseEntity<String> addToWishlist(String userId, String productId) {
        Optional<User> u = userRepository.findById(userId);
        if (u.isEmpty()) {
            return ResponseEntity.badRequest().body("User not found.");
        }
        User user = u.get();
        if (user.getWishList() == null) {
            user.setWishList(new ArrayList<>());
        }
        if (user.getWishList().contains(productId)) {
            return ResponseEntity.badRequest().body("Product already in wishlist.");
        }
        user.getWishList().add(productId);
        userRepository.save(user);
        return ResponseEntity.ok("Product added to wishlist.");
    }

    public ResponseEntity<String> removeFromWishlist(String userId, String productId) {
        Optional<User> u = userRepository.findById(userId);
        if (u.isEmpty()) {
            return ResponseEntity.badRequest().body("User not found.");
        }
        User user = u.get();
        if (user.getWishList() == null || !user.getWishList().remove(productId)) {
            return ResponseEntity.badRequest().body("Product not in wishlist.");
        }
        userRepository.save(user);
        return ResponseEntity.ok("Product removed from wishlist.");
    }

    public ResponseEntity<List<Product>> getWishlist(String userId) {
        Optional<User> u = userRepository.findById(userId);
        if (u.isEmpty()) {
            // you could also return 404 here if you prefer
            return ResponseEntity.badRequest().body(Collections.emptyList());
        }
        User user = u.get();
        List<String> ids = user.getWishList();
        if (ids == null || ids.isEmpty()) {
            return ResponseEntity.ok(Collections.emptyList());
        }
        List<Product> prods = productRepository.findAllById(ids);
        return ResponseEntity.ok(prods);
    }

}
