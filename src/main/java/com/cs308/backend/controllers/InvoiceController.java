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
import java.util.*;

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
            result.add(entry);
        }

        return ResponseEntity.ok(result);
    }



}
