package com.cs308.backend.controllers;

import com.cs308.backend.models.Order;
import com.cs308.backend.services.CartService;
import com.cs308.backend.services.OrderHistoryService;
import com.cs308.backend.services.OrderService;
import com.cs308.backend.services.PaymentService;
import com.cs308.backend.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @PostMapping("/create")
    public ResponseEntity<String> createOrder(@RequestParam String userId) {
        return orderService.createOrder(userId);
    }

    @PutMapping("/payment/{orderId}")
    public ResponseEntity<String> markOrderAsPaid(
            @PathVariable String orderId,
            @RequestParam String userId,
            @RequestParam String cardNumber,
            @RequestParam String expiryDate,
            @RequestParam String cvv) {

        // process payment and mark order paid
        ResponseEntity<String> result =
                paymentService.processPayment(userId, orderId, cardNumber, expiryDate, cvv);

        // if payment was successful, record it in history & clear cart
        if (result.getStatusCode().is2xxSuccessful()) {
            orderHistoryService.recordOrderAndClearCart(userId, orderId);
        }
        return result;
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

    /**
     * Get this user’s entire order‑history (list of order IDs).
     */
    @GetMapping("/history/{userId}")
    public ResponseEntity<List<String>> getOrderHistory(@PathVariable String userId) {
        return orderHistoryService.getHistoryByUser(userId);
    }
}
