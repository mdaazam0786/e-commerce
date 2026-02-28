# 🔍 How the E-Commerce Platform Works Internally

This guide explains how everything works under the hood - perfect for understanding the system deeply!

---

## 📋 Table of Contents

- [System Startup](#system-startup)
- [Request Lifecycle](#request-lifecycle)
- [User Registration & Login](#user-registration--login)
- [Product Management](#product-management)
- [Shopping Cart Operations](#shopping-cart-operations)
- [Order Processing](#order-processing)
- [Payment Processing](#payment-processing)
- [Notification System](#notification-system)
- [Error Handling](#error-handling)
- [Database Operations](#database-operations)

---

## 🚀 System Startup

### What Happens When You Run `docker-compose up`

#### Step 1: Infrastructure Services Start (0-30 seconds)

```
1. MySQL Databases Start
   ├─ mysql-user (Port 3306)
   ├─ mysql-product (Port 3307)
   └─ mysql-order (Port 3308)

2. MongoDB Starts (Port 27017)

3. DynamoDB Local Starts (Port 8000)
```

**What's happening**:
- Docker creates isolated containers
- Databases initialize empty schemas
- Health checks begin

#### Step 2: Service Discovery Starts (30-45 seconds)

```
Eureka Server starts on Port 8761
├─ Creates service registry (empty)
├─ Starts health check scheduler
└─ Opens dashboard at http://localhost:8761
```

**What's happening**:
- Eureka prepares to track services
- Waits for services to register
- Dashboard shows "No instances available"

#### Step 3: Microservices Start (45-90 seconds)

```
Services start in parallel:
├─ User Service (8081)
├─ Product Service (8082)
├─ Cart Service (8083)
├─ Order Service (8084)
├─ Payment Service (8085)
└─ Notification Service (8086)

Each service:
1. Connects to its database
2. Creates tables (if not exist)
3. Registers with Eureka
4. Starts accepting requests
```

**What's happening**:
- Spring Boot applications initialize
- Database connections established
- JPA creates database tables
- Services register with Eureka

#### Step 4: API Gateway Starts (90-120 seconds)

```
API Gateway starts on Port 8080
├─ Fetches service list from Eureka
├─ Configures routes
└─ Ready to accept client requests
```

**What's happening**:
- Gateway discovers all services
- Sets up routing rules
- Starts load balancing

### Verification

Check if everything is running:
```bash
docker-compose ps
```

You should see all services "Up" and "healthy".

---

## 🔄 Request Lifecycle

### Anatomy of a Request

Let's trace what happens when you call: `GET /api/products/1`

#### Step 1: Client Makes Request

```
Browser/App sends:
GET http://localhost:8080/api/products/1
Headers:
  - Authorization: Bearer eyJhbGc...
  - Content-Type: application/json
```

#### Step 2: API Gateway Receives Request

```java
// Inside API Gateway
1. Request arrives at port 8080
2. Gateway checks route configuration:
   - Path: /api/products/** → Product Service
3. Validates JWT token
4. Looks up Product Service in Eureka
5. Gets service location: http://product-service:8082
```

**Code flow**:
```
GatewayFilter → JwtAuthenticationFilter → RouteLocator
```

#### Step 3: Request Forwarded to Product Service

```
API Gateway → Product Service
POST http://product-service:8082/api/products/1
Headers: (same as original + service metadata)
```

#### Step 4: Product Service Processes Request

```java
// Inside Product Service
1. Request hits ProductController
2. Controller calls ProductService
3. Service calls ProductRepository
4. Repository queries MySQL database
5. Result mapped to ProductDto
6. Response sent back
```

**Code flow**:
```
Controller → Service → Repository → Database
    ↓
Response ← DTO ← Entity ← Query Result
```

#### Step 5: Response Returns to Client

```
Product Service → API Gateway → Client

Response:
{
  "id": 1,
  "name": "Laptop",
  "price": 999.99,
  "stockQuantity": 50
}
```

**Total time**: ~50-200ms

---

## 👤 User Registration & Login

### Registration Flow (Step by Step)

#### Request
```http
POST /api/users/register
Content-Type: application/json

{
  "username": "john@example.com",
  "password": "password123",
  "firstName": "John",
  "lastName": "Doe"
}
```

#### What Happens Internally

**Step 1: API Gateway**
```
1. Receives request
2. No auth needed for registration
3. Routes to User Service
```

**Step 2: User Service - Controller Layer**
```java
@PostMapping("/register")
public ResponseEntity<UserDto> register(@RequestBody RegisterRequest request) {
    // Validation happens here
    // - Check if username exists
    // - Validate email format
    // - Check password strength
}
```

**Step 3: User Service - Service Layer**
```java
public UserDto registerUser(RegisterRequest request) {
    // 1. Hash password using BCrypt
    String hashedPassword = passwordEncoder.encode(request.getPassword());
    
    // 2. Create User entity
    User user = new User();
    user.setUsername(request.getUsername());
    user.setPassword(hashedPassword);
    user.setFirstName(request.getFirstName());
    user.setLastName(request.getLastName());
    
    // 3. Save to MySQL
    User savedUser = userRepository.save(user);
    
    // 4. Create DynamoDB profile
    UserDynamoDB profile = new UserDynamoDB();
    profile.setUserId(savedUser.getId().toString());
    profile.setUsername(savedUser.getUsername());
    dynamoDBRepository.save(profile);
    
    // 5. Return DTO (without password)
    return userMapper.toDto(savedUser);
}
```

**Step 4: Database Operations**

MySQL:
```sql
INSERT INTO users (username, password, first_name, last_name, created_at)
VALUES ('john@example.com', '$2a$10$...', 'John', 'Doe', NOW());
```

DynamoDB:
```json
{
  "userId": "1",
  "username": "john@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "createdAt": 1234567890
}
```

**Step 5: Response**
```json
{
  "id": 1,
  "username": "john@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "createdAt": "2024-02-28T10:30:00"
}
```

### Login Flow

#### Request
```http
POST /api/users/login
Content-Type: application/json

{
  "username": "john@example.com",
  "password": "password123"
}
```

#### What Happens Internally

**Step 1: Validate Credentials**
```java
public AuthResponse login(LoginRequest request) {
    // 1. Find user by username
    User user = userRepository.findByUsername(request.getUsername())
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    
    // 2. Verify password
    if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
        throw new InvalidArgumentException("Invalid credentials");
    }
    
    // 3. Generate JWT token
    String token = jwtService.generateToken(user);
    
    // 4. Return token
    return new AuthResponse(token, user.getId(), user.getUsername());
}
```

**Step 2: JWT Token Generation**
```java
public String generateToken(User user) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("userId", user.getId());
    claims.put("username", user.getUsername());
    claims.put("roles", user.getRoles());
    
    return Jwts.builder()
        .setClaims(claims)
        .setSubject(user.getUsername())
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + 86400000)) // 24 hours
        .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
        .compact();
}
```

**Step 3: Response**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "userId": 1,
  "username": "john@example.com"
}
```

**Step 4: Client Stores Token**
```javascript
// Client-side
localStorage.setItem('token', response.token);

// All future requests include:
headers: {
  'Authorization': 'Bearer ' + token
}
```

---

## 📦 Product Management

### Creating a Product

#### Request
```http
POST /api/products
Authorization: Bearer eyJhbGc...
Content-Type: application/json

{
  "name": "Laptop",
  "description": "High-performance laptop",
  "price": 999.99,
  "stockQuantity": 50,
  "category": "Electronics"
}
```

#### Internal Flow

**Step 1: Authentication**
```java
// API Gateway validates JWT
JwtAuthenticationFilter.doFilter() {
    String token = extractToken(request);
    if (jwtService.validateToken(token)) {
        // Extract user info
        String username = jwtService.getUsernameFromToken(token);
        // Add to request context
        request.setAttribute("username", username);
        // Forward to service
    } else {
        throw new UnauthorizedException();
    }
}
```

**Step 2: Product Service Processing**
```java
@PostMapping
public ResponseEntity<ProductDto> createProduct(@RequestBody CreateProductRequest request) {
    // 1. Validate input
    validateProductRequest(request);
    
    // 2. Create entity
    Product product = new Product();
    product.setName(request.getName());
    product.setDescription(request.getDescription());
    product.setPrice(request.getPrice());
    product.setStockQuantity(request.getStockQuantity());
    product.setCategory(request.getCategory());
    
    // 3. Save to database
    Product saved = productRepository.save(product);
    
    // 4. Return DTO
    return ResponseEntity.ok(productMapper.toDto(saved));
}
```

**Step 3: Database Operation**
```sql
INSERT INTO products (name, description, price, stock_quantity, category, created_at, updated_at)
VALUES ('Laptop', 'High-performance laptop', 999.99, 50, 'Electronics', NOW(), NOW());
```

**Step 4: Response**
```json
{
  "id": 1,
  "name": "Laptop",
  "description": "High-performance laptop",
  "price": 999.99,
  "stockQuantity": 50,
  "category": "Electronics",
  "createdAt": "2024-02-28T10:30:00"
}
```

### Listing Products (with Pagination)

#### Request
```http
GET /api/products?page=0&size=10&sort=name,asc
```

#### Internal Flow

```java
@GetMapping
public ResponseEntity<Page<ProductDto>> getProducts(
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "10") int size,
    @RequestParam(defaultValue = "id,desc") String[] sort
) {
    // 1. Create pagination request
    Pageable pageable = PageRequest.of(page, size, Sort.by(sort));
    
    // 2. Query database
    Page<Product> products = productRepository.findAll(pageable);
    
    // 3. Convert to DTOs
    Page<ProductDto> productDtos = products.map(productMapper::toDto);
    
    // 4. Return paginated response
    return ResponseEntity.ok(productDtos);
}
```

**Database Query**:
```sql
SELECT * FROM products
ORDER BY name ASC
LIMIT 10 OFFSET 0;

-- Also executes count query:
SELECT COUNT(*) FROM products;
```

**Response**:
```json
{
  "content": [
    { "id": 1, "name": "Laptop", ... },
    { "id": 2, "name": "Mouse", ... }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10
  },
  "totalElements": 100,
  "totalPages": 10
}
```

---

## 🛒 Shopping Cart Operations

### Adding Item to Cart

#### Request
```http
POST /api/cart/items
Authorization: Bearer eyJhbGc...
Content-Type: application/json

{
  "productId": 1,
  "quantity": 2
}
```

#### Internal Flow

**Step 1: Extract User from Token**
```java
String userId = jwtService.getUserIdFromToken(token);
```

**Step 2: Get Product Details**
```java
// Cart Service calls Product Service
RestTemplate restTemplate = new RestTemplate();
ProductDto product = restTemplate.getForObject(
    "http://product-service:8082/api/products/" + productId,
    ProductDto.class
);
```

**Step 3: Update Cart in MongoDB**
```java
public CartDto addItem(String userId, AddItemRequest request) {
    // 1. Find or create cart
    Cart cart = cartRepository.findByUserId(userId)
        .orElse(new Cart(userId));
    
    // 2. Check if item already in cart
    Optional<CartItem> existingItem = cart.getItems().stream()
        .filter(item -> item.getProductId().equals(request.getProductId()))
        .findFirst();
    
    if (existingItem.isPresent()) {
        // Update quantity
        existingItem.get().setQuantity(
            existingItem.get().getQuantity() + request.getQuantity()
        );
    } else {
        // Add new item
        CartItem newItem = new CartItem();
        newItem.setProductId(request.getProductId());
        newItem.setProductName(product.getName());
        newItem.setPrice(product.getPrice());
        newItem.setQuantity(request.getQuantity());
        cart.getItems().add(newItem);
    }
    
    // 3. Recalculate total
    BigDecimal total = cart.getItems().stream()
        .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    cart.setTotalAmount(total);
    
    // 4. Save to MongoDB
    Cart saved = cartRepository.save(cart);
    
    return cartMapper.toDto(saved);
}
```

**MongoDB Document**:
```json
{
  "_id": "cart_user_1",
  "userId": "1",
  "items": [
    {
      "productId": 1,
      "productName": "Laptop",
      "quantity": 2,
      "price": 999.99
    }
  ],
  "totalAmount": 1999.98,
  "updatedAt": "2024-02-28T10:30:00"
}
```

---

## 📝 Order Processing

### Complete Order Flow

#### Request
```http
POST /api/orders
Authorization: Bearer eyJhbGc...
Content-Type: application/json

{
  "shippingAddress": "123 Main St, City, Country",
  "paymentMethod": "RAZORPAY"
}
```

#### Internal Flow (Multi-Service)

**Step 1: Order Service - Create Order**
```java
public OrderDto createOrder(String userId, CreateOrderRequest request) {
    // 1. Get cart from Cart Service
    CartDto cart = cartServiceClient.getCart(userId);
    
    if (cart.getItems().isEmpty()) {
        throw new InvalidArgumentException("Cart is empty");
    }
    
    // 2. Verify stock availability
    for (CartItemDto item : cart.getItems()) {
        ProductDto product = productServiceClient.getProduct(item.getProductId());
        if (product.getStockQuantity() < item.getQuantity()) {
            throw new InvalidArgumentException("Insufficient stock for " + product.getName());
        }
    }
    
    // 3. Create order
    Order order = new Order();
    order.setUserId(Long.parseLong(userId));
    order.setTotalAmount(cart.getTotalAmount());
    order.setStatus(OrderStatus.PENDING);
    order.setShippingAddress(request.getShippingAddress());
    
    // 4. Add order items
    for (CartItemDto cartItem : cart.getItems()) {
        OrderItem orderItem = new OrderItem();
        orderItem.setProductId(cartItem.getProductId());
        orderItem.setProductName(cartItem.getProductName());
        orderItem.setQuantity(cartItem.getQuantity());
        orderItem.setPrice(cartItem.getPrice());
        order.addItem(orderItem);
    }
    
    // 5. Save order
    Order savedOrder = orderRepository.save(order);
    
    // 6. Initiate payment
    PaymentResponse payment = paymentServiceClient.createPayment(
        savedOrder.getId(),
        savedOrder.getTotalAmount()
    );
    
    savedOrder.setPaymentId(payment.getPaymentId());
    orderRepository.save(savedOrder);
    
    return orderMapper.toDto(savedOrder);
}
```

**Step 2: Payment Service - Create Razorpay Order**
```java
public PaymentResponse createPayment(Long orderId, BigDecimal amount) {
    // 1. Create Razorpay order
    JSONObject orderRequest = new JSONObject();
    orderRequest.put("amount", amount.multiply(BigDecimal.valueOf(100)).intValue()); // Convert to paise
    orderRequest.put("currency", "INR");
    orderRequest.put("receipt", "order_" + orderId);
    
    // 2. Call Razorpay API
    com.razorpay.Order razorpayOrder = razorpayClient.Orders.create(orderRequest);
    
    // 3. Return payment details
    return new PaymentResponse(
        razorpayOrder.get("id"),
        razorpayOrder.get("amount"),
        razorpayOrder.get("currency"),
        "CREATED"
    );
}
```

**Step 3: Customer Pays on Razorpay**
```
1. Customer redirected to Razorpay
2. Enters payment details
3. Payment processed
4. Razorpay sends webhook to our server
```

**Step 4: Payment Service - Handle Webhook**
```java
@PostMapping("/webhook")
public ResponseEntity<Void> handleWebhook(@RequestBody RazorpayWebhookRequest webhook) {
    // 1. Verify webhook signature
    if (!verifySignature(webhook)) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    
    // 2. Extract payment details
    String paymentId = webhook.getPayload().getPayment().getEntity().getId();
    String orderId = webhook.getPayload().getPayment().getEntity().getOrderId();
    String status = webhook.getPayload().getPayment().getEntity().getStatus();
    
    // 3. Update order status
    if ("captured".equals(status)) {
        orderServiceClient.updateOrderStatus(orderId, "CONFIRMED");
        
        // 4. Send confirmation email
        notificationServiceClient.sendOrderConfirmation(orderId);
        
        // 5. Update product stock
        productServiceClient.decreaseStock(orderId);
        
        // 6. Clear cart
        cartServiceClient.clearCart(userId);
    }
    
    return ResponseEntity.ok().build();
}
```

**Step 5: Notification Service - Send Email**
```java
public void sendOrderConfirmation(Long orderId) {
    // 1. Get order details
    OrderDto order = orderServiceClient.getOrder(orderId);
    
    // 2. Get user details
    UserDto user = userServiceClient.getUser(order.getUserId());
    
    // 3. Prepare email
    MimeMessage message = mailSender.createMimeMessage();
    MimeMessageHelper helper = new MimeMessageHelper(message, true);
    
    helper.setTo(user.getUsername());
    helper.setSubject("Order Confirmation - Order #" + orderId);
    helper.setText(buildEmailContent(order), true);
    
    // 4. Send email
    mailSender.send(message);
}
```

**Step 6: Product Service - Update Stock**
```java
public void decreaseStock(Long orderId) {
    Order order = orderServiceClient.getOrder(orderId);
    
    for (OrderItem item : order.getItems()) {
        Product product = productRepository.findById(item.getProductId())
            .orElseThrow();
        
        product.setStockQuantity(product.getStockQuantity() - item.getQuantity());
        productRepository.save(product);
    }
}
```

---

## ⚠️ Error Handling

### How Errors are Handled

**Step 1: Exception Occurs**
```java
// In any service
if (product == null) {
    throw new ResourceNotFoundException("Product not found with id: " + id);
}
```

**Step 2: Global Exception Handler Catches It**
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex) {
        ErrorResponse error = new ErrorResponse(
            HttpStatus.NOT_FOUND.value(),
            ex.getMessage(),
            LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    
    @ExceptionHandler(InvalidArgumentException.class)
    public ResponseEntity<ErrorResponse> handleInvalidArgument(InvalidArgumentException ex) {
        ErrorResponse error = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            ex.getMessage(),
            LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
}
```

**Step 3: Formatted Error Response**
```json
{
  "status": 404,
  "message": "Product not found with id: 999",
  "timestamp": "2024-02-28T10:30:00"
}
```

---

## 💾 Database Operations

### Connection Pooling (HikariCP)

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 100      # Max connections
      minimum-idle: 25            # Min idle connections
      connection-timeout: 30000   # 30 seconds
      idle-timeout: 60000         # 60 seconds
```

**How it works**:
1. Application starts → Creates 25 idle connections
2. Request comes → Uses existing connection
3. High load → Creates more connections (up to 100)
4. Load decreases → Closes idle connections

### JPA/Hibernate Operations

**Entity to Table Mapping**:
```java
@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    // Hibernate automatically creates:
    // CREATE TABLE products (
    //   id BIGINT AUTO_INCREMENT PRIMARY KEY,
    //   name VARCHAR(255) NOT NULL,
    //   ...
    // );
}
```

**Repository Methods**:
```java
// Spring Data JPA generates SQL automatically
productRepository.findById(1L);
// SELECT * FROM products WHERE id = 1;

productRepository.findByName("Laptop");
// SELECT * FROM products WHERE name = 'Laptop';

productRepository.findByPriceBetween(100, 1000);
// SELECT * FROM products WHERE price BETWEEN 100 AND 1000;
```

---

## 🎯 Summary

This platform demonstrates:
- **Microservices Architecture**: Independent, scalable services
- **Service Discovery**: Dynamic service location
- **API Gateway**: Single entry point
- **Multiple Databases**: Right tool for the job
- **External Integration**: Payment gateway
- **Asynchronous Communication**: Webhooks
- **Error Handling**: Graceful failure management
- **Security**: JWT authentication

Each component works together to create a robust, scalable e-commerce platform!

---

**Questions?** Check [README.md](README.md) or [ARCHITECTURE.md](ARCHITECTURE.md)
