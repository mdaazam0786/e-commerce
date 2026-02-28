package com.example.ecommerce.cart.controller;

import com.example.ecommerce.cart.dto.AddItemRequest;
import com.example.ecommerce.cart.dto.CartDto;
import com.example.ecommerce.cart.dto.UpdateItemRequest;
import com.example.ecommerce.cart.service.CartService;
import com.example.ecommerce.common.dto.UIBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {
    
    private static final Logger logger = LoggerFactory.getLogger(CartController.class);
    
    private final CartService cartService;
    
    @Autowired
    public CartController(CartService cartService) {
        this.cartService = cartService;
    }
    
    @GetMapping
    public ResponseEntity<UIBean<CartDto>> getCart(@RequestHeader("X-User-Email") String userEmail) {
        if (logger.isDebugEnabled()) {
            logger.debug("Fetching cart for user: {}", userEmail);
        }
        
        try {
            CartDto cart = cartService.getCartByUserEmail(userEmail);
            UIBean<CartDto> response = UIBean.success(cart, "Cart retrieved successfully");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error fetching cart for user: {}", userEmail, e);
            UIBean<CartDto> errorResponse = new UIBean<>();
            errorResponse.setSuccess(false);
            errorResponse.setMessage("Error occurred: " + e.getMessage());
            errorResponse.setResponse("ERROR");
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @GetMapping("/or-create")
    public ResponseEntity<UIBean<CartDto>> getOrCreateCart(@RequestHeader("X-User-Email") String userEmail) {
        if (logger.isDebugEnabled()) {
            logger.debug("Getting or creating cart for user: {}", userEmail);
        }
        
        try {
            CartDto cart = cartService.getOrCreateCart(userEmail);
            UIBean<CartDto> response = UIBean.success(cart, "Cart retrieved successfully");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error getting or creating cart for user: {}", userEmail, e);
            UIBean<CartDto> errorResponse = new UIBean<>();
            errorResponse.setSuccess(false);
            errorResponse.setMessage("Error occurred: " + e.getMessage());
            errorResponse.setResponse("ERROR");
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @PostMapping("/items")
    public ResponseEntity<UIBean<CartDto>> addItem(
            @RequestHeader("X-User-Email") String userEmail, 
            @RequestBody AddItemRequest request) {
        
        if (logger.isDebugEnabled()) {
            logger.debug("Adding item to cart for user: {}, request: {}", userEmail, request);
        }
        
        try {
            CartDto cart = cartService.addItem(userEmail, request);
            UIBean<CartDto> response = UIBean.success(cart, "Item added to cart successfully");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error adding item to cart for user: {}", userEmail, e);
            UIBean<CartDto> errorResponse = new UIBean<>();
            errorResponse.setSuccess(false);
            errorResponse.setMessage("Error occurred: " + e.getMessage());
            errorResponse.setResponse("ERROR");
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @PutMapping("/items/{productId}")
    public ResponseEntity<UIBean<CartDto>> updateItem(
            @RequestHeader("X-User-Email") String userEmail,
            @PathVariable Long productId,
            @RequestBody UpdateItemRequest request) {
        
        if (logger.isDebugEnabled()) {
            logger.debug("Updating cart item for user: {}, productId: {}, quantity: {}", 
                userEmail, productId, request.getQuantity());
        }
        
        try {
            CartDto cart = cartService.updateItem(userEmail, productId, request.getQuantity());
            UIBean<CartDto> response = UIBean.success(cart, "Cart item updated successfully");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error updating cart item for user: {}, productId: {}", userEmail, productId, e);
            UIBean<CartDto> errorResponse = new UIBean<>();
            errorResponse.setSuccess(false);
            errorResponse.setMessage("Error occurred: " + e.getMessage());
            errorResponse.setResponse("ERROR");
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @DeleteMapping("/items/{productId}")
    public ResponseEntity<UIBean<CartDto>> removeItem(
            @RequestHeader("X-User-Email") String userEmail,
            @PathVariable Long productId) {
        
        if (logger.isDebugEnabled()) {
            logger.debug("Removing item from cart for user: {}, productId: {}", userEmail, productId);
        }
        
        try {
            CartDto cart = cartService.removeItem(userEmail, productId);
            UIBean<CartDto> response = UIBean.success(cart, "Item removed from cart successfully");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error removing item from cart for user: {}, productId: {}", userEmail, productId, e);
            UIBean<CartDto> errorResponse = new UIBean<>();
            errorResponse.setSuccess(false);
            errorResponse.setMessage("Error occurred: " + e.getMessage());
            errorResponse.setResponse("ERROR");
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @DeleteMapping
    public ResponseEntity<UIBean<Void>> clearCart(@RequestHeader("X-User-Email") String userEmail) {
        if (logger.isDebugEnabled()) {
            logger.debug("Clearing cart for user: {}", userEmail);
        }
        
        try {
            cartService.clearCart(userEmail);
            UIBean<Void> response = UIBean.success(null, "Cart cleared successfully");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error clearing cart for user: {}", userEmail, e);
            UIBean<Void> errorResponse = new UIBean<>();
            errorResponse.setSuccess(false);
            errorResponse.setMessage("Error occurred: " + e.getMessage());
            errorResponse.setResponse("ERROR");
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
