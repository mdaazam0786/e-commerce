package com.example.ecommerce.notification.dto;

import lombok.Data;

@Data
public class EmailRequest {
    private String to;
    private String subject;
    private String body;
}
