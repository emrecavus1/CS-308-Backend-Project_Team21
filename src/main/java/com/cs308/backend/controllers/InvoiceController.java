// src/main/java/com/cs308/backend/controllers/InvoiceController.java
package com.cs308.backend.controllers;

import com.cs308.backend.models.Order;
import com.cs308.backend.models.Product;
import com.cs308.backend.models.User;
import com.cs308.backend.repositories.OrderRepository;
import com.cs308.backend.repositories.ProductRepository;
import com.cs308.backend.repositories.UserRepository;
import com.cs308.backend.util.PdfInvoiceBuilder;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.*;

import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/invoices")
@CrossOrigin(
        origins = "http://localhost:5173",
        allowCredentials = "true"
)
public class InvoiceController {

    private final OrderRepository   orderRepo;
    private final UserRepository    userRepo;
    private final ProductRepository prodRepo;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public InvoiceController(OrderRepository orderRepo,
                             UserRepository userRepo,
                             ProductRepository prodRepo) {
        this.orderRepo = orderRepo;
        this.userRepo  = userRepo;
        this.prodRepo  = prodRepo;
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ByteArrayResource> getInvoicePdf(@PathVariable String orderId) throws IOException {
        // 1) load Order and User
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
        User user = userRepo.findById(order.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found for order: " + orderId));

        // 2) load products & quantities
        List<Product> products   = prodRepo.findAllById(order.getProductIds());
        List<Integer> quantities = order.getQuantities();

        // 3) build the PDF bytes
        byte[] pdfBytes = PdfInvoiceBuilder.buildInvoicePdf(
                orderId,
                user,
                order,
                products,
                quantities,
                order.getCardNumber(),
                order.getExpiryDate(),
                order.getCvv()
        );

        // 4) wrap in a ByteArrayResource
        ByteArrayResource resource = new ByteArrayResource(pdfBytes);

        // 5) set headers so browser can render inline and know the length
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(
                ContentDisposition
                        .inline()
                        .filename("invoice-" + orderId + ".pdf")
                        .build()
        );
        headers.setContentLength(pdfBytes.length);

        return ResponseEntity
                .ok()
                .headers(headers)
                .body(resource);
    }

    @GetMapping("/user/{userId}/invoices")
    public ResponseEntity<List<Map<String, String>>> getInvoicesByUser(@PathVariable String userId) {
        List<Order> orders = orderRepo.findByUserId(userId).stream()
                .filter(Order::isPaid)
                .filter(o -> o.getInvoicePath() != null)
                .toList();

        Optional<User> userOpt = userRepo.findById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        User u = userOpt.get();

        List<Map<String, String>> result = new ArrayList<>();
        for (Order o : orders) {
            Map<String, String> entry = new HashMap<>();
            entry.put("orderId", o.getOrderId());
            entry.put("userName", u.getName() + " " + u.getSurname());
            entry.put("invoicePath", o.getInvoicePath());

            // Add the invoice sent date if available
            if (o.getInvoiceSentDate() != null) {
                entry.put("invoiceSentDate", o.getInvoiceSentDate().format(DATE_FORMATTER));
            } else {
                entry.put("invoiceSentDate", "Not sent yet");
            }

            result.add(entry);
        }

        return ResponseEntity.ok(result);
    }


    @GetMapping("/date-range")
    public ResponseEntity<List<Map<String, String>>> getInvoicesByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        // Find all orders that have been paid and have an invoice path
        List<Order> allInvoicedOrders = orderRepo.findByPaidIsTrueAndInvoicePathIsNotNull();

        // Filter orders by date range
        List<Order> ordersInRange = allInvoicedOrders.stream()
                .filter(order -> order.getInvoiceSentDate() != null)
                .filter(order -> !order.getInvoiceSentDate().isBefore(startDate) &&
                        !order.getInvoiceSentDate().isAfter(endDate))
                .toList();

        // Transform orders to response format
        List<Map<String, String>> result = new ArrayList<>();
        for (Order order : ordersInRange) {
            Map<String, String> entry = new HashMap<>();
            entry.put("orderId", order.getOrderId());

            // Fetch user info
            try {
                User user = userRepo.findById(order.getUserId())
                        .orElseThrow(() -> new IllegalArgumentException("User not found: " + order.getUserId()));
                entry.put("userName", user.getName() + " " + user.getSurname());
            } catch (Exception e) {
                entry.put("userName", "Unknown");
            }

            entry.put("invoicePath", order.getInvoicePath());

            if (order.getInvoiceSentDate() != null) {
                entry.put("invoiceSentDate", order.getInvoiceSentDate().format(DATE_FORMATTER));
            } else {
                entry.put("invoiceSentDate", "Not sent yet");
            }

            // Add total amount from the order if available
            double totalAmount = 0.0;
            if (order.getProductIds() != null && order.getQuantities() != null) {
                List<Product> products = prodRepo.findAllById(order.getProductIds());
                for (int i = 0; i < products.size(); i++) {
                    if (i < order.getQuantities().size()) {
                        totalAmount += products.get(i).getCurrentPrice() * order.getQuantities().get(i);
                    }
                }
                entry.put("totalAmount", String.format("%.2f", totalAmount));
            }

            result.add(entry);
        }

        return ResponseEntity.ok(result);
    }
}