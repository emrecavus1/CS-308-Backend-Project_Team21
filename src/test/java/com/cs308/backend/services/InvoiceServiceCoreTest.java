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
import org.springframework.mail.javamail.JavaMailSender;

import jakarta.mail.internet.MimeMessage;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class InvoiceServiceCoreTest {

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

        // Basic test data
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
    public void testEmailPdfInvoice_Success() throws Exception {
        // Arrange
        when(orderRepo.findById("order123")).thenReturn(Optional.of(testOrder));
        when(userRepo.findById("user456")).thenReturn(Optional.of(testUser));
        when(prodRepo.findAllById(List.of("p1"))).thenReturn(List.of(testProduct));
        byte[] fakePdf = new byte[]{1, 2, 3};

        try (MockedStatic<PdfInvoiceBuilder> mocked = mockStatic(PdfInvoiceBuilder.class)) {
            mocked.when(() -> PdfInvoiceBuilder.buildInvoicePdf(
                    eq("order123"),
                    eq(testUser),
                    eq(testOrder),
                    eq(List.of(testProduct)),
                    eq(List.of(1)),
                    any(), any(), any()
            )).thenReturn(fakePdf);

            // Act
            invoiceService.emailPdfInvoice("order123");

            // Assert
            verify(mailSender).send(any(MimeMessage.class));
            verify(orderRepo).save(testOrder);
            assertNotNull(testOrder.getInvoiceSentDate());
        }
    }

    @Test
    public void testEmailPdfInvoice_OrderNotFound() {
        when(orderRepo.findById("missing")).thenReturn(Optional.empty());
        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            invoiceService.emailPdfInvoice("missing");
        });
        assertTrue(ex.getMessage().contains("Order not found"));
    }

    @Test
    public void testEmailPdfInvoice_UserNotFound() {
        when(orderRepo.findById("order123")).thenReturn(Optional.of(testOrder));
        when(userRepo.findById("user456")).thenReturn(Optional.empty());
        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            invoiceService.emailPdfInvoice("order123");
        });
        assertTrue(ex.getMessage().contains("User not found"));
    }

    @Test
    public void testEmailPdfInvoice_PdfBuildFails() {
        when(orderRepo.findById("order123")).thenReturn(Optional.of(testOrder));
        when(userRepo.findById("user456")).thenReturn(Optional.of(testUser));
        when(prodRepo.findAllById(any())).thenReturn(List.of(testProduct));

        try (MockedStatic<PdfInvoiceBuilder> mocked = mockStatic(PdfInvoiceBuilder.class)) {
            mocked.when(() -> PdfInvoiceBuilder.buildInvoicePdf(any(), any(), any(), any(), any(), any(), any(), any()))
                    .thenThrow(new RuntimeException("PDF build failed"));

            Exception ex = assertThrows(RuntimeException.class, () -> {
                invoiceService.emailPdfInvoice("order123");
            });
            assertTrue(ex.getMessage().contains("PDF build failed"));
        }
    }

    @Test
    public void testEmailPdfInvoice_EmailStructure() throws Exception {
        // Just ensure email sending logic executes properly
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
}
