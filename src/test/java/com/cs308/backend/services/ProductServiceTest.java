// 3. ProductServiceTest.java
package com.cs308.backend.services;

import com.cs308.backend.models.Category;
import com.cs308.backend.models.Product;
import com.cs308.backend.repositories.CategoryRepository;
import com.cs308.backend.repositories.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ProductService productService;

    private Product testProduct;
    private Category testCategory;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        testCategory = new Category();
        testCategory.setCategoryId(UUID.randomUUID().toString());
        testCategory.setCategoryName("Electronics");
        testCategory.setProductIds(new ArrayList<>());

        testProduct = new Product();
        testProduct.setProductId(UUID.randomUUID().toString());
        testProduct.setProductName("Test Product");
        testProduct.setProductInfo("Test product description");
        testProduct.setCategoryId(testCategory.getCategoryId());
        testProduct.setStockCount(10);
        testProduct.setPrice(99.99);
        testProduct.setSerialNumber("SN12345");
        testProduct.setWarrantyStatus("1 year");
        testProduct.setDistributorInfo("Test Distributor");
    }

    @Test
    public void testAddProduct_Successful() {
        // Arrange
        when(productRepository.findByProductNameIgnoreCase(testProduct.getProductName())).thenReturn(null);
        when(categoryRepository.findByCategoryNameIgnoreCase(testCategory.getCategoryName())).thenReturn(Optional.of(testCategory));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);
        when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);

        // Act
        ResponseEntity<String> response = productService.addProduct(
                testProduct,
                testProduct.getProductName(),
                testProduct.getProductInfo(),
                testCategory.getCategoryName(),
                testProduct.getStockCount(),
                testProduct.getSerialNumber(),
                testProduct.getWarrantyStatus(),
                testProduct.getDistributorInfo()
        );

        // Assert
        assertEquals("Product added successfully!", response.getBody());
        assertEquals(200, response.getStatusCodeValue());
        verify(productRepository, times(1)).save(any(Product.class));
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    public void testUpdateStock_Successful() {
        // Arrange
        int newStock = 20;
        when(productRepository.findById(testProduct.getProductId())).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // Act
        Product result = productService.updateStock(testProduct.getProductId(), newStock);

        // Assert
        assertEquals(newStock, result.getStockCount());
        verify(productRepository, times(1)).save(testProduct);
    }

    @Test
    public void testUpdateProduct_WithMultipleFields() {
        // Arrange
        Map<String, Object> updates = new HashMap<>();
        updates.put("productName", "Updated Product Name");
        updates.put("price", "149.99");
        updates.put("stockCount", "15");

        when(productRepository.findById(testProduct.getProductId())).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // Act
        Product result = productService.updateProduct(testProduct.getProductId(), updates);

        // Assert
        assertEquals("Updated Product Name", result.getProductName());
        assertEquals(149.99, result.getPrice());
        assertEquals(15, result.getStockCount());
        verify(productRepository, times(1)).save(testProduct);
    }

    @Test
    public void testSearchProducts_ReturnsMatchingProducts() {
        // Arrange
        String query = "Test";
        List<Product> expectedProducts = Collections.singletonList(testProduct);
        when(productRepository.findByProductNameContainingIgnoreCaseOrProductInfoContainingIgnoreCase(query, query))
                .thenReturn(expectedProducts);

        // Act
        List<Product> result = productService.searchProducts(query);

        // Assert
        assertEquals(expectedProducts, result);
        verify(productRepository, times(1))
                .findByProductNameContainingIgnoreCaseOrProductInfoContainingIgnoreCase(query, query);
    }
}