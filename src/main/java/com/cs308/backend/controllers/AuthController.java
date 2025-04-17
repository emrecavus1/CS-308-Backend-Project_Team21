package com.cs308.backend.controllers;

import com.cs308.backend.exception.UserAlreadyExistsException;
import com.cs308.backend.models.User;
import com.cs308.backend.repositories.UserRepository;
import com.cs308.backend.services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;


@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    private final UserRepository userRepository;

    private final UserChecks userChecks;


    @Autowired
    public AuthController(UserService userService, UserRepository userRepository, UserChecks userChecks) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.userChecks = userChecks;
    }

    @PostMapping("/signup")
    public ResponseEntity<Object> signUp(@RequestBody User user) {
        List<String> errors = new ArrayList<>();

        // Email validation
        String emailError = userChecks.emailChecks(user.getEmail()).getBody();
        if (!emailError.isEmpty()) errors.add("Email: " + emailError);

        // Password validation
        String passwordError = userChecks.passwordChecks(user.getPassword()).getBody();
        if (!passwordError.isEmpty()) errors.add("Password: " + passwordError);

        // Name validation
        String nameError = userChecks.nameChecks(user.getName(), user.getSurname()).getBody();
        if (!nameError.isEmpty()) errors.add("Name/Surname: " + nameError);

        // Role validation
        String roleError = userChecks.roleChecks(user.getRole()).getBody();
        if (!roleError.isEmpty()) errors.add("Role: " + roleError);

        // City validation
        String cityError = userChecks.cityChecks(user.getCity()).getBody();
        if (!cityError.isEmpty()) errors.add("City: " + cityError);

        // Address validation
        String addressError = userChecks.addressChecks(user.getSpecificAddress()).getBody();
        if (!addressError.isEmpty()) errors.add("Address: " + addressError);

        // Phone number validation
        String phoneNumberError = userChecks.phoneNumberChecks(user.getPhoneNumber()).getBody();
        if (!phoneNumberError.isEmpty()) errors.add("Phone Number: " + phoneNumberError);

        // If any errors exist, return them in a JSON response
        if (!errors.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("errors", errors));
        }

        try {
            userService.registerUser(user);
        } catch (UserAlreadyExistsException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
        }

        return ResponseEntity.ok("Registration successful!");
    }


    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody User user) {
        // Find user by email
        Optional<User> existingUser = userRepository.findByEmail(user.getEmail());

        if (existingUser.isEmpty()) {
            return ResponseEntity.badRequest().body("Email not registered");
        }

        /*if (!userService.isEnabled(existingUser.get().getUserId())) {
            return ResponseEntity.badRequest().body("Email not verified, please check your email box");
        } */
        // Retrieve the user
        User foundUser = existingUser.get();

        // Check if password matches
        if (!foundUser.getPassword().equals(user.getPassword())) {
            return ResponseEntity.badRequest().body("Incorrect password");
        }


        return ResponseEntity.ok("Login successful!");
    }
}
