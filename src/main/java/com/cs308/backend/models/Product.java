package com.cs308.backend.models;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.annotation.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Date;

import com.cs308.backend.models.*;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "products")
public class Product {
    @Id
    private String productId;

    private String serialNumber;

    private int stockCount;
    private double price;
    private String productName;
    private List<String> reviewIds;
    private double rating;
    private String productInfo;
    private String categoryId;
    private String warrantyStatus;
    private String distributorInfo;

    // Production cost field with default value calculation handled in service layer
    private Double productionCost;

    // New discount fields
    private double discountPercentage;
    private boolean discountActive;
    private Date discountStartDate;
    private Date discountEndDate;

    // Helper method to get the current price (with discount applied if active)
    public double getCurrentPrice() {
        if (discountActive && discountStartDate != null && discountEndDate != null) {
            Date now = new Date();
            if (now.after(discountStartDate) && now.before(discountEndDate)) {
                return price * (1 - discountPercentage / 100);
            }
        }
        return price;
    }

    // Helper method to calculate default production cost (50% of price)
    public double getDefaultProductionCost() {
        return price * 0.5;
    }
}