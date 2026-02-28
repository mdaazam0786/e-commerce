package com.example.ecommerce.payment.config;

import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RazorpayConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(RazorpayConfig.class);
    
    @Value("${razorpay.key.id}")
    private String razorpayKeyId;
    
    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;
    
    @Bean
    public RazorpayClient razorpayClient() throws RazorpayException {
        logger.info("Initializing Razorpay client");
        return new RazorpayClient(razorpayKeyId, razorpayKeySecret);
    }
}
