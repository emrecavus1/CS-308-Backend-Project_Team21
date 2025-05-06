package com.cs308.backend.mailing;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;

@Service
public class DefaultEmailService implements EmailService {


    @Autowired
    private final JavaMailSender emailSender;

    private final TemplateEngine templateEngine;

    @Autowired
    public DefaultEmailService(JavaMailSender emailSender, TemplateEngine templateEngine) {
        this.emailSender = emailSender;
        this.templateEngine = templateEngine;
    }

    @Override
    public void sendMail(final AbstractEmailContext email) throws MessagingException {
        // Create a MIME message
        MimeMessage message = emailSender.createMimeMessage();

        // Create a helper for the MIME message (handles encoding and multipart support)
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(
                message,
                MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                StandardCharsets.UTF_8.name()
        );

        // Prepare the Thymeleaf context with the email's context variables
        Context thymeleafContext = new Context();
        thymeleafContext.setVariables(email.getContext());

        // Process the email template to generate the email content
        String emailContent = templateEngine.process(email.getTemplateLocation(), thymeleafContext);

        // Set the email details from the email context
        mimeMessageHelper.setTo(email.getTo());
        mimeMessageHelper.setSubject(email.getSubject());
        mimeMessageHelper.setFrom(email.getFrom());
        mimeMessageHelper.setText(emailContent, true); // 'true' indicates HTML content

        System.out.println("🔔 Sending email to: " + email.getTo());
        System.out.println("Subject: " + email.getSubject());
        System.out.println("From: " + email.getFrom());
        System.out.println("Context: " + email.getContext());


        // Send the email
        emailSender.send(message);
    }
}

