package com.cs308.backend.models;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.annotation.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.cs308.backend.models.*;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "products")
public class Product {
    @Id
    private String pid;
    private int stockCount;
    private String imageUrl;
    private double price;
    private String productName;
    private Review[] reviews;
    private String productInfo;
    private Category theCategory;

    public Product(String pid, int count, String url, double price, String name,  Review[] reviews, String info, Category category) {
        this.pid = pid;
        this.stockCount = count;
        this.imageUrl = url;
        this.price = price;
        this.productName = name;
        this.reviews = reviews;
        this.productInfo = info;
        this.theCategory = category;
    }
}