package com.cs308.backend.models;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@Setter
@Document(collection = "secure_tokens")
public class SecureToken extends Auditable {

    @Id
    private String id;

    @Indexed(unique = true)
    private String token;

    private LocalDateTime expiredAt;

    @DBRef
    private User user;

    public SecureToken(String token, LocalDateTime expiredAt, User user) {
        this.token = token;
        this.expiredAt = expiredAt;
        this.user = user;
    }
}
