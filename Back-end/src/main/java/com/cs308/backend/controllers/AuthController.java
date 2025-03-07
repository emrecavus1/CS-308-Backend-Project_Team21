package com.cs308.backend.controllers;

import com.cs308.backend.models.User;
import com.cs308.backend.repositories.UserRepository;
import com.cs308.backend.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;


@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    private final UserRepository userRepository;

    public AuthController(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }


    /**
     * Checks if a name or surname contains only alphabetic letters and '-'.
     */
    private boolean isValidName(String name) {
        return name.matches("^[A-Za-z-]+$");
    }

    /**
     * Capitalizes the first letter and converts the rest to lowercase.
     */
    private String formatName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return name; // Return as is if null or empty
        }
        return name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
    }

    @PostMapping("/signup")
    public ResponseEntity<String> signUp(@RequestBody User user) {
        // Validate email format
        String[] emailTypes = {"outlook", "gmail", "hotmail", "yahoo", "icloud", "sabanciuniv"};
        String[] emailEndings = {".com", ".edu", ".org"};
        if (user.getEmail() == null || !user.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            return ResponseEntity.badRequest().body("Invalid email format");
        }

        boolean typeFound = false;
        for (String type : emailTypes) {
            if (user.getEmail().contains(type)) {
                if (typeFound) {
                    return ResponseEntity.badRequest().body("Email must contain exactly one email type (e.g., gmail, outlook, etc.).");
                }
                typeFound = true;
            }
        }
        if (!typeFound) {
            return ResponseEntity.badRequest().body("Email must contain one of the allowed email types.");
        }

        // Ensure email ends with exactly one email ending
        boolean endingFound = false;
        for (String ending : emailEndings) {
            if (user.getEmail().endsWith(ending)) {
                if (endingFound) {
                    return ResponseEntity.badRequest().body("Email must end with exactly one of the allowed endings (.com, .edu, .tr, .org).");
                }
                endingFound = true;
            }
        }
        if (!endingFound) {
            return ResponseEntity.badRequest().body("Email must end with .com, .edu, or .org");
        }

        // Validate password (must be at least 8 characters, contain one number & special character)
        if (user.getPassword() == null || user.getPassword().length() < 8) {
            return ResponseEntity.badRequest().body("Password must be at least 8 characters long");
        }

        String passwordPattern = "^(?=.*[0-9])(?=.*[.!@#$%^&*()_+=-])(?=.*[A-Z])(?=.*[a-z]).*$";

        if (!user.getPassword().matches(passwordPattern)) {
            return ResponseEntity.badRequest().body("Password must contain one number & special character & one uppercase letter & one lowercase letter");
        }

        // Check if email is already registered
        if (userService.isEmailTaken(user.getEmail())) {
            return ResponseEntity.badRequest().body("Email already exists");
        }

        if (user.getName() == null || user.getName().length() < 2) {
            return ResponseEntity.badRequest().body("Name must be at least 2 characters long");
        }

        if (!isValidName(user.getName())) {
            return ResponseEntity.badRequest().body("Name must contain only alphabetic letters and '-'");
        }
        user.setName(formatName(user.getName()));

        if (user.getSurname() == null || user.getSurname().length() < 2) {
            return ResponseEntity.badRequest().body("Surname must be at least 2 characters long");
        }

        if (!isValidName(user.getSurname())) {
            return ResponseEntity.badRequest().body("Surname must contain only alphabetic letters and '-'");
        }

        user.setSurname(formatName(user.getSurname()));

        String[] typeOfRoles = {"Customer", "Product Manager", "Sales Manager"};

        boolean roleFound = false;

        for (String role : typeOfRoles) {
            if (user.getRole().equals(role)) {
                roleFound = true;
            }
        }
        if (!roleFound) {
            return ResponseEntity.badRequest().body("Role must be either Customer, Product Manager, or Sales Manager");
        }

        if (!userService.isValidCountry(user.getCountry())) {
            return ResponseEntity.badRequest().body("Invalid country name.");
        }

        if (user.getAddress() == null || user.getAddress().length() < 10) {
            return ResponseEntity.badRequest().body("Address must be at least 10 characters long");
        }

        if (user.getPhoneNumber() == null || !user.getPhoneNumber().matches("\\d{11}")) {
            return ResponseEntity.badRequest().body("Phone number must be exactly 11 digits and contain only numbers.");
        }

        if (!user.getPhoneNumber().startsWith("0"))
        {
            return ResponseEntity.badRequest().body("Phone number must start with 0");
        }

        // Register user
        userService.registerUser(user);
        return ResponseEntity.ok("User registered successfully!");
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody User user) {
        // Find user by email
        Optional<User> existingUser = userRepository.findByEmail(user.getEmail());

        if (existingUser.isEmpty()) {
            return ResponseEntity.badRequest().body("Email not registered");
        }

        // Retrieve the user
        User foundUser = existingUser.get();

        // Check if password matches
        if (!foundUser.getPassword().equals(user.getPassword())) {
            return ResponseEntity.badRequest().body("Incorrect password");
        }

        return ResponseEntity.ok("Login successful!");
    }
}
