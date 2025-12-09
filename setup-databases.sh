#!/bin/bash

echo "Setting up PostgreSQL databases for Healthcare Appointment System..."

# Database credentials
DB_USER="postgres"
DB_PASS="postgres"

# Create authdb database
echo "Creating authdb database..."
psql -U $DB_USER -c "CREATE DATABASE authdb;" 2>/dev/null || echo "authdb already exists"

# Create docteurdb database
echo "Creating docteurdb database..."
psql -U $DB_USER -c "CREATE DATABASE docteurdb;" 2>/dev/null || echo "docteurdb already exists"

# Create rdvdb database
echo "Creating rdvdb database..."
psql -U $DB_USER -c "CREATE DATABASE rdvdb;" 2>/dev/null || echo "rdvdb already exists"

echo "✓ Database setup complete!"
echo ""
echo "Databases created:"
echo "  - authdb (for authentication service)"
echo "  - docteurdb (for doctor service)"
echo "  - rdvdb (for appointment service)"
