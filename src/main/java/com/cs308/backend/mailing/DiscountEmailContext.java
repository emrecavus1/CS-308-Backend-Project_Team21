package com.cs308.backend.mailing;

import java.util.HashMap;
import java.util.Map;

public class DiscountEmailContext extends AbstractEmailContext {

    private String productName;
    private double discountPercentage;
    private double originalPrice;
    private double discountedPrice;
    private String productId;

    public DiscountEmailContext() {
        super();
    }

    // Implement the abstract method required by AbstractEmailContext
    @Override
    public <T> void init(T context) {
        // Implementation depends on what type of context is passed
        if (context instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> contextMap = (Map<String, Object>) context;
            setContext(contextMap);

            // Extract values if they exist in the map
            if (contextMap.containsKey("productName")) {
                this.productName = (String) contextMap.get("productName");
                setSubject("Special Discount on Your Wishlist Item: " + this.productName);
            }
        }

        // Set the template location regardless of the context type
        setTemplateLocation("email-templates/discount-notification");
    }

    // Keep our custom initialization method but rename it to avoid confusion
    public void initWithDetails(String userName, String productName, double discountPercentage,
                                double originalPrice, double discountedPrice, String productId) {
        // Set predefined context variables
        Map<String, Object> contextMap = new HashMap<>();
        contextMap.put("userName", userName);
        contextMap.put("productName", productName);
        contextMap.put("discountPercentage", discountPercentage);
        contextMap.put("originalPrice", originalPrice);
        contextMap.put("discountedPrice", discountedPrice);
        contextMap.put("productId", productId);

        // Use the generic init method to set the context
        init(contextMap);

        // Store values for later use if needed
        this.productName = productName;
        this.discountPercentage = discountPercentage;
        this.originalPrice = originalPrice;
        this.discountedPrice = discountedPrice;
        this.productId = productId;

        // Set subject (already set in init() but we do it again to be safe)
        setSubject("Special Discount on Your Wishlist Item: " + productName);
    }

    // Getters for the properties
    public String getProductName() {
        return productName;
    }

    public double getDiscountPercentage() {
        return discountPercentage;
    }

    public double getOriginalPrice() {
        return originalPrice;
    }

    public double getDiscountedPrice() {
        return discountedPrice;
    }

    public String getProductId() {
        return productId;
    }
}