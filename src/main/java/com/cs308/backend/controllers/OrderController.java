package com.cs308.backend.controllers;

import com.cs308.backend.models.*;
import com.cs308.backend.services.*;
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


    @PutMapping("/payment")
    public ResponseEntity<String> processPayment(
            @RequestParam String userId,
            @RequestParam String cardNumber,
            @RequestParam String expiryDate,
            @RequestParam String cvv) {

        // 1. Validate bank info, charge card, etc.
        ResponseEntity<String> validation = paymentService.getBankInformation(userId, cardNumber, expiryDate, cvv);
        if (!validation.getStatusCode().is2xxSuccessful()) {
            return validation;
        }

        // 2. Create the Order now that we know payment can proceed
        String newOrderId = orderService.createOrderFromCart(userId);
        //    ^— you’d write a helper method that both checks the cart and persists the Order

        // 3. Process payment & link to that order
        return paymentService.processPayment(userId, newOrderId, cardNumber, expiryDate, cvv);
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


    @GetMapping("/viewPreviousOrders/{userId}")
    public ResponseEntity<List<String>> viewPreviousOrders(@PathVariable String userId) {
        return orderHistoryService.viewPreviousOrdersByUser(userId);
    }

    @GetMapping("/viewActiveOrders/{userId}")
    public ResponseEntity<List<String>> viewActiveOrders(@PathVariable String userId) {
        return orderHistoryService.viewActiveOrdersByUser(userId);
    }

    @GetMapping("/previous-products/{userId}")
    public ResponseEntity<List<Product>> previousProducts(@PathVariable String userId) {
        return orderHistoryService.getProductsFromPreviousOrders(userId);
    }
}
