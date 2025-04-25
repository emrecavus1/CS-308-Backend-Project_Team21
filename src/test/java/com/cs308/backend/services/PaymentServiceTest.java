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

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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
    public void setup() throws Exception {
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

        doNothing().when(invoiceService).emailPdfInvoice(anyString());
    }

    @Test
    public void testGetBankInformation_ValidInfo() {
        // Arrange
        String cardNumber = "1234567890123456";
        String expiryDate = "12/30"; // Future date
        String cvv = "123";

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        // Act
        ResponseEntity<String> response = paymentService.getBankInformation(
                userId, cardNumber, expiryDate, cvv
        );

        // Assert
        assertEquals("Bank information is valid.", response.getBody());
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    public void testProcessPayment_Successful() throws Exception {
        // Arrange
        String cardNumber = "1234567890123456";
        String expiryDate = "12/30";
        String cvv = "123";

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(paymentRepository.save(any(Payment.class))).thenReturn(new Payment());
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        // Act
        ResponseEntity<String> response = paymentService.processPayment(
                userId, orderId, cardNumber, expiryDate, cvv
        );

        // Assert
        assertEquals("Payment processed & invoice emailed!", response.getBody());
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(testOrder.isPaid());
        assertEquals("Processing", testOrder.getStatus());
        assertNotNull(testOrder.getPaymentId());
        verify(invoiceService, times(1)).emailPdfInvoice(orderId);
        verify(paymentRepository, times(1)).save(any(Payment.class));
        verify(orderRepository, times(1)).save(testOrder);
    }

    @Test
    public void testGetPaymentByOrderId_ReturnsPayment() {
        // Arrange
        Payment expectedPayment = new Payment();
        expectedPayment.setPaymentId(UUID.randomUUID().toString());
        expectedPayment.setOrderId(orderId);

        when(paymentRepository.findByOrderId(orderId)).thenReturn(Optional.of(expectedPayment));

        // Act
        Optional<Payment> result = paymentService.getPaymentByOrderId(orderId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(expectedPayment, result.get());
        verify(paymentRepository, times(1)).findByOrderId(orderId);
    }
}