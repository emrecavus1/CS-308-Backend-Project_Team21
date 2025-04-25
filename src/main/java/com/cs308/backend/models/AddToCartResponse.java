package com.cs308.backend.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
public class AddToCartResponse {
    private String cartId;
    private String message;
}