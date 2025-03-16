package com.cs308.backend.services;

import com.cs308.backend.models.User;
import com.cs308.backend.repositories.UserRepository;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class UserService {
    private final UserRepository userRepository;

    // Temporary storage for unverified users
    private final Map<String, User> unverifiedUsers = new HashMap<>();

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Check if an email is already registered
    public boolean isEmailTaken(String email) {
        return userRepository.findByEmail(email).isPresent() || unverifiedUsers.values().stream().anyMatch(user -> user.getEmail().equals(email));
    }

    public void registerUser(User user) {
        userRepository.save(user);
    }

}
