package com.cs308.backend.services;

import com.cs308.backend.models.*;
import com.cs308.backend.repositories.*;
import com.cs308.backend.services.*;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.util.*;
import java.time.LocalDateTime;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;

import com.cs308.backend.util.PdfInvoiceBuilder; // if you use a separate utility class
import com.cs308.backend.util.MongoIdUtils;


@Service
public class PaymentService {
    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final PaymentRepository paymentRepository;
    private final InvoiceService invoiceService;
    private final OrderService orderService;

    public PaymentService(OrderRepository orderRepository, CartRepository cartRepository, UserRepository userRepository, ProductRepository productRepository, PaymentRepository paymentRepository, InvoiceService invoiceService, OrderService orderService) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.paymentRepository = paymentRepository;
        this.invoiceService = invoiceService;
        this.orderService = orderService;
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

    public ResponseEntity<String> processPayment(
            String userId,
            String orderId,
            String cardNumber,
            String expiryDate,
            String cvv
    ) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
        if (!order.getUserId().equals(userId)) {
            return ResponseEntity.badRequest()
                    .body("Order does not belong to this user.");
        }

        // Update order fields
        order.setPaid(true);
        order.setStatus("Processing");
        String maskedCard = "*".repeat(cardNumber.length() - 4) + cardNumber.substring(cardNumber.length() - 4);
        order.setCardNumber(maskedCard);
        order.setCvv("***"); // Mask CVV
        order.setExpiryDate(expiryDate); // This is usually OK to store in plain format

        // Decrease product stock
        List<String> productIds = order.getProductIds();
        List<Integer> quantities = order.getQuantities();
        for (int i = 0; i < productIds.size(); i++) {
            String pid = productIds.get(i);
            int qty = quantities.get(i);
            productRepository.findById(pid).ifPresent(prod -> {
                prod.setStockCount(prod.getStockCount() - qty);
                productRepository.save(prod);
            });
        }

        if (order.getInvoiceSentDate() == null) {
            order.setInvoiceSentDate(LocalDateTime.now());
        }

        // Save order before email generation
        orderRepository.save(order);

        try {
            // Send email
            invoiceService.emailPdfInvoice(orderId);

            // Generate & save PDF
            String filePath = "invoices/invoice-" + orderId + ".pdf";
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            byte[] pdfBytes = PdfInvoiceBuilder.buildInvoicePdf(
                    orderId,
                    user,
                    order,
                    productRepository.findAllById(productIds),
                    quantities,
                    cardNumber,
                    expiryDate,
                    cvv
            );

            Files.createDirectories(Paths.get("invoices"));
            Files.write(Paths.get(filePath), pdfBytes);

            // Save path to order & persist it again
            order.setInvoicePath(filePath);
            orderRepository.save(order); // ✅ save again after setting path

        } catch (Exception e) {
            throw new IllegalStateException("Payment succeeded but failed to send invoice", e);
        }

        return ResponseEntity.ok("Payment processed, stock updated & invoice emailed!");
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