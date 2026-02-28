.PHONY: build start stop clean logs test

# Build all services
build:
	./mvnw clean package -DskipTests

# Start all services
start:
	docker-compose up -d

# Stop all services
stop:
	docker-compose down

# Clean and remove volumes
clean:
	docker-compose down -v
	./mvnw clean

# View logs
logs:
	docker-compose logs -f

# Run tests
test:
	./mvnw test

# Restart specific service
restart-user:
	docker-compose restart user-service

restart-product:
	docker-compose restart product-service

restart-cart:
	docker-compose restart cart-service

restart-order:
	docker-compose restart order-service

# Scale services
scale-product:
	docker-compose up -d --scale product-service=3

# Check service health
health:
	@echo "Checking Eureka..."
	@curl -s http://localhost:8761/actuator/health || echo "Eureka not ready"
	@echo "\nChecking API Gateway..."
	@curl -s http://localhost:8080/actuator/health || echo "Gateway not ready"
	@echo "\nChecking User Service..."
	@curl -s http://localhost:8081/actuator/health || echo "User Service not ready"

# Build and start
all: build start
