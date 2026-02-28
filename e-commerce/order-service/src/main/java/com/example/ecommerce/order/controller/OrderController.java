package com.example.ecommerce.order.controller;

import com.example.ecommerce.common.dto.UIBean;
import com.example.ecommerce.common.dto.UIBeanPaginated;
import com.example.ecommerce.order.dto.CreateOrderRequest;
import com.example.ecommerce.order.dto.OrderDto;
import com.example.ecommerce.order.dto.UpdateOrderStatusRequest;
import com.example.ecommerce.order.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    
    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);
    
    private final OrderService orderService;
    
    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }
    
    @PostMapping
    public ResponseEntity<UIBean<OrderDto>> createOrder(
            @RequestHeader("X-User-Email") String userEmail,
            @RequestBody CreateOrderRequest request) {
        
        if (logger.isDebugEnabled()) {
            logger.debug("Creating order for user: {}, items count: {}", 
                userEmail, request.getItems() != null ? request.getItems().size() : 0);
        }
        
        try {
            OrderDto order = orderService.createOrder(userEmail, request);
            UIBean<OrderDto> response = UIBean.success(order, "Order created successfully");
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("Error creating order for user: {}", userEmail, e);
            UIBean<OrderDto> errorResponse = new UIBean<>();
            errorResponse.setSuccess(false);
            errorResponse.setMessage("Error occurred: " + e.getMessage());
            errorResponse.setResponse("ERROR");
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @GetMapping
    public ResponseEntity<UIBeanPaginated<List<OrderDto>>> getOrders(
            @RequestHeader("X-User-Email") String userEmail,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize) {
        
        if (logger.isDebugEnabled()) {
            logger.debug("Fetching orders for user: {}, page: {}, pageSize: {}", userEmail, page, pageSize);
        }
        
        try {
            Page<OrderDto> orderPage = orderService.getOrdersByUserEmail(userEmail, page, pageSize);
            
            UIBeanPaginated<List<OrderDto>> response = UIBeanPaginated.success(
                orderPage.getContent(),
                orderPage.getTotalElements(),
                orderPage.getTotalPages(),
                orderPage.getNumber(),
                orderPage.getSize()
            );
            
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error fetching orders for user: {}", userEmail, e);
            UIBeanPaginated<List<OrderDto>> errorResponse = new UIBeanPaginated<>();
            errorResponse.setSuccess(false);
            errorResponse.setMessage("Error occurred: " + e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<UIBean<OrderDto>> getOrderById(@PathVariable Long id) {
        if (logger.isDebugEnabled()) {
            logger.debug("Fetching order by ID: {}", id);
        }
        
        try {
            OrderDto order = orderService.getOrderById(id);
            UIBean<OrderDto> response = UIBean.success(order, "Order retrieved successfully");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error fetching order by ID: {}", id, e);
            UIBean<OrderDto> errorResponse = new UIBean<>();
            errorResponse.setSuccess(false);
            errorResponse.setMessage("Error occurred: " + e.getMessage());
            errorResponse.setResponse("ERROR");
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @PutMapping("/{id}/status")
    public ResponseEntity<UIBean<OrderDto>> updateOrderStatus(
            @PathVariable Long id,
            @RequestBody UpdateOrderStatusRequest request) {
        
        if (logger.isDebugEnabled()) {
            logger.debug("Updating order status for ID: {}, new status: {}", id, request.getStatus());
        }
        
        try {
            OrderDto order = orderService.updateOrderStatus(id, request.getStatus());
            UIBean<OrderDto> response = UIBean.success(order, "Order status updated successfully");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error updating order status for ID: {}", id, e);
            UIBean<OrderDto> errorResponse = new UIBean<>();
            errorResponse.setSuccess(false);
            errorResponse.setMessage("Error occurred: " + e.getMessage());
            errorResponse.setResponse("ERROR");
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @GetMapping("/by-razorpay-order/{razorpayOrderId}")
    public ResponseEntity<UIBean<OrderDto>> getOrderByRazorpayOrderId(
            @PathVariable String razorpayOrderId) {
        
        if (logger.isDebugEnabled()) {
            logger.debug("Fetching order by Razorpay order ID: {}", razorpayOrderId);
        }
        
        try {
            OrderDto order = orderService.getOrderByRazorpayOrderId(razorpayOrderId);
            UIBean<OrderDto> response = UIBean.success(order, "Order retrieved successfully");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error fetching order by Razorpay order ID: {}", razorpayOrderId, e);
            UIBean<OrderDto> errorResponse = new UIBean<>();
            errorResponse.setSuccess(false);
            errorResponse.setMessage("Error occurred: " + e.getMessage());
            errorResponse.setResponse("ERROR");
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @PutMapping("/{id}/razorpay-order-id")
    public ResponseEntity<UIBean<String>> updateRazorpayOrderId(
            @PathVariable Long id,
            @RequestBody String razorpayOrderId) {
        
        if (logger.isDebugEnabled()) {
            logger.debug("Updating Razorpay order ID for order: {}, razorpayOrderId: {}", id, razorpayOrderId);
        }
        
        try {
            orderService.updateRazorpayOrderId(id, razorpayOrderId);
            UIBean<String> response = UIBean.success("Razorpay order ID updated successfully", 
                "Razorpay order ID updated successfully");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error updating Razorpay order ID for order: {}", id, e);
            UIBean<String> errorResponse = new UIBean<>();
            errorResponse.setSuccess(false);
            errorResponse.setMessage("Error occurred: " + e.getMessage());
            errorResponse.setResponse("ERROR");
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
