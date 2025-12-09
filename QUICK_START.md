# Quick Start Guide - Healthcare Appointment System

## Prerequisites

### Required Software
- **Java 17+** (JDK)
- **Maven 3.6+**
- **Node.js 16+** and npm
- **PostgreSQL 13+**
- **Docker** (for RabbitMQ)

### Check Your System
```bash
java -version      # Should be 17+
mvn -version       # Should be 3.6+
node -version      # Should be 16+
docker -version    # Any recent version
psql --version     # Should be 13+
```

---

## 🚀 One-Command Start

```bash
./start-all.sh
```

This will:
1. ✅ Check all dependencies
2. ✅ Start/create RabbitMQ container
3. ✅ Setup PostgreSQL databases
4. ✅ Install frontend dependencies
5. ✅ Start all 6 microservices + Eureka + API Gateway
6. ✅ Start React frontend

**Total startup time: ~2-3 minutes**

---

## 📋 Step-by-Step Manual Setup

### 1. Start RabbitMQ
```bash
docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3-management
```

Wait 60 seconds for RabbitMQ to fully initialize.

### 2. Setup PostgreSQL
```bash
# Start PostgreSQL service
sudo systemctl start postgresql

# Create databases
sudo -u postgres psql -c "CREATE DATABASE authdb;"
sudo -u postgres psql -c "CREATE DATABASE docteurdb;"
sudo -u postgres psql -c "CREATE DATABASE rdvdb;"
```

### 3. Build Services
```bash
# Build each service
cd eureka-server && mvn clean install && cd ..
cd api-gateway && mvn clean install && cd ..
cd auth-service && mvn clean install && cd ..
cd docteur-service && mvn clean install && cd ..
cd rdv-service && mvn clean install && cd ..
cd notification-service && mvn clean install && cd ..
```

### 4. Start Services (in order)
```bash
# Terminal 1: Eureka Server (wait 10s)
cd eureka-server && mvn spring-boot:run

# Terminal 2: API Gateway (wait 5s)
cd api-gateway && mvn spring-boot:run

# Terminal 3: Auth Service (wait 5s)
cd auth-service && mvn spring-boot:run

# Terminal 4: Docteur Service (wait 5s)
cd docteur-service && mvn spring-boot:run

# Terminal 5: RDV Service (wait 5s)
cd rdv-service && mvn spring-boot:run

# Terminal 6: Notification Service (wait 5s)
cd notification-service && mvn spring-boot:run

# Terminal 7: Frontend
cd frontend && npm install && npm start
```

---

## 🔍 Verify Everything is Running

### Option 1: Health Check Script
```bash
./check-health.sh
```

### Option 2: Manual Check
1. **Eureka Dashboard**: http://localhost:8761
   - Should show all 6 services registered

2. **RabbitMQ Management**: http://localhost:15672
   - Login: guest/guest
   - Check Exchanges → `appointments.exchange` exists
   - Check Queues → `appointment.notifications.queue` exists

3. **Frontend**: http://localhost:3000
   - Should load the React application

4. **API Gateway**: http://localhost:8080/api
   - Should be accessible

---

## 🧪 Test the System

### 1. Register a User
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "patient1",
    "password": "password123",
    "email": "patient1@example.com",
    "role": "PATIENT"
  }'
```

### 2. Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "patient1",
    "password": "password123"
  }'
```

Save the JWT token from the response.

### 3. List Doctors
```bash
curl http://localhost:8080/api/docteurs \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 4. Create Appointment (Tests RabbitMQ!)
```bash
curl -X POST http://localhost:8080/api/rdv \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "docteurId": 1,
    "patientNom": "Doe",
    "patientPrenom": "John",
    "patientEmail": "john@example.com",
    "patientTelephone": "+33612345678",
    "dateHeure": "2025-12-15T10:00:00",
    "motif": "Consultation générale"
  }'
```

**What happens:**
1. Appointment saved to database
2. Event published to RabbitMQ
3. Notification service consumes event
4. SMS + Email notifications sent

### 5. Check RabbitMQ Message Flow
1. Go to http://localhost:15672
2. Queues → `appointment.notifications.queue`
3. See message count and processing

### 6. Test Circuit Breaker
```bash
# Stop docteur-service
pkill -f "docteur-service"

# Try to create appointment - should still work with fallback!
curl -X POST http://localhost:8080/api/rdv \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{...}'

# Check circuit breaker status
curl http://localhost:8082/actuator/circuitbreakers
```

---

## 🛑 Stop All Services

### Option 1: Stop Script
```bash
./stop-all.sh
```

### Option 2: Manual Stop
```bash
# Kill all Java processes
pkill -f "spring-boot"

# Stop frontend
pkill -f "react-scripts"

# Stop RabbitMQ (optional)
docker stop rabbitmq
```

---

## 📊 Monitoring & Debugging

### Service Status
```bash
# Check what's running
lsof -i :8761  # Eureka
lsof -i :8080  # Gateway
lsof -i :8084  # Auth
lsof -i :8081  # Docteur
lsof -i :8082  # RDV
lsof -i :8083  # Notification
lsof -i :3000  # Frontend
```

### Logs
```bash
# Application logs
tail -f logs/rdv-service.log
tail -f logs/notification-service.log

# RabbitMQ logs
docker logs -f rabbitmq
```

### Actuator Endpoints
```bash
# Health checks
curl http://localhost:8082/actuator/health

# Circuit breaker status
curl http://localhost:8082/actuator/circuitbreakers

# Circuit breaker events
curl http://localhost:8082/actuator/circuitbreakerevents

# All endpoints
curl http://localhost:8082/actuator
```

---

## 🐛 Troubleshooting

### RabbitMQ won't start
```bash
# Remove old container
docker stop rabbitmq && docker rm rabbitmq

# Start fresh
docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3-management

# Wait 60 seconds
sleep 60
```

### PostgreSQL connection errors
```bash
# Check if running
sudo systemctl status postgresql

# Start if stopped
sudo systemctl start postgresql

# Check databases exist
psql -U postgres -l | grep -E "authdb|docteurdb|rdvdb"
```

### Port already in use
```bash
# Find what's using the port
lsof -i :8082

# Kill the process
kill -9 <PID>
```

### Services won't start
```bash
# Clean and rebuild
cd rdv-service
mvn clean install
mvn spring-boot:run
```

### Frontend won't start
```bash
cd frontend
rm -rf node_modules package-lock.json
npm install
npm start
```

---

## 🎯 Architecture Features to Demonstrate

### 1. Microservices
- 4 business services (auth, docteur, rdv, notification)
- Service discovery (Eureka)
- API Gateway routing

### 2. Resilience Patterns
- Circuit breaker: Stop docteur-service → appointments still work
- Retry: Automatic retry on failures
- Timeout: 5-second protection
- Fallback: Graceful degradation

### 3. Async Communication
- Event-driven notifications via RabbitMQ
- Decoupled services
- Message queue visible in RabbitMQ UI

### 4. Security
- JWT authentication
- Role-based access (PATIENT, DOCTOR, ADMIN)
- API Gateway security filter

### 5. Error Handling
- Global exception handler
- Validation errors with field details
- Consistent error format

---

## 📈 Performance Testing

### Load Test with Apache Bench
```bash
# Install Apache Bench
sudo apt install apache2-utils  # Ubuntu/Debian
sudo pacman -S apache  # Arch Linux

# Test appointment creation
ab -n 100 -c 10 \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -p appointment.json \
  http://localhost:8080/api/rdv
```

### Monitor Circuit Breaker Under Load
```bash
# Watch circuit breaker status
watch -n 1 'curl -s http://localhost:8082/actuator/circuitbreakers | jq'
```

---

## 🎓 Grading Checklist

- ✅ Multiple microservices with clear boundaries
- ✅ Service discovery (Eureka)
- ✅ API Gateway with JWT security
- ✅ Synchronous communication (Feign)
- ✅ Asynchronous communication (RabbitMQ)
- ✅ Circuit breaker pattern
- ✅ Retry pattern
- ✅ Timeout protection
- ✅ Fallback mechanisms
- ✅ Global exception handling
- ✅ Input validation
- ✅ Structured logging
- ✅ Health checks and monitoring
- ✅ Role-based access control

---

## 🆘 Need Help?

Check these files:
- `IMPLEMENTATION_COMPLETE.md` - Full implementation details
- `RABBITMQ_SETUP.md` - RabbitMQ setup guide
- `BACKEND_STANDARDS_AUDIT.md` - Architecture analysis
- `PHASE1_TESTING_GUIDE.md` - Security testing

---

**Your system is production-ready! Good luck with your demo! 🚀**
