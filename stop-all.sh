#!/bin/bash

echo "========================================"
echo "Healthcare System - Stop All Services"
echo "========================================"
echo ""

# Colors
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

# Stop Java processes
echo "Stopping Java services..."
pkill -f "eureka-server" && print_status "Eureka Server stopped" || print_warning "Eureka Server not running"
pkill -f "api-gateway" && print_status "API Gateway stopped" || print_warning "API Gateway not running"
pkill -f "auth-service" && print_status "Auth Service stopped" || print_warning "Auth Service not running"
pkill -f "docteur-service" && print_status "Docteur Service stopped" || print_warning "Docteur Service not running"
pkill -f "rdv-service" && print_status "RDV Service stopped" || print_warning "RDV Service not running"
pkill -f "notification-service" && print_status "Notification Service stopped" || print_warning "Notification Service not running"
pkill -f "billing-service" && print_status "Billing Service stopped" || print_warning "Billing Service not running"

# Stop Node.js (frontend)
echo ""
echo "Stopping Frontend..."
pkill -f "react-scripts start" && print_status "Frontend stopped" || print_warning "Frontend not running"
pkill -f "node.*frontend" && print_status "Frontend processes stopped" || print_warning "No frontend processes found"

# Stop RabbitMQ (optional - you can keep it running)
echo ""
echo "Stopping RabbitMQ..."
read -p "Do you want to stop RabbitMQ container? (y/n): " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    if docker ps --format '{{.Names}}' | grep -q "^rabbitmq$"; then
        docker stop rabbitmq
        print_status "RabbitMQ container stopped"
    else
        print_warning "RabbitMQ container not running"
    fi
else
    print_status "RabbitMQ container kept running"
fi

echo ""
echo "========================================"
echo "All services stopped!"
echo "========================================"
echo ""
echo "To restart services, run: ./start-all.sh"
echo ""
