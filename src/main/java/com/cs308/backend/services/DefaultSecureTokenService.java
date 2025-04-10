package com.cs308.backend.services;

import com.cs308.backend.models.SecureToken;
import com.cs308.backend.repositories.SecureTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class DefaultSecureTokenService implements SecureTokenService {

    private final SecureTokenRepository secureTokenRepository;

    @Autowired
    public DefaultSecureTokenService(SecureTokenRepository secureTokenRepository) {
        this.secureTokenRepository = secureTokenRepository;
    }

    @Override
    public SecureToken createToken(SecureToken token) {
        return secureTokenRepository.save(token);
    }

    @Override
    public Optional<SecureToken> getToken(String token) {
        return secureTokenRepository.findByToken(token);
    }

    @Override
    public void removeToken(String token) {
        secureTokenRepository.removeByToken(token);
    }
}

