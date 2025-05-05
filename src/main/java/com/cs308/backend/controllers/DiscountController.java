package com.cs308.backend.controllers;

import com.cs308.backend.models.Product;
import com.cs308.backend.services.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
@RestController
@RequestMapping("/api/discounts")
public class DiscountController {

    private final ProductService productService;

    @Autowired
    public DiscountController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * Sets a discount on a product and notifies users who have it in their wishlist
     * @param productId the ID of the product
     * @param discountPercentage the discount percentage (e.g., 10 for 10%)
     * @param startDate when the discount starts
     * @param endDate when the discount ends
     * @return the updated product and number of notified users
     */
    @PostMapping("/set")
    public ResponseEntity<Map<String, Object>> setDiscountAndNotify(
            @RequestParam String productId,
            @RequestParam double discountPercentage,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date endDate) {

        Map<String, Object> result = productService.setDiscountAndNotifyUsers(
                productId, discountPercentage, startDate, endDate);

        return ResponseEntity.ok(result);
    }

    /**
     * Sets a discount on a product without sending notifications
     * @param productId the ID of the product
     * @param discountPercentage the discount percentage (e.g., 10 for 10%)
     * @param startDate when the discount starts
     * @param endDate when the discount ends
     * @return the updated product
     */
    @PostMapping("/set-silent")
    public ResponseEntity<Product> setDiscount(
            @RequestParam String productId,
            @RequestParam double discountPercentage,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date endDate) {

        Product updatedProduct = productService.setDiscount(
                productId, discountPercentage, startDate, endDate);

        return ResponseEntity.ok(updatedProduct);
    }

    /**
     * Removes a discount from a product
     * @param productId the ID of the product
     * @return the updated product
     */
    @DeleteMapping("/remove")
    public ResponseEntity<Product> removeDiscount(@RequestParam String productId) {
        Product updatedProduct = productService.removeDiscount(productId);
        return ResponseEntity.ok(updatedProduct);
    }

    /**
     * Sends notifications about a discount to users who have the product in their wishlist
     * @param productId the ID of the product
     * @param discountPercentage the discount percentage (e.g., 10 for 10%)
     * @return the number of users notified
     */
    @PostMapping("/notify")
    public ResponseEntity<Map<String, Object>> notifyUsers(
            @RequestParam String productId,
            @RequestParam double discountPercentage) {

        int notifiedUsers = productService.notifyUsersAboutDiscount(productId, discountPercentage);

        Map<String, Object> result = Map.of(
                "productId", productId,
                "notifiedUsers", notifiedUsers
        );

        return ResponseEntity.ok(result);
    }
}