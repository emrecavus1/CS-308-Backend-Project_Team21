// 4. CartServiceTest.java
package com.cs308.backend.services;

import com.cs308.backend.models.Cart;
import com.cs308.backend.models.CartItem;
import com.cs308.backend.models.Product;
import com.cs308.backend.models.AddToCartResponse;
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
    private String cartId;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        cartId = UUID.randomUUID().toString();

        testProduct = new Product();
        testProduct.setProductId(UUID.randomUUID().toString());
        testProduct.setProductName("Test Product");
        testProduct.setStockCount(10);
        testProduct.setPrice(99.99);

        testCart = new Cart();
        testCart.setCartId(cartId);
        testCart.setUserId(UUID.randomUUID().toString());
        testCart.setItems(new ArrayList<>());
    }

    @Test
    public void testAddToCart_NewItem_Successful() {
        // Arrange
        when(productRepository.findById(testProduct.getProductId())).thenReturn(Optional.of(testProduct));
        when(cartRepository.existsById(cartId)).thenReturn(true);
        when(cartRepository.findById(cartId)).thenReturn(Optional.of(testCart));
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        // Act
        ResponseEntity<AddToCartResponse> response = cartService.addToCart(cartId, testProduct.getProductId());

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(cartId, response.getBody().getCartId());
        assertTrue(response.getBody().getMessage().contains("You now have 1×"));
        assertEquals(1, testCart.getItems().size());
        assertEquals(testProduct.getProductId(), testCart.getItems().get(0).getProductId());
        assertEquals(1, testCart.getItems().get(0).getQuantity());
        verify(cartRepository, times(1)).save(testCart);
    }

    @Test
    public void testAddToCart_ExistingItem_IncreasesQuantity() {
        // Arrange
        CartItem existingItem = new CartItem(testProduct.getProductId(), 1);
        testCart.getItems().add(existingItem);

        when(productRepository.findById(testProduct.getProductId())).thenReturn(Optional.of(testProduct));
        when(cartRepository.existsById(cartId)).thenReturn(true);
        when(cartRepository.findById(cartId)).thenReturn(Optional.of(testCart));
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        // Act
        ResponseEntity<AddToCartResponse> response = cartService.addToCart(cartId, testProduct.getProductId());

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(cartId, response.getBody().getCartId());
        assertTrue(response.getBody().getMessage().contains("You now have 2×"));
        assertEquals(1, testCart.getItems().size());
        assertEquals(2, testCart.getItems().get(0).getQuantity()); // Quantity increased
        verify(cartRepository, times(1)).save(testCart);
    }

    @Test
    public void testAddToCart_CreateNewCartIfNotExists() {
        // Arrange
        when(productRepository.findById(testProduct.getProductId())).thenReturn(Optional.of(testProduct));
        when(cartRepository.existsById(cartId)).thenReturn(false);
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        // Act
        ResponseEntity<AddToCartResponse> response = cartService.addToCart(cartId, testProduct.getProductId());

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getCartId()); // Just check that a cartId is assigned
        assertTrue(response.getBody().getMessage().contains("New cart created"));
        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    @Test
    public void testClearCart_Successful() {
        // Arrange
        CartItem item = new CartItem(testProduct.getProductId(), 2);
        testCart.getItems().add(item);

        when(cartRepository.findById(cartId)).thenReturn(Optional.of(testCart));
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        // Act
        cartService.clearCart(cartId);

        // Assert
        assertTrue(testCart.getItems().isEmpty());
        verify(cartRepository, times(1)).save(testCart);
    }

    @Test
    public void testGetCartItems_ReturnsItems() {
        // Arrange
        CartItem item = new CartItem(testProduct.getProductId(), 2);
        testCart.getItems().add(item);

        when(cartRepository.findById(cartId)).thenReturn(Optional.of(testCart));

        // Act
        List<CartItem> result = cartService.getCartItems(cartId);

        // Assert
        assertEquals(1, result.size());
        assertEquals(testProduct.getProductId(), result.get(0).getProductId());
        assertEquals(2, result.get(0).getQuantity());
    }
}

// No need to define AddToCartResponse here as it's already defined in com.cs308.backend.models package