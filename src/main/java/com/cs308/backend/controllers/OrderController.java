package com.cs308.backend.controllers;

import com.cs308.backend.models.*;
import com.cs308.backend.repositories.*;
import com.cs308.backend.services.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/order")
public class OrderController {
    private final UserService userService;
    private final CartService cartService;
    private final OrderService orderService;
    private final PaymentService paymentService;


    public OrderController(UserService userService, CartService cartService, OrderService orderService, PaymentService paymentService) {
        this.userService = userService;
        this.cartService = cartService;
        this.orderService = orderService;
        this.paymentService = paymentService;
    }

    @PostMapping("/create")
    public ResponseEntity<String> createOrder(@RequestParam String userId) {
        // The OrderService.createOrder(userId) should handle checking cart details, etc.
        return orderService.createOrder(userId);
    }

    @PutMapping("/payment/{orderId}")
    public ResponseEntity<String> markOrderAsPaid(
            @PathVariable String orderId,
            @RequestParam String userId,
            @RequestParam String cardNumber,
            @RequestParam String expiryDate,
            @RequestParam String cvv) {
        // PaymentService.processPayment handles validation and updating the order's status
        return paymentService.processPayment(userId, orderId, cardNumber, expiryDate, cvv);
    }

    @PutMapping("/markShipped/{orderId}")
    public ResponseEntity<String> markOrderAsShipped(@PathVariable String orderId) {
        return orderService.markAsShipped(orderId);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Order>> getOrdersByUser(@PathVariable String userId) {
        List<Order> orders = orderService.getOrdersByUser(userId);
        return ResponseEntity.ok(orders);
    }
}