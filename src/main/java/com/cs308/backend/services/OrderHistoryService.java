package com.cs308.backend.services;

import com.cs308.backend.models.*;
import com.cs308.backend.repositories.*;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderHistoryService {


    private final OrderHistoryRepository orderHistoryRepository;
    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;

    public OrderHistoryService(OrderHistoryRepository orderHistoryRepository,
                               CartRepository cartRepository,
                               OrderRepository orderRepository) {
        this.orderHistoryRepository = orderHistoryRepository;
        this.cartRepository   = cartRepository;
        this.orderRepository = orderRepository;
    }

    /**
     * Call this once an order has been paid:
     * 1) Clear the user's cart
     * 2) Add the orderId to their order_history document
     */
    public void recordOrderAndClearCart(String userId, String orderId) {
        // clear all items
        cartRepository.findByUserId(userId)
                .ifPresent(cart -> {
                    cart.getItems().clear();
                    cartRepository.save(cart);
                });

        // append to history
        OrderHistory history = orderHistoryRepository.findByUserId(userId)
                .orElseGet(() -> {
                    OrderHistory oh = new OrderHistory();
                    oh.setOrderHistoryId(UUID.randomUUID().toString());
                    oh.setUserId(userId);
                    oh.setOrderIds(new ArrayList<>());
                    return oh;
                });

        history.getOrderIds().add(orderId);
        orderHistoryRepository.save(history);
    }

    /**
     * Returns all order‑IDs this user has ever placed.
     * 404 if no history yet.
     */
    public ResponseEntity<List<String>> getHistoryByUser(String userId) {
        return orderHistoryRepository.findByUserId(userId)
                .map(h -> ResponseEntity.ok(h.getOrderIds()))
                .orElse(ResponseEntity.notFound().build());
    }

    public ResponseEntity<List<String>> viewPreviousOrdersByUser(String userId) {
        // find all orders shipped=true
        List<Order> shippedOrders = orderRepository.findByUserIdAndShippedTrue(userId);
        List<String> ids = shippedOrders.stream()
                .map(Order::getOrderId)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ids);
    }

    public ResponseEntity<List<String>> viewActiveOrdersByUser(String userId) {
        // find all orders shipped=true
        List<Order> shippedOrders = orderRepository.findByUserIdAndShippedFalse(userId);
        List<String> ids = shippedOrders.stream()
                .map(Order::getOrderId)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ids);
    }
}
