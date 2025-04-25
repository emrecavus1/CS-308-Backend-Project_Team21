// 6. PaymentServiceTest.java
package com.cs308.backend.services;

import com.cs308.backend.models.Order;
import com.cs308.backend.models.Payment;
import com.cs308.backend.models.User;
import com.cs308.backend.repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class PaymentServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private InvoiceService invoiceService;

    @InjectMocks
    private PaymentService paymentService;

    private String userId;
    private String orderId;
    private User testUser;
    private Order testOrder;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        userId = UUID.randomUUID().toString();
        orderId = UUID.randomUUID().toString();

        testUser = new User();
        testUser.setUserId(userId);
        testUser.setEmail("test@gmail.com");

        testOrder = new Order();
        testOrder.setOrderId(orderId);
        testOrder.setUserId(userId);
        testOrder.setStatus("Processing");
        testOrder.setPaid(false);
        testOrder.setProductIds(List.of());
        testOrder.setQuantities(List.of());

        // Stub invoice email to do nothing
        try {
            doNothing().when(invoiceService).emailPdfInvoice(anyString());
        } catch (Exception e) {
            throw new RuntimeException("Failed to stub InvoiceService", e);
        }
    }

    @Test
    public void testGetBankInformation_ValidInfo() {
        String cardNumber = "1234567890123456";
        String expiryDate = "12/30";
        String cvv = "123";

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        ResponseEntity<String> response = paymentService.getBankInformation(
                userId, cardNumber, expiryDate, cvv
        );

        assertEquals("Bank information is valid.", response.getBody());
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    public void testProcessPayment_Successful() {
        String cardNumber = "1234567890123456";
        String expiryDate = "12/30";
        String cvv = "123";

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        ResponseEntity<String> response = paymentService.processPayment(
                userId, orderId, cardNumber, expiryDate, cvv
        );

        assertEquals("Payment processed, stock updated & invoice emailed!", response.getBody());
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(testOrder.isPaid());
        assertEquals("Processing", testOrder.getStatus());

        try {
            verify(invoiceService, times(1)).emailPdfInvoice(orderId);
        } catch (Exception e) {
            fail("Invoice email verification failed: " + e.getMessage());
        }

        // No paymentRepository.save() in service, so don't verify it here
        verify(orderRepository, times(1)).save(testOrder);
    }

    @Test
    public void testGetPaymentByOrderId_ReturnsPayment() {
        Payment expectedPayment = new Payment();
        expectedPayment.setPaymentId(UUID.randomUUID().toString());
        expectedPayment.setOrderId(orderId);

        when(paymentRepository.findByOrderId(orderId)).thenReturn(Optional.of(expectedPayment));

        Optional<Payment> result = paymentService.getPaymentByOrderId(orderId);

        assertTrue(result.isPresent());
        assertEquals(expectedPayment, result.get());
        verify(paymentRepository, times(1)).findByOrderId(orderId);
    }
}
