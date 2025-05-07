package com.cs308.backend.models;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.annotation.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.*;
import java.time.LocalDateTime;

import com.cs308.backend.models.*;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "order")
public class Order {
    @Id
    private String orderId;
    private String cartId;
    private String userId;
    private String status;
    private String paymentId;
    private List<String> productIds = new ArrayList<>();
    private List<Integer> quantities = new ArrayList<>();
    private boolean paid;
    private boolean shipped;
    private boolean refundRequested;
    private String cardNumber;
    private String expiryDate;
    private String cvv;
    private String invoicePath; // e.g., "invoices/invoice-abc123.pdf"
    private LocalDateTime invoiceSentDate; // New field to store when the invoice was sent
    private List<Double> prices = new ArrayList<>();
}