package com.example.ecommerce.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RazorpayWebhookRequest {
    private String event;
    private Map<String, Object> payload;
    private Long createdAt;
}
