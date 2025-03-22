package com.cs308.backend.services;

import com.cs308.backend.models.*;
import com.cs308.backend.repositories.*;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public OrderService(OrderRepository orderRepository, CartRepository cartRepository, UserRepository userRepository, ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }


    public ResponseEntity<String> createOrder(String userId) {
        Optional<Cart> cartOptional = cartRepository.findByUserId(userId);

        if (!cartOptional.isPresent()) {
            return ResponseEntity.badRequest().body("Cart not found for user.");
        }

        Cart cart = cartOptional.get();
        if (cart.getProductIds().isEmpty()) {
            return ResponseEntity.badRequest().body("Cart is empty. Cannot place an order.");
        }

        Order newOrder = new Order();
        newOrder.setOrderId(UUID.randomUUID().toString());
        newOrder.setCartId(cart.getCartId());
        newOrder.setUserId(userId);
        newOrder.setStatus("Pending");
        newOrder.setPaid(false);
        newOrder.setShipped(false);

        orderRepository.save(newOrder);
        return ResponseEntity.ok("Order created successfully. Order ID: " + newOrder.getOrderId());
    }

    public List<Order> getOrdersByUser(String userId) {
        return orderRepository.findByUserId(userId);
    }

    public ResponseEntity<String> markAsPaid(String orderId) {
        Optional<Order> optionalOrder = orderRepository.findById(orderId);

        if (!optionalOrder.isPresent()) {
            return ResponseEntity.badRequest().body("Order not found.");
        }

        Order order = optionalOrder.get();
        order.setPaid(true);
        order.setStatus("Paid");
        orderRepository.save(order);
        return ResponseEntity.ok("Order marked as paid.");
    }

    public ResponseEntity<String> markAsShipped(String orderId) {
        Optional<Order> optionalOrder = orderRepository.findById(orderId);

        if (!optionalOrder.isPresent()) {
            return ResponseEntity.badRequest().body("Order not found.");
        }

        Order order = optionalOrder.get();
        order.setShipped(true);
        order.setStatus("Shipped");
        orderRepository.save(order);
        return ResponseEntity.ok("Order marked as shipped.");
    }


}