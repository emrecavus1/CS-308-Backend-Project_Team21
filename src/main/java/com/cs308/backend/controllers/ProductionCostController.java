package com.cs308.backend.controllers;

import com.cs308.backend.models.Product;
import com.cs308.backend.services.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
@RestController
@RequestMapping("/api/production-cost")
public class ProductionCostController {

    private final ProductService productService;

    public ProductionCostController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * Get the production cost for a specific product
     */
    @GetMapping("/{productId}")
    public ResponseEntity<Map<String, Object>> getProductionCost(@PathVariable String productId) {
        double productionCost = productService.getProductionCost(productId);
        Map<String, Object> response = Map.of(
                "productId", productId,
                "productionCost", productionCost
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Set a custom production cost for a product
     */
    @PutMapping("/{productId}")
    public ResponseEntity<Product> setProductionCost(
            @PathVariable String productId,
            @RequestParam double productionCost) {
        Product updatedProduct = productService.setProductionCost(productId, productionCost);
        return ResponseEntity.ok(updatedProduct);
    }

    /**
     * Reset the production cost to the default value (50% of price)
     */
    @PostMapping("/{productId}/reset")
    public ResponseEntity<Product> resetProductionCostToDefault(@PathVariable String productId) {
        Product updatedProduct = productService.resetProductionCostToDefault(productId);
        return ResponseEntity.ok(updatedProduct);
    }

    /**
     * Get products sorted by profit margin (price - production cost)
     */
    @GetMapping("/sort-by-profit-margin")
    public ResponseEntity<List<Product>> getProductsSortedByProfitMargin() {
        List<Product> sortedProducts = productService.sortProductsByProfitMargin();
        return ResponseEntity.ok(sortedProducts);
    }

    /**
     * Get products sorted by profit margin percentage
     */
    @GetMapping("/sort-by-profit-margin-percentage")
    public ResponseEntity<List<Product>> getProductsSortedByProfitMarginPercentage() {
        List<Product> sortedProducts = productService.sortProductsByProfitMarginPercentage();
        return ResponseEntity.ok(sortedProducts);
    }
}