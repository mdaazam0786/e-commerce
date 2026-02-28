package com.example.ecommerce.notification.service;

import com.example.ecommerce.common.exception.InvalidArgumentException;
import com.example.ecommerce.notification.dto.EmailRequest;
import com.example.ecommerce.notification.dto.EmailResponse;
import com.example.ecommerce.notification.dto.OrderConfirmationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class NotificationService {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    
    private final JavaMailSender mailSender;
    
    @Autowired
    public NotificationService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }
    
    public EmailResponse sendEmail(EmailRequest request) {
        if (logger.isDebugEnabled()) {
            logger.debug("Sending email to: {}, subject: {}", request.getTo(), request.getSubject());
        }
        
        validateEmailRequest(request);
        
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(request.getTo());
            message.setSubject(request.getSubject());
            message.setText(request.getBody());
            
            mailSender.send(message);
            
            logger.info("Email sent successfully to: {}", request.getTo());
            return new EmailResponse(true, "Email sent successfully", request.getTo());
            
        } catch (Exception e) {
            logger.error("Failed to send email to: {}, error: {}", request.getTo(), e.getMessage(), e);
            throw new RuntimeException("Failed to send email: " + e.getMessage(), e);
        }
    }
    
    public EmailResponse sendOrderConfirmation(OrderConfirmationRequest request) {
        if (logger.isDebugEnabled()) {
            logger.debug("Sending order confirmation email for orderId: {} to: {}", 
                request.getOrderId(), request.getEmail());
        }
        
        validateOrderConfirmationRequest(request);
        
        EmailRequest emailRequest = new EmailRequest();
        emailRequest.setTo(request.getEmail());
        emailRequest.setSubject("Order Confirmation - Order #" + request.getOrderId());
        
        StringBuilder body = new StringBuilder();
        body.append("Dear Customer,\n\n");
        body.append("Your order has been confirmed successfully.\n\n");
        body.append("Order ID: ").append(request.getOrderId()).append("\n");
        
        if (StringUtils.hasText(request.getOrderDetails())) {
            body.append("Order Details:\n").append(request.getOrderDetails()).append("\n");
        }
        
        body.append("\nThank you for shopping with us!\n");
        body.append("\nBest regards,\n");
        body.append("E-Commerce Platform Team");
        
        emailRequest.setBody(body.toString());
        
        return sendEmail(emailRequest);
    }
    
    private void validateEmailRequest(EmailRequest request) {
        if (request == null) {
            throw new InvalidArgumentException("Email request cannot be null");
        }
        
        if (!StringUtils.hasText(request.getTo())) {
            throw new InvalidArgumentException("Recipient email cannot be null or empty");
        }
        
        if (!isValidEmail(request.getTo())) {
            throw new InvalidArgumentException("Invalid recipient email format: " + request.getTo());
        }
        
        if (!StringUtils.hasText(request.getSubject())) {
            throw new InvalidArgumentException("Email subject cannot be null or empty");
        }
        
        if (!StringUtils.hasText(request.getBody())) {
            throw new InvalidArgumentException("Email body cannot be null or empty");
        }
    }
    
    private void validateOrderConfirmationRequest(OrderConfirmationRequest request) {
        if (request == null) {
            throw new InvalidArgumentException("Order confirmation request cannot be null");
        }
        
        if (!StringUtils.hasText(request.getEmail())) {
            throw new InvalidArgumentException("Email cannot be null or empty");
        }
        
        if (!isValidEmail(request.getEmail())) {
            throw new InvalidArgumentException("Invalid email format: " + request.getEmail());
        }
        
        if (request.getOrderId() == null || request.getOrderId() <= 0) {
            throw new InvalidArgumentException("Invalid order ID: " + request.getOrderId());
        }
    }
    
    private boolean isValidEmail(String email) {
        if (!StringUtils.hasText(email)) {
            return false;
        }
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(emailRegex);
    }
}
