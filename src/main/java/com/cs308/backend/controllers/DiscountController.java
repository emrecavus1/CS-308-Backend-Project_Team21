package com.cs308.backend.controllers;

import com.cs308.backend.models.Product;
import com.cs308.backend.services.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.Map;

@RestController
@RequestMapping("/api/discounts")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class DiscountController {

    private final ProductService productService;

    @Autowired
    public DiscountController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping("/set")
    public ResponseEntity<Map<String, Object>> setDiscountAndNotify(
            @RequestParam String productId,
            @RequestParam double discountPercentage) {

        Map<String, Object> result = productService.setDiscountAndNotifyUsers(productId, discountPercentage);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/set-silent")
    public ResponseEntity<Product> setDiscount(
            @RequestParam String productId,
            @RequestParam double discountPercentage) {

        Product product = productService.setDiscount(productId, discountPercentage);
        return ResponseEntity.ok(product);
    }


    @DeleteMapping("/remove")
    public ResponseEntity<String> removeDiscount(@RequestParam String productId) {
        return ResponseEntity.badRequest().body("Removing discount is not supported anymore since original prices are not stored.");
    }
}