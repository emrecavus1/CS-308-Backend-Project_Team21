package com.cs308.backend.services;

import com.cs308.backend.models.Order;
import com.cs308.backend.models.User;
import com.cs308.backend.models.Product;
import com.cs308.backend.repositories.OrderRepository;
import com.cs308.backend.repositories.UserRepository;
import com.cs308.backend.repositories.ProductRepository;
import com.cs308.backend.util.PdfInvoiceBuilder;

import org.springframework.stereotype.Service;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.core.io.ByteArrayResource;

import jakarta.mail.internet.MimeMessage;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class InvoiceService {
    private final JavaMailSender mailSender;
    private final OrderRepository orderRepo;
    private final UserRepository  userRepo;
    private final ProductRepository prodRepo;

    public InvoiceService(JavaMailSender mailSender,
                          OrderRepository orderRepo,
                          UserRepository userRepo,
                          ProductRepository prodRepo) {
        this.mailSender = mailSender;
        this.orderRepo  = orderRepo;
        this.userRepo   = userRepo;
        this.prodRepo   = prodRepo;
    }

    public void emailPdfInvoice(String orderId) throws Exception {
        // 1) load order + user
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
        User user = userRepo.findById(order.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + order.getUserId()));

        // 2) load products + quantities
        List<String> ids       = order.getProductIds();
        List<Integer> qtys     = order.getQuantities();
        List<Product> products = prodRepo.findAllById(ids);

        // 3) build the PDF, now including card/payment details
        byte[] pdfBytes = PdfInvoiceBuilder.buildInvoicePdf(
                orderId,
                user,
                order,
                products,
                qtys,
                order.getCardNumber(),   // your new getter on Order
                order.getExpiryDate(),   // your new getter on Order
                order.getCvv()           // your new getter on Order
        );

        // 4) prepare the mail
        MimeMessage msg = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(msg, true);
        helper.setTo(user.getEmail());
        helper.setSubject("Your Invoice for Order #" + orderId);
        helper.setText(
                "Hello " + user.getName() + ",\n\n" +
                        "Thank you for your purchase!  Please find your invoice attached as a PDF.\n\n" +
                        "Best regards,\nThe Online Shop Team"
        );

        // 5) attach and send
        helper.addAttachment(
                "invoice-" + orderId + ".pdf",
                new ByteArrayResource(pdfBytes),
                "application/pdf"
        );
        mailSender.send(msg);

        // 6) Record the invoice sent date and time
        order.setInvoiceSentDate(LocalDateTime.now());
        orderRepo.save(order);
    }

}