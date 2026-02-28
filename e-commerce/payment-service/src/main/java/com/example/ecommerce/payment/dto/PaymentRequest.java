package com.example.ecommerce.payment.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class PaymentRequest {
    private Long orderId;
    private BigDecimal amount;
    private String paymentMethod;
    private String cardNumber;
    private String cvv;
    private String expiryDate;
}
