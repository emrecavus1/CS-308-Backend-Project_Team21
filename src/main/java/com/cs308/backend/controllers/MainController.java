package com.cs308.backend.controllers;


import com.cs308.backend.models.*;
import com.cs308.backend.models.CartItem;
import com.cs308.backend.repositories.*;
import com.cs308.backend.services.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.stream.Collectors;


import jakarta.servlet.http.HttpServletResponse;


import org.springframework.http.ResponseCookie;
import org.springframework.http.HttpHeaders;
import java.time.Duration;


@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
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


    @GetMapping("/products/{productId}")
    public ResponseEntity<Product> getProductById(@PathVariable String productId) {
        return productService.getProductById(productId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
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

    @GetMapping("/category/{categoryId}/showProductsByCategory")
    public ResponseEntity<Map<String, Object>> showProductsByCategory(@PathVariable String categoryId) {
        List<Product> products = productService.getProductsByCategory(categoryId).stream()
                .filter(p -> p.getPrice() > 0)
                .toList();

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
    public ResponseEntity<AddToCartResponse> addToCart(
            @CookieValue(value="CART_ID", required=false) String cartId,
            @RequestParam String productId,
            HttpServletResponse servletResponse
    ) {
        ResponseEntity<AddToCartResponse> resp = cartService.addToCart(cartId, productId);


        if (resp.getStatusCode().is2xxSuccessful()) {
            AddToCartResponse body = resp.getBody();
            if (body != null && (cartId == null || !cartId.equals(body.getCartId()))) {
                ResponseCookie cookie = ResponseCookie.from("CART_ID", body.getCartId())
                        .httpOnly(true)
                        .sameSite("Lax")
                        // .secure(true)    ← remove or comment this out for local HTTP
                        .path("/")
                        .maxAge(Duration.ofDays(30))
                        .build();
                servletResponse.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
            }
        }


        return resp;
    }






    @GetMapping("/items")
    public ResponseEntity<List<CartItem>> getCartItems(
            @CookieValue(value="CART_ID", required=false) String cartId
    ) {
        System.out.println("→ getCartItems, received cookie CART_ID=" + cartId);


        if (cartId == null) {
            // no cart yet → return empty list
            return ResponseEntity.ok(Collections.emptyList());
        }


        // 1) fetch the items into a local variable
        List<CartItem> items = cartService.getCartItems(cartId);


        // 2) log what you’re about to return
        System.out.println("→ getCartItems, returning items: " + items);


        // 3) return the same list
        return ResponseEntity.ok(items);
    }




    @GetMapping("/category/{categoryId}")
    public ResponseEntity<Category> getCategory(@PathVariable String categoryId) {
        Category c = categoryService.findById(categoryId);
        return ResponseEntity.ok(c);
    }


    @DeleteMapping("/cart/clear")
    public ResponseEntity<Void> clearCart(@RequestParam String cartId) {
        cartService.clearCart(cartId);
        return ResponseEntity.noContent().build();
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

    @GetMapping("/reviews/pending-comments")
    public ResponseEntity<List<Map<String, String>>> getPendingCommentsWithUserIds() {
        List<Map<String, String>> pending = reviewService.getPendingCommentsWithUserIds();
        return ResponseEntity.ok(pending);
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

    @DeleteMapping("/review/{reviewId}")
    public ResponseEntity<Void> deleteReview(@PathVariable String reviewId) {
        reviewService.deleteReview(reviewId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/deleteProduct/{productId}")
    public ResponseEntity<Void> deleteProductWithCleanup(@PathVariable String productId) {
        productService.deleteProductWithCleanup(productId);
        return ResponseEntity.noContent().build();
    }


    @DeleteMapping("/deleteCategory/{categoryId}")
    public ResponseEntity<Void> deleteCategoryWithCleanup(@PathVariable String categoryId) {
        // 1. Fetch all products in this category
        List<Product> products = productService.getProductsByCategory(categoryId);

        // 2. Delete each product using the cleanup method
        for (Product p : products) {
            productService.deleteProductWithCleanup(p.getProductId());
        }

        // 3. Now delete the category itself
        categoryService.deleteCategory(categoryId);

        return ResponseEntity.noContent().build();
    }

    /**
     * Update the production cost of a product
     */
    @PutMapping("/updateProductionCost/{productId}/{productionCost}")
    public ResponseEntity<Product> updateProductionCost(@PathVariable String productId, @PathVariable double productionCost) {
        Product updatedProduct = productService.setProductionCost(productId, productionCost);
        return ResponseEntity.ok(updatedProduct);
    }

    /**
     * Reset the production cost to default value (50% of price)
     */
    @PostMapping("/resetProductionCost/{productId}")
    public ResponseEntity<Product> resetProductionCost(@PathVariable String productId) {
        Product updatedProduct = productService.resetProductionCostToDefault(productId);
        return ResponseEntity.ok(updatedProduct);
    }


    @GetMapping("/products/new")
    public ResponseEntity<List<Product>> getNewProducts() {
        List<Product> newProducts = productService.getNewProducts();
        return ResponseEntity.ok(newProducts);
    }


}
