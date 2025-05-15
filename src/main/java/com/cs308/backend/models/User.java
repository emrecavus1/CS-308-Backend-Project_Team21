package com.cs308.backend.models;

import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

import java.util.*;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "users")
public class User extends Auditable {
    @Id
    private String userId;
    @Indexed(unique = true) // Ensure email uniqueness if desired
    private String email;
    private String password;
    private String name;
    private String surname;
    private String role;
    private String city;
    private String phoneNumber;
    private String specificAddress;
    private String token;
    private List<String> wishList;
    private String taxId;

    // Tokens associated with the user; stored as references to SecureToken documents
    @DBRef
    private Set<SecureToken> tokens = new HashSet<>();

    public User(String email, String password, String name, String surname, String role, String specificAddress, String city, String phoneNumber) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.surname = surname;
        this.role = role;
        this.specificAddress = specificAddress;
        this.city = city;
        this.phoneNumber = phoneNumber;
    }

}