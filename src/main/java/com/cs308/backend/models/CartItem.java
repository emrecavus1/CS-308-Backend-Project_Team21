package com.cs308.backend.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A single product + quantity inside a user's cart.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {
    /** The product this line refers to */
    private String productId;
    /** How many of that product the user wants */
    private int quantity;
}
