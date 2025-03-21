package com.cs308.backend.services;

import com.cs308.backend.models.*;
import com.cs308.backend.repositories.*;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class CartService {
    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public CartService(CartRepository cartRepository, UserRepository userRepository, ProductRepository productRepository) {
        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    public ResponseEntity<String> addToCart(String userId, String productId) {
        Optional<Product> productOptional = productRepository.findById(productId);

        if (!productOptional.isPresent()) {
            return ResponseEntity.badRequest().body("Product not found!");
        }

        Product product = productOptional.get();
        if (product.getStockCount() < 1) {
            return ResponseEntity.badRequest().body("No available stocks!");
        }

        // Reduce stock count
        product.setStockCount(product.getStockCount() - 1);
        productRepository.save(product);

        // Retrieve user's cart
        Optional<Cart> cartOptional = cartRepository.findByUserId(userId);

        Cart cart;
        if (cartOptional.isPresent()) {
            cart = cartOptional.get();
        } else {
            cart = new Cart();
            cart.setUserId(userId);
            cart.setProductIds(new ArrayList<>());
        }

        // Add product to the cart
        cart.getProductIds().add(productId);
        cartRepository.save(cart);

        return ResponseEntity.ok("Product added to cart successfully!");
    }

    public ResponseEntity<String> deleteProductsInCart(String userId) {
        Optional<Cart> cartOptional = cartRepository.findByUserId(userId);

        if (!cartOptional.isPresent()) {
            return ResponseEntity.badRequest().body("Cart not found for this user!");
        }

        Cart cart = cartOptional.get();

        // Remove all products from cart
        cart.getProductIds().clear();
        cartRepository.save(cart);

        return ResponseEntity.ok("Cart has been cleared after successful order!");
    }


    public ResponseEntity<List<Product>> getProductsInCart(String userId) {
        Optional<Cart> cartOptional = cartRepository.findByUserId(userId);

        if (!cartOptional.isPresent()) {
            return ResponseEntity.badRequest().body(null);
        }

        Cart cart = cartOptional.get();
        List<String> productIds = cart.getProductIds();

        List<Product> productsInCart = productRepository.findAllById(productIds);

        return ResponseEntity.ok(productsInCart);
    }
}