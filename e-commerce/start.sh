#!/bin/bash

echo "=========================================="
echo "E-Commerce Microservices Platform"
echo "=========================================="
echo ""

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker is not running. Please start Docker and try again."
    exit 1
fi

echo "✅ Docker is running"
echo ""

# Check if .env file exists
if [ ! -f .env ]; then
    echo "⚠️  .env file not found. Creating from .env.example..."
    cp .env.example .env
    echo "📝 Please edit .env file with your email credentials for notifications"
fi

echo "🔨 Building all services..."
./mvnw clean package -DskipTests

if [ $? -ne 0 ]; then
    echo "❌ Build failed. Please check the errors above."
    exit 1
fi

echo "✅ Build successful"
echo ""

echo "🚀 Starting all services with Docker Compose..."
docker-compose up -d

if [ $? -ne 0 ]; then
    echo "❌ Failed to start services. Please check the errors above."
    exit 1
fi

echo ""
echo "✅ All services are starting..."
echo ""
echo "⏳ Waiting for services to be ready (this may take 1-2 minutes)..."
sleep 30

echo ""
echo "=========================================="
echo "Service URLs:"
echo "=========================================="
echo "🌐 API Gateway:        http://localhost:8080"
echo "🔍 Eureka Dashboard:   http://localhost:8761"
echo "👤 User Service:       http://localhost:8081"
echo "📦 Product Service:    http://localhost:8082"
echo "🛒 Cart Service:       http://localhost:8083"
echo "📋 Order Service:      http://localhost:8084"
echo "💳 Payment Service:    http://localhost:8085"
echo "📧 Notification:       http://localhost:8086"
echo ""
echo "=========================================="
echo "Quick Commands:"
echo "=========================================="
echo "View logs:           docker-compose logs -f"
echo "Stop services:       docker-compose down"
echo "Restart service:     docker-compose restart <service-name>"
echo "Check status:        docker-compose ps"
echo ""
echo "=========================================="
echo "Next Steps:"
echo "=========================================="
echo "1. Wait for all services to register with Eureka"
echo "2. Check Eureka dashboard: http://localhost:8761"
echo "3. Test APIs through API Gateway: http://localhost:8080"
echo "4. See README.md for API documentation"
echo ""
echo "✨ Happy coding!"
