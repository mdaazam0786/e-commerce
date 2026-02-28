package com.example.ecommerce.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private String transactionId;
    private String razorpayOrderId;
    private String status;
    private String message;
    private Long amount;
    private String currency;
    
    public PaymentResponse(String transactionId, String status, String message) {
        this.transactionId = transactionId;
        this.status = status;
        this.message = message;
    }
}
