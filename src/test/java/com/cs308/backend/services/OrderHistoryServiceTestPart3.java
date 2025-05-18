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

public class OrderHistoryServiceTestPart3 {

    @Mock private OrderHistoryRepository orderHistoryRepository;
    @Mock private CartRepository cartRepository;
    @Mock private OrderRepository orderRepository;
    @Mock private ProductRepository productRepository;
    @Mock private RefundRequestRepository refundRequestRepository;

    @InjectMocks private OrderHistoryService orderHistoryService;

    private String userId;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        userId = UUID.randomUUID().toString();
    }

    @Test
    public void testViewActiveOrdersByUser_MixedStatuses() {
        Order o1 = new Order(); o1.setOrderId("1"); o1.setStatus("Processing");
        Order o2 = new Order(); o2.setOrderId("2"); o2.setStatus("In-transit");
        Order o3 = new Order(); o3.setOrderId("3"); o3.setStatus("Delivered");

        when(orderRepository.findByUserIdAndShippedFalse(userId)).thenReturn(List.of(o1, o2, o3));

        ResponseEntity<List<String>> response = orderHistoryService.viewActiveOrdersByUser(userId);

        assertEquals(2, response.getBody().size());
        assertTrue(response.getBody().contains("1"));
        assertTrue(response.getBody().contains("2"));
    }

    @Test
    public void testGetProductsFromPreviousOrders_RefundedOrdersExcluded() {
        Order o1 = new Order(); o1.setProductIds(List.of("p1")); o1.setStatus("Delivered"); o1.setShipped(true);
        Order o2 = new Order(); o2.setProductIds(List.of("p2")); o2.setStatus("Refunded"); o2.setShipped(true);

        when(orderRepository.findByUserIdAndShippedTrue(userId)).thenReturn(List.of(o1, o2));

        Product product = new Product(); product.setProductId("p1");
        when(productRepository.findAllById(Set.of("p1"))).thenReturn(List.of(product));

        ResponseEntity<List<Product>> response = orderHistoryService.getProductsFromPreviousOrders(userId);

        assertEquals(1, response.getBody().size());
        assertEquals("p1", response.getBody().get(0).getProductId());
    }

    @Test
    public void testRemoveOrderFromHistory_Success() {
        OrderHistory history = new OrderHistory();
        history.setOrderIds(new ArrayList<>(List.of("order1")));

        when(orderHistoryRepository.findByUserId(userId)).thenReturn(Optional.of(history));

        ResponseEntity<String> response = orderHistoryService.removeOrderFromHistory(userId, "order1");

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Order removed from history.", response.getBody());
        verify(orderHistoryRepository).save(history);
    }
}
