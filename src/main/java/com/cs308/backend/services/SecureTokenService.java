package com.cs308.backend.services;

import com.cs308.backend.models.SecureToken;
import java.util.Optional;

public interface SecureTokenService {
    SecureToken createToken(SecureToken token);
    Optional<SecureToken> getToken(String token);
    void removeToken(String token);
}
