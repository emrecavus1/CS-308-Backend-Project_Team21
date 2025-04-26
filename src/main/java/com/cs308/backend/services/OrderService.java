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


    public String createOrderFromCart(String cartId, String userId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new IllegalArgumentException("Cart not found: " + cartId));

        List<CartItem> items = cart.getItems();
        if (items.isEmpty()) throw new IllegalStateException("Cart is empty");

        // extract parallel lists
        List<String>  productIds = items.stream()
                .map(CartItem::getProductId)
                .toList();
        List<Integer> quantities = items.stream()
                .map(CartItem::getQuantity)
                .toList();

        Order o = new Order();
        o.setOrderId(UUID.randomUUID().toString());
        o.setCartId(cartId);
        o.setUserId(userId);
        o.setStatus("Processing");
        o.setPaid(false);
        o.setShipped(false);
        o.setProductIds(productIds);
        o.setQuantities(quantities);

        orderRepository.save(o);

        return o.getOrderId();
    }

    public ResponseEntity<String> cancelOrder(String orderId) {
        Optional<Order> optionalOrder = orderRepository.findById(orderId);

        if (!optionalOrder.isPresent()) {
            return ResponseEntity.badRequest().body("Order not found.");
        }

        Order order = optionalOrder.get();

        // Check if the order can be cancelled (must be in "Processing" status)
        if (!order.getStatus().equals("Processing")) {
            return ResponseEntity.badRequest().body("Order can only be cancelled if it is in 'Processing' status. Current status: " + order.getStatus());
        }

        List<String> ids   = order.getProductIds();
        List<Integer> qtys = order.getQuantities();
        for (int i = 0; i < ids.size(); i++) {
            String productId = ids.get(i);
            int    qty       = qtys.get(i);

            productRepository.findById(productId).ifPresent(product -> {
                product.setStockCount(product.getStockCount() + qty);
                productRepository.save(product);
            });
        }

        // Update order status
        order.setStatus("Cancelled");
        orderRepository.save(order);

        return ResponseEntity.ok("Order cancelled successfully.");
    }


    public Order getOrderById(String id) {
        return orderRepository.findById(id).orElse(null);
    }


}