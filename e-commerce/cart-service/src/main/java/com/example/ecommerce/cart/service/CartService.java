package com.example.ecommerce.cart.service;

import com.example.ecommerce.cart.dto.AddItemRequest;
import com.example.ecommerce.cart.dto.CartDto;
import com.example.ecommerce.cart.mapper.CartMapper;
import com.example.ecommerce.cart.model.Cart;
import com.example.ecommerce.cart.model.CartItem;
import com.example.ecommerce.cart.repository.CartRepository;
import com.example.ecommerce.common.exception.InvalidArgumentException;
import com.example.ecommerce.common.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;

@Service
public class CartService {
    
    private static final Logger logger = LoggerFactory.getLogger(CartService.class);
    
    private final CartRepository cartRepository;
    private final CartMapper cartMapper;
    
    @Autowired
    public CartService(CartRepository cartRepository, CartMapper cartMapper) {
        this.cartRepository = cartRepository;
        this.cartMapper = cartMapper;
    }
    
    public CartDto getCartByUserEmail(String userEmail) {
        if (logger.isDebugEnabled()) {
            logger.debug("Fetching cart for user: {}", userEmail);
        }
        
        validateUserEmail(userEmail);
        
        Cart cart = cartRepository.findByUserEmail(userEmail)
            .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user: " + userEmail));
        
        return cartMapper.toDto(cart);
    }
    
    public CartDto getOrCreateCart(String userEmail) {
        if (logger.isDebugEnabled()) {
            logger.debug("Getting or creating cart for user: {}", userEmail);
        }
        
        validateUserEmail(userEmail);
        
        Cart cart = cartRepository.findByUserEmail(userEmail)
            .orElseGet(() -> {
                Cart newCart = new Cart();
                newCart.setUserEmail(userEmail);
                return cartRepository.save(newCart);
            });
        
        return cartMapper.toDto(cart);
    }
    
    public CartDto addItem(String userEmail, AddItemRequest request) {
        if (logger.isDebugEnabled()) {
            logger.debug("Adding item to cart for user: {}, productId: {}", userEmail, request.getProductId());
        }
        
        validateUserEmail(userEmail);
        validateAddItemRequest(request);
        
        Cart cart = cartRepository.findByUserEmail(userEmail)
            .orElseGet(() -> {
                Cart newCart = new Cart();
                newCart.setUserEmail(userEmail);
                return newCart;
            });
        
        CartItem newItem = new CartItem();
        newItem.setProductId(request.getProductId());
        newItem.setProductName(request.getProductName());
        newItem.setPrice(request.getPrice());
        newItem.setQuantity(request.getQuantity());
        
        cart.getItems().stream()
            .filter(i -> i.getProductId().equals(request.getProductId()))
            .findFirst()
            .ifPresentOrElse(
                existing -> {
                    logger.debug("Product already in cart, updating quantity");
                    existing.setQuantity(existing.getQuantity() + request.getQuantity());
                },
                () -> {
                    logger.debug("Adding new product to cart");
                    cart.getItems().add(newItem);
                }
            );
        
        cart.setUpdatedAt(new Date());
        Cart savedCart = cartRepository.save(cart);
        
        logger.info("Item added to cart successfully for user: {}", userEmail);
        return cartMapper.toDto(savedCart);
    }
    
    public CartDto updateItem(String userEmail, Long productId, Integer quantity) {
        if (logger.isDebugEnabled()) {
            logger.debug("Updating cart item for user: {}, productId: {}, quantity: {}", 
                userEmail, productId, quantity);
        }
        
        validateUserEmail(userEmail);
        validateProductId(productId);
        validateQuantity(quantity);
        
        Cart cart = cartRepository.findByUserEmail(userEmail)
            .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user: " + userEmail));
        
        cart.getItems().stream()
            .filter(i -> i.getProductId().equals(productId))
            .findFirst()
            .ifPresentOrElse(
                item -> item.setQuantity(quantity),
                () -> {
                    throw new ResourceNotFoundException("Product not found in cart: " + productId);
                }
            );
        
        cart.setUpdatedAt(new Date());
        Cart savedCart = cartRepository.save(cart);
        
        logger.info("Cart item updated successfully for user: {}", userEmail);
        return cartMapper.toDto(savedCart);
    }
    
    public CartDto removeItem(String userEmail, Long productId) {
        if (logger.isDebugEnabled()) {
            logger.debug("Removing item from cart for user: {}, productId: {}", userEmail, productId);
        }
        
        validateUserEmail(userEmail);
        validateProductId(productId);
        
        Cart cart = cartRepository.findByUserEmail(userEmail)
            .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user: " + userEmail));
        
        boolean removed = cart.getItems().removeIf(i -> i.getProductId().equals(productId));
        
        if (!removed) {
            throw new ResourceNotFoundException("Product not found in cart: " + productId);
        }
        
        cart.setUpdatedAt(new Date());
        Cart savedCart = cartRepository.save(cart);
        
        logger.info("Item removed from cart successfully for user: {}", userEmail);
        return cartMapper.toDto(savedCart);
    }
    
    public void clearCart(String userEmail) {
        if (logger.isDebugEnabled()) {
            logger.debug("Clearing cart for user: {}", userEmail);
        }
        
        validateUserEmail(userEmail);
        
        Cart cart = cartRepository.findByUserEmail(userEmail)
            .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user: " + userEmail));
        
        cart.getItems().clear();
        cart.setUpdatedAt(new Date());
        cartRepository.save(cart);
        
        logger.info("Cart cleared successfully for user: {}", userEmail);
    }
    
    private void validateUserEmail(String userEmail) {
        if (!StringUtils.hasText(userEmail)) {
            throw new InvalidArgumentException("User email cannot be null or empty");
        }
    }
    
    private void validateProductId(Long productId) {
        if (productId == null || productId <= 0) {
            throw new InvalidArgumentException("Invalid product ID: " + productId);
        }
    }
    
    private void validateQuantity(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new InvalidArgumentException("Quantity must be greater than 0");
        }
    }
    
    private void validateAddItemRequest(AddItemRequest request) {
        if (request == null) {
            throw new InvalidArgumentException("Add item request cannot be null");
        }
        
        validateProductId(request.getProductId());
        validateQuantity(request.getQuantity());
        
        if (!StringUtils.hasText(request.getProductName())) {
            throw new InvalidArgumentException("Product name cannot be null or empty");
        }
        
        if (request.getPrice() == null || request.getPrice().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new InvalidArgumentException("Price must be greater than 0");
        }
    }
}
