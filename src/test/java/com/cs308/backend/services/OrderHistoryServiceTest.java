// 7. OrderHistoryServiceTest.java
package com.cs308.backend.services;

import com.cs308.backend.models.Cart;
import com.cs308.backend.models.Order;
import com.cs308.backend.models.OrderHistory;
import com.cs308.backend.models.Product;
import com.cs308.backend.repositories.CartRepository;
import com.cs308.backend.repositories.OrderHistoryRepository;
import com.cs308.backend.repositories.OrderRepository;
import com.cs308.backend.repositories.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class OrderHistoryServiceTest {

    @Mock
    private OrderHistoryRepository orderHistoryRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private OrderHistoryService orderHistoryService;

    private String userId;
    private String orderId;
    private Cart testCart;
    private OrderHistory testOrderHistory;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        userId = UUID.randomUUID().toString();
        orderId = UUID.randomUUID().toString();

        testCart = new Cart();
        testCart.setCartId(UUID.randomUUID().toString());
        testCart.setUserId(userId);
        testCart.setItems(new ArrayList<>());

        testOrderHistory = new OrderHistory();
        testOrderHistory.setOrderHistoryId(UUID.randomUUID().toString());
        testOrderHistory.setUserId(userId);
        testOrderHistory.setOrderIds(new ArrayList<>());
    }

    @Test
    public void testRecordOrderAndClearCart_ExistingOrderHistory() {
        // Arrange
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(testCart));
        when(orderHistoryRepository.findByUserId(userId)).thenReturn(Optional.of(testOrderHistory));
        when(orderHistoryRepository.save(any(OrderHistory.class))).thenReturn(testOrderHistory);

        // Act
        orderHistoryService.recordOrderAndClearCart(userId, orderId);

        // Assert
        verify(cartRepository, times(1)).save(testCart);
        verify(orderHistoryRepository, times(1)).save(testOrderHistory);
        assertTrue(testOrderHistory.getOrderIds().contains(orderId));
        assertTrue(testCart.getItems().isEmpty());
    }

    @Test
    public void testViewPreviousOrdersByUser() {
        // Arrange
        Order order1 = new Order();
        order1.setOrderId(UUID.randomUUID().toString());
        order1.setUserId(userId);
        order1.setShipped(true);

        Order order2 = new Order();
        order2.setOrderId(UUID.randomUUID().toString());
        order2.setUserId(userId);
        order2.setShipped(true);

        List<Order> shippedOrders = Arrays.asList(order1, order2);
        List<String> expectedOrderIds = Arrays.asList(order1.getOrderId(), order2.getOrderId());

        when(orderRepository.findByUserIdAndShippedTrue(userId)).thenReturn(shippedOrders);

        // Act
        ResponseEntity<List<String>> response = orderHistoryService.viewPreviousOrdersByUser(userId);

        // Assert
        assertEquals(expectedOrderIds, response.getBody());
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    public void testGetProductsFromPreviousOrders() {
        // Arrange
        String productId1 = UUID.randomUUID().toString();
        String productId2 = UUID.randomUUID().toString();

        Order order1 = new Order();
        order1.setOrderId(UUID.randomUUID().toString());
        order1.setUserId(userId);
        order1.setShipped(true);
        order1.setProductIds(Arrays.asList(productId1));

        Order order2 = new Order();
        order2.setOrderId(UUID.randomUUID().toString());
        order2.setUserId(userId);
        order2.setShipped(true);
        order2.setProductIds(Arrays.asList(productId2));

        List<Order> shippedOrders = Arrays.asList(order1, order2);

        Product product1 = new Product();
        product1.setProductId(productId1);
        product1.setProductName("Product 1");

        Product product2 = new Product();
        product2.setProductId(productId2);
        product2.setProductName("Product 2");

        List<Product> expectedProducts = Arrays.asList(product1, product2);

        when(orderRepository.findByUserIdAndShippedTrue(userId)).thenReturn(shippedOrders);
        when(productRepository.findAllById(Arrays.asList(productId1, productId2))).thenReturn(expectedProducts);

        // Act
        ResponseEntity<List<Product>> response = orderHistoryService.getProductsFromPreviousOrders(userId);

        // Assert
        assertEquals(expectedProducts, response.getBody());
        assertEquals(200, response.getStatusCodeValue());
    }
}