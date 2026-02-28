package com.example.ecommerce.product.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateProductRequest {
    private String name;
    private String description;
    private BigDecimal price;
    private String category;
    private String imageUrl;
    private Integer stock;
}
