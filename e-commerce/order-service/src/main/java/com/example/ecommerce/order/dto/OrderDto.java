package com.example.ecommerce.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDto {
    private Long id;
    private String userEmail;
    private List<OrderItemDto> items = new ArrayList<>();
    private BigDecimal totalAmount;
    private String status;
    private String shippingAddress;
    private String razorpayOrderId;
    private Date createdAt;
    private Date updatedAt;
}
