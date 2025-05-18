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

public class PaymentServiceTestPart2 {

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
    public void testGetBankInformation_InvalidCardNumber() {
        String cardNumber = "1234";
        String expiry = "12/30";
        String cvv = "123";

        when(userRepository.findById(userId)).thenReturn(Optional.of(new User()));
        var response = paymentService.getBankInformation(userId, cardNumber, expiry, cvv);

        assertEquals(400, response.getStatusCodeValue());
        assertTrue(response.getBody().contains("Invalid card number"));
    }

    @Test
    public void testGetBankInformation_ExpiredCard() {
        String cardNumber = "1234567812345678";
        String expiry = "01/20";  // expired
        String cvv = "123";

        when(userRepository.findById(userId)).thenReturn(Optional.of(new User()));
        var response = paymentService.getBankInformation(userId, cardNumber, expiry, cvv);

        assertEquals(400, response.getStatusCodeValue());
        assertTrue(response.getBody().contains("expired"));
    }

    @Test
    public void testDeletePayment_Successful() {
        String paymentId = UUID.randomUUID().toString();
        when(paymentRepository.existsById(paymentId)).thenReturn(true);

        ResponseEntity<String> response = paymentService.deletePayment(paymentId);

        assertEquals(200, response.getStatusCodeValue());
        verify(paymentRepository).deleteById(paymentId);
    }
}
