package com.example.ecommerce.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderConfirmationRequest {
    private String email;
    private Long orderId;
    private String orderDetails;
}
