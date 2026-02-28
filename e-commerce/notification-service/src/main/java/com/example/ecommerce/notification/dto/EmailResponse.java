package com.example.ecommerce.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailResponse {
    private boolean sent;
    private String message;
    private String recipient;
}
