package com.cs308.backend.repositories;

import com.cs308.backend.models.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByEmail(String email); // ✅ Added this method

}
