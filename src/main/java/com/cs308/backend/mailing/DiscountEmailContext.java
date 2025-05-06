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
        setTemplateLocation("discount-notification");

    }

    // Keep our custom initialization method but rename it to avoid confusion
    public void initWithDetails(String name, String productName, double discountPercentage,
                                double originalPrice, double discountedPrice, String productId) {
        Map<String, Object> contextMap = new HashMap<>();
        contextMap.put("name", name);  // changed from "userName" to match the template
        contextMap.put("productName", productName);
        contextMap.put("discountPercentage", discountPercentage);
        contextMap.put("originalPrice", originalPrice);
        contextMap.put("discountedPrice", discountedPrice);
        contextMap.put("productId", productId);

        init(contextMap);
        setSubject("Special Discount on Your Wishlist Item: " + productName);

        // Optionally update internal state if needed later
        this.productName = productName;
        this.discountPercentage = discountPercentage;
        this.originalPrice = originalPrice;
        this.discountedPrice = discountedPrice;
        this.productId = productId;
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