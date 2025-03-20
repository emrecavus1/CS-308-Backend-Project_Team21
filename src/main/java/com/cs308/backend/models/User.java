package com.cs308.backend.models;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.annotation.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "users")
public class User {
    @Id
    private String userId;
    private String email;
    private String password;
    private String name;
    private String surname;
    private String role;
    private String city;
    private String phoneNumber;
    private String specificAddress;

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