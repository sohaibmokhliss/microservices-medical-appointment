# Pre-Deployment Checklist

## ✅ Before Starting Services

### 1. Prerequisites Installed
- [ ] Java 17 or higher (`java -version`)
- [ ] Maven 3.6+ (`mvn -version`)
- [ ] PostgreSQL 14+ (`psql --version`)
- [ ] Docker (`docker --version`)
- [ ] Git (`git --version`)

### 2. RabbitMQ Running
- [ ] Start RabbitMQ: `docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3-management`
- [ ] Verify: `docker ps | grep rabbitmq`
- [ ] Access UI: http://localhost:15672 (guest/guest)
- [ ] Wait 60 seconds for management plugin to load

### 3. Databases Created
```bash
# Run database setup script
cd /home/browncj/Work/projet_architecture_des_composants
./setup-databases.sh
```

Expected databases:
- [ ] `authdb` on port 5432
- [ ] `docteurdb` on port 5432
- [ ] `rdvdb` on port 5432

### 4. Environment Variables (Optional but Recommended)
```bash
export JWT_SECRET=your-secret-key-minimum-256-bits
export SMS_API_KEY=your-sms-provider-key
export EMAIL_API_KEY=your-email-provider-key
```

### 5. Build All Services
```bash
# Build each service
cd auth-service && mvn clean install && cd ..
cd rdv-service && mvn clean install && cd ..
cd docteur-service && mvn clean install && cd ..
cd notification-service && mvn clean install && cd ..
cd api-gateway && mvn clean install && cd ..
cd eureka-server && mvn clean install && cd ..
```

Expected output: `BUILD SUCCESS` for each

---

## 🚀 Starting Services

### Option 1: Use start-all.sh Script (Recommended)
```bash
cd /home/browncj/Work/projet_architecture_des_composants
./start-all.sh
```

### Option 2: Manual Start (for debugging)
Start in this order:

1. **Eureka Server** (Port 8761)
   ```bash
   cd eureka-server
   mvn spring-boot:run
   ```
   Wait for: "Eureka Server initialized"

2. **Auth Service** (Port 8084)
   ```bash
   cd auth-service
   mvn spring-boot:run
   ```
   Wait for: "Started AuthServiceApplication"

3. **Docteur Service** (Port 8081)
   ```bash
   cd docteur-service
   mvn spring-boot:run
   ```
   Wait for: "Started DocteurServiceApplication"

4. **RDV Service** (Port 8082)
   ```bash
   cd rdv-service
   mvn spring-boot:run
   ```
   Wait for: "Started RdvServiceApplication"

5. **Notification Service** (Port 8083)
   ```bash
   cd notification-service
   mvn spring-boot:run
   ```
   Wait for: "Started NotificationServiceApplication"

6. **API Gateway** (Port 8080)
   ```bash
   cd api-gateway
   mvn spring-boot:run
   ```
   Wait for: "Started ApiGatewayApplication"

---

## ✅ Verification Steps

### 1. Check Eureka Dashboard
- [ ] Open: http://localhost:8761
- [ ] Verify 5 services registered:
  - AUTH-SERVICE
  - DOCTEUR-SERVICE
  - RDV-SERVICE
  - NOTIFICATION-SERVICE
  - API-GATEWAY

### 2. Check Service Health
```bash
curl http://localhost:8084/actuator/health  # auth-service
curl http://localhost:8081/actuator/health  # docteur-service
curl http://localhost:8082/actuator/health  # rdv-service
curl http://localhost:8083/actuator/health  # notification-service
curl http://localhost:8080/actuator/health  # api-gateway
```

Expected: `{"status":"UP"}` for all

### 3. Check RabbitMQ Connections
- [ ] Open: http://localhost:15672
- [ ] Go to **Connections** tab
- [ ] Verify 2 connections:
  - rdv-service (publisher)
  - notification-service (consumer)

### 4. Check RabbitMQ Resources
- [ ] **Exchanges** tab → `appointments.exchange` exists
- [ ] **Queues** tab → `appointment.notifications.queue` exists
- [ ] **Bindings** → 3 bindings with routing keys

### 5. Test Authentication
```bash
# Register a test user
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'
```

Expected: JWT token in response

### 6. Test Circuit Breaker Endpoint
```bash
curl http://localhost:8082/actuator/circuitbreakers
```

Expected: Circuit breaker configuration for "docteurService"

---

## 🧪 Functional Testing

### Test 1: Create Appointment (End-to-End)
```bash
# Get JWT token first
TOKEN=$(curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' \
  | jq -r '.token')

# Create appointment
curl -X POST http://localhost:8080/api/rdv \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "docteurId": 1,
    "patientNom": "Dupont",
    "patientPrenom": "Jean",
    "patientEmail": "jean.dupont@example.com",
    "patientTelephone": "+33612345678",
    "dateHeure": "2025-12-15T10:00:00",
    "motif": "Consultation générale"
  }'
```

**Expected Results:**
- [x] HTTP 201 Created
- [x] Appointment returned with ID
- [x] RabbitMQ message published (check UI)
- [x] Notification service processes event (check logs)
- [x] SMS/Email sent (check notification-service logs)

### Test 2: Validation Errors
```bash
# Try to create with invalid data
curl -X POST http://localhost:8080/api/rdv \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "patientEmail": "invalid-email",
    "patientTelephone": "123"
  }'
```

**Expected Results:**
- [x] HTTP 400 Bad Request
- [x] Error response with field validation messages
- [x] Format matches ErrorResponse model

### Test 3: Resource Not Found
```bash
# Try to get non-existent appointment
curl http://localhost:8080/api/rdv/99999 \
  -H "Authorization: Bearer $TOKEN"
```

**Expected Results:**
- [x] HTTP 404 Not Found
- [x] Error message: "Appointment not found with id: '99999'"
- [x] Proper ErrorResponse format

### Test 4: Circuit Breaker
```bash
# Stop docteur-service
# Create appointment (should use fallback)
curl -X POST http://localhost:8080/api/rdv \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "docteurId": 1,
    "patientNom": "Test",
    "patientPrenom": "Circuit",
    "patientEmail": "test@example.com",
    "patientTelephone": "+33612345678",
    "dateHeure": "2025-12-20T14:00:00",
    "motif": "Test fallback"
  }'
```

**Expected Results:**
- [x] HTTP 201 Created
- [x] Status: "EN ATTENTE" (not "CONFIRMÉ")
- [x] No 500 error (fallback works)
- [x] Circuit opens after failures

### Test 5: Unauthorized Access
```bash
# Try to access without token
curl http://localhost:8080/api/rdv
```

**Expected Results:**
- [x] HTTP 401 Unauthorized
- [x] Error message about missing/invalid token

### Test 6: Role-Based Access
```bash
# Try to delete as PATIENT (should fail)
curl -X DELETE http://localhost:8080/api/rdv/1 \
  -H "Authorization: Bearer $PATIENT_TOKEN"
```

**Expected Results:**
- [x] HTTP 403 Forbidden
- [x] Message about insufficient permissions

---

## 📊 Monitoring During Demo

### Real-Time Monitoring
Keep these browser tabs open during presentation:

1. **Eureka Dashboard**
   - URL: http://localhost:8761
   - Shows: All registered services

2. **RabbitMQ Management**
   - URL: http://localhost:15672
   - Shows: Message flow, queue depth

3. **Circuit Breaker Metrics**
   - URL: http://localhost:8082/actuator/circuitbreakers
   - Shows: Circuit state (CLOSED, OPEN, HALF_OPEN)

4. **Health Endpoints**
   - URL: http://localhost:8082/actuator/health
   - Shows: Overall system health

### Terminal Windows for Logs
```bash
# Terminal 1: RDV Service
cd rdv-service && mvn spring-boot:run

# Terminal 2: Notification Service
cd notification-service && mvn spring-boot:run

# Watch for:
# - "Publishing CREATED event"
# - "Received CREATED event"
# - "Notifications sent successfully"
# - Circuit breaker fallback messages
```

---

## 🎓 Demo Script

### 1. Introduction (2 minutes)
- "I've built a healthcare appointment system using microservices architecture"
- Show architecture diagram (diagrams folder)
- Explain 4 services + infrastructure

### 2. Show Services Running (2 minutes)
- Eureka dashboard - all services UP
- RabbitMQ management UI - connections established
- Actuator health checks

### 3. Create Appointment - Happy Path (3 minutes)
- Use Postman or curl
- Show request with validation
- Check response (201, appointment with ID)
- Show RabbitMQ message published
- Show notification service logs processing event
- Emphasize: "Async - no waiting for notifications"

### 4. Demonstrate Resilience (3 minutes)
- Stop docteur-service
- Create appointment again
- Show fallback working (status "EN ATTENTE")
- Show circuit breaker metrics
- Explain: "System degraded but still functional"

### 5. Show Error Handling (2 minutes)
- Send invalid data (wrong email format)
- Show validation error response
- Try to get non-existent ID
- Show 404 with proper error format

### 6. Code Walkthrough (3 minutes)
- Show RdvController with annotations:
  - @CircuitBreaker
  - @Retry
  - @PreAuthorize
  - @Valid
- Show GlobalExceptionHandler
- Show AppointmentEventPublisher

---

## 🐛 Troubleshooting

### Service Won't Start
```bash
# Check port availability
netstat -tulpn | grep 808

# Check RabbitMQ connection
telnet localhost 5672

# Check database connection
psql -U postgres -l

# Check dependencies
mvn dependency:tree

# Clean and rebuild
mvn clean install -DskipTests
```

### RabbitMQ Connection Errors
```bash
# Restart RabbitMQ
docker restart rabbitmq

# Check RabbitMQ logs
docker logs rabbitmq

# Verify services can reach RabbitMQ
docker exec rabbitmq rabbitmqctl list_connections
```

### Circuit Breaker Not Working
```bash
# Check Resilience4J configuration
grep resilience4j application.properties

# Verify AOP is enabled (check pom.xml for spring-boot-starter-aop)

# Check logs for circuit events
grep -i "circuit" logs/*.log
```

### Events Not Being Consumed
```bash
# Check RabbitMQ queue
# Management UI → Queues → appointment.notifications.queue
# Should show "Ready" count

# Check listener is registered
grep "@RabbitListener" notification-service/src/main/**/*.java

# Check notification-service logs
grep "Received.*event" notification-service.log
```

---

## 📝 Final Checklist Before Demo

- [ ] All services running (check Eureka)
- [ ] RabbitMQ running (check management UI)
- [ ] PostgreSQL running (check `psql -l`)
- [ ] Test user registered
- [ ] Test appointment created successfully
- [ ] Circuit breaker tested
- [ ] Error handling tested
- [ ] Browser tabs prepared (Eureka, RabbitMQ)
- [ ] Code ready to show in IDE
- [ ] Architecture diagram ready
- [ ] Postman collection or curl commands ready

---

## 🎯 Success Criteria

Your system is demo-ready when:
- ✅ All 6 services show UP in Eureka
- ✅ RabbitMQ shows 2 active connections
- ✅ Creating appointment publishes event to RabbitMQ
- ✅ Notification service consumes and processes event
- ✅ Circuit breaker triggers when service is down
- ✅ Validation returns proper error messages
- ✅ Actuator endpoints show health and circuit metrics

**You're ready to present!** 🚀
