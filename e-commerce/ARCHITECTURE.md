# 🏗️ System Architecture

This document explains the architecture of our E-Commerce Microservices Platform in detail.

---

## 📋 Table of Contents

- [Architecture Overview](#architecture-overview)
- [Microservices Explained](#microservices-explained)
- [Communication Patterns](#communication-patterns)
- [Data Flow](#data-flow)
- [Database Architecture](#database-architecture)
- [Security Architecture](#security-architecture)
- [Scalability & Performance](#scalability--performance)
- [Deployment Architecture](#deployment-architecture)

---

## 🎯 Architecture Overview

### Complete System Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                         CLIENT LAYER                             │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │ Web Browser  │  │  Mobile App  │  │  Third Party │          │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘          │
└─────────┼──────────────────┼──────────────────┼─────────────────┘
          │                  │                  │
          └──────────────────┼──────────────────┘
                             │
┌────────────────────────────┼─────────────────────────────────────┐
│                    API GATEWAY LAYER                              │
│                   ┌────────┴────────┐                            │
│                   │   API Gateway   │                            │
│                   │   Port: 8080    │                            │
│                   │  - Routing      │                            │
│                   │  - Load Balance │                            │
│                   │  - Auth Check   │                            │
│                   └────────┬────────┘                            │
└────────────────────────────┼─────────────────────────────────────┘
                             │
┌────────────────────────────┼─────────────────────────────────────┐
│                  SERVICE DISCOVERY LAYER                          │
│                   ┌────────┴────────┐                            │
│                   │ Eureka Server   │                            │
│                   │  Port: 8761     │                            │
│                   │ - Registration  │                            │
│                   │ - Health Check  │                            │
│                   └─────────────────┘                            │
└──────────────────────────────────────────────────────────────────┘
                             │
        ┌────────────────────┼────────────────────┐
        │                    │                    │
┌───────┴────────┐  ┌────────┴────────┐  ┌───────┴────────┐
│  User Service  │  │ Product Service │  │  Cart Service  │
│   Port: 8081   │  │   Port: 8082    │  │  Port: 8083    │
│                │  │                 │  │                │
│ - Register     │  │ - CRUD Products │  │ - Add Items    │
│ - Login        │  │ - Search        │  │ - Update Qty   │
│ - Profile      │  │ - Inventory     │  │ - Remove Items │
└───────┬────────┘  └────────┬────────┘  └───────┬────────┘
        │                    │                    │
        ↓                    ↓                    ↓
   [MySQL DB]           [MySQL DB]           [MongoDB]
   [DynamoDB]
        
        ┌────────────────────┼────────────────────┐
        │                    │                    │
┌───────┴────────┐  ┌────────┴────────┐  ┌───────┴────────┐
│ Order Service  │  │Payment Service  │  │Notification Svc│
│  Port: 8084    │  │  Port: 8085     │  │  Port: 8086    │
│                │  │                 │  │                │
│ - Create Order │  │ - Process Pay   │  │ - Send Email   │
│ - Track Order  │  │ - Razorpay      │  │ - Confirm      │
│ - Update Status│  │ - Verify        │  │ - Notify       │
└───────┬────────┘  └─────────────────┘  └────────────────┘
        │
        ↓
   [MySQL DB]
```

---

## 🔧 Microservices Explained

### 1. Service Discovery (Eureka Server)

**Purpose**: Acts as a phone book for all services

**How it works**:
- When a service starts, it registers itself with Eureka
- Eureka keeps track of all running services
- Services can find each other through Eureka
- Performs health checks every 30 seconds

**Port**: 8761

**Key Features**:
- Service registration
- Service discovery
- Health monitoring
- Load balancing information

**Real-world analogy**: Like a receptionist who knows where everyone in the building is located.

---

### 2. API Gateway

**Purpose**: Single entry point for all client requests

**How it works**:
- All client requests come here first
- Routes requests to appropriate services
- Handles authentication
- Provides load balancing

**Port**: 8080

**Key Features**:
- Request routing
- Authentication/Authorization
- Rate limiting
- Request/Response transformation

**Routes**:
```
/api/users/**      → User Service
/api/products/**   → Product Service
/api/cart/**       → Cart Service
/api/orders/**     → Order Service
/api/payments/**   → Payment Service
```

**Real-world analogy**: Like a mall entrance where security checks you and directs you to the right shop.

---

### 3. User Service

**Purpose**: Manages user accounts and authentication

**Port**: 8081

**Databases**:
- **MySQL**: Stores user credentials and basic info
- **DynamoDB**: Stores user profiles and preferences

**Key Features**:
- User registration
- Login/Logout
- JWT token generation
- Profile management
- Password encryption (BCrypt)

**API Endpoints**:
```
POST   /api/users/register    - Create new user
POST   /api/users/login       - Authenticate user
GET    /api/users/{id}        - Get user details
PUT    /api/users/{id}        - Update user
DELETE /api/users/{id}        - Delete user
```

**Data Flow**:
```
1. User registers → Data saved to MySQL
2. Profile created → Saved to DynamoDB
3. Login → JWT token generated
4. Token used for all subsequent requests
```

---

### 4. Product Service

**Purpose**: Manages product catalog

**Port**: 8082

**Database**: MySQL

**Key Features**:
- Product CRUD operations
- Inventory management
- Product search
- Category management
- Stock tracking

**API Endpoints**:
```
POST   /api/products          - Create product
GET    /api/products          - List all products (paginated)
GET    /api/products/{id}     - Get product details
PUT    /api/products/{id}     - Update product
DELETE /api/products/{id}     - Delete product
PATCH  /api/products/{id}/stock - Update stock
```

**Data Model**:
```
Product:
- id (Long)
- name (String)
- description (String)
- price (BigDecimal)
- stockQuantity (Integer)
- category (String)
- imageUrl (String)
- createdAt (Timestamp)
- updatedAt (Timestamp)
```

---

### 5. Cart Service

**Purpose**: Manages shopping carts

**Port**: 8083

**Database**: MongoDB (NoSQL - flexible schema)

**Why MongoDB?**
- Shopping carts change frequently
- Flexible structure (different items, different attributes)
- Fast read/write operations
- No complex relationships needed

**Key Features**:
- Add items to cart
- Update quantities
- Remove items
- Calculate totals
- Clear cart

**API Endpoints**:
```
GET    /api/cart/{userId}           - Get user's cart
POST   /api/cart/items              - Add item to cart
PUT    /api/cart/items/{itemId}     - Update item quantity
DELETE /api/cart/items/{itemId}     - Remove item
DELETE /api/cart/{userId}           - Clear cart
```

**Data Model**:
```
Cart:
- userId (String)
- items (Array):
  - productId (Long)
  - productName (String)
  - quantity (Integer)
  - price (BigDecimal)
- totalAmount (BigDecimal)
- createdAt (Date)
- updatedAt (Date)
```

---

### 6. Order Service

**Purpose**: Processes and manages orders

**Port**: 8084

**Database**: MySQL

**Key Features**:
- Create orders from cart
- Order tracking
- Status updates
- Order history
- Integration with Payment Service

**API Endpoints**:
```
POST   /api/orders               - Create order
GET    /api/orders               - List user's orders
GET    /api/orders/{id}          - Get order details
PUT    /api/orders/{id}/status   - Update order status
DELETE /api/orders/{id}          - Cancel order
```

**Order Status Flow**:
```
PENDING → CONFIRMED → PROCESSING → SHIPPED → DELIVERED
                ↓
            CANCELLED
```

**Data Model**:
```
Order:
- id (Long)
- userId (Long)
- items (List<OrderItem>)
- totalAmount (BigDecimal)
- status (OrderStatus)
- shippingAddress (String)
- paymentId (String)
- createdAt (Timestamp)
- updatedAt (Timestamp)
```

---

### 7. Payment Service

**Purpose**: Handles payment processing

**Port**: 8085

**External Integration**: Razorpay Payment Gateway

**Key Features**:
- Create payment orders
- Process payments
- Verify payments
- Handle webhooks
- Refund processing

**API Endpoints**:
```
POST   /api/payments/create      - Create payment order
POST   /api/payments/verify      - Verify payment
POST   /api/payments/webhook     - Handle Razorpay webhook
GET    /api/payments/{id}        - Get payment status
```

**Payment Flow**:
```
1. Order Service requests payment
2. Payment Service creates Razorpay order
3. Customer pays on Razorpay
4. Razorpay sends webhook
5. Payment Service verifies
6. Order Service notified
7. Notification Service sends email
```

---

### 8. Notification Service

**Purpose**: Sends notifications to users

**Port**: 8086

**Integration**: SMTP Email Server

**Key Features**:
- Order confirmation emails
- Payment receipts
- Shipping updates
- Account notifications

**API Endpoints**:
```
POST   /api/notifications/email           - Send email
POST   /api/notifications/order-confirm   - Order confirmation
```

**Email Templates**:
- Order confirmation
- Payment receipt
- Shipping notification
- Account verification

---

## 🔄 Communication Patterns

### 1. Synchronous Communication (REST APIs)

Used for immediate responses:

```
Client → API Gateway → Service → Database
                          ↓
                      Response
```

**Example**: Get product details
- Client requests product
- Product Service queries database
- Returns product immediately

### 2. Service-to-Service Communication

Services communicate directly when needed:

```
Order Service → Payment Service → Razorpay
      ↓
Notification Service
```

**Example**: Creating an order
1. Order Service creates order
2. Calls Payment Service
3. Calls Notification Service

### 3. Service Discovery Pattern

Services find each other dynamically:

```
Service A → Eureka (Where is Service B?)
              ↓
         Returns Service B location
              ↓
Service A → Service B (Direct call)
```

---

## 📊 Data Flow

### Complete Purchase Flow

```
┌─────────┐
│  User   │
└────┬────┘
     │ 1. Register/Login
     ↓
┌─────────────┐
│ User Service│
└────┬────────┘
     │ 2. JWT Token
     ↓
┌─────────────┐
│   Browse    │
│  Products   │
└────┬────────┘
     │ 3. View Products
     ↓
┌──────────────┐
│Product Service│
└────┬─────────┘
     │ 4. Add to Cart
     ↓
┌─────────────┐
│Cart Service │
└────┬────────┘
     │ 5. Checkout
     ↓
┌─────────────┐
│Order Service│
└────┬────────┘
     │ 6. Process Payment
     ↓
┌──────────────┐
│Payment Service│
└────┬─────────┘
     │ 7. Send Confirmation
     ↓
┌──────────────────┐
│Notification Service│
└──────────────────┘
```

---

## 💾 Database Architecture

### Database Per Service Pattern

Each service has its own database - this is a key microservices principle!

**Why?**
- Services are independent
- Can choose best database for the job
- No shared database bottleneck
- Easier to scale

### Database Choices

#### MySQL (Relational)
Used for: User, Product, Order services

**Why?**
- Structured data
- ACID transactions needed
- Complex queries
- Data relationships

#### MongoDB (Document)
Used for: Cart service

**Why?**
- Flexible schema
- Fast reads/writes
- No complex relationships
- Frequently changing data

#### DynamoDB (Key-Value)
Used for: User profiles

**Why?**
- High performance
- Scalable
- Simple key-value lookups
- AWS integration

### Data Consistency

**Challenge**: Data is spread across multiple databases

**Solution**: Eventual Consistency
- Services sync data when needed
- Use events/messages for updates
- Accept small delays in consistency

---

## 🔒 Security Architecture

### Authentication Flow

```
1. User Login
   ↓
2. User Service validates credentials
   ↓
3. JWT Token generated
   ↓
4. Token sent to client
   ↓
5. Client includes token in all requests
   ↓
6. API Gateway validates token
   ↓
7. Request forwarded to service
```

### Security Layers

1. **API Gateway Level**
   - JWT validation
   - Rate limiting
   - IP filtering

2. **Service Level**
   - Input validation
   - SQL injection prevention
   - XSS protection

3. **Database Level**
   - Encrypted passwords (BCrypt)
   - Prepared statements
   - Access control

### JWT Token Structure

```json
{
  "header": {
    "alg": "HS256",
    "typ": "JWT"
  },
  "payload": {
    "userId": "123",
    "username": "john@example.com",
    "roles": ["USER"],
    "exp": 1234567890
  },
  "signature": "..."
}
```

---

## 📈 Scalability & Performance

### Horizontal Scaling

Can run multiple instances of any service:

```
        ┌─── Product Service Instance 1
Gateway ├─── Product Service Instance 2
        └─── Product Service Instance 3
```

### Load Balancing

API Gateway distributes requests:
- Round-robin
- Least connections
- Response time based

### Caching Strategy

Future enhancement - add Redis:
```
Request → Check Cache → If miss → Database
                ↓
            Return cached data
```

### Database Optimization

- Connection pooling (HikariCP)
- Indexed columns
- Query optimization
- Pagination for large datasets

---

## 🚀 Deployment Architecture

### Docker Containers

Each service runs in its own container:

```
┌─────────────────────────────────────┐
│         Docker Host                  │
│  ┌──────────┐  ┌──────────┐        │
│  │ Service  │  │ Service  │        │
│  │    1     │  │    2     │  ...   │
│  └──────────┘  └──────────┘        │
│                                     │
│  ┌──────────┐  ┌──────────┐        │
│  │ MySQL    │  │ MongoDB  │        │
│  └──────────┘  └──────────┘        │
└─────────────────────────────────────┘
```

### Environment-Based Deployment

- **Development**: Docker Compose
- **Staging**: Kubernetes cluster
- **Production**: Kubernetes with auto-scaling

### CI/CD Pipeline

```
Code Push → Jenkins → Build → Test → Docker Build → Deploy
                        ↓
                   SonarQube
                   (Quality Check)
```

---

## 🎯 Design Principles

### 1. Single Responsibility
Each service does one thing well

### 2. Loose Coupling
Services are independent

### 3. High Cohesion
Related functionality grouped together

### 4. API First
Well-defined interfaces

### 5. Fail Fast
Quick error detection

### 6. Graceful Degradation
System works even if parts fail

---

## 📚 Further Reading

- [Microservices Patterns](https://microservices.io/patterns/index.html)
- [Spring Cloud Documentation](https://spring.io/projects/spring-cloud)
- [Docker Best Practices](https://docs.docker.com/develop/dev-best-practices/)
- [Database Per Service Pattern](https://microservices.io/patterns/data/database-per-service.html)

---

**Questions?** Open an issue or check our [README.md](README.md)
