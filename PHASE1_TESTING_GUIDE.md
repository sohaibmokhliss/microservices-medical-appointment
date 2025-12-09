# Phase 1: Authentication & Authorization - Testing Guide

## Overview

Phase 1 implementation is complete! You now have a fully functional authentication system with JWT tokens and role-based access control.

## What's Been Implemented

✅ **New Auth Service (Port 8084)**
- User registration with password hashing (BCrypt)
- User login with JWT token generation
- Token validation endpoint
- User info endpoint
- PostgreSQL database (authdb)

✅ **API Gateway Security (Port 8080)**
- JWT authentication filter
- Validates tokens on all protected endpoints
- Passes user info to downstream services via headers
- Routes for auth-service configured

✅ **Secured Microservices**
- rdv-service: Protected with Spring Security
- docteur-service: Protected with Spring Security
- notification-service: Protected with Spring Security
- All services extract user info from gateway headers

✅ **Security Improvements**
- Hardcoded API keys externalized to environment variables
- JWT secret configurable via environment variable
- CORS properly configured for frontend

---

## Starting the System

1. **Start all services:**
   ```bash
   ./start-all.sh
   ```

   This will:
   - Create databases (authdb, docteurdb, rdvdb)
   - Start Eureka Server (8761)
   - Start API Gateway (8080)
   - Start Auth Service (8084)
   - Start Docteur Service (8081)
   - Start RDV Service (8082)
   - Start Notification Service (8083)
   - Start Frontend (3000)

2. **Wait for all services to register** with Eureka (check http://localhost:8761)

---

## Testing Authentication Flow

### 1. Register a New User

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "doctor1",
    "password": "password123",
    "email": "doctor1@example.com",
    "role": "DOCTOR"
  }'
```

**Expected Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "username": "doctor1",
  "role": "DOCTOR",
  "message": "User registered successfully"
}
```

**Save the token!** You'll need it for subsequent requests.

### 2. Login with Existing User

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "doctor1",
    "password": "password123"
  }'
```

**Expected Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "username": "doctor1",
  "role": "DOCTOR",
  "message": "Login successful"
}
```

### 3. Access Protected Endpoints

**Without Token (should fail with 401):**
```bash
curl http://localhost:8080/api/docteurs
```

**With Token (should succeed):**
```bash
TOKEN="<your-token-here>"

curl http://localhost:8080/api/docteurs \
  -H "Authorization: Bearer $TOKEN"
```

**Expected Response:** List of doctors

### 4. Get Current User Info

```bash
curl http://localhost:8080/api/auth/me \
  -H "Authorization: Bearer $TOKEN"
```

**Expected Response:**
```json
{
  "id": 1,
  "username": "doctor1",
  "email": "doctor1@example.com",
  "role": "DOCTOR"
}
```

### 5. Create Appointment (Protected)

```bash
curl -X POST http://localhost:8080/api/rdv \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "docteurId": 1,
    "patientNom": "Doe",
    "patientPrenom": "John",
    "patientEmail": "john.doe@example.com",
    "patientTelephone": "+1234567890",
    "dateHeure": "2025-12-01T10:00:00",
    "motif": "Consultation générale"
  }'
```

---

## Testing Different Roles

### Create Admin User

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin1",
    "password": "admin123",
    "email": "admin@healthcare.com",
    "role": "ADMIN"
  }'
```

### Create Doctor User

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "doctor1",
    "password": "doctor123",
    "email": "doctor1@healthcare.com",
    "role": "DOCTOR"
  }'
```

---

## Checking Service Health

All services now have actuator health endpoints:

```bash
# Auth Service
curl http://localhost:8084/actuator/health

# RDV Service
curl http://localhost:8082/actuator/health

# Docteur Service
curl http://localhost:8081/actuator/health

# Notification Service
curl http://localhost:8083/actuator/health
```

---

## Database Inspection

### Connect to authdb

```bash
psql -U postgres -d authdb
```

### View Users Table

```sql
SELECT id, username, email, role, enabled FROM users;
```

---

## Common Issues & Troubleshooting

### Issue: 401 Unauthorized on all requests

**Cause:** Token not provided or invalid

**Solution:**
1. Register/login to get a new token
2. Ensure you're passing `Authorization: Bearer <token>` header
3. Check token hasn't expired (24 hours default)

### Issue: Auth service won't start

**Cause:** Database not created

**Solution:**
```bash
sudo -u postgres psql -c "CREATE DATABASE authdb;"
```

### Issue: Services can't communicate

**Cause:** Not all services registered with Eureka

**Solution:**
1. Check Eureka dashboard: http://localhost:8761
2. Wait 30 seconds for registration
3. Restart services if needed

### Issue: CORS errors in frontend

**Cause:** Frontend not configured to use API Gateway

**Solution:** Frontend needs to be updated to:
1. Route all requests through http://localhost:8080
2. Include JWT token in Authorization header
3. Store token in localStorage after login

---

## Next Steps (Phase 2)

After testing Phase 1, you can proceed to:

**Phase 2: Resilience Patterns (Day 3)**
- Add Circuit Breaker to rdv-service → docteur-service calls
- Implement retry logic with exponential backoff
- Add fallback mechanisms
- Configure health checks

**Phase 3: Async Communication with RabbitMQ (Days 4-5)**
- Install RabbitMQ
- Configure event-driven notification triggering
- Decouple services with message queues

---

## Architecture Diagram (Current State)

```
Frontend (React)
      ↓
API Gateway (8080) [JWT Filter]
      ↓
   Eureka Server (8761)
      ↓
   ┌──────┴──────┬──────────┬──────────────┐
   ↓             ↓          ↓              ↓
Auth Service  Docteur   RDV Service   Notification
  (8084)      (8081)      (8082)         (8083)
   ↓             ↓          ↓
 authdb      docteurdb    rdvdb
```

---

## Grading Criteria Met (Phase 1)

✅ **Multiple microservices** - 4 services (auth, docteur, rdv, notification)
✅ **Service discovery** - All services registered with Eureka
✅ **API Gateway** - Centralized routing with security
✅ **Authentication & Authorization** - JWT-based with role-based access control
✅ **Security best practices** - No hardcoded secrets, password hashing

**Remaining for full score:**
- ⏳ Resilience patterns (Phase 2)
- ⏳ Async communication (Phase 3)

---

## Questions?

If you encounter issues:
1. Check logs in `./logs/` directory
2. Verify all services are UP in Eureka dashboard
3. Check database connections
4. Ensure ports are not already in use

**You're ready to test! Start with the registration and login flow above.**
