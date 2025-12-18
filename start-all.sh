#!/bin/bash

echo "========================================"
echo "Healthcare System - Setup & Start"
echo "========================================"
echo ""

# Configuration
DB_USER="postgres"
DB_PASSWORD="postgres"
AUTH_DB="authdb"
DOCTEUR_DB="docteurdb"
RDV_DB="rdvdb"
BILLING_DB="billingdb"
RABBITMQ_CONTAINER="rabbitmq"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

print_status() {
    echo -e "${GREEN}[OK]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Function to check if a port is in use
check_port() {
    if lsof -Pi :$1 -sTCP:LISTEN -t >/dev/null 2>&1 ; then
        print_warning "Port $1 is already in use"
        return 1
    fi
    return 0
}

# ========================================
# DEPENDENCY CHECKS
# ========================================
echo "Checking dependencies..."
echo ""

# Check Java
if command -v java &> /dev/null; then
    print_status "Java installed"
else
    print_error "Java is not installed. Please install JDK 17+"
    echo "  Arch Linux: sudo pacman -S jdk-openjdk"
    exit 1
fi

# Check Maven
if command -v mvn &> /dev/null; then
    print_status "Maven installed"
else
    print_error "Maven is not installed"
    echo "  Arch Linux: sudo pacman -S maven"
    exit 1
fi

# Check Node.js
if command -v node &> /dev/null; then
    print_status "Node.js installed"
else
    print_error "Node.js is not installed"
    echo "  Arch Linux: sudo pacman -S nodejs npm"
    exit 1
fi

# Check PostgreSQL
if command -v psql &> /dev/null; then
    print_status "PostgreSQL installed"
else
    print_error "PostgreSQL is not installed"
    echo "  Arch Linux: sudo pacman -S postgresql"
    exit 1
fi

# Check Docker
if ! command -v docker &> /dev/null; then
    print_error "Docker is not installed"
    echo "  Arch Linux: sudo pacman -S docker"
    echo "  Or visit: https://docs.docker.com/get-docker/"
    exit 1
fi

echo ""

# ========================================
# RABBITMQ SETUP
# ========================================
echo "Setting up RabbitMQ..."

# Check if RabbitMQ container exists and is running
if docker ps -a --format '{{.Names}}' | grep -q "^${RABBITMQ_CONTAINER}$"; then
    if docker ps --format '{{.Names}}' | grep -q "^${RABBITMQ_CONTAINER}$"; then
        print_status "RabbitMQ container already running"
    else
        echo "Starting existing RabbitMQ container..."
        docker start $RABBITMQ_CONTAINER
        print_status "RabbitMQ container started"
        sleep 10
    fi
else
    echo "Creating and starting RabbitMQ container..."
    docker run -d \
        --name $RABBITMQ_CONTAINER \
        -p 5672:5672 \
        -p 15672:15672 \
        rabbitmq:3-management
    print_status "RabbitMQ container created and started"
    echo "Waiting for RabbitMQ to be ready (60 seconds)..."
    sleep 60
fi

# Verify RabbitMQ is accessible
if docker exec $RABBITMQ_CONTAINER rabbitmqctl status > /dev/null 2>&1; then
    print_status "RabbitMQ is ready"
    echo "  - Management UI: http://localhost:15672 (guest/guest)"
    echo "  - AMQP Port: 5672"
else
    print_warning "RabbitMQ might still be starting up..."
fi

echo ""

# ========================================
# POSTGRESQL SETUP
# ========================================
echo "Setting up PostgreSQL..."

# Start PostgreSQL if not running
if ! systemctl is-active --quiet postgresql; then
    echo "Starting PostgreSQL service..."
    sudo systemctl start postgresql
    sudo systemctl enable postgresql
fi
print_status "PostgreSQL service running"

# Set postgres password
sudo -u postgres psql -c "ALTER USER $DB_USER PASSWORD '$DB_PASSWORD';" 2>/dev/null
print_status "PostgreSQL password configured"

# Create databases
sudo -u postgres psql -tc "SELECT 1 FROM pg_database WHERE datname = '$AUTH_DB'" | grep -q 1 || sudo -u postgres psql -c "CREATE DATABASE $AUTH_DB;"
sudo -u postgres psql -tc "SELECT 1 FROM pg_database WHERE datname = '$DOCTEUR_DB'" | grep -q 1 || sudo -u postgres psql -c "CREATE DATABASE $DOCTEUR_DB;"
sudo -u postgres psql -tc "SELECT 1 FROM pg_database WHERE datname = '$RDV_DB'" | grep -q 1 || sudo -u postgres psql -c "CREATE DATABASE $RDV_DB;"
sudo -u postgres psql -tc "SELECT 1 FROM pg_database WHERE datname = '$BILLING_DB'" | grep -q 1 || sudo -u postgres psql -c "CREATE DATABASE $BILLING_DB;"
print_status "Databases ready: $AUTH_DB, $DOCTEUR_DB, $RDV_DB, $BILLING_DB"

echo ""

# ========================================
# FRONTEND SETUP
# ========================================
echo "Checking frontend dependencies..."

if [ ! -d "frontend/node_modules" ]; then
    echo "Installing frontend dependencies..."
    cd frontend
    npm install
    cd ..
    print_status "Frontend dependencies installed"
else
    print_status "Frontend dependencies already installed"
fi

echo ""

# ========================================
# PRE-START CLEANUP
# ========================================
echo "Preparing to start services..."

# Create logs directory
mkdir -p logs
print_status "Logs directory ready"

# Clean target directories to avoid permission issues
sudo rm -rf eureka-server/target api-gateway/target auth-service/target docteur-service/target rdv-service/target notification-service/target billing-service/target
print_status "Build directories cleaned"

# Check ports
check_port 8761
check_port 8080
check_port 8084
check_port 8081
check_port 8082
check_port 8083
check_port 8085
check_port 3000

echo ""

# ========================================
# START SERVICES
# ========================================
echo "Starting services..."
echo ""

# Start Eureka Server (must start first for service discovery)
echo "Starting Eureka Server on port 8761..."
cd eureka-server
mvn spring-boot:run > ../logs/eureka-server.log 2>&1 &
EUREKA_PID=$!
cd ..
print_status "Eureka Server started - PID: $EUREKA_PID"

echo "Waiting for Eureka Server to be ready..."
sleep 10

# Start API Gateway
echo "Starting API Gateway on port 8080..."
cd api-gateway
mvn spring-boot:run > ../logs/api-gateway.log 2>&1 &
GATEWAY_PID=$!
cd ..
print_status "API Gateway started - PID: $GATEWAY_PID"

sleep 5

# Start Auth Service
echo "Starting Auth Service on port 8084..."
cd auth-service
mvn spring-boot:run > ../logs/auth-service.log 2>&1 &
AUTH_PID=$!
cd ..
print_status "Auth Service started - PID: $AUTH_PID"

sleep 5

# Start Docteur Service
echo "Starting Docteur Service on port 8081..."
cd docteur-service
mvn spring-boot:run > ../logs/docteur-service.log 2>&1 &
DOCTEUR_PID=$!
cd ..
print_status "Docteur Service started - PID: $DOCTEUR_PID"

sleep 5

# Start RDV Service
echo "Starting RDV Service on port 8082..."
cd rdv-service
mvn spring-boot:run > ../logs/rdv-service.log 2>&1 &
RDV_PID=$!
cd ..
print_status "RDV Service started - PID: $RDV_PID"

sleep 5

# Start Notification Service
echo "Starting Notification Service on port 8083..."
cd notification-service
mvn spring-boot:run > ../logs/notification-service.log 2>&1 &
NOTIFICATION_PID=$!
cd ..
print_status "Notification Service started - PID: $NOTIFICATION_PID"

sleep 5

# Start Billing Service
echo "Starting Billing Service on port 8085..."
cd billing-service
mvn spring-boot:run > ../logs/billing-service.log 2>&1 &
BILLING_PID=$!
cd ..
print_status "Billing Service started - PID: $BILLING_PID"

sleep 5

# Start Frontend
echo "Starting React Frontend on port 3000..."
cd frontend
npm start > ../logs/frontend.log 2>&1 &
FRONTEND_PID=$!
cd ..
print_status "Frontend started - PID: $FRONTEND_PID"

echo ""
echo "========================================"
echo "All services started successfully!"
echo "========================================"
echo ""
echo "Service URLs:"
echo "  - Eureka Dashboard:     http://localhost:8761"
echo "  - API Gateway:          http://localhost:8080/api"
echo "  - Frontend:             http://localhost:3000"
echo "  - RabbitMQ UI:          http://localhost:15672 (guest/guest)"
echo ""
echo "Gateway Routes (Recommended):"
echo "  - Auth Service:         http://localhost:8080/api/auth"
echo "  - Docteur Service:      http://localhost:8080/api/docteurs"
echo "  - RDV Service:          http://localhost:8080/api/rdv"
echo "  - Notification Service: http://localhost:8080/api/notifications"
echo "  - Billing Service:      http://localhost:8080/api/billing"
echo ""
echo "Direct Service URLs (For debugging):"
echo "  - Auth Service:         http://localhost:8084"
echo "  - Docteur Service:      http://localhost:8081"
echo "  - RDV Service:          http://localhost:8082"
echo "  - Notification Service: http://localhost:8083"
echo "  - Billing Service:      http://localhost:8085"
echo ""
echo "Database Connection:"
echo "  Host:     localhost"
echo "  Port:     5432"
echo "  Database: $AUTH_DB, $DOCTEUR_DB, $RDV_DB, $BILLING_DB"
echo "  Username: $DB_USER"
echo "  Password: $DB_PASSWORD"
echo ""
echo "Process IDs:"
echo "  - Eureka Server:        $EUREKA_PID"
echo "  - API Gateway:          $GATEWAY_PID"
echo "  - Auth Service:         $AUTH_PID"
echo "  - Docteur Service:      $DOCTEUR_PID"
echo "  - RDV Service:          $RDV_PID"
echo "  - Notification Service: $NOTIFICATION_PID"
echo "  - Billing Service:      $BILLING_PID"
echo "  - Frontend:             $FRONTEND_PID"
echo ""
echo "Logs: ./logs/"
echo ""
echo "To stop all services, run: ./stop-all.sh"
echo "To check service status, run: ./check-health.sh"
echo ""
