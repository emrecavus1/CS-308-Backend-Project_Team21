package com.cs308.backend.services;

import com.cs308.backend.models.*;
import com.cs308.backend.repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import org.springframework.http.ResponseEntity;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class OrderHistoryServiceTestPart2 {

    @Mock private OrderHistoryRepository orderHistoryRepository;
    @Mock private CartRepository cartRepository;
    @Mock private OrderRepository orderRepository;
    @Mock private ProductRepository productRepository;
    @Mock private RefundRequestRepository refundRequestRepository;

    @InjectMocks private OrderHistoryService orderHistoryService;

    private String userId;
    private String orderId;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        userId = UUID.randomUUID().toString();
        orderId = UUID.randomUUID().toString();
    }

    @Test
    public void testRecordOrderAndClearCart_NoExistingHistory() {
        Cart cart = new Cart();
        cart.setItems(new ArrayList<>());

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(orderHistoryRepository.findByUserId(userId)).thenReturn(Optional.empty());

        orderHistoryService.recordOrderAndClearCart(userId, orderId);

        verify(cartRepository).save(cart);
        verify(orderHistoryRepository).save(any(OrderHistory.class));
    }

    @Test
    public void testGetHistoryByUser_Found() {
        OrderHistory history = new OrderHistory();
        history.setOrderIds(List.of("order1", "order2"));

        when(orderHistoryRepository.findByUserId(userId)).thenReturn(Optional.of(history));

        ResponseEntity<List<String>> response = orderHistoryService.getHistoryByUser(userId);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(history.getOrderIds(), response.getBody());
    }

    @Test
    public void testGetHistoryByUser_NotFound() {
        when(orderHistoryRepository.findByUserId(userId)).thenReturn(Optional.empty());

        ResponseEntity<List<String>> response = orderHistoryService.getHistoryByUser(userId);

        assertEquals(404, response.getStatusCodeValue());
    }
}
