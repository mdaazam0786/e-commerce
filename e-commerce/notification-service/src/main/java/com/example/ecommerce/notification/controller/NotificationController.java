package com.example.ecommerce.notification.controller;

import com.example.ecommerce.common.dto.UIBean;
import com.example.ecommerce.notification.dto.EmailRequest;
import com.example.ecommerce.notification.dto.EmailResponse;
import com.example.ecommerce.notification.dto.OrderConfirmationRequest;
import com.example.ecommerce.notification.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);
    
    private final NotificationService notificationService;
    
    @Autowired
    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }
    
    @PostMapping("/email")
    public ResponseEntity<UIBean<EmailResponse>> sendEmail(@RequestBody EmailRequest request) {
        if (logger.isDebugEnabled()) {
            logger.debug("Sending email to: {}, subject: {}", request.getTo(), request.getSubject());
        }
        
        try {
            EmailResponse emailResponse = notificationService.sendEmail(request);
            UIBean<EmailResponse> response = UIBean.success(emailResponse, "Email sent successfully");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error sending email to: {}", request.getTo(), e);
            UIBean<EmailResponse> errorResponse = new UIBean<>();
            errorResponse.setSuccess(false);
            errorResponse.setMessage("Error occurred: " + e.getMessage());
            errorResponse.setResponse("ERROR");
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @PostMapping("/order-confirmation")
    public ResponseEntity<UIBean<EmailResponse>> sendOrderConfirmation(@RequestBody OrderConfirmationRequest request) {
        if (logger.isDebugEnabled()) {
            logger.debug("Sending order confirmation for orderId: {} to: {}", 
                request.getOrderId(), request.getEmail());
        }
        
        try {
            EmailResponse emailResponse = notificationService.sendOrderConfirmation(request);
            UIBean<EmailResponse> response = UIBean.success(emailResponse, "Order confirmation email sent successfully");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error sending order confirmation for orderId: {}", request.getOrderId(), e);
            UIBean<EmailResponse> errorResponse = new UIBean<>();
            errorResponse.setSuccess(false);
            errorResponse.setMessage("Error occurred: " + e.getMessage());
            errorResponse.setResponse("ERROR");
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
