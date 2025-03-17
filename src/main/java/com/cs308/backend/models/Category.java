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
@Document(collection = "categories")
public class Category {
    @Id
    private Product[] theProducts;
    private String categoryName;

}