package com.example.ecommerce.user.service;

import com.example.ecommerce.common.exception.InvalidArgumentException;
import com.example.ecommerce.common.exception.ResourceNotFoundException;
import com.example.ecommerce.user.dto.AuthResponse;
import com.example.ecommerce.user.dto.LoginRequest;
import com.example.ecommerce.user.dto.RegisterRequest;
import com.example.ecommerce.user.dto.UserDto;
import com.example.ecommerce.user.mapper.UserMapper;
import com.example.ecommerce.user.model.User;
import com.example.ecommerce.user.model.UserDynamoDB;
import com.example.ecommerce.user.repository.UserDynamoDBRepository;
import com.example.ecommerce.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class UserService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    
    private final UserRepository userRepository;
    private final UserDynamoDBRepository userDynamoDBRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    
    @Autowired
    public UserService(UserRepository userRepository,
                      UserDynamoDBRepository userDynamoDBRepository,
                      PasswordEncoder passwordEncoder,
                      JwtService jwtService) {
        this.userRepository = userRepository;
        this.userDynamoDBRepository = userDynamoDBRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }
    
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (logger.isDebugEnabled()) {
            logger.debug("Registering user with email: {}", request.getEmail());
        }
        
        validateRegisterRequest(request);
        
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new InvalidArgumentException("Email already exists: " + request.getEmail());
        }
        
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());
        user.setRole(User.Role.USER);
        
        User savedUser = userRepository.save(user);
        logger.info("User registered successfully in MySQL: {}", savedUser.getEmail());
        
        try {
            UserDynamoDB dynamoUser = UserMapper.toDynamoDB(savedUser);
            userDynamoDBRepository.save(dynamoUser);
            logger.info("User synced to DynamoDB: {}", savedUser.getEmail());
        } catch (Exception e) {
            logger.error("Failed to sync user to DynamoDB: {}", savedUser.getEmail(), e);
        }
        
        String token = jwtService.generateToken(savedUser.getEmail());
        return new AuthResponse(token, savedUser.getEmail(), savedUser.getFirstName(), savedUser.getLastName());
    }
    
    public AuthResponse login(LoginRequest request) {
        if (logger.isDebugEnabled()) {
            logger.debug("Login attempt for email: {}", request.getEmail());
        }
        
        validateLoginRequest(request);
        
        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new InvalidArgumentException("Invalid email or password"));
        
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            logger.warn("Invalid password attempt for email: {}", request.getEmail());
            throw new InvalidArgumentException("Invalid email or password");
        }
        
        logger.info("User logged in successfully: {}", user.getEmail());
        
        String token = jwtService.generateToken(user.getEmail());
        return new AuthResponse(token, user.getEmail(), user.getFirstName(), user.getLastName());
    }
    
    public UserDto getUserByEmail(String email) {
        if (logger.isDebugEnabled()) {
            logger.debug("Fetching user by email: {}", email);
        }
        
        validateEmail(email);
        
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        
        return UserMapper.toDto(user);
    }
    
    public UserDto getUserById(Long id) {
        if (logger.isDebugEnabled()) {
            logger.debug("Fetching user by ID: {}", id);
        }
        
        if (id == null || id <= 0) {
            throw new InvalidArgumentException("Invalid user ID: " + id);
        }
        
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
        
        return UserMapper.toDto(user);
    }
    
    private void validateRegisterRequest(RegisterRequest request) {
        if (request == null) {
            throw new InvalidArgumentException("Register request cannot be null");
        }
        
        validateEmail(request.getEmail());
        
        if (!StringUtils.hasText(request.getPassword())) {
            throw new InvalidArgumentException("Password cannot be null or empty");
        }
        
        if (request.getPassword().length() < 6) {
            throw new InvalidArgumentException("Password must be at least 6 characters long");
        }
        
        if (!StringUtils.hasText(request.getFirstName())) {
            throw new InvalidArgumentException("First name cannot be null or empty");
        }
        
        if (!StringUtils.hasText(request.getLastName())) {
            throw new InvalidArgumentException("Last name cannot be null or empty");
        }
        
        if (StringUtils.hasText(request.getPhone())) {
            if (!request.getPhone().matches("\\d{10}")) {
                throw new InvalidArgumentException("Phone number must be 10 digits");
            }
        }
    }
    
    private void validateLoginRequest(LoginRequest request) {
        if (request == null) {
            throw new InvalidArgumentException("Login request cannot be null");
        }
        
        validateEmail(request.getEmail());
        
        if (!StringUtils.hasText(request.getPassword())) {
            throw new InvalidArgumentException("Password cannot be null or empty");
        }
    }
    
    private void validateEmail(String email) {
        if (!StringUtils.hasText(email)) {
            throw new InvalidArgumentException("Email cannot be null or empty");
        }
        
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        if (!email.matches(emailRegex)) {
            throw new InvalidArgumentException("Invalid email format: " + email);
        }
    }
}
