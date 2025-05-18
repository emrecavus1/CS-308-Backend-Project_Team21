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

public class PaymentServiceTestPart3 {

    @Mock private OrderRepository orderRepository;
    @Mock private PaymentRepository paymentRepository;
    @Mock private UserRepository userRepository;
    @Mock private ProductRepository productRepository;
    @Mock private CartRepository cartRepository;
    @Mock private InvoiceService invoiceService;

    @InjectMocks private PaymentService paymentService;

    private String userId;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        userId = UUID.randomUUID().toString();
    }

    @Test
    public void testDeletePayment_NotFound() {
        String paymentId = "nonexistent";
        when(paymentRepository.existsById(paymentId)).thenReturn(false);

        ResponseEntity<String> response = paymentService.deletePayment(paymentId);

        assertEquals(400, response.getStatusCodeValue());
        assertTrue(response.getBody().contains("not found"));
    }

    @Test
    public void testGetPaymentsByUserId_ReturnsList() {
        List<Payment> payments = List.of(new Payment(), new Payment());
        when(paymentRepository.findByUserId(userId)).thenReturn(payments);

        List<Payment> result = paymentService.getPaymentsByUserId(userId);

        assertEquals(2, result.size());
        verify(paymentRepository).findByUserId(userId);
    }

    @Test
    public void testGetBankInformation_InvalidExpiryFormat() {
        String cardNumber = "1234567812345678";
        String expiry = "2025-01";  // bad format
        String cvv = "123";

        when(userRepository.findById(userId)).thenReturn(Optional.of(new User()));
        var response = paymentService.getBankInformation(userId, cardNumber, expiry, cvv);

        assertEquals(400, response.getStatusCodeValue());
        assertTrue(response.getBody().contains("Invalid expiry date format"));
    }
}
