package com.cs308.backend.models;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.annotation.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

import com.cs308.backend.models.*;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "payment")
public class Payment {
    @Id
    private String paymentId;
    private String orderId;
    private String userId;
    private String cardNumber;
    private String expiryDate;
    private String cvv;
}