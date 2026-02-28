package com.example.ecommerce.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UIBean<T> implements Serializable {
    @JsonProperty("data")
    private T data;
    
    private boolean success;
    
    private String message;
    
    private String response;
    
    public static <T> UIBean<T> success(T data) {
        return new UIBean<>(data, true, "Success", "SUCCESS");
    }
    
    public static <T> UIBean<T> success(T data, String message) {
        return new UIBean<>(data, true, message, "SUCCESS");
    }
    
    public static <T> UIBean<T> error(String message) {
        return new UIBean<>(null, false, message, "ERROR");
    }
}
