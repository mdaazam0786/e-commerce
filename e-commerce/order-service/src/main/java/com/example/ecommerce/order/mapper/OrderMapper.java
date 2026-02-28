package com.example.ecommerce.order.mapper;

import com.example.ecommerce.order.dto.OrderDto;
import com.example.ecommerce.order.dto.OrderItemDto;
import com.example.ecommerce.order.model.Order;
import com.example.ecommerce.order.model.OrderItem;

import java.util.stream.Collectors;

public class OrderMapper {
    
    public static OrderDto toDto(Order order) {
        if (order == null) {
            return null;
        }
        
        OrderDto dto = new OrderDto();
        dto.setId(order.getId());
        dto.setUserEmail(order.getUserEmail());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setStatus(order.getStatus() != null ? order.getStatus().name() : null);
        dto.setShippingAddress(order.getShippingAddress());
        dto.setRazorpayOrderId(order.getRazorpayOrderId());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setUpdatedAt(order.getUpdatedAt());
        
        if (order.getItems() != null) {
            dto.setItems(order.getItems().stream()
                .map(OrderMapper::toItemDto)
                .collect(Collectors.toList()));
        }
        
        return dto;
    }
    
    public static OrderItemDto toItemDto(OrderItem item) {
        if (item == null) {
            return null;
        }
        
        OrderItemDto dto = new OrderItemDto();
        dto.setId(item.getId());
        dto.setProductId(item.getProductId());
        dto.setProductName(item.getProductName());
        dto.setPrice(item.getPrice());
        dto.setQuantity(item.getQuantity());
        
        return dto;
    }
    
    public static OrderItem toItemEntity(OrderItemDto dto, Order order) {
        if (dto == null) {
            return null;
        }
        
        OrderItem item = new OrderItem();
        item.setId(dto.getId());
        item.setOrder(order);
        item.setProductId(dto.getProductId());
        item.setProductName(dto.getProductName());
        item.setPrice(dto.getPrice());
        item.setQuantity(dto.getQuantity());
        
        return item;
    }
}
