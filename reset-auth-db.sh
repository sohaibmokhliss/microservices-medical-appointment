#!/bin/bash

echo "Resetting authentication database..."

# Database credentials
DB_USER="postgres"

# Drop and recreate authdb database
echo "Dropping authdb database..."
psql -U $DB_USER -c "DROP DATABASE IF EXISTS authdb;"

echo "Creating authdb database..."
psql -U $DB_USER -c "CREATE DATABASE authdb;"

echo "✓ Authentication database has been reset!"
echo ""
echo "When you restart the auth-service, it will create:"
echo "  - Username: admin    Password: admin123    Role: ADMIN"
echo "  - Username: sohaib   Password: root1312    Role: RECEPTIONIST"
echo "  - Username: othmane  Password: root1312    Role: RECEPTIONIST"
