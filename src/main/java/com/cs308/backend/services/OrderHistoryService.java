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
    private final RefundRequestRepository refundRequestRepository;

    public OrderHistoryService(OrderHistoryRepository orderHistoryRepository,
                               CartRepository cartRepository,
                               OrderRepository orderRepository,
                               ProductRepository productRepository,
                               RefundRequestRepository refundRequestRepository) {
        this.orderHistoryRepository = orderHistoryRepository;
        this.cartRepository   = cartRepository;
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.refundRequestRepository = refundRequestRepository;
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
        List<Order> shippedOrders = orderRepository.findByUserIdAndShippedTrue(userId);

        List<String> ids = shippedOrders.stream()
                .map(Order::getOrderId)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ids);
    }



    public ResponseEntity<List<String>> viewActiveOrdersByUser(String userId) {
        List<Order> allOrders = orderRepository.findByUserIdAndShippedFalse(userId);

        List<String> activeOrderIds = allOrders.stream()
                .filter(order -> {
                    String status = order.getStatus();
                    return "Processing".equalsIgnoreCase(status) || "In-transit".equalsIgnoreCase(status);
                })
                .map(Order::getOrderId)
                .collect(Collectors.toList());

        return ResponseEntity.ok(activeOrderIds);
    }


    public ResponseEntity<List<Product>> getProductsFromPreviousOrders(String userId) {
        // 1) Get all shipped=true orders for the user
        List<Order> deliveredOrders = orderRepository.findByUserIdAndShippedTrue(userId);

        // 2) Filter out refunded orders
        List<Order> nonRefundedOrders = deliveredOrders.stream()
                .filter(order -> !"Refunded".equalsIgnoreCase(order.getStatus()))
                .collect(Collectors.toList());

        // 3) Collect all productIds from non-refunded orders
        Set<String> productIds = nonRefundedOrders.stream()
                .flatMap(order -> order.getProductIds().stream())
                .collect(Collectors.toSet());

        // 4) If empty, return 404
        if (productIds.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // 5) Load and return the products
        List<Product> products = productRepository.findAllById(productIds);
        return ResponseEntity.ok(products);
    }


    public ResponseEntity<String> removeOrderFromHistory(String userId, String orderId) {
        // Find the user's order history
        Optional<OrderHistory> optionalHistory = orderHistoryRepository.findByUserId(userId);

        if (!optionalHistory.isPresent()) {
            return ResponseEntity.badRequest().body("Order history not found for this user.");
        }

        OrderHistory history = optionalHistory.get();

        // Remove the order ID from the history
        boolean removed = history.getOrderIds().remove(orderId);

        if (!removed) {
            return ResponseEntity.badRequest().body("Order not found in user's history.");
        }

        // Save the updated history
        orderHistoryRepository.save(history);

        return ResponseEntity.ok("Order removed from history.");
    }



}
