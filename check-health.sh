#!/bin/bash

echo "========================================"
echo "Healthcare System - Health Check"
echo "========================================"
echo ""

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

print_up() {
    echo -e "${GREEN}✓${NC} $1"
}

print_down() {
    echo -e "${RED}✗${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}⚠${NC} $1"
}

# Function to check HTTP endpoint
check_http() {
    if curl -s -o /dev/null -w "%{http_code}" "$1" | grep -q "200\|401\|404"; then
        return 0
    else
        return 1
    fi
}

# Function to check if process is running
check_process() {
    if pgrep -f "$1" > /dev/null 2>&1; then
        return 0
    else
        return 1
    fi
}

echo "Checking Services..."
echo ""

# Check RabbitMQ
echo -n "RabbitMQ Container:       "
if docker ps --format '{{.Names}}' | grep -q "^rabbitmq$"; then
    print_up "Running (http://localhost:15672)"
else
    print_down "Not running (required for async notifications)"
    echo "  Start with: docker start rabbitmq"
    echo "  Or: docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3-management"
fi

echo ""
echo "Checking Java Services..."
echo ""

# Check Eureka Server
echo -n "Eureka Server (8761):     "
if check_http "http://localhost:8761"; then
    print_up "UP (http://localhost:8761)"
else
    print_down "DOWN"
fi

# Check API Gateway
echo -n "API Gateway (8080):       "
if check_http "http://localhost:8080"; then
    print_up "UP (http://localhost:8080)"
else
    print_down "DOWN"
fi

# Check Auth Service
echo -n "Auth Service (8084):      "
if check_http "http://localhost:8084/actuator/health"; then
    print_up "UP (http://localhost:8084)"
else
    print_down "DOWN"
fi

# Check Docteur Service
echo -n "Docteur Service (8081):   "
if check_http "http://localhost:8081/actuator/health"; then
    print_up "UP (http://localhost:8081)"
else
    print_down "DOWN"
fi

# Check RDV Service
echo -n "RDV Service (8082):       "
if check_http "http://localhost:8082/actuator/health"; then
    print_up "UP (http://localhost:8082)"
else
    print_down "DOWN"
fi

# Check Notification Service
echo -n "Notification Service (8083): "
if check_http "http://localhost:8083/actuator/health"; then
    print_up "UP (http://localhost:8083)"
else
    print_down "DOWN"
fi

echo ""
echo "Checking Frontend..."
echo ""

# Check Frontend
echo -n "React Frontend (3000):    "
if check_http "http://localhost:3000"; then
    print_up "UP (http://localhost:3000)"
else
    print_down "DOWN"
fi

echo ""
echo "Checking PostgreSQL..."
echo ""

# Check PostgreSQL
echo -n "PostgreSQL:               "
if systemctl is-active --quiet postgresql 2>/dev/null || pg_isready -q 2>/dev/null; then
    print_up "Running"
    
    # Check databases
    echo -n "  - authdb:               "
    if psql -U postgres -lqt | cut -d \| -f 1 | grep -qw authdb 2>/dev/null; then
        print_up "EXISTS"
    else
        print_warning "NOT FOUND"
    fi
    
    echo -n "  - docteurdb:            "
    if psql -U postgres -lqt | cut -d \| -f 1 | grep -qw docteurdb 2>/dev/null; then
        print_up "EXISTS"
    else
        print_warning "NOT FOUND"
    fi
    
    echo -n "  - rdvdb:                "
    if psql -U postgres -lqt | cut -d \| -f 1 | grep -qw rdvdb 2>/dev/null; then
        print_up "EXISTS"
    else
        print_warning "NOT FOUND"
    fi
else
    print_down "Not running"
fi

echo ""
echo "Checking Actuator Endpoints..."
echo ""

# Circuit Breakers
echo -n "Circuit Breakers:         "
if check_http "http://localhost:8082/actuator/circuitbreakers"; then
    print_up "http://localhost:8082/actuator/circuitbreakers"
else
    print_down "Not available"
fi

# Health endpoints
echo -n "Health Checks:            "
if check_http "http://localhost:8082/actuator/health"; then
    print_up "http://localhost:808X/actuator/health (X=1,2,3,4)"
else
    print_down "Not available"
fi

echo ""
echo "========================================"
echo "Summary"
echo "========================================"

# Count services
SERVICES_UP=0
SERVICES_DOWN=0

for port in 8761 8080 8084 8081 8082 8083 3000; do
    if lsof -Pi :$port -sTCP:LISTEN -t >/dev/null 2>&1; then
        ((SERVICES_UP++))
    else
        ((SERVICES_DOWN++))
    fi
done

echo "Services UP:   $SERVICES_UP / 7"
echo "Services DOWN: $SERVICES_DOWN / 7"

if [ $SERVICES_DOWN -eq 0 ]; then
    echo ""
    print_up "All systems operational!"
    echo ""
    echo "Quick Links:"
    echo "  - Frontend:    http://localhost:3000"
    echo "  - Eureka:      http://localhost:8761"
    echo "  - RabbitMQ:    http://localhost:15672 (guest/guest)"
    echo "  - API Gateway: http://localhost:8080/api"
else
    echo ""
    print_warning "Some services are not running"
    echo ""
    echo "To start all services, run: ./start-all.sh"
fi

echo ""
