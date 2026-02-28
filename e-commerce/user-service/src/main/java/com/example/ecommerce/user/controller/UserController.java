package com.example.ecommerce.user.controller;

import com.example.ecommerce.common.dto.UIBean;
import com.example.ecommerce.user.dto.AuthResponse;
import com.example.ecommerce.user.dto.LoginRequest;
import com.example.ecommerce.user.dto.RegisterRequest;
import com.example.ecommerce.user.dto.UserDto;
import com.example.ecommerce.user.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {
    
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    
    private final UserService userService;
    
    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }
    
    @PostMapping("/register")
    public ResponseEntity<UIBean<AuthResponse>> register(@RequestBody RegisterRequest request) {
        if (logger.isDebugEnabled()) {
            logger.debug("Register request for email: {}", request.getEmail());
        }
        
        try {
            AuthResponse response = userService.register(request);
            UIBean<AuthResponse> uiBean = UIBean.success(response, "User registered successfully");
            return new ResponseEntity<>(uiBean, HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("Error registering user: {}", request.getEmail(), e);
            UIBean<AuthResponse> errorResponse = new UIBean<>();
            errorResponse.setSuccess(false);
            errorResponse.setMessage("Error occurred: " + e.getMessage());
            errorResponse.setResponse("ERROR");
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @PostMapping("/login")
    public ResponseEntity<UIBean<AuthResponse>> login(@RequestBody LoginRequest request) {
        if (logger.isDebugEnabled()) {
            logger.debug("Login request for email: {}", request.getEmail());
        }
        
        try {
            AuthResponse response = userService.login(request);
            UIBean<AuthResponse> uiBean = UIBean.success(response, "Login successful");
            return new ResponseEntity<>(uiBean, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error during login for email: {}", request.getEmail(), e);
            UIBean<AuthResponse> errorResponse = new UIBean<>();
            errorResponse.setSuccess(false);
            errorResponse.setMessage("Error occurred: " + e.getMessage());
            errorResponse.setResponse("ERROR");
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @GetMapping("/profile")
    public ResponseEntity<UIBean<UserDto>> getProfile(@RequestHeader("X-User-Email") String email) {
        if (logger.isDebugEnabled()) {
            logger.debug("Fetching profile for email: {}", email);
        }
        
        try {
            UserDto user = userService.getUserByEmail(email);
            UIBean<UserDto> uiBean = UIBean.success(user, "Profile retrieved successfully");
            return new ResponseEntity<>(uiBean, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error fetching profile for email: {}", email, e);
            UIBean<UserDto> errorResponse = new UIBean<>();
            errorResponse.setSuccess(false);
            errorResponse.setMessage("Error occurred: " + e.getMessage());
            errorResponse.setResponse("ERROR");
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<UIBean<UserDto>> getUserById(@PathVariable Long id) {
        if (logger.isDebugEnabled()) {
            logger.debug("Fetching user by ID: {}", id);
        }
        
        try {
            UserDto user = userService.getUserById(id);
            UIBean<UserDto> uiBean = UIBean.success(user, "User retrieved successfully");
            return new ResponseEntity<>(uiBean, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error fetching user by ID: {}", id, e);
            UIBean<UserDto> errorResponse = new UIBean<>();
            errorResponse.setSuccess(false);
            errorResponse.setMessage("Error occurred: " + e.getMessage());
            errorResponse.setResponse("ERROR");
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
