package com.example.ecommerce.user.mapper;

import com.example.ecommerce.user.dto.UserDto;
import com.example.ecommerce.user.model.User;
import com.example.ecommerce.user.model.UserDynamoDB;

import java.util.Date;

public class UserMapper {
    
    public static UserDto toDto(User user) {
        if (user == null) {
            return null;
        }
        
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setPhone(user.getPhone());
        dto.setRole(user.getRole() != null ? user.getRole().name() : null);
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        
        return dto;
    }
    
    public static UserDynamoDB toDynamoDB(User user) {
        if (user == null) {
            return null;
        }
        
        UserDynamoDB dynamoUser = new UserDynamoDB();
        dynamoUser.setEmail(user.getEmail());
        dynamoUser.setUserId(user.getId());
        dynamoUser.setFirstName(user.getFirstName());
        dynamoUser.setLastName(user.getLastName());
        dynamoUser.setPhone(user.getPhone());
        dynamoUser.setRole(user.getRole() != null ? user.getRole().name() : null);
        dynamoUser.setCreatedAt(user.getCreatedAt() != null ? user.getCreatedAt().getTime() : System.currentTimeMillis());
        dynamoUser.setUpdatedAt(user.getUpdatedAt() != null ? user.getUpdatedAt().getTime() : System.currentTimeMillis());
        dynamoUser.setIsActive(true);
        
        return dynamoUser;
    }
}
