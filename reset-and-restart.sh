#!/bin/bash

echo "🔄 Resetting auth database and restarting auth-service..."
echo ""

# Database credentials
DB_USER="postgres"

# Step 1: Stop auth-service
echo "1. Stopping auth-service..."
pkill -f "auth-service" 2>/dev/null
sleep 2

# Step 2: Drop and recreate database
echo "2. Dropping authdb database..."
psql -U $DB_USER -c "DROP DATABASE IF EXISTS authdb;" 2>/dev/null

echo "3. Creating authdb database..."
psql -U $DB_USER -c "CREATE DATABASE authdb;" 2>/dev/null

echo ""
echo "✅ Database reset complete!"
echo ""
echo "4. Starting auth-service..."
cd auth-service
mvn spring-boot:run > /tmp/auth-service.log 2>&1 &
AUTH_PID=$!

echo "   Auth-service starting (PID: $AUTH_PID)..."
echo "   Waiting for service to initialize..."
sleep 5

echo ""
echo "✅ Setup complete!"
echo ""
echo "📝 Default accounts created:"
echo "   Admin:        username=admin    password=admin123"
echo "   Receptionist: username=sohaib   password=root1312"
echo "   Receptionist: username=othmane  password=root1312"
echo ""
echo "🌐 Go to http://localhost:3000 and login as admin"
echo ""
echo "💡 To view auth-service logs: tail -f /tmp/auth-service.log"
