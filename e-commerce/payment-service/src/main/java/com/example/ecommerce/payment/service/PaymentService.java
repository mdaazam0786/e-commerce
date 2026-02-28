package com.example.ecommerce.payment.service;

import com.example.ecommerce.common.exception.InvalidArgumentException;
import com.example.ecommerce.payment.dto.CreateRazorpayOrderRequest;
import com.example.ecommerce.payment.dto.PaymentRequest;
import com.example.ecommerce.payment.dto.PaymentResponse;
import com.example.ecommerce.payment.dto.VerifyPaymentRequest;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

@Service
public class PaymentService {
    
    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);
    
    private final RazorpayClient razorpayClient;
    
    private final RestTemplate restTemplate;
    
    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;
    
    @Value("${razorpay.webhook.secret:}")
    private String webhookSecret;
    
    @Value("${services.order-service.url:http://order-service}")
    private String orderServiceUrl;
    
    @Value("${services.notification-service.url:http://notification-service}")
    private String notificationServiceUrl;
    
    @Autowired
    public PaymentService(RazorpayClient razorpayClient, RestTemplate restTemplate) {
        this.razorpayClient = razorpayClient;
        this.restTemplate = restTemplate;
    }
    
    public PaymentResponse createRazorpayOrder(CreateRazorpayOrderRequest request) {
        if (logger.isDebugEnabled()) {
            logger.debug("Creating Razorpay order for orderId: {}, amount: {}", 
                request.getOrderId(), request.getAmount());
        }
        
        validateCreateRazorpayOrderRequest(request);
        
        try {
            long amountInPaise = request.getAmount().multiply(BigDecimal.valueOf(100)).longValue();
            
            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", amountInPaise);
            orderRequest.put("currency", request.getCurrency() != null ? request.getCurrency() : "INR");
            orderRequest.put("receipt", request.getReceipt() != null ? request.getReceipt() : "order_" + request.getOrderId());
            
            JSONObject notes = new JSONObject();
            notes.put("order_id", request.getOrderId());
            notes.put("source", "ecommerce-platform");
            orderRequest.put("notes", notes);
            
            Order razorpayOrder = razorpayClient.orders.create(orderRequest);
            
            logger.info("Razorpay order created successfully. OrderId: {}, RazorpayOrderId: {}", 
                request.getOrderId(), razorpayOrder.get("id"));
            
            PaymentResponse response = new PaymentResponse();
            response.setRazorpayOrderId(razorpayOrder.get("id"));
            response.setStatus(razorpayOrder.get("status"));
            response.setAmount(razorpayOrder.get("amount"));
            response.setCurrency(razorpayOrder.get("currency"));
            response.setMessage("Razorpay order created successfully");
            
            return response;
            
        } catch (RazorpayException e) {
            logger.error("Error creating Razorpay order for orderId: {}", request.getOrderId(), e);
            throw new RuntimeException("Failed to create Razorpay order: " + e.getMessage(), e);
        }
    }
    
    public PaymentResponse verifyPayment(VerifyPaymentRequest request) {
        if (logger.isDebugEnabled()) {
            logger.debug("Verifying payment for razorpayOrderId: {}, razorpayPaymentId: {}", 
                request.getRazorpayOrderId(), request.getRazorpayPaymentId());
        }
        
        validateVerifyPaymentRequest(request);
        
        try {
            JSONObject options = new JSONObject();
            options.put("razorpay_order_id", request.getRazorpayOrderId());
            options.put("razorpay_payment_id", request.getRazorpayPaymentId());
            options.put("razorpay_signature", request.getRazorpaySignature());
            
            boolean isValidSignature = Utils.verifyPaymentSignature(options, razorpayKeySecret);
            
            if (isValidSignature) {
                logger.info("Payment verified successfully. PaymentId: {}, OrderId: {}", 
                    request.getRazorpayPaymentId(), request.getRazorpayOrderId());
                
                PaymentResponse response = new PaymentResponse();
                response.setTransactionId(request.getRazorpayPaymentId());
                response.setRazorpayOrderId(request.getRazorpayOrderId());
                response.setStatus("SUCCESS");
                response.setMessage("Payment verified successfully");
                
                return response;
            } else {
                logger.warn("Payment verification failed. Invalid signature for PaymentId: {}", 
                    request.getRazorpayPaymentId());
                
                PaymentResponse response = new PaymentResponse();
                response.setTransactionId(request.getRazorpayPaymentId());
                response.setRazorpayOrderId(request.getRazorpayOrderId());
                response.setStatus("FAILED");
                response.setMessage("Payment verification failed - Invalid signature");
                
                return response;
            }
            
        } catch (RazorpayException e) {
            logger.error("Error verifying payment for razorpayPaymentId: {}", request.getRazorpayPaymentId(), e);
            throw new RuntimeException("Failed to verify payment: " + e.getMessage(), e);
        }
    }
    
    public PaymentResponse processPayment(PaymentRequest request) {
        if (logger.isDebugEnabled()) {
            logger.debug("Processing payment for orderId: {}, amount: {}, method: {}", 
                request.getOrderId(), request.getAmount(), request.getPaymentMethod());
        }
        
        validatePaymentRequest(request);
        
        logger.info("Payment processing initiated for orderId: {}", request.getOrderId());
        
        PaymentResponse response = new PaymentResponse();
        response.setStatus("PENDING");
        response.setMessage("Payment initiated. Please complete payment via Razorpay checkout");
        
        return response;
    }
    
    public PaymentResponse getPaymentStatus(String transactionId) {
        if (logger.isDebugEnabled()) {
            logger.debug("Fetching payment status for transactionId: {}", transactionId);
        }
        
        if (!StringUtils.hasText(transactionId)) {
            throw new InvalidArgumentException("Transaction ID cannot be null or empty");
        }
        
        try {
            com.razorpay.Payment payment = razorpayClient.payments.fetch(transactionId);
            
            PaymentResponse response = new PaymentResponse();
            response.setTransactionId(payment.get("id"));
            response.setRazorpayOrderId(payment.get("order_id"));
            response.setStatus(payment.get("status"));
            response.setAmount(payment.get("amount"));
            response.setCurrency(payment.get("currency"));
            response.setMessage("Payment status retrieved successfully");
            
            return response;
            
        } catch (RazorpayException e) {
            logger.error("Error fetching payment status for transactionId: {}", transactionId, e);
            throw new RuntimeException("Failed to fetch payment status: " + e.getMessage(), e);
        }
    }
    
    public void handleWebhook(String webhookSignature, String webhookBody) {
        if (logger.isDebugEnabled()) {
            logger.debug("Processing Razorpay webhook");
        }
        
        if (!verifyWebhookSignature(webhookSignature, webhookBody)) {
            logger.error("Invalid webhook signature received");
            throw new InvalidArgumentException("Invalid webhook signature");
        }
        
        try {
            JSONObject webhookData = new JSONObject(webhookBody);
            String event = webhookData.getString("event");
            JSONObject payload = webhookData.getJSONObject("payload");
            JSONObject paymentEntity = payload.getJSONObject("payment").getJSONObject("entity");
            
            String paymentId = paymentEntity.getString("id");
            String orderId = paymentEntity.getString("order_id");
            String status = paymentEntity.getString("status");
            
            logger.info("Webhook received - Event: {}, PaymentId: {}, OrderId: {}, Status: {}", 
                event, paymentId, orderId, status);
            
            switch (event) {
                case "payment.authorized":
                    handlePaymentAuthorized(paymentId, orderId, paymentEntity);
                    break;
                    
                case "payment.captured":
                    handlePaymentCaptured(paymentId, orderId, paymentEntity);
                    break;
                    
                case "payment.failed":
                    handlePaymentFailed(paymentId, orderId, paymentEntity);
                    break;
                    
                default:
                    logger.info("Unhandled webhook event: {}", event);
            }
            
        } catch (Exception e) {
            logger.error("Error processing webhook", e);
            throw new RuntimeException("Failed to process webhook: " + e.getMessage(), e);
        }
    }
    
    private Long extractOrderIdFromWebhook(JSONObject paymentEntity) {
        JSONObject notes = paymentEntity.optJSONObject("notes");
        if (notes != null) {
            long orderId = notes.optLong("order_id", 0);
            if (orderId > 0) {
                logger.debug("Extracted order ID from notes: {}", orderId);
                return orderId;
            }
        }
        
        String razorpayOrderId = paymentEntity.optString("order_id");
        return extractOrderId(razorpayOrderId);
    }
    
    private boolean verifyWebhookSignature(String webhookSignature, String webhookBody) {
        if (!StringUtils.hasText(webhookSecret)) {
            logger.warn("Webhook secret not configured, skipping signature verification");
            return true;
        }
        
        try {
            String expectedSignature = Utils.getHash(webhookBody, webhookSecret);
            return expectedSignature.equals(webhookSignature);
        } catch (RazorpayException e) {
            logger.error("Error verifying webhook signature", e);
            return false;
        }
    }
    
    private void handlePaymentAuthorized(String paymentId, String orderId, JSONObject paymentEntity) {
        logger.info("Payment authorized - PaymentId: {}, OrderId: {}", paymentId, orderId);
        
        try {
            updateOrderStatus(orderId, "CONFIRMED");
            
            logger.info("Payment authorized and order updated for OrderId: {}", orderId);
            
        } catch (Exception e) {
            logger.error("Error handling payment authorized for OrderId: {}", orderId, e);
        }
    }
    
    private void handlePaymentCaptured(String paymentId, String orderId, JSONObject paymentEntity) {
        logger.info("Payment captured - PaymentId: {}, OrderId: {}", paymentId, orderId);
        
        try {
            String email = paymentEntity.optString("email", "");
            long amountInPaise = paymentEntity.optLong("amount", 0);
            
            updateOrderStatus(orderId, "CONFIRMED");
            
            if (StringUtils.hasText(email)) {
                sendOrderConfirmationEmail(orderId, email);
            }
            
            logger.info("Payment captured, order updated and email sent for OrderId: {}", orderId);
            
        } catch (Exception e) {
            logger.error("Error handling payment captured for OrderId: {}", orderId, e);
        }
    }
    
    private void handlePaymentFailed(String paymentId, String orderId, JSONObject paymentEntity) {
        String errorCode = paymentEntity.optString("error_code", "UNKNOWN");
        String errorDescription = paymentEntity.optString("error_description", "Payment failed");
        String email = paymentEntity.optString("email", "");
        
        logger.warn("Payment failed - PaymentId: {}, OrderId: {}, Error: {} - {}", 
            paymentId, orderId, errorCode, errorDescription);
        
        try {
            if (StringUtils.hasText(email)) {
                sendPaymentFailureEmail(orderId, email, errorDescription);
            }
            
            logger.info("Payment failure handled for OrderId: {}", orderId);
            
        } catch (Exception e) {
            logger.error("Error handling payment failed for OrderId: {}", orderId, e);
        }
    }
    
    private void updateOrderStatus(String razorpayOrderId, String status) {
        try {
            String url = orderServiceUrl + "/api/orders/" + extractOrderId(razorpayOrderId) + "/status";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            JSONObject requestBody = new JSONObject();
            requestBody.put("status", status);
            
            HttpEntity<String> request = new HttpEntity<>(requestBody.toString(), headers);
            
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                logger.info("Order status updated successfully for OrderId: {}", razorpayOrderId);
            } else {
                logger.warn("Failed to update order status. Response: {}", response.getStatusCode());
            }
            
        } catch (Exception e) {
            logger.error("Error updating order status for OrderId: {}", razorpayOrderId, e);
        }
    }
    
    private void sendOrderConfirmationEmail(String razorpayOrderId, String email) {
        try {
            String url = notificationServiceUrl + "/api/notifications/order-confirmation";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            JSONObject requestBody = new JSONObject();
            requestBody.put("email", email);
            requestBody.put("orderId", extractOrderId(razorpayOrderId));
            requestBody.put("orderDetails", "Your payment has been confirmed successfully.");
            
            HttpEntity<String> request = new HttpEntity<>(requestBody.toString(), headers);
            
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                logger.info("Order confirmation email sent to: {}", email);
            } else {
                logger.warn("Failed to send confirmation email. Response: {}", response.getStatusCode());
            }
            
        } catch (Exception e) {
            logger.error("Error sending confirmation email to: {}", email, e);
        }
    }
    
    private void sendPaymentFailureEmail(String razorpayOrderId, String email, String errorDescription) {
        try {
            String url = notificationServiceUrl + "/api/notifications/email";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            JSONObject requestBody = new JSONObject();
            requestBody.put("to", email);
            requestBody.put("subject", "Payment Failed - Order #" + extractOrderId(razorpayOrderId));
            requestBody.put("body", "Your payment attempt failed. Reason: " + errorDescription + 
                "\n\nPlease try again or contact support if the issue persists.");
            
            HttpEntity<String> request = new HttpEntity<>(requestBody.toString(), headers);
            
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                logger.info("Payment failure email sent to: {}", email);
            } else {
                logger.warn("Failed to send failure email. Response: {}", response.getStatusCode());
            }
            
        } catch (Exception e) {
            logger.error("Error sending failure email to: {}", email, e);
        }
    }
    
    private Long extractOrderId(String razorpayOrderId) {
        if (logger.isDebugEnabled()) {
            logger.debug("Extracting order ID from Razorpay order ID: {}", razorpayOrderId);
        }
        
        if (!StringUtils.hasText(razorpayOrderId)) {
            logger.error("Razorpay order ID is null or empty");
            return null;
        }
        
        try {
            Order razorpayOrder = razorpayClient.orders.fetch(razorpayOrderId);
            String receipt = razorpayOrder.get("receipt");
            
            if (!StringUtils.hasText(receipt)) {
                logger.error("Receipt is null or empty for Razorpay order ID: {}", razorpayOrderId);
                return fallbackToOrderServiceQuery(razorpayOrderId);
            }
            
            if (!receipt.startsWith("order_")) {
                logger.warn("Receipt format unexpected: {}. Expected format: order_<id>", receipt);
                return parseOrderIdFromReceipt(receipt);
            }
            
            String orderIdStr = receipt.substring(6); // Remove "order_" prefix
            
            if (orderIdStr.isEmpty()) {
                logger.error("Order ID is empty after removing prefix from receipt: {}", receipt);
                return fallbackToOrderServiceQuery(razorpayOrderId);
            }
            
            Long orderId = Long.parseLong(orderIdStr);
            
            if (orderId <= 0) {
                logger.error("Invalid order ID extracted: {}. Must be positive.", orderId);
                return null;
            }
            
            logger.info("Successfully extracted order ID: {} from receipt: {}", orderId, receipt);
            return orderId;
            
        } catch (NumberFormatException e) {
            logger.error("Failed to parse order ID from receipt. Invalid number format for Razorpay order: {}", 
                razorpayOrderId, e);
            return fallbackToOrderServiceQuery(razorpayOrderId);
            
        } catch (RazorpayException e) {
            logger.error("Failed to fetch Razorpay order details for order ID: {}", razorpayOrderId, e);
            return fallbackToOrderServiceQuery(razorpayOrderId);
            
        } catch (Exception e) {
            logger.error("Unexpected error extracting order ID from Razorpay order: {}", razorpayOrderId, e);
            return null;
        }
    }
    
    private Long parseOrderIdFromReceipt(String receipt) {
        try {
            Long orderId = Long.parseLong(receipt);
            if (orderId > 0) {
                logger.info("Parsed order ID directly from receipt: {}", orderId);
                return orderId;
            }
        } catch (NumberFormatException e) {
            logger.warn("Could not parse receipt as order ID: {}", receipt);
        }
        return null;
    }
    
    private Long fallbackToOrderServiceQuery(String razorpayOrderId) {
        logger.info("Attempting fallback: Querying Order Service for Razorpay order ID: {}", razorpayOrderId);
        
        try {
            String url = orderServiceUrl + "/api/orders/by-razorpay-order/" + razorpayOrderId;
            
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JSONObject responseBody = new JSONObject(response.getBody());

                if (responseBody.optBoolean("success", false)) {
                    JSONObject data = responseBody.optJSONObject("data");
                    if (data != null) {
                        Long orderId = data.optLong("id", 0);
                        if (orderId > 0) {
                            logger.info("Successfully retrieved order ID from Order Service: {}", orderId);
                            return orderId;
                        }
                    }
                }
            }
            
            logger.warn("Order Service query returned no valid order ID for Razorpay order: {}", razorpayOrderId);
            return null;
            
        } catch (Exception e) {
            logger.error("Fallback failed: Could not query Order Service for Razorpay order: {}", 
                razorpayOrderId, e);
            return null;
        }
    }

    private void validateCreateRazorpayOrderRequest(CreateRazorpayOrderRequest request) {
        if (request == null) {
            throw new InvalidArgumentException("Razorpay order request cannot be null");
        }
        
        if (request.getOrderId() == null || request.getOrderId() <= 0) {
            throw new InvalidArgumentException("Invalid order ID: " + request.getOrderId());
        }
        
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidArgumentException("Order amount must be greater than zero");
        }
    }
    
    private void validateVerifyPaymentRequest(VerifyPaymentRequest request) {
        if (request == null) {
            throw new InvalidArgumentException("Verify payment request cannot be null");
        }
        
        if (!StringUtils.hasText(request.getRazorpayOrderId())) {
            throw new InvalidArgumentException("Razorpay order ID cannot be null or empty");
        }
        
        if (!StringUtils.hasText(request.getRazorpayPaymentId())) {
            throw new InvalidArgumentException("Razorpay payment ID cannot be null or empty");
        }
        
        if (!StringUtils.hasText(request.getRazorpaySignature())) {
            throw new InvalidArgumentException("Razorpay signature cannot be null or empty");
        }
    }
    
    private void validatePaymentRequest(PaymentRequest request) {
        if (request == null) {
            throw new InvalidArgumentException("Payment request cannot be null");
        }
        
        if (request.getOrderId() == null || request.getOrderId() <= 0) {
            throw new InvalidArgumentException("Invalid order ID: " + request.getOrderId());
        }
        
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidArgumentException("Payment amount must be greater than zero");
        }
        
        if (!StringUtils.hasText(request.getPaymentMethod())) {
            throw new InvalidArgumentException("Payment method cannot be null or empty");
        }
        
        if ("CARD".equalsIgnoreCase(request.getPaymentMethod())) {
            validateCardDetails(request);
        }
    }
    
    private void validateCardDetails(PaymentRequest request) {
        if (!StringUtils.hasText(request.getCardNumber())) {
            throw new InvalidArgumentException("Card number cannot be null or empty");
        }
        
        String cardNumber = request.getCardNumber().replaceAll("\\s+", "");
        if (!cardNumber.matches("\\d{13,19}")) {
            throw new InvalidArgumentException("Invalid card number format");
        }
        
        if (!StringUtils.hasText(request.getCvv())) {
            throw new InvalidArgumentException("CVV cannot be null or empty");
        }
        
        if (!request.getCvv().matches("\\d{3,4}")) {
            throw new InvalidArgumentException("Invalid CVV format");
        }
        
        if (!StringUtils.hasText(request.getExpiryDate())) {
            throw new InvalidArgumentException("Expiry date cannot be null or empty");
        }
        
        // Basic expiry date validation (MM/YY format)
        if (!request.getExpiryDate().matches("(0[1-9]|1[0-2])/\\d{2}")) {
            throw new InvalidArgumentException("Invalid expiry date format. Expected MM/YY");
        }
    }
}
