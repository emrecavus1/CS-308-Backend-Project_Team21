package com.cs308.backend.services;

import com.cs308.backend.models.*;
import com.cs308.backend.repositories.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;
import com.cs308.backend.util.MongoIdUtils;

import org.bson.types.ObjectId;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    @Autowired
    private OrderHistoryRepository orderHistoryRepository;
    private RefundRequestRepository refundRequestRepository = null;
    private final UserService userService;
    private final JavaMailSender mailSender;

    public OrderService(OrderRepository orderRepository, CartRepository cartRepository, UserRepository userRepository, ProductRepository productRepository, OrderHistoryRepository orderHistoryRepositoryRefundRequestRepository,
                        RefundRequestRepository refundRequestRepository, UserService userService,
                        JavaMailSender mailSender) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.refundRequestRepository = refundRequestRepository;
        this.userService = userService;
        this.orderHistoryRepository = orderHistoryRepository;
        this.mailSender = mailSender;
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

        // ← insert here: capture the price _at time of purchase_
        List<Double> prices = items.stream()
                .map(CartItem::getPrice)
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
        o.setPrices(prices);

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


    public List<RefundRequest> getAllRefundRequests() {
        return refundRequestRepository.findAll();
    }

    public List<RefundRequest> getRefundRequestsByProcessed(boolean processed) {
        return refundRequestRepository.findByProcessed(processed);
    }

    public ResponseEntity<String> markRefundProcessed(String requestId) {
        Optional<RefundRequest> opt = refundRequestRepository.findById(requestId);
        if (opt.isEmpty()) {
            return ResponseEntity
                    .badRequest()
                    .body("Refund request not found: " + requestId);
        }
        RefundRequest rr = opt.get();
        if (rr.isProcessed()) {
            return ResponseEntity
                    .badRequest()
                    .body("Refund request already processed.");
        }
        rr.setProcessed(true);
        refundRequestRepository.save(rr);
        return ResponseEntity
                .ok("Refund request marked as processed.");
    }

    public ResponseEntity<String> approveRefund(String requestId) {
        var opt = refundRequestRepository.findById(requestId);
        if (opt.isEmpty()) {
            return ResponseEntity
                    .badRequest()
                    .body("Refund request not found: " + requestId);
        }

        RefundRequest rr = opt.get();
        if (rr.isProcessed()) {
            return ResponseEntity
                    .badRequest()
                    .body("Refund request already handled.");
        }

        double totalAmount = 0.0;
        for (RefundItem item : rr.getItems()) {
            Product p = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new IllegalStateException(
                            "Product not found: " + item.getProductId()));
            p.setStockCount(p.getStockCount() + item.getQuantity());  // ✅ correct setter
            productRepository.save(p);
            totalAmount += item.getPrice() * item.getQuantity();
        }

        // ✅ Also update the order status
        Order order = orderRepository.findById(rr.getOrderId())
                .orElseThrow(() -> new IllegalStateException("Order not found: " + rr.getOrderId()));
        order.setStatus("Refunded");
        orderRepository.save(order);

        // ✅ Remove the order from order history
        orderHistoryRepository.findByUserId(rr.getUserId()).ifPresent(history -> {
            history.getOrderIds().remove(rr.getOrderId());
            orderHistoryRepository.save(history);
        });

// ✅ Remove refunded product(s) from the recommendations logic (optional: you can filter them out dynamically instead of removing)


        // ✅ Mark processed & remove request
        rr.setProcessed(true);
        refundRequestRepository.save(rr);

        // ✅ Send email
        String email = userService.getEmailByUserId(rr.getUserId());
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(email);
        msg.setSubject("Your refund has been approved");
        msg.setText(String.format(
                "Hello,\n\nYour refund request for order %s has been approved.%n" +
                        "Total refunded amount: %.2f%n\nThank you.",
                rr.getOrderId(), totalAmount
        ));
        mailSender.send(msg);

        refundRequestRepository.deleteById(requestId);

        return ResponseEntity.ok("Refund approved, customer notified, request removed.");
    }


    public ResponseEntity<String> rejectRefund(String requestId) {
        // 1) Load the RefundRequest
        Optional<RefundRequest> opt = refundRequestRepository.findById(requestId);
        if (opt.isEmpty()) {
            return ResponseEntity
                    .badRequest()
                    .body("Refund request not found: " + requestId);
        }
        else {
            refundRequestRepository.deleteById(requestId);

            return ResponseEntity.ok("Refund request rejected and removed.");
        }
    }

    public ResponseEntity<String> requestRefundSingle(
            String orderId,
            String userId,
            String productId,
            String quantityStr
    ) {
        try {
            int quantity = Integer.parseInt(quantityStr);

            Optional<Order> opt = orderRepository.findById(orderId);
            if (opt.isEmpty()) return ResponseEntity.badRequest().body("Order not found.");
            Order order = opt.get();

            if (!order.getUserId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Unauthorized refund attempt.");
            }

            if (order.getInvoiceSentDate() == null || order.getInvoiceSentDate().isBefore(LocalDateTime.now().minusDays(30))) {
                return ResponseEntity.badRequest().body("Refund window expired.");
            }

            if (!order.isShipped() || !"Delivered".equalsIgnoreCase(order.getStatus())) {
                return ResponseEntity.badRequest().body("Refunds allowed only for delivered orders.");
            }

            int idx = order.getProductIds().indexOf(productId);
            if (idx == -1) return ResponseEntity.badRequest().body("Product not found in order.");
            int origQty = order.getQuantities().get(idx);
            if (quantity < 1 || quantity > origQty) {
                return ResponseEntity.badRequest().body("Invalid quantity for refund.");
            }

            // ✅ Use stored price at time of purchase
            double storedPrice = order.getPrices().get(idx);

            RefundItem item = new RefundItem(productId, quantity, storedPrice);

            order.setRefundRequested(true);
            orderRepository.save(order);

            RefundRequest rr = new RefundRequest(
                    UUID.randomUUID().toString(),
                    orderId,
                    userId,
                    LocalDateTime.now(),
                    false,
                    List.of(item)
            );
            refundRequestRepository.save(rr);

            return ResponseEntity.ok("Refund request submitted.");
        } catch (Exception e) {
            e.printStackTrace(); // logs full error to backend
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unexpected error: " + e.getMessage());
        }
    }



    public ResponseEntity<Map<String, Object>> calculateRevenueCostProfit(String startDate, String endDate) {
        try {
            LocalDateTime start = LocalDateTime.parse(startDate);
            LocalDateTime end   = LocalDateTime.parse(endDate);

            List<Order> orders = orderRepository.findAll().stream()
                    .filter(o -> o.getInvoiceSentDate() != null)
                    .filter(o -> !o.getInvoiceSentDate().isBefore(start) && !o.getInvoiceSentDate().isAfter(end))
                    .toList();

            double totalRevenue = 0.0;
            double totalCost = 0.0;
            List<Map<String, Object>> breakdown = new ArrayList<>();

            for (Order order : orders) {
                List<String> productIds = order.getProductIds();
                List<Integer> quantities = order.getQuantities();
                List<Double> prices = order.getPrices();

                for (int i = 0; i < productIds.size(); i++) {
                    String pid = productIds.get(i);
                    int qty = quantities.get(i);
                    double price = prices.get(i);

                    double revenue = price * qty;
                    double cost;

                    Optional<Product> optProduct = productRepository.findById(pid);
                    if (optProduct.isPresent()) {
                        Product p = optProduct.get();
                        cost = (p.getProductionCost() != null ? p.getProductionCost() : price * 0.5) * qty;
                    } else {
                        cost = price * 0.5 * qty; // fallback
                    }

                    totalRevenue += revenue;
                    totalCost += cost;

                    Map<String, Object> row = new HashMap<>();
                    row.put("productId", pid);
                    row.put("quantity", qty);
                    row.put("revenue", revenue);
                    row.put("cost", cost);
                    row.put("profit", revenue - cost);
                    breakdown.add(row);
                }
            }

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("totalRevenue", totalRevenue);
            result.put("totalCost", totalCost);
            result.put("profit", totalRevenue - totalCost);
            result.put("breakdown", breakdown);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to calculate analytics", "details", e.getMessage()));
        }
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public void saveOrder(Order order) {
        orderRepository.save(order);
    }




    public Order getOrderById(String id) {
        return orderRepository.findById(id).orElse(null);
    }


}