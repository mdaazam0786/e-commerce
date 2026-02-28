package com.example.ecommerce.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateRazorpayOrderRequest {
    private Long orderId;
    private BigDecimal amount;
    private String currency;
    private String receipt;
}
