// 5. OrderServiceTest.java
package com.cs308.backend.services;

import com.cs308.backend.models.Cart;
import com.cs308.backend.models.CartItem;
import com.cs308.backend.models.Order;
import com.cs308.backend.repositories.CartRepository;
import com.cs308.backend.repositories.OrderHistoryRepository;
import com.cs308.backend.repositories.OrderRepository;
import com.cs308.backend.repositories.ProductRepository;
import com.cs308.backend.repositories.UserRepository;
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

public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderHistoryRepository orderHistoryRepository;

    @InjectMocks
    private OrderService orderService;

    private String userId;
    private Cart testCart;
    private Order testOrder;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        userId = UUID.randomUUID().toString();

        // Setup test cart with items
        testCart = new Cart();
        testCart.setCartId(UUID.randomUUID().toString());
        testCart.setUserId(userId);

        List<CartItem> cartItems = new ArrayList<>();
        cartItems.add(new CartItem(UUID.randomUUID().toString(), 2, 99.99));
        cartItems.add(new CartItem(UUID.randomUUID().toString(), 1, 49.99));
        testCart.setItems(cartItems);

        // Setup test order
        testOrder = new Order();
        testOrder.setOrderId(UUID.randomUUID().toString());
        testOrder.setUserId(userId);
        testOrder.setCartId(testCart.getCartId());
        testOrder.setStatus("Processing");
        testOrder.setPaid(false);
        testOrder.setShipped(false);
    }

    @Test
    public void testCreateOrderFromCart_Successful() {
        // Arrange
        when(cartRepository.findById(testCart.getCartId())).thenReturn(Optional.of(testCart));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        // Act
        String orderId = orderService.createOrderFromCart(testCart.getCartId(), userId);

        // Assert
        assertNotNull(orderId);
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    public void testMarkAsShipped_Successful() {
        // Arrange
        when(orderRepository.findById(testOrder.getOrderId())).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        // Act
        ResponseEntity<String> response = orderService.markAsShipped(testOrder.getOrderId());

        // Assert
        assertEquals("Order marked as shipped.", response.getBody());
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(testOrder.isShipped());
        assertEquals("Delivered", testOrder.getStatus());
        verify(orderRepository, times(1)).save(testOrder);
    }

    @Test
    public void testMarkAsInTransit_Successful() {
        // Arrange
        when(orderRepository.findById(testOrder.getOrderId())).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        // Act
        ResponseEntity<String> response = orderService.markAsInTransit(testOrder.getOrderId());

        // Assert
        assertEquals("Order marked as in-transit.", response.getBody());
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("In-Transit", testOrder.getStatus());
        verify(orderRepository, times(1)).save(testOrder);
    }

    @Test
    public void testGetOrdersByUser_ReturnsUserOrders() {
        // Arrange
        List<Order> expectedOrders = List.of(testOrder);
        when(orderRepository.findByUserId(userId)).thenReturn(expectedOrders);

        // Act
        List<Order> result = orderService.getOrdersByUser(userId);

        // Assert
        assertEquals(expectedOrders, result);
        verify(orderRepository, times(1)).findByUserId(userId);
    }
}