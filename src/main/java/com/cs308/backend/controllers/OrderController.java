package com.cs308.backend.controllers;

import com.cs308.backend.models.*;
import com.cs308.backend.services.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;


import java.time.LocalDateTime;
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
            @CookieValue(value = "TAB_CART_ID", required = false) String tabCartId,
            @RequestParam(required = false) String tabId,
            @RequestParam String cardNumber,
            @RequestParam String expiryDate,
            @RequestParam String cvv,
            Authentication auth
    ) {
        String userId = auth.getName();
        String cartId = null;

        // Normalize and prioritize cart ID
        if (tabId != null && tabId.contains(",")) {
            tabId = tabId.split(",")[0];
        }

        if (tabId != null && tabCartId != null) {
            cartId = tabCartId;
        } else if (tabId != null) {
            cartId = "cart-" + tabId;
        } else {
            return ResponseEntity.badRequest().body("Cart ID could not be resolved.");
        }

        // Sanitize for cookie safety
        cartId = cartId.replaceAll("[^a-zA-Z0-9\\-]", "");

        try {
            String orderId = orderService.createOrderFromCart(cartId, userId);

            ResponseEntity<String> result = paymentService.processPayment(
                    userId, orderId, cardNumber, expiryDate, cvv);

            if (result.getStatusCode().is2xxSuccessful()) {
                orderHistoryService.recordOrderAndClearCart(userId, orderId);
                cartService.clearCart(cartId);
                return ResponseEntity.ok(orderId);
            }

            return result;

        } catch (Exception e) {
            e.printStackTrace(); // Log the error
            return ResponseEntity.internalServerError()
                    .body("Payment failed: " + e.getMessage());
        }
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
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Order>> viewPreviousOrders(
            @PathVariable String userId,
            @RequestParam(required = false) Boolean refundable) {

        List<String> ids = orderHistoryService
                .viewPreviousOrdersByUser(userId)
                .getBody();

        if (ids == null) {
            return ResponseEntity.ok(Collections.emptyList());
        }

        LocalDateTime cutoff = LocalDateTime.now().minusDays(30);

        List<Order> orders = ids.stream()
                .map(orderService::getOrderById)
                .filter(Objects::nonNull)
                .filter(order -> {
                    if (!Boolean.TRUE.equals(refundable)) {
                        return true; // No filtering if refundable flag is not true
                    }

                    boolean validInvoiceDate = order.getInvoiceSentDate() != null &&
                            order.getInvoiceSentDate().isAfter(cutoff);
                    boolean isShipped = order.isShipped();
                    boolean isDelivered = "Delivered".equalsIgnoreCase(order.getStatus());
                    boolean notRefunded = !order.isRefundRequested();

                    boolean isRefundable = validInvoiceDate && isShipped && isDelivered && notRefunded;

                    if (!isRefundable) {
                        System.out.println("❌ Excluding order: " + order.getOrderId());
                        System.out.println("   - invoiceSentDate: " + order.getInvoiceSentDate());
                        System.out.println("   - validInvoiceDate: " + validInvoiceDate);
                        System.out.println("   - isShipped: " + isShipped);
                        System.out.println("   - isDelivered: " + isDelivered);
                        System.out.println("   - isRefundRequested: " + order.isRefundRequested());
                    }

                    return isRefundable;
                })
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

    @PutMapping("/requestRefund/{orderId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> requestRefund(
            @PathVariable String orderId,
            @RequestParam String userId,
            @RequestParam String productId,
            @RequestParam String quantity
    ) {
        return orderService.requestRefundSingle(orderId, userId, productId, quantity);
    }



    @GetMapping("/refundRequests/active")
    public ResponseEntity<List<RefundRequest>> getActiveRefundRequests() {
        List<RefundRequest> activeRequests = orderService.getRefundRequestsByProcessed(false);
        return ResponseEntity.ok(activeRequests);
    }




    @PutMapping("/refund/approve/{requestId}")
    public ResponseEntity<String> approveRefund(@PathVariable String requestId) {
        return orderService.approveRefund(requestId);
    }

    @DeleteMapping("/refund/reject/{requestId}")
    public ResponseEntity<String> rejectRefund(@PathVariable String requestId) {
        return orderService.rejectRefund(requestId);
    }

    @GetMapping("/analytics/revenue")
    public ResponseEntity<Map<String, Object>> calculateRevenueAndProfit(
            @RequestParam String startDate,
            @RequestParam String endDate
    ) {
        return orderService.calculateRevenueCostProfit(startDate, endDate);
    }


    @PutMapping("/sanitizeOldPayments")
    public ResponseEntity<String> sanitizeOldOrders() {
        List<Order> orders = orderService.getAllOrders(); // You might need to add this method to service/repo

        for (Order order : orders) {
            String rawCard = order.getCardNumber();
            if (rawCard != null && rawCard.length() >= 4) {
                String masked = "*".repeat(rawCard.length() - 4) + rawCard.substring(rawCard.length() - 4);
                order.setCardNumber(masked);
            }
            order.setCvv("***");
            orderService.saveOrder(order); // you may already have a save method in your service
        }

        return ResponseEntity.ok("✅ All old card numbers and CVVs sanitized.");
    }





}
