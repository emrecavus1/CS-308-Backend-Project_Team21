package com.cs308.backend.services;

import com.cs308.backend.models.SecureToken;
import com.cs308.backend.models.User;
import com.cs308.backend.repositories.SecureTokenRepository;
import com.cs308.backend.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class SecureTokenServiceImpl implements SecureTokenService {
    private final SecureTokenRepository tokenRepo;
    private final UserRepository userRepo;

    public SecureTokenServiceImpl(SecureTokenRepository tokenRepo,
                                  UserRepository userRepo) {
        this.tokenRepo = tokenRepo;
        this.userRepo  = userRepo;
    }

    @Override
    public SecureToken createToken(SecureToken token) {
        return tokenRepo.save(token);
    }

    @Override
    public Optional<SecureToken> getToken(String token) {

        return tokenRepo.findByToken(token).filter(t -> {
                    if (t.getExpiredAt().isBefore(LocalDateTime.now())) {
                        // expired → clean up & filter out
                        tokenRepo.removeByToken(token);
                        return false;
                    }
                    return true;
                });
    }

    @Override
    public void removeToken(String token) {
        tokenRepo.removeByToken(token);
    }

    @Override
    public SecureToken generateForUser(String userId, int minutesValid) {
        String t = UUID.randomUUID().toString();
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(minutesValid);

        User u = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("No such user"));

        // Set the token field of the user
        u.setToken(t);
        userRepo.save(u);  // Save the updated user

        SecureToken st = new SecureToken(t, expiry, u);
        return createToken(st);
    }

}
