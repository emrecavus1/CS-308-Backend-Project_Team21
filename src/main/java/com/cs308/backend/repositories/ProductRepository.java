package com.cs308.backend.repositories;

import com.cs308.backend.models.Product;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductRepository extends MongoRepository<Product, String> {

    // Find products by category
    List<Product> findByCategoryId(String categoryId);

    // Find a product by its name (case insensitive)
    Product findByProductNameIgnoreCase(String productName);

    // Find all products that are in stock
    List<Product> findByStockCountGreaterThan(int minStock);

    List<Product> findByProductNameContainingIgnoreCaseOrProductInfoContainingIgnoreCase(
            String productName, String productInfo);


    // Delete all products by category
    void deleteByCategoryId(String categoryId);

}

