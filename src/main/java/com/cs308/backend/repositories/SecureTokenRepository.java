package com.cs308.backend.repositories;

import com.cs308.backend.models.SecureToken;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface SecureTokenRepository extends MongoRepository<SecureToken, String> {
    Optional<SecureToken> findByToken(String token);
    void removeByToken(String token);
}
