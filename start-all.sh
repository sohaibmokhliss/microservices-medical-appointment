#!/bin/bash

echo "========================================"
echo "Healthcare System - Setup & Start"
echo "========================================"
echo ""

# Configuration
DB_USER="postgres"
DB_PASSWORD="postgres"
DOCTEUR_DB="docteurdb"
RDV_DB="rdvdb"

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
sudo -u postgres psql -tc "SELECT 1 FROM pg_database WHERE datname = '$DOCTEUR_DB'" | grep -q 1 || sudo -u postgres psql -c "CREATE DATABASE $DOCTEUR_DB;"
sudo -u postgres psql -tc "SELECT 1 FROM pg_database WHERE datname = '$RDV_DB'" | grep -q 1 || sudo -u postgres psql -c "CREATE DATABASE $RDV_DB;"
print_status "Databases ready ($DOCTEUR_DB, $RDV_DB)"

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
sudo rm -rf docteur-service/target rdv-service/target notification-service/target
print_status "Build directories cleaned"

# Check ports
check_port 8081
check_port 8082
check_port 8083
check_port 3000

echo ""

# ========================================
# START SERVICES
# ========================================
echo "Starting services..."
echo ""

# Start Docteur Service
echo "Starting Docteur Service (Port 8081)..."
cd docteur-service
mvn spring-boot:run > ../logs/docteur-service.log 2>&1 &
DOCTEUR_PID=$!
cd ..
print_status "Docteur Service started (PID: $DOCTEUR_PID)"

sleep 5

# Start RDV Service
echo "Starting RDV Service (Port 8082)..."
cd rdv-service
mvn spring-boot:run > ../logs/rdv-service.log 2>&1 &
RDV_PID=$!
cd ..
print_status "RDV Service started (PID: $RDV_PID)"

sleep 5

# Start Notification Service
echo "Starting Notification Service (Port 8083)..."
cd notification-service
mvn spring-boot:run > ../logs/notification-service.log 2>&1 &
NOTIFICATION_PID=$!
cd ..
print_status "Notification Service started (PID: $NOTIFICATION_PID)"

sleep 5

# Start Frontend
echo "Starting React Frontend (Port 3000)..."
cd frontend
npm start > ../logs/frontend.log 2>&1 &
FRONTEND_PID=$!
cd ..
print_status "Frontend started (PID: $FRONTEND_PID)"

echo ""
echo "========================================"
echo "All services started successfully!"
echo "========================================"
echo ""
echo "Service URLs:"
echo "  - Frontend:             http://localhost:3000"
echo "  - Docteur Service:      http://localhost:8081/api/docteurs"
echo "  - RDV Service:          http://localhost:8082/api/rdv"
echo "  - Notification Service: http://localhost:8083/api/notifications"
echo ""
echo "Database Connection (psql):"
echo "  psql -U $DB_USER -d $DOCTEUR_DB"
echo "  psql -U $DB_USER -d $RDV_DB"
echo ""
echo "DBeaver Connection:"
echo "  Host:     localhost"
echo "  Port:     5432"
echo "  Database: $DOCTEUR_DB or $RDV_DB"
echo "  Username: $DB_USER"
echo "  Password: $DB_PASSWORD"
echo ""
echo "Process IDs:"
echo "  - Docteur Service:      $DOCTEUR_PID"
echo "  - RDV Service:          $RDV_PID"
echo "  - Notification Service: $NOTIFICATION_PID"
echo "  - Frontend:             $FRONTEND_PID"
echo ""
echo "To stop all services:"
echo "  kill $DOCTEUR_PID $RDV_PID $NOTIFICATION_PID $FRONTEND_PID"
echo ""
echo "Logs: ./logs/"
echo ""
