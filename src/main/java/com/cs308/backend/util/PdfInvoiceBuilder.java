package com.cs308.backend.util;

import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import com.cs308.backend.models.*;

public class PdfInvoiceBuilder {
    public static byte[] buildInvoicePdf(
            String orderId,
            User user,
            Order order,
            List<Product> products,
            List<Integer> quantities,
            String cardNumber,
            String expiryDate,
            String cvv
    ) throws IOException {
        try (PDDocument doc = new PDDocument();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            PDPage page = new PDPage(PDRectangle.LETTER);
            doc.addPage(page);

            // define your column X‑coordinates:
            float itemX  = 50;
            float qtyX   = 300;
            float unitX  = 370;
            float totalX = 460;

            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                float y = 700;

                // — Header —
                cs.setFont(PDType1Font.HELVETICA_BOLD, 14);
                cs.beginText();
                cs.newLineAtOffset(itemX, y);
                cs.showText("Invoice for Order #" + orderId);
                cs.endText();

                y -= 25;
                cs.setFont(PDType1Font.HELVETICA, 12);

                // Customer line
                cs.beginText();
                cs.newLineAtOffset(itemX, y);
                cs.showText("Customer: " + user.getName() + " " + user.getSurname());
                cs.endText();

                y -= 20;
                // Payment method block
                cs.beginText();
                cs.newLineAtOffset(itemX, y);
                cs.showText("Payment Method:");
                cs.endText();

                y -= 15;
                cs.beginText();
                cs.newLineAtOffset(itemX + 20, y);
                cs.showText("Card Number: " + cardNumber);
                cs.endText();

                y -= 15;
                cs.beginText();
                cs.newLineAtOffset(itemX + 20, y);
                cs.showText("Expiry: " + expiryDate + "   CVV: " + cvv);
                cs.endText();

                // — Table header —
                y -= 30;
                cs.setFont(PDType1Font.HELVETICA_BOLD, 12);
                cs.beginText();
                cs.newLineAtOffset(itemX, y);
                cs.showText("Item");
                cs.newLineAtOffset(qtyX - itemX, 0);
                cs.showText("Quantity");
                cs.newLineAtOffset(unitX - qtyX, 0);
                cs.showText("Unit Price");
                cs.newLineAtOffset(totalX - unitX, 0);
                cs.showText("Total Price");
                cs.endText();

                // — Line items —
                cs.setFont(PDType1Font.HELVETICA, 12);
                double grandTotal = 0;
                for (int i = 0; i < products.size(); i++) {
                    Product p = products.get(i);
                    int    q = quantities.get(i);
                    double lineTotal = p.getPrice() * q;
                    grandTotal += lineTotal;

                    y -= 15;
                    cs.beginText();
                    cs.newLineAtOffset(itemX, y);
                    cs.showText(p.getProductName());
                    cs.newLineAtOffset(qtyX - itemX, 0);
                    cs.showText(String.valueOf(q));
                    cs.newLineAtOffset(unitX - qtyX, 0);
                    cs.showText(String.format("%.2f", p.getPrice()));
                    cs.newLineAtOffset(totalX - unitX, 0);
                    cs.showText(String.format("%.2f", lineTotal));
                    cs.endText();
                }

                // — Grand total —
                y -= 25;
                cs.beginText();
                cs.newLineAtOffset(itemX, y);
                cs.showText("Grand total: $" + String.format("%.2f", grandTotal));
                cs.endText();
            }

            doc.save(baos);
            return baos.toByteArray();
        }
    }
}
