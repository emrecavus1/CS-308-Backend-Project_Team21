// 4. CartServiceTest.java
package com.cs308.backend.services;

import com.cs308.backend.models.Cart;
import com.cs308.backend.models.CartItem;
import com.cs308.backend.models.Product;
import com.cs308.backend.repositories.CartRepository;
import com.cs308.backend.repositories.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private CartService cartService;

    private Product testProduct;
    private Cart testCart;
    private String userId;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        userId = UUID.randomUUID().toString();

        testProduct = new Product();
        testProduct.setProductId(UUID.randomUUID().toString());
        testProduct.setProductName("Test Product");
        testProduct.setStockCount(10);
        testProduct.setPrice(99.99);

        testCart = new Cart();
        testCart.setCartId(UUID.randomUUID().toString());
        testCart.setUserId(userId);
        testCart.setItems(new ArrayList<>());
    }

    @Test
    public void testAddToCart_NewItem_Successful() {
        // Arrange
        when(productRepository.findById(testProduct.getProductId())).thenReturn(Optional.of(testProduct));
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(testCart));
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // Act
        ResponseEntity<String> response = cartService.addToCart(userId, testProduct.getProductId());

        // Assert
        assertEquals("Product added to cart successfully!", response.getBody());
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, testCart.getItems().size());
        assertEquals(testProduct.getProductId(), testCart.getItems().get(0).getProductId());
        assertEquals(1, testCart.getItems().get(0).getQuantity());
        assertEquals(9, testProduct.getStockCount()); // Stock reduced by 1
        verify(productRepository, times(1)).save(testProduct);
        verify(cartRepository, times(1)).save(testCart);
    }

    @Test
    public void testAddToCart_ExistingItem_IncreasesQuantity() {
        // Arrange
        CartItem existingItem = new CartItem(testProduct.getProductId(), 1);
        testCart.getItems().add(existingItem);

        when(productRepository.findById(testProduct.getProductId())).thenReturn(Optional.of(testProduct));
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(testCart));
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // Act
        ResponseEntity<String> response = cartService.addToCart(userId, testProduct.getProductId());

        // Assert
        assertEquals("Product added to cart successfully!", response.getBody());
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, testCart.getItems().size());
        assertEquals(2, testCart.getItems().get(0).getQuantity()); // Quantity increased
        assertEquals(9, testProduct.getStockCount()); // Stock reduced by 1
        verify(productRepository, times(1)).save(testProduct);
        verify(cartRepository, times(1)).save(testCart);
    }

    @Test
    public void testAddToCart_CreateNewCartIfNotExists() {
        // Arrange
        when(productRepository.findById(testProduct.getProductId())).thenReturn(Optional.of(testProduct));
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.empty()); // No existing cart
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // Act
        ResponseEntity<String> response = cartService.addToCart(userId, testProduct.getProductId());

        // Assert
        assertEquals("Product added to cart successfully!", response.getBody());
        assertEquals(200, response.getStatusCodeValue());
        verify(productRepository, times(1)).save(testProduct);
        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    @Test
    public void testDeleteProductsInCart_Successful() {
        // Arrange
        CartItem item = new CartItem(testProduct.getProductId(), 2);
        testCart.getItems().add(item);

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(testCart));
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        // Act
        ResponseEntity<String> response = cartService.deleteProductsInCart(userId);

        // Assert
        assertEquals("Cart cleared.", response.getBody());
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(testCart.getItems().isEmpty());
        verify(cartRepository, times(1)).save(testCart);
    }
}