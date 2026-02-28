package com.example.ecommerce.payment.controller;

import com.example.ecommerce.common.dto.UIBean;
import com.example.ecommerce.payment.dto.CreateRazorpayOrderRequest;
import com.example.ecommerce.payment.dto.PaymentRequest;
import com.example.ecommerce.payment.dto.PaymentResponse;
import com.example.ecommerce.payment.dto.VerifyPaymentRequest;
import com.example.ecommerce.payment.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    
    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);
    
    private final PaymentService paymentService;
    
    @Autowired
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }
    
    @PostMapping("/razorpay/create-order")
    public ResponseEntity<UIBean<PaymentResponse>> createRazorpayOrder(@RequestBody CreateRazorpayOrderRequest request) {
        if (logger.isDebugEnabled()) {
            logger.debug("Creating Razorpay order for orderId: {}, amount: {}", 
                request.getOrderId(), request.getAmount());
        }
        
        try {
            PaymentResponse paymentResponse = paymentService.createRazorpayOrder(request);
            UIBean<PaymentResponse> response = UIBean.success(paymentResponse, "Razorpay order created successfully");
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("Error creating Razorpay order for orderId: {}", request.getOrderId(), e);
            UIBean<PaymentResponse> errorResponse = new UIBean<>();
            errorResponse.setSuccess(false);
            errorResponse.setMessage("Error occurred: " + e.getMessage());
            errorResponse.setResponse("ERROR");
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @PostMapping("/razorpay/verify")
    public ResponseEntity<UIBean<PaymentResponse>> verifyPayment(@RequestBody VerifyPaymentRequest request) {
        if (logger.isDebugEnabled()) {
            logger.debug("Verifying payment for razorpayOrderId: {}, razorpayPaymentId: {}", 
                request.getRazorpayOrderId(), request.getRazorpayPaymentId());
        }
        
        try {
            PaymentResponse paymentResponse = paymentService.verifyPayment(request);
            UIBean<PaymentResponse> response = UIBean.success(paymentResponse, "Payment verification completed");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error verifying payment for razorpayPaymentId: {}", request.getRazorpayPaymentId(), e);
            UIBean<PaymentResponse> errorResponse = new UIBean<>();
            errorResponse.setSuccess(false);
            errorResponse.setMessage("Error occurred: " + e.getMessage());
            errorResponse.setResponse("ERROR");
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @PostMapping("/process")
    public ResponseEntity<UIBean<PaymentResponse>> processPayment(@RequestBody PaymentRequest request) {
        if (logger.isDebugEnabled()) {
            logger.debug("Processing payment for orderId: {}, amount: {}", 
                request.getOrderId(), request.getAmount());
        }
        
        try {
            PaymentResponse paymentResponse = paymentService.processPayment(request);
            UIBean<PaymentResponse> response = UIBean.success(paymentResponse, "Payment processed");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error processing payment for orderId: {}", request.getOrderId(), e);
            UIBean<PaymentResponse> errorResponse = new UIBean<>();
            errorResponse.setSuccess(false);
            errorResponse.setMessage("Error occurred: " + e.getMessage());
            errorResponse.setResponse("ERROR");
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @GetMapping("/{transactionId}")
    public ResponseEntity<UIBean<PaymentResponse>> getPaymentStatus(@PathVariable String transactionId) {
        if (logger.isDebugEnabled()) {
            logger.debug("Fetching payment status for transactionId: {}", transactionId);
        }
        
        try {
            PaymentResponse paymentResponse = paymentService.getPaymentStatus(transactionId);
            UIBean<PaymentResponse> response = UIBean.success(paymentResponse, "Payment status retrieved successfully");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error fetching payment status for transactionId: {}", transactionId, e);
            UIBean<PaymentResponse> errorResponse = new UIBean<>();
            errorResponse.setSuccess(false);
            errorResponse.setMessage("Error occurred: " + e.getMessage());
            errorResponse.setResponse("ERROR");
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(
            @RequestHeader("X-Razorpay-Signature") String signature,
            @RequestBody String payload) {
        
        if (logger.isDebugEnabled()) {
            logger.debug("Received Razorpay webhook");
        }
        
        try {
            paymentService.handleWebhook(signature, payload);
            return ResponseEntity.ok("Webhook processed successfully");
        } catch (Exception e) {
            logger.error("Error processing webhook", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Webhook processing failed");
        }
    }
}
