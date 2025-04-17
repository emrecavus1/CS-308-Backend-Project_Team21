package com.cs308.backend.services;

import com.cs308.backend.models.*;
import com.cs308.backend.repositories.*;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.util.*;

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


    public ResponseEntity<String> createOrder(String userId) {
        Optional<Cart> cartOptional = cartRepository.findByUserId(userId);

        if (!cartOptional.isPresent()) {
            return ResponseEntity.badRequest().body("Cart not found for user.");
        }

        Cart cart = cartOptional.get();
        if (cart.getProductIds().isEmpty()) {
            return ResponseEntity.badRequest().body("Cart is empty. Cannot place an order.");
        }

        Order newOrder = new Order();
        newOrder.setOrderId(UUID.randomUUID().toString());
        newOrder.setCartId(cart.getCartId());
        newOrder.setUserId(userId);
        newOrder.setStatus("Pending");
        newOrder.setPaid(false);
        newOrder.setShipped(false);

        orderRepository.save(newOrder);
        return ResponseEntity.ok("Order created successfully. Order ID: " + newOrder.getOrderId());
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
        // 1) load & validate cart
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("Cart not found"));
        if (cart.getProductIds().isEmpty())
            throw new IllegalStateException("Cart is empty");

        // 2) create & save the Order
        Order o = new Order();
        o.setOrderId(UUID.randomUUID().toString());
        o.setCartId(cart.getCartId());
        o.setUserId(userId);
        o.setStatus("Pending");
        o.setPaid(false);
        o.setShipped(false);
        orderRepository.save(o);

        // 3) record it in history
        OrderHistory history = orderHistoryRepository
                .findByUserId(userId)
                .orElseGet(() -> {
                    OrderHistory h = new OrderHistory();
                    h.setOrderHistoryId(UUID.randomUUID().toString());
                    h.setUserId(userId);
                    h.setOrderIds(new ArrayList<>());
                    return h;
                });
        history.getOrderIds().add(o.getOrderId());
        orderHistoryRepository.save(history);

        // 4) clear the cart
        cart.setProductIds(new ArrayList<>());
        cartRepository.save(cart);

        return o.getOrderId();
    }



}