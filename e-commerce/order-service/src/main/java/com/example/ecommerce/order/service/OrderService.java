package com.example.ecommerce.order.service;

import com.example.ecommerce.common.constants.PaginationConstants;
import com.example.ecommerce.common.exception.InvalidArgumentException;
import com.example.ecommerce.common.exception.ResourceNotFoundException;
import com.example.ecommerce.order.dto.CreateOrderRequest;
import com.example.ecommerce.order.dto.OrderDto;
import com.example.ecommerce.order.dto.OrderItemDto;
import com.example.ecommerce.order.mapper.OrderMapper;
import com.example.ecommerce.order.model.Order;
import com.example.ecommerce.order.model.OrderItem;
import com.example.ecommerce.order.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.stream.Collectors;

@Service
public class OrderService {
    
    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);
    
    private final OrderRepository orderRepository;
    
    @Autowired
    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }
    
    @Transactional
    public OrderDto createOrder(String userEmail, CreateOrderRequest request) {
        if (logger.isDebugEnabled()) {
            logger.debug("Creating order for user: {}, items count: {}", 
                userEmail, request.getItems() != null ? request.getItems().size() : 0);
        }
        
        validateCreateOrderRequest(userEmail, request);
        
        Order order = new Order();
        order.setUserEmail(userEmail);
        order.setShippingAddress(request.getShippingAddress());
        order.setStatus(Order.OrderStatus.PENDING);
        
        order.setItems(request.getItems().stream()
            .map(itemDto -> OrderMapper.toItemEntity(itemDto, order))
            .collect(Collectors.toList()));
        
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (OrderItemDto item : request.getItems()) {
            BigDecimal itemTotal = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);
        }
        order.setTotalAmount(totalAmount);
        
        Order savedOrder = orderRepository.save(order);
        logger.info("Order created successfully with ID: {} for user: {}", savedOrder.getId(), userEmail);
        
        return OrderMapper.toDto(savedOrder);
    }
    
    public Page<OrderDto> getOrdersByUserEmail(String userEmail, Integer page, Integer pageSize) {
        if (logger.isDebugEnabled()) {
            logger.debug("Fetching orders for user: {}, page: {}, pageSize: {}", userEmail, page, pageSize);
        }
        
        validateUserEmail(userEmail);
        
        int size = pageSize != null ? Math.min(pageSize, PaginationConstants.MAX_PAGE_SIZE) 
            : PaginationConstants.DEFAULT_PAGE_SIZE;
        int pageNumber = page != null ? page : PaginationConstants.DEFAULT_PAGE;
        
        Sort sort = Sort.by("createdAt").descending();
        Pageable pageable = PageRequest.of(pageNumber, size, sort);
        
        Page<Order> orderPage = orderRepository.findByUserEmail(userEmail, pageable);
        return orderPage.map(OrderMapper::toDto);
    }
    
    public OrderDto getOrderById(Long id) {
        if (logger.isDebugEnabled()) {
            logger.debug("Fetching order by ID: {}", id);
        }
        
        if (id == null || id <= 0) {
            throw new InvalidArgumentException("Invalid order ID: " + id);
        }
        
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + id));
        
        return OrderMapper.toDto(order);
    }
    
    @Transactional
    public OrderDto updateOrderStatus(Long id, String statusStr) {
        if (logger.isDebugEnabled()) {
            logger.debug("Updating order status for ID: {}, new status: {}", id, statusStr);
        }
        
        if (id == null || id <= 0) {
            throw new InvalidArgumentException("Invalid order ID: " + id);
        }
        
        if (!StringUtils.hasText(statusStr)) {
            throw new InvalidArgumentException("Order status cannot be null or empty");
        }
        
        Order.OrderStatus status;
        try {
            status = Order.OrderStatus.valueOf(statusStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidArgumentException("Invalid order status: " + statusStr + 
                ". Valid values are: PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED");
        }
        
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + id));
        
        order.setStatus(status);
        Order updatedOrder = orderRepository.save(order);
        
        logger.info("Order status updated successfully for ID: {}, new status: {}", id, status);
        
        return OrderMapper.toDto(updatedOrder);
    }
    
    public OrderDto getOrderByRazorpayOrderId(String razorpayOrderId) {
        if (logger.isDebugEnabled()) {
            logger.debug("Fetching order by Razorpay order ID: {}", razorpayOrderId);
        }
        
        if (!StringUtils.hasText(razorpayOrderId)) {
            throw new InvalidArgumentException("Razorpay order ID cannot be null or empty");
        }
        
        Order order = orderRepository.findByRazorpayOrderId(razorpayOrderId);
        
        if (order == null) {
            throw new ResourceNotFoundException("Order not found with Razorpay order ID: " + razorpayOrderId);
        }
        
        logger.info("Order found with Razorpay order ID: {}, Order ID: {}", razorpayOrderId, order.getId());
        
        return OrderMapper.toDto(order);
    }
    
    @Transactional
    public void updateRazorpayOrderId(Long orderId, String razorpayOrderId) {
        if (logger.isDebugEnabled()) {
            logger.debug("Updating Razorpay order ID for order: {}, razorpayOrderId: {}", orderId, razorpayOrderId);
        }
        
        if (orderId == null || orderId <= 0) {
            throw new InvalidArgumentException("Invalid order ID: " + orderId);
        }
        
        if (!StringUtils.hasText(razorpayOrderId)) {
            throw new InvalidArgumentException("Razorpay order ID cannot be null or empty");
        }
        
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));
        
        order.setRazorpayOrderId(razorpayOrderId);
        orderRepository.save(order);
        
        logger.info("Razorpay order ID updated successfully for order: {}", orderId);
    }
    
    private void validateCreateOrderRequest(String userEmail, CreateOrderRequest request) {
        validateUserEmail(userEmail);
        
        if (request == null) {
            throw new InvalidArgumentException("Order request cannot be null");
        }
        
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new InvalidArgumentException("Order must contain at least one item");
        }
        
        if (!StringUtils.hasText(request.getShippingAddress())) {
            throw new InvalidArgumentException("Shipping address cannot be null or empty");
        }
        
        for (int i = 0; i < request.getItems().size(); i++) {
            var item = request.getItems().get(i);
            
            if (item.getProductId() == null || item.getProductId() <= 0) {
                throw new InvalidArgumentException("Invalid product ID at item index " + i);
            }
            
            if (!StringUtils.hasText(item.getProductName())) {
                throw new InvalidArgumentException("Product name cannot be null or empty at item index " + i);
            }
            
            if (item.getPrice() == null || item.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                throw new InvalidArgumentException("Invalid price at item index " + i);
            }
            
            if (item.getQuantity() == null || item.getQuantity() <= 0) {
                throw new InvalidArgumentException("Invalid quantity at item index " + i);
            }
        }
    }
    
    private void validateUserEmail(String userEmail) {
        if (!StringUtils.hasText(userEmail)) {
            throw new InvalidArgumentException("User email cannot be null or empty");
        }
        
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        if (!userEmail.matches(emailRegex)) {
            throw new InvalidArgumentException("Invalid email format: " + userEmail);
        }
    }
}
