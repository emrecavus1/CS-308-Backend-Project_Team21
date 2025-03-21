package com.cs308.backend.controllers;

import com.cs308.backend.models.*;
import com.cs308.backend.repositories.*;
import com.cs308.backend.services.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/main")
public class MainController {
    private final ProductService productService;
    private final CategoryService categoryService;
    private final UserService userService;
    private final ReviewService reviewService;

    public MainController(ProductService productService, CategoryService categoryService, ReviewService reviewService, UserService userService) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.reviewService = reviewService;
        this.userService = userService;
    }

    @GetMapping("/customer/getCategories")
    public ResponseEntity<Map<String, Object>> getCustomerMainPage() {
        List<Category> categories = categoryService.getAllCategories();

        Map<String, Object> response = new HashMap<>();
        response.put("categories", categories);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/customer/category/{categoryId}/getProductsByCategory")
    public ResponseEntity<Map<String, Object>> getProductsByCategory(@PathVariable String categoryId) {
        List<Product> products = productService.getProductsByCategory(categoryId);

        Map<String, Object> response = new HashMap<>();
        response.put("products", products);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/productmanager/addProduct")
    public ResponseEntity<String> addProduct(
            @RequestBody Product product,
            @RequestParam String categoryName) {

        // Call the service method with the correct parameters
        ResponseEntity<String> response = productService.addProduct(
                product, // Pass the full Product object
                product.getProductName(),
                product.getProductInfo(),
                categoryName, // Pass categoryName instead of categoryId
                product.getStockCount()
        );

        return response; // Return the response message from ProductService
    }

    @PostMapping("/productmanager/addCategory")
    public ResponseEntity<String> addCategory(@RequestBody Category category)
    {
        return categoryService.addCategory(category, category.getCategoryName());
    }

    @PutMapping("/productmanager/updateStock/{productId}/{newStock}")
    public ResponseEntity<Product> updateStock(@PathVariable String productId, @PathVariable int newStock) {
        Product updated = productService.updateStock(productId, newStock);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/productmanager/approveReview/{reviewId}")
    public ResponseEntity<Review> approveReview(@PathVariable String reviewId) {
        Review approved = reviewService.approveReview(reviewId);
        return ResponseEntity.ok(approved);
    }

    @PostMapping("/postReview")
    public ResponseEntity<Review> postReview(@RequestBody Review review) {
        Review savedReview = reviewService.postReview(review);
        return ResponseEntity.ok(savedReview);
    }

    @PutMapping("/updatePrice/{productId}/{newPrice}")
    public ResponseEntity<Product> updatePrice(@PathVariable String productId, @PathVariable double newPrice) {
        Product updatedProduct = productService.setPrice(productId, newPrice);
        return ResponseEntity.ok(updatedProduct);
    }

    @PatchMapping("/updateProduct/{productId}")
    public ResponseEntity<Product> updateProduct(@PathVariable String productId, @RequestBody Map<String, Object> updates) {
        Product updatedProduct = productService.updateProduct(productId, updates);
        return ResponseEntity.ok(updatedProduct);
    }




}