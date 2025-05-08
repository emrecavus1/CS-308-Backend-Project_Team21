package com.cs308.backend.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RefundItem {
    private String productId;
    private int quantity;
    private double price; // price at purchase time
}
