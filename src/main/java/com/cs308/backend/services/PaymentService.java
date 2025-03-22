package com.cs308.backend.services;

import com.cs308.backend.models.*;
import com.cs308.backend.repositories.*;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class PaymentService {
    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final PaymentRepository paymentRepository;

    public PaymentService(OrderRepository orderRepository, CartRepository cartRepository, UserRepository userRepository, ProductRepository productRepository, PaymentRepository paymentRepository) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.paymentRepository = paymentRepository;
    }

    private boolean isCardExpired(String expiryDate) {
        try {
            String[] parts = expiryDate.split("/");
            int month = Integer.parseInt(parts[0]);
            int year = Integer.parseInt(parts[1]) + 2000;

            Calendar now = Calendar.getInstance();
            Calendar expiry = Calendar.getInstance();
            expiry.set(Calendar.MONTH, month - 1);
            expiry.set(Calendar.YEAR, year);
            expiry.set(Calendar.DAY_OF_MONTH, 1);
            expiry.add(Calendar.MONTH, 1);  // end of expiry month

            return expiry.before(now);
        } catch (Exception e) {
            return true;  // assume expired if invalid format
        }
    }


    public ResponseEntity<String> getBankInformation(String userId, String cardNumber, String expiryDate, String cvv) {
        // Validate user existence
        Optional<User> userOptional = userRepository.findById(userId);
        if (!userOptional.isPresent()) {
            return ResponseEntity.badRequest().body("User not found.");
        }

        // Validate card number: must be 16 digits
        if (cardNumber == null || !cardNumber.matches("\\d{16}")) {
            return ResponseEntity.badRequest().body("Invalid card number. It must be exactly 16 digits.");
        }

        // Validate expiry date format: MM/YY
        if (expiryDate == null || !expiryDate.matches("(0[1-9]|1[0-2])/\\d{2}")) {
            return ResponseEntity.badRequest().body("Invalid expiry date format. Use MM/YY.");
        }

        if (isCardExpired(expiryDate)) {
            return ResponseEntity.badRequest().body("The card is expired!");
        }

        // Validate CVV: must be 3 or 4 digits
        if (cvv == null || !cvv.matches("\\d{3,4}")) {
            return ResponseEntity.badRequest().body("Invalid CVV. It must be 3 or 4 digits.");
        }

        // [Optional] You might want to store or tokenize this securely later
        return ResponseEntity.ok("Bank information is valid.");
    }

    public ResponseEntity<String> processPayment(String userId, String orderId, String cardNumber, String expiryDate, String cvv) {
        // Validate order
        Optional<Order> orderOptional = orderRepository.findById(orderId);
        if (!orderOptional.isPresent()) {
            return ResponseEntity.badRequest().body("Order not found.");
        }

        Order order = orderOptional.get();
        if (!order.getUserId().equals(userId)) {
            return ResponseEntity.badRequest().body("Order does not belong to this user.");
        }

        // Bank info validation (reuse existing method)
        ResponseEntity<String> validation = getBankInformation(userId, cardNumber, expiryDate, cvv);
        if (!validation.getStatusCode().is2xxSuccessful()) {
            return validation;
        }

        // Create payment
        Payment payment = new Payment();
        payment.setPaymentId(UUID.randomUUID().toString());
        payment.setUserId(userId);
        payment.setOrderId(orderId);
        payment.setCardNumber(cardNumber);
        payment.setExpiryDate(expiryDate);
        payment.setCvv(cvv);
        paymentRepository.save(payment);

        // Update order
        order.setPaid(true);
        order.setStatus("Paid");
        order.setPaymentId(payment.getPaymentId());
        orderRepository.save(order);

        return ResponseEntity.ok("Payment processed successfully.");
    }

    public Optional<Payment> getPaymentByOrderId(String orderId) {
        return paymentRepository.findByOrderId(orderId);
    }

    public List<Payment> getPaymentsByUserId(String userId) {
        return paymentRepository.findByUserId(userId);
    }

    public ResponseEntity<String> deletePayment(String paymentId) {
        if (!paymentRepository.existsById(paymentId)) {
            return ResponseEntity.badRequest().body("Payment not found.");
        }
        paymentRepository.deleteById(paymentId);
        return ResponseEntity.ok("Payment deleted successfully.");
    }


}