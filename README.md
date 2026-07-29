# 🛒 E-Commerce Microservices Platform

> A modern, scalable e-commerce platform built with microservices architecture - like having multiple specialized shops working together instead of one giant store!

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.2-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Docker](https://img.shields.io/badge/Docker-Ready-blue.svg)](https://www.docker.com/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

---

## 📖 Table of Contents

- [What is This Project?](#what-is-this-project)
- [Why Microservices?](#why-microservices)
- [System Architecture](#system-architecture)
- [How It Works](#how-it-works)
- [Quick Start](#quick-start)
- [Detailed Setup Guide](#detailed-setup-guide)
- [Testing the APIs](#testing-the-apis)
- [Project Structure](#project-structure)
- [Technologies Used](#technologies-used)
- [CI/CD Pipeline](#cicd-pipeline)
- [Troubleshooting](#troubleshooting)
- [Contributing](#contributing)

---

## 🎯 What is This Project?

Imagine you're building an online shopping website like Amazon or Flipkart. Instead of creating one massive application that does everything, we've broken it down into **8 smaller, specialized services** that work together. Each service has one specific job, making the system:

- **Easier to maintain** - Fix one service without touching others
- **Scalable** - Scale only the parts that need more power
- **Reliable** - If one service fails, others keep working
- **Team-friendly** - Different teams can work on different services

### Real-World Analogy

Think of a shopping mall:
- **Service Discovery** = Mall Directory (helps you find shops)
- **API Gateway** = Main Entrance (single entry point)
- **User Service** = Customer Service Desk (handles accounts)
- **Product Service** = Product Catalog (manages inventory)
- **Cart Service** = Shopping Cart (holds your items)
- **Order Service** = Checkout Counter (processes orders)
- **Payment Service** = Payment Terminal (handles payments)
- **Notification Service** = Receipt Printer (sends confirmations)

---

## 🤔 Why Microservices?

### Traditional Approach (Monolithic)
```
┌─────────────────────────────────┐
│                                 │
│    ONE BIG APPLICATION          │
│    (Everything in one place)    │
│                                 │
└─────────────────────────────────┘
```
**Problems:**
- Hard to update (change one thing, test everything)
- Can't scale specific parts
- One bug can crash everything
- Large teams stepping on each other's toes

### Our Approach (Microservices)
```
┌──────────┐  ┌──────────┐  ┌──────────┐
│  User    │  │ Product  │  │  Cart    │
│ Service  │  │ Service  │  │ Service  │
└──────────┘  └──────────┘  └──────────┘
     ↓             ↓             ↓
┌──────────┐  ┌──────────┐  ┌──────────┐
│  Order   │  │ Payment  │  │  Notify  │
│ Service  │  │ Service  │  │ Service  │
└──────────┘  └──────────┘  └──────────┘
```
**Benefits:**
- Update one service independently
- Scale only what needs scaling
- Isolated failures
- Teams work independently

---

## 🏗️ System Architecture

### High-Level Overview

```
                    ┌─────────────────┐
                    │   Web Browser   │
                    │   Mobile App    │
                    └────────┬────────┘
                             │
                             ↓
                    ┌─────────────────┐
                    │   API Gateway   │ ← Single Entry Point
                    │   (Port 8080)   │
                    └────────┬────────┘
                             │
        ┌────────────────────┼────────────────────┐
        ↓                    ↓                    ↓
┌──────────────┐    ┌──────────────┐    ┌──────────────┐
│ User Service │    │Product Service│   │ Cart Service │
│  Port 8081   │    │  Port 8082    │   │  Port 8083   │
└──────┬───────┘    └──────┬────────┘   └──────┬───────┘
       │                   │                    │
       ↓                   ↓                    ↓
   [MySQL DB]         [MySQL DB]          [MongoDB]
```

### Complete Architecture Diagram

See [ARCHITECTURE.md](ARCHITECTURE.md) for detailed architecture diagrams and explanations.

---

## 🔄 How It Works

### Example: Customer Buying a Product

Let's walk through what happens when someone buys a product:

#### Step 1: User Logs In
```
Customer → API Gateway → User Service → MySQL
                              ↓
                         JWT Token Created
                              ↓
                         Token Sent Back
```

#### Step 2: Browse Products
```
Customer → API Gateway → Product Service → MySQL
                              ↓
                         Product List Retrieved
                              ↓
                         Products Displayed
```

#### Step 3: Add to Cart
```
Customer → API Gateway → Cart Service → MongoDB
                              ↓
                         Item Added to Cart
                              ↓
                         Cart Updated
```

#### Step 4: Place Order
```
Customer → API Gateway → Order Service → MySQL
                              ↓
                         Order Created
                              ↓
                    Payment Service Called
                              ↓
                    Razorpay Payment Gateway
                              ↓
                    Payment Confirmed
                              ↓
                    Notification Service
                              ↓
                    Email Sent to Customer
```

### Service Communication

Services talk to each other through:
1. **REST APIs** - Like phone calls between services
2. **Service Discovery** - Like a phone book to find services
3. **API Gateway** - Like a receptionist routing calls

---

## 🚀 Quick Start

### Prerequisites

Before you begin, make sure you have:

- **Java 17** - [Download here](https://www.oracle.com/java/technologies/downloads/#java17)
- **Docker** - [Download here](https://www.docker.com/products/docker-desktop)
- **Git** - [Download here](https://git-scm.com/downloads)

### One-Command Start

```bash
# Clone the project
git clone <your-repo-url>
cd e-commerce

# Start everything with Docker
docker-compose up -d

# Wait 2-3 minutes for all services to start
# Check if everything is running
docker-compose ps
```

That's it! Your e-commerce platform is now running! 🎉

### Access Points

- **API Gateway**: http://localhost:8080
- **Eureka Dashboard**: http://localhost:8761
- **User Service**: http://localhost:8081
- **Product Service**: http://localhost:8082
- **Cart Service**: http://localhost:8083
- **Order Service**: http://localhost:8084
- **Payment Service**: http://localhost:8085
- **Notification Service**: http://localhost:8086

---

## 📚 Detailed Setup Guide

### Option 1: Docker (Recommended for Beginners)

Docker packages everything you need into containers - like shipping containers for software!

```bash
# 1. Start all services
docker-compose up -d

# 2. View logs (optional)
docker-compose logs -f

# 3. Stop all services
docker-compose down

# 4. Stop and remove all data
docker-compose down -v
```

### Option 2: Manual Setup (For Developers)

#### Step 1: Install Databases

**MySQL** (for User, Product, Order services):
```bash
# Using Docker
docker run -d --name mysql-user -p 3306:3306 \
  -e MYSQL_ROOT_PASSWORD=root \
  -e MYSQL_DATABASE=userdb \
  mysql:8.0

docker run -d --name mysql-product -p 3307:3306 \
  -e MYSQL_ROOT_PASSWORD=root \
  -e MYSQL_DATABASE=productdb \
  mysql:8.0

docker run -d --name mysql-order -p 3308:3306 \
  -e MYSQL_ROOT_PASSWORD=root \
  -e MYSQL_DATABASE=orderdb \
  mysql:8.0
```

**MongoDB** (for Cart service):
```bash
docker run -d --name mongodb -p 27017:27017 mongo:7-jammy
```

**DynamoDB Local** (for User service):
```bash
docker run -d --name dynamodb -p 8000:8000 \
  amazon/dynamodb-local:latest
```

#### Step 2: Build Services

```bash
# Build all services
mvn clean install

# Or build individually
cd service-discovery && mvn clean package
cd ../api-gateway && mvn clean package
cd ../user-service && mvn clean package
# ... and so on
```

#### Step 3: Start Services (in order)

```bash
# 1. Start Service Discovery first
cd service-discovery
java -jar target/service-discovery-1.0.0.jar

# 2. Wait 30 seconds, then start API Gateway
cd ../api-gateway
java -jar target/api-gateway-1.0.0.jar

# 3. Start all other services (any order)
cd ../user-service
java -jar target/user-service-1.0.0.jar

cd ../product-service
java -jar target/product-service-1.0.0.jar

# ... continue for other services
```

---

## 🧪 Testing the APIs

### Using the Provided Test File

We've included an `api-tests.http` file with ready-to-use API calls.

**Using VS Code:**
1. Install "REST Client" extension
2. Open `api-tests.http`
3. Click "Send Request" above any request

**Using IntelliJ IDEA:**
1. Open `api-tests.http`
2. Click the green play button next to any request

### Manual Testing with cURL

#### 1. Register a User
```bash
curl -X POST http://localhost:8080/api/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john@example.com",
    "password": "password123",
    "firstName": "John",
    "lastName": "Doe"
  }'
```

#### 2. Login
```bash
curl -X POST http://localhost:8080/api/users/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john@example.com",
    "password": "password123"
  }'
```

#### 3. Create a Product
```bash
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "name": "Laptop",
    "description": "High-performance laptop",
    "price": 999.99,
    "stockQuantity": 50
  }'
```

#### 4. Add to Cart
```bash
curl -X POST http://localhost:8080/api/cart/items \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "productId": 1,
    "quantity": 2
  }'
```

---

## 📁 Project Structure

```
e-commerce/
├── common/                      # Shared code (exceptions, DTOs)
├── service-discovery/           # Eureka Server (finds services)
├── api-gateway/                 # Entry point for all requests
├── user-service/                # User management & auth
│   ├── src/main/java/          # Java source code
│   ├── src/main/resources/     # Configuration files
│   └── pom.xml                 # Dependencies
├── product-service/             # Product catalog
├── cart-service/                # Shopping cart
├── order-service/               # Order processing
├── payment-service/             # Payment handling
├── notification-service/        # Email notifications
├── jenkins/                     # CI/CD configuration
├── docker-compose.yml           # Docker setup
├── Jenkinsfile                  # Build pipeline
├── README.md                    # This file
├── ARCHITECTURE.md              # Detailed architecture
└── JENKINS_SETUP.md            # CI/CD setup guide
```

---

## 🛠️ Technologies Used

### Backend
- **Java 17** - Programming language
- **Spring Boot 3.2.2** - Application framework
- **Spring Cloud** - Microservices tools
- **Maven** - Build tool

### Databases
- **MySQL 8.0** - Relational database (User, Product, Order)
- **MongoDB 7** - Document database (Cart)
- **DynamoDB** - NoSQL database (User profiles)

### Infrastructure
- **Docker** - Containerization
- **Eureka** - Service discovery
- **Spring Cloud Gateway** - API routing

### External Services
- **Razorpay** - Payment gateway
- **SMTP** - Email sending

### DevOps
- **Jenkins** - CI/CD automation
- **SonarQube** - Code quality
- **GitHub Actions** - Automated workflows

---

## 🔄 CI/CD Pipeline

We've set up automated testing and deployment using Jenkins.

### What Gets Automated

1. **Code Checkout** - Pull latest code
2. **Build** - Compile all services
3. **Test** - Run unit tests
4. **Quality Check** - SonarQube analysis
5. **Docker Build** - Create container images
6. **Deploy** - Push to environment

### Setting Up CI/CD

See [JENKINS_SETUP.md](JENKINS_SETUP.md) for complete instructions.

Quick start:
```bash
cd jenkins
docker-compose -f docker-compose.jenkins.yml up -d
```

Access Jenkins at: http://localhost:8090

---

## 🐛 Troubleshooting

### Services Won't Start

**Problem**: Port already in use
```
Error: Port 8080 is already allocated
```

**Solution**: Stop conflicting services or change ports
```bash
# Find what's using the port
lsof -i :8080

# Kill the process
kill -9 <PID>

# Or change port in application.yml
```

### Database Connection Failed

**Problem**: Can't connect to MySQL
```
Error: Connection refused
```

**Solution**: Ensure databases are running
```bash
# Check if MySQL is running
docker ps | grep mysql

# Restart MySQL
docker restart mysql-user
```

### Service Not Registering with Eureka

**Problem**: Service doesn't appear in Eureka dashboard

**Solution**: 
1. Check if Eureka is running: http://localhost:8761
2. Wait 30-60 seconds for registration
3. Check service logs for errors
```bash
docker logs user-service
```

### Out of Memory Error

**Problem**: Java heap space error

**Solution**: Increase memory allocation
```bash
# In docker-compose.yml, add:
environment:
  - JAVA_OPTS=-Xmx512m -Xms256m
```

---

## 🤝 Contributing

We welcome contributions! Here's how:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Development Guidelines

- Follow Java coding conventions
- Write unit tests for new features
- Update documentation
- Keep commits atomic and meaningful

---

## 🎓 Learning Resources

New to microservices? Check these out:

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Microservices Pattern](https://microservices.io/)
- [Docker Getting Started](https://docs.docker.com/get-started/)
- [REST API Tutorial](https://restfulapi.net/)

---

## 🗺️ Roadmap

Future enhancements planned:

- [ ] Add Kubernetes deployment
- [ ] Implement API rate limiting
- [ ] Add Redis caching
- [ ] Create admin dashboard
- [ ] Add product reviews and ratings
- [ ] Implement real-time order tracking
- [ ] Add GraphQL API
- [ ] Mobile app integration

---

## 👏 Acknowledgments

- Spring Boot team for the amazing framework
- Netflix OSS for Eureka
- Docker for containerization
- All contributors and supporters

---

**Made with ❤️ for learning and building scalable systems**

*Star ⭐ this repo if you find it helpful!*
