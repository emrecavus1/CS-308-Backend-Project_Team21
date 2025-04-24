package com.cs308.backend.controllers;

import com.cs308.backend.models.Order;
import com.cs308.backend.models.Product;
import com.cs308.backend.models.User;
import com.cs308.backend.repositories.OrderRepository;
import com.cs308.backend.repositories.ProductRepository;
import com.cs308.backend.repositories.UserRepository;
import com.cs308.backend.util.PdfInvoiceBuilder;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {

    private final OrderRepository orderRepo;
    private final UserRepository userRepo;
    private final ProductRepository productRepo;

    public InvoiceController(OrderRepository orderRepo,
                             UserRepository userRepo,
                             ProductRepository productRepo) {
        this.orderRepo   = orderRepo;
        this.userRepo    = userRepo;
        this.productRepo = productRepo;
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<byte[]> getInvoicePdf(@PathVariable String orderId) throws IOException {
        // 1) load Order and User
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
        User user = userRepo.findById(order.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found for order: " + orderId));

        // 2) load products & quantities
        List<Product> products   = productRepo.findAllById(order.getProductIds());
        List<Integer> quantities = order.getQuantities();

        // 3) build the PDF bytes
        byte[] pdf = PdfInvoiceBuilder.buildInvoicePdf(
                orderId,
                user,
                order,
                products,
                quantities,
                order.getCardNumber(),
                order.getExpiryDate(),
                order.getCvv()
        );

        // 4) set headers so browser will render inline
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition
                .inline()
                .filename("invoice-" + orderId + ".pdf")
                .build()
        );

        return ResponseEntity
                .ok()
                .headers(headers)
                .body(pdf);
    }
}
