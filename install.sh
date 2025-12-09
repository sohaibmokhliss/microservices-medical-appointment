#!/bin/bash

echo "========================================"
echo "Healthcare System Installation Script"
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
NC='\033[0m' # No Color

print_status() {
    echo -e "${GREEN}[OK]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check for required dependencies
echo "Checking dependencies..."
echo ""

# Check Java
if command -v java &> /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | head -n 1)
    print_status "Java installed: $JAVA_VERSION"
else
    print_error "Java is not installed. Please install JDK 17+"
    echo "  Arch Linux: sudo pacman -S jdk-openjdk"
    exit 1
fi

# Check Maven
if command -v mvn &> /dev/null; then
    MVN_VERSION=$(mvn -version 2>&1 | head -n 1)
    print_status "Maven installed: $MVN_VERSION"
else
    print_error "Maven is not installed. Please install Maven"
    echo "  Arch Linux: sudo pacman -S maven"
    exit 1
fi

# Check Node.js
if command -v node &> /dev/null; then
    NODE_VERSION=$(node -v)
    print_status "Node.js installed: $NODE_VERSION"
else
    print_error "Node.js is not installed. Please install Node.js"
    echo "  Arch Linux: sudo pacman -S nodejs npm"
    exit 1
fi

# Check npm
if command -v npm &> /dev/null; then
    NPM_VERSION=$(npm -v)
    print_status "npm installed: $NPM_VERSION"
else
    print_error "npm is not installed. Please install npm"
    exit 1
fi

# Check PostgreSQL
if command -v psql &> /dev/null; then
    PSQL_VERSION=$(psql --version)
    print_status "PostgreSQL installed: $PSQL_VERSION"
else
    print_error "PostgreSQL is not installed. Please install PostgreSQL"
    echo "  Arch Linux: sudo pacman -S postgresql"
    exit 1
fi

echo ""
echo "========================================"
echo "Setting up PostgreSQL..."
echo "========================================"
echo ""

# Check if PostgreSQL service is running
if ! systemctl is-active --quiet postgresql; then
    echo "Starting PostgreSQL service..."
    sudo systemctl start postgresql
    sudo systemctl enable postgresql
fi
print_status "PostgreSQL service is running"

# Initialize PostgreSQL if needed (Arch Linux specific)
if [ ! -d "/var/lib/postgres/data" ] || [ -z "$(ls -A /var/lib/postgres/data 2>/dev/null)" ]; then
    echo "Initializing PostgreSQL database cluster..."
    sudo -u postgres initdb -D /var/lib/postgres/data
    sudo systemctl restart postgresql
fi

# Set postgres user password
echo "Setting PostgreSQL password for user '$DB_USER'..."
sudo -u postgres psql -c "ALTER USER $DB_USER PASSWORD '$DB_PASSWORD';" 2>/dev/null
print_status "PostgreSQL password set"

# Create databases
echo "Creating databases..."

sudo -u postgres psql -tc "SELECT 1 FROM pg_database WHERE datname = '$DOCTEUR_DB'" | grep -q 1
if [ $? -ne 0 ]; then
    sudo -u postgres psql -c "CREATE DATABASE $DOCTEUR_DB;"
    print_status "Database '$DOCTEUR_DB' created"
else
    print_status "Database '$DOCTEUR_DB' already exists"
fi

sudo -u postgres psql -tc "SELECT 1 FROM pg_database WHERE datname = '$RDV_DB'" | grep -q 1
if [ $? -ne 0 ]; then
    sudo -u postgres psql -c "CREATE DATABASE $RDV_DB;"
    print_status "Database '$RDV_DB' created"
else
    print_status "Database '$RDV_DB' already exists"
fi

echo ""
echo "========================================"
echo "Installing frontend dependencies..."
echo "========================================"
echo ""

cd frontend
npm install
if [ $? -eq 0 ]; then
    print_status "Frontend dependencies installed"
else
    print_error "Failed to install frontend dependencies"
    exit 1
fi
cd ..

echo ""
echo "========================================"
echo "Building backend services..."
echo "========================================"
echo ""

# Build Docteur Service
echo "Building Docteur Service..."
cd docteur-service
mvn clean compile -q
if [ $? -eq 0 ]; then
    print_status "Docteur Service built successfully"
else
    print_error "Failed to build Docteur Service"
    exit 1
fi
cd ..

# Build RDV Service
echo "Building RDV Service..."
cd rdv-service
mvn clean compile -q
if [ $? -eq 0 ]; then
    print_status "RDV Service built successfully"
else
    print_error "Failed to build RDV Service"
    exit 1
fi
cd ..

# Build Notification Service
echo "Building Notification Service..."
cd notification-service
mvn clean compile -q
if [ $? -eq 0 ]; then
    print_status "Notification Service built successfully"
else
    print_error "Failed to build Notification Service"
    exit 1
fi
cd ..

# Create logs directory
mkdir -p logs
print_status "Logs directory created"

echo ""
echo "========================================"
echo "Installation Complete!"
echo "========================================"
echo ""
echo "Configuration Summary:"
echo "  - PostgreSQL User: $DB_USER"
echo "  - PostgreSQL Password: $DB_PASSWORD"
echo "  - Docteur Database: $DOCTEUR_DB"
echo "  - RDV Database: $RDV_DB"
echo ""
echo "Service Ports:"
echo "  - Docteur Service:      http://localhost:8081"
echo "  - RDV Service:          http://localhost:8082"
echo "  - Notification Service: http://localhost:8083"
echo "  - Frontend:             http://localhost:3000"
echo ""
echo "To start all services, run:"
echo "  ./start-all.sh"
echo ""
echo "To connect to databases:"
echo "  psql -U $DB_USER -d $DOCTEUR_DB"
echo "  psql -U $DB_USER -d $RDV_DB"
echo ""
echo "DBeaver Connection Settings:"
echo "  Host:     localhost"
echo "  Port:     5432"
echo "  Database: $DOCTEUR_DB (for doctors) or $RDV_DB (for appointments)"
echo "  Username: $DB_USER"
echo "  Password: $DB_PASSWORD"
echo ""
