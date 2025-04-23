package com.cs308.backend.controllers;

import com.cs308.backend.models.*;
import com.cs308.backend.models.CartItem;
import com.cs308.backend.repositories.*;
import com.cs308.backend.services.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/main")
public class MainController {
    private final ProductService productService;
    private final CategoryService categoryService;
    private final UserService userService;
    private final ReviewService reviewService;
    private final CartService cartService;

    public MainController(ProductService productService, CategoryService categoryService, ReviewService reviewService, UserService userService, CartService cartService) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.reviewService = reviewService;
        this.userService = userService;
        this.cartService = cartService;
    }

    @GetMapping("/getCategories")
    public ResponseEntity<Map<String, Object>> getCustomerMainPage() {
        List<Category> categories = categoryService.getAllCategories();

        Map<String, Object> response = new HashMap<>();
        response.put("categories", categories);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/category/{categoryId}/getProductsByCategory")
    public ResponseEntity<Map<String, Object>> getProductsByCategory(@PathVariable String categoryId) {
        List<Product> products = productService.getProductsByCategory(categoryId);

        Map<String, Object> response = new HashMap<>();
        response.put("products", products);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/addProduct")
    public ResponseEntity<String> addProduct(
            @RequestBody Product product,
            @RequestParam String categoryName) {

        // Call the service method with the correct parameters
        ResponseEntity<String> response = productService.addProduct(
                product, // Pass the full Product object
                product.getProductName(),
                product.getProductInfo(),
                categoryName, // Pass categoryName instead of categoryId
                product.getStockCount(),
                product.getSerialNumber(),
                product.getWarrantyStatus(),
                product.getDistributorInfo()
        );

        return response;
    }

    @PostMapping("/addCategory")
    public ResponseEntity<String> addCategory(@RequestBody Category category)
    {
        return categoryService.addCategory(category, category.getCategoryName());
    }

    @PutMapping("/updateStock/{productId}/{newStock}")
    public ResponseEntity<Product> updateStock(@PathVariable String productId, @PathVariable int newStock) {
        Product updated = productService.updateStock(productId, newStock);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/approveReview/{reviewId}")
    public ResponseEntity<Review> approveReview(@PathVariable String reviewId) {
        Review approved = reviewService.approveReview(reviewId);
        return ResponseEntity.ok(approved);
    }

    @PostMapping("/declineReview/{reviewId}")
    public ResponseEntity<Review> declineReview(@PathVariable String reviewId) {
        Review declined = reviewService.declineReview(reviewId);
        return ResponseEntity.ok(declined);
    }

    @PostMapping("/postReview")
    public ResponseEntity<Review> postReview(@RequestBody Review req) {
        Review saved = reviewService.postReview(req);
        return ResponseEntity.ok(saved);
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


    @PostMapping("/cart/add")
    public ResponseEntity<String> addToCart(
            @RequestParam String userId,
            @RequestParam String productId) {
        return cartService.addToCart(userId, productId);
    }


    @GetMapping("/cart/items")
    public ResponseEntity<List<CartItem>> getCartItems(@RequestParam String userId) {
        return cartService.getCartItems(userId);
    }


    @GetMapping("/search")
    public ResponseEntity<List<Product>> searchProducts(@RequestParam String query) {
        List<Product> products = productService.searchProducts(query);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/sortProductsByPrice")
    public ResponseEntity<List<Product>> sortProductsByPrice(@RequestParam(defaultValue = "asc") String order) {
        List<Product> sortedProducts = productService.sortProductsByPrice();
        return ResponseEntity.ok(sortedProducts);
    }

    @GetMapping("/sortProductsByRating")
    public ResponseEntity<List<Product>> sortProductsByRating(@RequestParam(defaultValue = "desc") String order) {
        List<Product> sortedProducts = productService.sortProductsByRating();
        return ResponseEntity.ok(sortedProducts);
    }

    /** Add a product to this user’s wishlist */
    @PostMapping("/{userId}/wishlist/{productId}")
    public ResponseEntity<String> addToWishlist(
            @PathVariable String userId,
            @PathVariable String productId) {
        return userService.addToWishlist(userId, productId);
    }

    /** Remove a product from this user’s wishlist */
    @DeleteMapping("/{userId}/wishlist/{productId}")
    public ResponseEntity<String> removeFromWishlist(
            @PathVariable String userId,
            @PathVariable String productId) {
        return userService.removeFromWishlist(userId, productId);
    }

    /** Get all products in this user’s wishlist */
    @GetMapping("/{userId}/wishlist")
    public ResponseEntity<List<Product>> getWishlist(
            @PathVariable String userId) {
        return userService.getWishlist(userId);
    }

    @GetMapping("/product/{productId}/verified")
    public ResponseEntity<List<Review>> getVerifiedReviewsForProduct(
            @PathVariable String productId
    ) {
        List<Review> reviews = reviewService.getVerifiedReviewsForProduct(productId);
        return ResponseEntity.ok(reviews);
    }





}