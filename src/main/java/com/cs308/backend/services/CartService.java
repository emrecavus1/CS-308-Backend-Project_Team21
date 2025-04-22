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
        // 1) check product exists & in stock
        Product p = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found!"));
        if (p.getStockCount() < 1) {
            return ResponseEntity.badRequest().body("No available stocks!");
        }

        // ← **NO longer reduce stock here!**

        // 2) fetch-or-create cart
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Cart c = new Cart();
                    c.setUserId(userId);
                    return c;
                });

        // 3) find an existing CartItem or add a new one
        List<CartItem> items = cart.getItems();
        CartItem match = items.stream()
                .filter(ci -> ci.getProductId().equals(productId))
                .findFirst()
                .orElse(null);

        if (match != null) {
            match.setQuantity(match.getQuantity() + 1);
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