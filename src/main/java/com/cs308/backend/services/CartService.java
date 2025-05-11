package com.cs308.backend.services;

import com.cs308.backend.models.*;
import com.cs308.backend.models.CartItem;
import com.cs308.backend.repositories.*;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.util.*;

import org.springframework.http.*;




@Service
public class CartService {
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;

    public CartService(CartRepository cartRepository, ProductRepository productRepository) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
    }

    public ResponseEntity<AddToCartResponse> addToCart(String cartId, String productId) {
        try {
            // 1) verify product exists
            Product p = productRepository.findById(productId)
                    .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));

            // 2) fetch-or-create cart
            Cart cart;
            boolean isNew = false;
            if (cartId == null || !cartRepository.existsById(cartId)) {
                cart = new Cart();
                if (cartId == null) {
                    cartId = UUID.randomUUID().toString().replaceAll("[^a-zA-Z0-9\\-]", "");
                }
                cart.setCartId(cartId);
                isNew = true;
            }
            else {
                cart = cartRepository.findById(cartId)
                        .orElseThrow(); // existsById already checked
            }

            // 3) find or create the CartItem
            List<CartItem> items = cart.getItems();
            CartItem match = items.stream()
                    .filter(ci -> ci.getProductId().equals(productId))
                    .findFirst()
                    .orElse(null);

            int newQty = (match != null ? match.getQuantity() : 0) + 1;
            // 4) enforce stock constraint
            if (p.getStockCount() < newQty) {
                throw new IllegalStateException("Cannot add more than " + p.getStockCount() +
                        " units of this item to your cart.");
            }

            // 5) commit to cart
            if (match != null) {
                match.setQuantity(newQty);
                if (match.getPrice() == 0.0) {
                    match.setPrice(p.getPrice());
                }
            } else {
                // Inside the else block in addToCart method:
                items.add(new CartItem(productId, 1, p.getPrice()));

            }
            cart.setItems(items);
            cartRepository.save(cart);

            AddToCartResponse body = new AddToCartResponse(
                    cartId,
                    (isNew ? "New cart created; " : "") +
                            "You now have " + newQty + "× “" + p.getProductName() + "” in your cart."
            );
            return ResponseEntity.ok(body);

        } catch (IllegalArgumentException | IllegalStateException ex) {
            // turn any known problem into a 400
            AddToCartResponse body = new AddToCartResponse(cartId, ex.getMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(body);
        }
    }

    public void clearCart(String cartId) {
        cartRepository.findById(cartId).ifPresent(c -> {
            c.getItems().clear();
            cartRepository.save(c);
        });
    }

    // in CartService.java
    public List<CartItem> getCartItems(String cartId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new IllegalArgumentException("Cart not found: " + cartId));
        return cart.getItems();
    }


}