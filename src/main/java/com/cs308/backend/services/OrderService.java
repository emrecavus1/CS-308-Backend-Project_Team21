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
        order.setStatus("Shipped");
        orderRepository.save(order);
        return ResponseEntity.ok("Order marked as shipped.");
    }


    public String createOrderFromCart(String userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("Cart not found"));

        // pull out just the productIds from the CartItem list:
        List<String> productIds = cart.getItems().stream()
                .map(CartItem::getProductId)
                .collect(Collectors.toList());

        if (productIds.isEmpty()) {
            throw new IllegalStateException("Cart is empty");
        }

        // build your Order
        Order o = new Order();
        o.setOrderId(UUID.randomUUID().toString());
        o.setCartId(cart.getCartId());
        o.setUserId(userId);
        o.setStatus("Pending");
        o.setPaid(false);
        o.setShipped(false);

        // ← snapshot the productIds
        o.setProductIds(productIds);

        orderRepository.save(o);

        // now clear the cart’s items
        cart.getItems().clear();
        cartRepository.save(cart);

        return o.getOrderId();
    }




}