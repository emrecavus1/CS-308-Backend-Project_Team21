package com.cs308.backend.services;

import com.cs308.backend.models.Order;
import com.cs308.backend.models.Product;
import com.cs308.backend.models.User;
import com.cs308.backend.repositories.OrderRepository;
import com.cs308.backend.repositories.ProductRepository;
import com.cs308.backend.repositories.UserRepository;
import com.cs308.backend.util.PdfInvoiceBuilder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class InvoiceServiceEmailTest {

    @Mock private JavaMailSender mailSender;
    @Mock private OrderRepository orderRepo;
    @Mock private UserRepository userRepo;
    @Mock private ProductRepository prodRepo;
    @Mock private MimeMessage mimeMessage;

    @InjectMocks private InvoiceService invoiceService;

    private Order testOrder;
    private User testUser;
    private Product testProduct;

    @BeforeEach
    public void setup() throws Exception {
        MockitoAnnotations.openMocks(this);

        testOrder = new Order();
        testOrder.setOrderId("order123");
        testOrder.setUserId("user456");
        testOrder.setProductIds(List.of("p1"));
        testOrder.setQuantities(List.of(1));
        testOrder.setCardNumber("1111222233334444");
        testOrder.setExpiryDate("12/30");
        testOrder.setCvv("123");

        testUser = new User();
        testUser.setUserId("user456");
        testUser.setName("Alice");
        testUser.setEmail("alice@example.com");

        testProduct = new Product();
        testProduct.setProductId("p1");

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
    }

    @Test
    public void testEmailPdfInvoice_EmailStructure() throws Exception {
        when(orderRepo.findById("order123")).thenReturn(Optional.of(testOrder));
        when(userRepo.findById("user456")).thenReturn(Optional.of(testUser));
        when(prodRepo.findAllById(any())).thenReturn(List.of(testProduct));
        byte[] fakePdf = new byte[]{10, 20, 30};

        try (MockedStatic<PdfInvoiceBuilder> mocked = mockStatic(PdfInvoiceBuilder.class)) {
            mocked.when(() -> PdfInvoiceBuilder.buildInvoicePdf(any(), any(), any(), any(), any(), any(), any(), any()))
                    .thenReturn(fakePdf);

            invoiceService.emailPdfInvoice("order123");

            verify(mailSender).send(any(MimeMessage.class));
        }
    }

    @Test
    public void testInvoiceSentDateIsSaved() throws Exception {
        when(orderRepo.findById("order123")).thenReturn(Optional.of(testOrder));
        when(userRepo.findById("user456")).thenReturn(Optional.of(testUser));
        when(prodRepo.findAllById(any())).thenReturn(List.of(testProduct));
        byte[] fakePdf = new byte[]{9, 9, 9};

        try (MockedStatic<PdfInvoiceBuilder> mocked = mockStatic(PdfInvoiceBuilder.class)) {
            mocked.when(() -> PdfInvoiceBuilder.buildInvoicePdf(any(), any(), any(), any(), any(), any(), any(), any()))
                    .thenReturn(fakePdf);

            invoiceService.emailPdfInvoice("order123");

            assertNotNull(testOrder.getInvoiceSentDate());
        }
    }
}
