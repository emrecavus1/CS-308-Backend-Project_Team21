package com.cs308.backend.controllers;


import com.cs308.backend.exception.UserAlreadyExistsException;
import com.cs308.backend.models.*;
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


    private final SecureTokenService tokenService;




    @Autowired
    public AuthController(UserService userService, UserRepository userRepository, UserChecks userChecks, SecureTokenService tokenService) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.userChecks = userChecks;
        this.tokenService = tokenService;
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
        String nameError = userChecks.nameChecks(user.getName()).getBody();
        if (!nameError.isEmpty()) errors.add("Name: " + nameError);


        String surnameError = userChecks.surnameChecks(user.getSurname()).getBody();
        if (!surnameError.isEmpty()) errors.add("Surname: " + surnameError);


        // Role validation
        String roleError = userChecks.roleChecks(user.getRole()).getBody();
        if (!roleError.isEmpty()) errors.add("Role: " + roleError);


        // City validation
        String cityError = userChecks.cityChecks(user.getCity()).getBody();
        if (!cityError.isEmpty()) errors.add("City: " + cityError);


        // Address validation
// Address validation
        String addressError = userChecks.addressChecks(user.getSpecificAddress()).getBody();
        if (!addressError.isEmpty()) errors.add("Specific Address: " + addressError);




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
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", ex.getMessage()));
        }


        return ResponseEntity.ok("Registration successful!");
    }




    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody User req) {
        // Authenticate using provided email/password
        User user = userService.authenticate(req.getEmail(), req.getPassword());
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Generate token
        SecureToken tok = tokenService.generateForUser(user.getUserId(), 120);

        // Prepare response
        Map<String, String> body = new HashMap<>();
        body.put("token", tok.getToken());
        body.put("userId", user.getUserId());
        body.put("name", user.getName());
        body.put("surname", user.getSurname());
        body.put("role", user.getRole());
        body.put("specificAddress", user.getSpecificAddress());
        body.put("email", user.getEmail());

        return ResponseEntity.ok(body);
    }








    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String auth) {
        String token = auth.replace("Bearer ","");
        tokenService.removeToken(token);
        return ResponseEntity.ok().build();
    }


    @GetMapping("/user/{userId}")
    public ResponseEntity<User> getUserById(@PathVariable String userId) {
        return userService.findById(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/all-users")
    public ResponseEntity<List<Map<String, String>>> getAllUsers() {
        List<User> users = userRepository.findAll();
        List<Map<String, String>> result = new ArrayList<>();
        for (User u : users) {
            Map<String, String> entry = new HashMap<>();
            entry.put("userId", u.getUserId());
            entry.put("name", u.getName());
            entry.put("surname", u.getSurname());
            result.add(entry);
        }
        return ResponseEntity.ok(result);
    }

    @PatchMapping("/assign-taxids")
    public ResponseEntity<String> assignTaxIdsToUsers() {
        return userService.assignTaxIdsToUsers();
    }



}
