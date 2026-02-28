package com.example.ecommerce.product.mapper;

import com.example.ecommerce.product.dto.CreateProductRequest;
import com.example.ecommerce.product.dto.ProductDto;
import com.example.ecommerce.product.dto.UpdateProductRequest;
import com.example.ecommerce.product.model.Product;

public class ProductMapper {
    
    public static ProductDto toDto(Product product) {
        if (product == null) {
            return null;
        }
        
        ProductDto dto = new ProductDto();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setCategory(product.getCategory());
        dto.setImageUrl(product.getImageUrl());
        dto.setStock(product.getStock());
        dto.setCreatedAt(product.getCreatedAt());
        dto.setUpdatedAt(product.getUpdatedAt());
        
        return dto;
    }
    
    public static Product toEntity(CreateProductRequest request) {
        if (request == null) {
            return null;
        }
        
        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setCategory(request.getCategory());
        product.setImageUrl(request.getImageUrl());
        product.setStock(request.getStock() != null ? request.getStock() : 0);
        
        return product;
    }
    
    public static void updateEntity(Product product, UpdateProductRequest request) {
        if (product == null || request == null) {
            return;
        }
        
        if (request.getName() != null) {
            product.setName(request.getName());
        }
        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }
        if (request.getPrice() != null) {
            product.setPrice(request.getPrice());
        }
        if (request.getCategory() != null) {
            product.setCategory(request.getCategory());
        }
        if (request.getImageUrl() != null) {
            product.setImageUrl(request.getImageUrl());
        }
        if (request.getStock() != null) {
            product.setStock(request.getStock());
        }
    }
}
