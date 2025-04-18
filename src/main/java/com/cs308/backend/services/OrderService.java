package com.cs308.backend.services;

import com.cs308.backend.models.*;
import com.cs308.backend.repositories.*;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderHistoryRepository orderHistoryRepository;

    public OrderService(OrderRepository orderRepository, CartRepository cartRepository, UserRepository userRepository, ProductRepository productRepository, OrderHistoryRepository orderHistoryRepository) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.orderHistoryRepository = orderHistoryRepository;
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
        order.setStatus("Delivered");
        orderRepository.save(order);
        return ResponseEntity.ok("Order marked as shipped.");
    }

    public ResponseEntity<String> markAsInTransit(String orderId) {
        Optional<Order> optionalOrder = orderRepository.findById(orderId);

        if (!optionalOrder.isPresent()) {
            return ResponseEntity.badRequest().body("Order not found.");
        }

        Order order = optionalOrder.get();
        order.setStatus("In-Transit");
        orderRepository.save(order);
        return ResponseEntity.ok("Order marked as in-transit.");
    }


    public String createOrderFromCart(String userId) {
        // 1) fetch-or-404 the cart
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("Cart not found"));

        // 2) pull out CartItems
        List<CartItem> items = cart.getItems();
        if (items.isEmpty()) {
            throw new IllegalStateException("Cart is empty");
        }

        // 3) extract parallel lists of IDs and quantities
        List<String>  productIds = items.stream()
                .map(CartItem::getProductId)
                .collect(Collectors.toList());
        List<Integer> quantities = items.stream()
                .map(CartItem::getQuantity)
                .collect(Collectors.toList());

        // 4) build the Order
        Order o = new Order();
        o.setOrderId(UUID.randomUUID().toString());
        o.setCartId(cart.getCartId());
        o.setUserId(userId);
        o.setStatus("Processing");
        o.setPaid(false);
        o.setShipped(false);

        // ← snapshot both fields
        o.setProductIds(productIds);
        o.setQuantities(quantities);

        orderRepository.save(o);

        // 5) clear out the cart for next time
        cart.getItems().clear();
        cartRepository.save(cart);

        return o.getOrderId();
    }




}