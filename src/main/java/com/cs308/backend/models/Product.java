package com.cs308.backend.models;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.annotation.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

import com.cs308.backend.models.*;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "products")
public class Product {
    @Id
    private String productId;
    private int stockCount;
    private String imageUrl;
    private double price;
    private String productName;
    private List<String> reviewIds;
    private String productInfo;
    private String categoryId;
}