# RabbitMQ Setup Guide

## Quick Start with Docker (Recommended)

### 1. Install Docker (if not already installed)
```bash
# Check if Docker is installed
docker --version

# If not installed, visit: https://docs.docker.com/get-docker/
```

### 2. Start RabbitMQ Container
```bash
docker run -d \
  --name rabbitmq \
  -p 5672:5672 \
  -p 15672:15672 \
  rabbitmq:3-management
```

**What this does:**
- Downloads RabbitMQ image with management plugin
- Starts container in detached mode (`-d`)
- Exposes port 5672 (AMQP protocol)
- Exposes port 15672 (Management UI)
- Names container "rabbitmq"

### 3. Verify RabbitMQ is Running
```bash
# Check container status
docker ps | grep rabbitmq

# Check logs
docker logs rabbitmq
```

### 4. Access Management UI
Open browser: **http://localhost:15672**

**Default credentials:**
- Username: `guest`
- Password: `guest`

### 5. Verify Configuration
In the Management UI, check:
- **Exchanges** tab - Should see `appointments.exchange` after starting services
- **Queues** tab - Should see `appointment.notifications.queue`
- **Connections** tab - Should see connections from rdv-service and notification-service

---

## Managing RabbitMQ Container

### Stop RabbitMQ
```bash
docker stop rabbitmq
```

### Start RabbitMQ (if stopped)
```bash
docker start rabbitmq
```

### Remove RabbitMQ Container
```bash
docker stop rabbitmq
docker rm rabbitmq
```

### View Logs
```bash
docker logs rabbitmq

# Follow logs in real-time
docker logs -f rabbitmq
```

---

## Alternative: Install RabbitMQ Locally

### On Ubuntu/Debian
```bash
# Add RabbitMQ repository
curl -fsSL https://github.com/rabbitmq/signing-keys/releases/download/2.0/rabbitmq-release-signing-key.asc | sudo apt-key add -
sudo apt-add-repository 'deb http://www.rabbitmq.com/debian/ testing main'

# Install RabbitMQ
sudo apt update
sudo apt install rabbitmq-server

# Enable management plugin
sudo rabbitmq-plugins enable rabbitmq_management

# Start service
sudo systemctl start rabbitmq-server
sudo systemctl enable rabbitmq-server
```

### On macOS (with Homebrew)
```bash
# Install RabbitMQ
brew install rabbitmq

# Start service
brew services start rabbitmq

# Enable management plugin
/usr/local/sbin/rabbitmq-plugins enable rabbitmq_management
```

### On Windows
1. Download from: https://www.rabbitmq.com/download.html
2. Install Erlang first (required dependency)
3. Run RabbitMQ installer
4. Enable management plugin:
   ```cmd
   rabbitmq-plugins enable rabbitmq_management
   ```

---

## Troubleshooting

### Port Already in Use
```bash
# Check what's using port 5672
sudo lsof -i :5672

# Kill the process or use different ports
docker run -d --name rabbitmq -p 5673:5672 -p 15673:15672 rabbitmq:3-management

# Update application.properties accordingly:
# spring.rabbitmq.port=5673
```

### Container Won't Start
```bash
# Check existing containers
docker ps -a

# Remove old container
docker rm rabbitmq

# Pull latest image
docker pull rabbitmq:3-management

# Start fresh
docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3-management
```

### Can't Access Management UI
```bash
# Wait 30-60 seconds after starting (plugins need time to load)
# Check if container is running
docker ps | grep rabbitmq

# Check logs for errors
docker logs rabbitmq

# Try accessing: http://127.0.0.1:15672
```

### Services Can't Connect
```bash
# Verify RabbitMQ is listening
docker exec rabbitmq rabbitmqctl status

# Check firewall rules (Linux)
sudo ufw allow 5672
sudo ufw allow 15672

# Test connection
telnet localhost 5672
```

---

## Monitoring Message Flow

### Via Management UI
1. Go to http://localhost:15672
2. Click **Queues** tab
3. Click `appointment.notifications.queue`
4. See message rates, counts, and details

### Via Command Line
```bash
# List queues
docker exec rabbitmq rabbitmqctl list_queues

# List exchanges
docker exec rabbitmq rabbitmqctl list_exchanges

# List bindings
docker exec rabbitmq rabbitmqctl list_bindings
```

---

## Testing Message Flow

### 1. Start Services in Order
```bash
# Terminal 1: RabbitMQ
docker start rabbitmq

# Terminal 2: Eureka Server
cd eureka-server && mvn spring-boot:run

# Terminal 3: Other services
./start-all.sh
```

### 2. Create an Appointment
```bash
# This will publish event to RabbitMQ
curl -X POST http://localhost:8080/api/rdv \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "docteurId": 1,
    "patientNom": "Test",
    "patientPrenom": "User",
    "patientEmail": "test@example.com",
    "patientTelephone": "+33612345678",
    "dateHeure": "2025-12-15T10:00:00",
    "motif": "Test appointment"
  }'
```

### 3. Check RabbitMQ UI
- **Exchanges** → `appointments.exchange` → Should show 1 published message
- **Queues** → `appointment.notifications.queue` → Should process message
- **Connections** → Should see rdv-service (publisher) and notification-service (consumer)

### 4. Check Service Logs
```bash
# RDV Service - Should show event publishing
grep "Publishing CREATED event" rdv-service.log

# Notification Service - Should show event receiving
grep "Received CREATED event" notification-service.log
```

---

## Production Configuration

For production, configure:

### 1. Create RabbitMQ User
```bash
docker exec rabbitmq rabbitmqctl add_user healthcare secure_password
docker exec rabbitmq rabbitmqctl set_user_tags healthcare administrator
docker exec rabbitmq rabbitmqctl set_permissions -p / healthcare ".*" ".*" ".*"
```

### 2. Update application.properties
```properties
spring.rabbitmq.username=healthcare
spring.rabbitmq.password=secure_password
```

### 3. Enable SSL (Optional)
```properties
spring.rabbitmq.ssl.enabled=true
spring.rabbitmq.ssl.key-store=classpath:keystore.p12
spring.rabbitmq.ssl.key-store-password=password
```

---

## Summary

✅ **Docker method is recommended** - Easiest and most consistent
✅ **Management UI at http://localhost:15672** - Monitor messages
✅ **Default credentials: guest/guest** - Change in production
✅ **Wait 30-60 seconds after start** - Plugins need time to load

**Next Step**: Run `./start-all.sh` and test appointment creation!
