package com.example.ecommerce.cart.dto;

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
public class CartDto {
    private String id;
    private String userEmail;
    private List<CartItemDto> items = new ArrayList<>();
    private BigDecimal total;
    private Date createdAt;
    private Date updatedAt;
}
