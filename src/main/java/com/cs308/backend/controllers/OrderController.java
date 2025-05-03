package com.cs308.backend.controllers;

import com.cs308.backend.models.*;
import com.cs308.backend.services.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;



import java.util.List;
import java.util.stream.Collectors;
import java.util.Objects;
import java.util.*;

@RestController
@RequestMapping("/api/order")
public class OrderController {
    private final UserService userService;
    private final CartService cartService;
    private final OrderService orderService;
    private final PaymentService paymentService;
    private final OrderHistoryService orderHistoryService;

    public OrderController(UserService userService,
                           CartService cartService,
                           OrderService orderService,
                           PaymentService paymentService,
                           OrderHistoryService orderHistoryService) {
        this.userService           = userService;
        this.cartService           = cartService;
        this.orderService          = orderService;
        this.paymentService        = paymentService;
        this.orderHistoryService   = orderHistoryService;
    }


    @PutMapping("/order")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> processPayment(
            @CookieValue("CART_ID") String cartId,
            @RequestParam String cardNumber,
            @RequestParam String expiryDate,
            @RequestParam String cvv,
            Authentication auth
    ){
        String userId = auth.getName();
        String orderId = orderService.createOrderFromCart(cartId, userId);

        ResponseEntity<String> result = paymentService.processPayment(
                userId, orderId, cardNumber, expiryDate, cvv);

        if (result.getStatusCode().is2xxSuccessful()) {
            orderHistoryService.recordOrderAndClearCart(userId, orderId);
            cartService.clearCart(cartId);

            // ⭐⭐ Here's the important change:
            return ResponseEntity.ok(orderId);
        }

        return result;
    }



    @PutMapping("/markShipped/{orderId}")
    public ResponseEntity<String> markOrderAsShipped(@PathVariable String orderId) {
        return orderService.markAsShipped(orderId);
    }

    @PutMapping("/markInTransit/{orderId}")
    public ResponseEntity<String> markOrderAsInTransit(@PathVariable String orderId) {
        return orderService.markAsInTransit(orderId);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Map<String, Object>>> getOrdersByUser(@PathVariable String userId) {
        List<Order> orders = orderService.getOrdersByUser(userId);

        List<Map<String, Object>> result = new ArrayList<>();
        for (Order o : orders) {
            if ("cancelled".equalsIgnoreCase(o.getStatus())) continue;  // ✅ Skip cancelled orders

            Map<String, Object> map = new HashMap<>();
            map.put("orderId", o.getOrderId());
            map.put("status", o.getStatus());
            map.put("shipped", o.isShipped());
            result.add(map);
        }

        return ResponseEntity.ok(result);
    }


    // --- NEW ENDPOINTS FOR ORDER HISTORY ---

    /**
     * Manually record a completed order (clears cart & appends to history).
     * You can also trigger this internally instead of calling it via HTTP.
     */
    @PostMapping("/record")
    public ResponseEntity<Void> recordOrder(
            @RequestParam String userId,
            @RequestParam String orderId) {
        orderHistoryService.recordOrderAndClearCart(userId, orderId);
        return ResponseEntity.ok().build();
    }


    @GetMapping("/viewPreviousOrders/{userId}")
    public ResponseEntity<List<Order>> viewPreviousOrders(@PathVariable String userId) {
        // 1) get the raw list of IDs
        List<String> ids = orderHistoryService
                .viewPreviousOrdersByUser(userId)
                .getBody();

        // 2) look up each Order
        List<Order> orders = ids.stream()
                .map(orderService::getOrderById)           // assume you add this helper
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return ResponseEntity.ok(orders);
    }

    @GetMapping("/viewActiveOrders/{userId}")
    public ResponseEntity<List<Order>> viewActiveOrders(@PathVariable String userId) {
        List<String> ids = orderHistoryService
                .viewActiveOrdersByUser(userId)
                .getBody();
        List<Order> orders = ids.stream()
                .map(orderService::getOrderById)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/previous-products/{userId}")
    public ResponseEntity<List<Product>> previousProducts(@PathVariable String userId) {
        return orderHistoryService.getProductsFromPreviousOrders(userId);
    }

    @DeleteMapping("/cancel/{orderId}")
    public ResponseEntity<String> cancelOrder(
            @PathVariable String orderId,
            @RequestParam String userId) {

        // 1. Cancel the order
        ResponseEntity<String> cancelResponse = orderService.cancelOrder(orderId);

        if (!cancelResponse.getStatusCode().is2xxSuccessful()) {
            return cancelResponse;
        }

        // 2. Remove from order history
        ResponseEntity<String> historyResponse = orderHistoryService.removeOrderFromHistory(userId, orderId);

        if (!historyResponse.getStatusCode().is2xxSuccessful()) {
            // Order was cancelled but couldn't be removed from history
            return ResponseEntity.ok("Order cancelled but could not be removed from history: "
                    + historyResponse.getBody());
        }

        return ResponseEntity.ok("Order cancelled and removed from history successfully.");
    }

}
