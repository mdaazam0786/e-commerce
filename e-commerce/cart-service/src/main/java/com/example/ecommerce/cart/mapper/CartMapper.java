package com.example.ecommerce.cart.mapper;

import com.example.ecommerce.cart.dto.CartDto;
import com.example.ecommerce.cart.dto.CartItemDto;
import com.example.ecommerce.cart.model.Cart;
import com.example.ecommerce.cart.model.CartItem;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class CartMapper {
    
    public CartDto toDto(Cart cart) {
        if (cart == null) {
            return null;
        }
        
        CartDto dto = new CartDto();
        dto.setId(cart.getId());
        dto.setUserEmail(cart.getUserEmail());
        dto.setItems(cart.getItems().stream()
            .map(this::toItemDto)
            .collect(Collectors.toList()));
        dto.setTotal(cart.getTotal());
        dto.setCreatedAt(cart.getCreatedAt());
        dto.setUpdatedAt(cart.getUpdatedAt());
        
        return dto;
    }
    
    public CartItemDto toItemDto(CartItem item) {
        if (item == null) {
            return null;
        }
        
        CartItemDto dto = new CartItemDto();
        dto.setProductId(item.getProductId());
        dto.setProductName(item.getProductName());
        dto.setPrice(item.getPrice());
        dto.setQuantity(item.getQuantity());
        dto.setSubtotal(item.getSubtotal());
        
        return dto;
    }
    
    public CartItem toEntity(CartItemDto dto) {
        if (dto == null) {
            return null;
        }
        
        CartItem item = new CartItem();
        item.setProductId(dto.getProductId());
        item.setProductName(dto.getProductName());
        item.setPrice(dto.getPrice());
        item.setQuantity(dto.getQuantity());
        
        return item;
    }
}
