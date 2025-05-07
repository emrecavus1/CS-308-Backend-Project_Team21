package com.cs308.backend.models;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.annotation.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;


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

    // Helper method to calculate default production cost (50% of price)
    public double getDefaultProductionCost() {
        return price * 0.5;
    }
    public int getStock() { return stockCount; }
    public void setStock(int stockCount) { this.stockCount = this.stockCount; }
}