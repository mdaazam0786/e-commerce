package com.example.ecommerce.order.repository;

import com.example.ecommerce.order.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findByUserEmail(String userEmail, Pageable pageable);
    
    Order findByRazorpayOrderId(String razorpayOrderId);
}
