package com.cs308.backend.services;

import com.cs308.backend.models.*;
import com.cs308.backend.models.CartItem;
import com.cs308.backend.repositories.*;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class CartService {
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;

    public CartService(CartRepository cartRepository, ProductRepository productRepository) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
    }

    public ResponseEntity<String> addToCart(String userId, String productId) {
        // 1) fetch the product and assert it exists
        Product p = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found!"));

        // 2) fetch-or-create the user's cart
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Cart c = new Cart();
                    c.setUserId(userId);
                    return c;
                });

        // 3) look for an existing line in the cart
        List<CartItem> items = cart.getItems();
        CartItem match = items.stream()
                .filter(ci -> ci.getProductId().equals(productId))
                .findFirst()
                .orElse(null);

        // compute what the new quantity would be
        int newQuantity = (match != null ? match.getQuantity() : 0) + 1;

        // 4) enforce stock constraint
        if (p.getStockCount() < newQuantity) {
            return ResponseEntity
                    .badRequest()
                    .body("Cannot add more than " + p.getStockCount() + " of this item to your cart.");
        }

        // 5) commit to cart
        if (match != null) {
            match.setQuantity(newQuantity);
        } else {
            items.add(new CartItem(productId, 1));
        }
        cart.setItems(items);
        cartRepository.save(cart);

        return ResponseEntity.ok("Product added to cart successfully!");
    }

    public ResponseEntity<String> deleteProductsInCart(String userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Cart not found!"));
        cart.getItems().clear();
        cartRepository.save(cart);
        return ResponseEntity.ok("Cart cleared.");
    }

    // in CartService.java
    public ResponseEntity<List<CartItem>> getCartItems(String userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Cart not found!"));
        return ResponseEntity.ok(cart.getItems());
    }

}