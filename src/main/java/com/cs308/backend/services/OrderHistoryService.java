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
    private final ProductRepository productRepository;

    public OrderHistoryService(OrderHistoryRepository orderHistoryRepository,
                               CartRepository cartRepository,
                               OrderRepository orderRepository,
                               ProductRepository productRepository) {
        this.orderHistoryRepository = orderHistoryRepository;
        this.cartRepository   = cartRepository;
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
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

    public ResponseEntity<List<Product>> getProductsFromPreviousOrders(String userId) {
        // 1) grab every Order with shipped=true
        List<Order> delivered = orderRepository.findByUserIdAndShippedTrue(userId);

        // 2) flatten all their productIds into one big list (and dedupe)
        List<String> allIds = delivered.stream()
                .flatMap(o -> o.getProductIds().stream())
                .distinct()
                .collect(Collectors.toList());

        // 3) if nothing found, 404
        if (allIds.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // 4) load the Products and return
        List<Product> prods = productRepository.findAllById(allIds);
        return ResponseEntity.ok(prods);
    }
}
