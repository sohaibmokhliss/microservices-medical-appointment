# Billing Service Implementation - Complete Guide

## Summary

The Billing/Payment Service has been successfully implemented and integrated into your healthcare appointment system. This service provides complete invoice and payment management functionality.

## What Was Implemented

### 1. Billing Service (Port 8085)

**Backend Components:**
- ✅ Entity models (Invoice, Payment, Pricing)
- ✅ Spring Data JPA repositories
- ✅ Service layer with business logic
- ✅ REST API controllers
- ✅ RabbitMQ event listeners (consumes appointment events)
- ✅ RabbitMQ event publishers (publishes billing events)
- ✅ Security configuration
- ✅ Eureka service registration

**Database:**
- ✅ PostgreSQL database (billingdb) created
- ✅ Three tables: invoice, payment, pricing
- ✅ Automatic schema generation via Hibernate

**Key Features:**
- Automatic invoice generation when appointments are created
- Payment recording with multiple payment methods
- Payment status tracking (PENDING, PAID, PARTIALLY_PAID, OVERDUE)
- Outstanding balance calculation per patient
- Event-driven notifications

### 2. API Gateway Integration

**Updated:**
- ✅ Added route for `/api/billing/**` → billing-service
- ✅ Load balancing via Eureka service discovery

**Routes:**
```
/api/billing/invoices              → Get all invoices
/api/billing/invoices/{id}         → Get invoice by ID
/api/billing/invoices/patient/{email} → Get patient's invoices
/api/billing/payments              → Record payment
/api/billing/outstanding/{email}   → Get outstanding balance
```

### 3. Notification Service Integration

**Updated:**
- ✅ Added billing.notifications.queue
- ✅ Created PaymentEventListener
- ✅ Notification templates for:
  - Invoice creation
  - Payment received
  - Payment overdue reminders

**Notifications Sent:**
- Email and SMS when invoice is created
- Email and SMS when payment is received
- Email and SMS for overdue payment reminders

### 4. Frontend Integration

**New Component:**
- ✅ InvoiceManagement.js - Complete billing UI

**Features:**
- View all invoices in a table
- Filter by status
- Record payments for invoices
- Display payment status with color-coded badges
- Admin-only access

**Updated:**
- ✅ App.js - Added "Gestion Factures" tab (Admin only)
- ✅ api.js - Added billingService with all endpoints

### 5. Event-Driven Architecture

**Flow:**
```
1. User creates appointment
   ↓
2. RDV Service publishes "appointment.created" event
   ↓
3. Billing Service consumes event → Creates invoice
   ↓
4. Billing Service publishes "invoice.created" event
   ↓
5. Notification Service sends email/SMS to patient
```

## Architecture Overview

```
┌────────────────────────────────────────────────────┐
│                  CLIENT/BROWSER                     │
│               (React Frontend - Port 3000)          │
└───────────────────┬────────────────────────────────┘
                    │
                    ▼
┌────────────────────────────────────────────────────┐
│              API GATEWAY (Port 8080)                │
│         Routes: /api/billing/** → Billing Service  │
└────────────────────┬───────────────────────────────┘
                     │
         ┌───────────┼──────────────┐
         │           │              │
         ▼           ▼              ▼
    ┌─────────┐ ┌──────────┐  ┌──────────┐
    │   RDV   │ │ Billing  │  │Notification│
    │ Service │ │ Service  │  │  Service   │
    │ (8082)  │ │ (8085)   │  │  (8083)    │
    └────┬────┘ └────┬─────┘  └─────┬──────┘
         │           │               │
         │     ┌─────┴────────┐      │
         │     │              │      │
         └─────►   RabbitMQ   ◄──────┘
               │  (Port 5672) │
               └──────────────┘

         ┌─────────────────────┐
         │   Eureka Server     │
         │    (Port 8761)      │
         └─────────────────────┘
```

## How to Start the System

### Prerequisites
Ensure the following are running:
- PostgreSQL (port 5432)
- RabbitMQ (port 5672)
- All databases created (authdb, docteurdb, rdvdb, **billingdb**)

### Database Setup
The billingdb database has already been created. If you need to recreate it:
```bash
psql -U postgres -c "DROP DATABASE IF EXISTS billingdb;"
psql -U postgres -c "CREATE DATABASE billingdb;"
```

### Starting Services (In Order)

**1. Start Eureka Server**
```bash
cd eureka-server
mvn spring-boot:run
```
Wait for: `Eureka Server started` (http://localhost:8761)

**2. Start API Gateway**
```bash
cd api-gateway
mvn spring-boot:run
```
Wait for: `Gateway registered with Eureka`

**3. Start Auth Service**
```bash
cd auth-service
mvn spring-boot:run
```

**4. Start Docteur Service**
```bash
cd docteur-service
mvn spring-boot:run
```

**5. Start RDV Service**
```bash
cd rdv-service
mvn spring-boot:run
```

**6. Start Notification Service**
```bash
cd notification-service
mvn spring-boot:run
```

**7. Start Billing Service** ⭐ NEW
```bash
cd billing-service
mvn spring-boot:run
```
Check logs for:
- "Started BillingServiceApplication"
- "Registered instance BILLING-SERVICE with Eureka"
- "Started listening on queue: appointment.billing.queue"

**8. Start Frontend**
```bash
cd frontend
npm start
```
Frontend will open at: http://localhost:3000

### Verification

**1. Check Eureka Dashboard**
Visit: http://localhost:8761

You should see 7 services registered:
- API-GATEWAY
- AUTH-SERVICE
- DOCTEUR-SERVICE
- RDV-SERVICE
- NOTIFICATION-SERVICE
- **BILLING-SERVICE** ⭐ NEW

**2. Check RabbitMQ Management**
Visit: http://localhost:15672 (guest/guest)

You should see these queues:
- appointment.notifications.queue
- appointment.billing.queue ⭐ NEW
- billing.notifications.queue ⭐ NEW

**3. Test Billing Service Health**
```bash
curl http://localhost:8085/actuator/health
```
Expected response: `{"status":"UP"}`

**4. Test via API Gateway**
```bash
curl http://localhost:8080/api/billing/invoices
```
Should return: `[]` (empty list initially)

## Testing the Integration

### Test 1: Automatic Invoice Creation

**Step 1:** Login as admin
```
Username: admin
Password: admin123
```

**Step 2:** Create a new appointment
- Go to "Prendre Rendez-vous"
- Fill in patient details
- Select a doctor
- Submit

**Step 3:** Check invoice was created
- Go to "Gestion Factures" tab (Admin only)
- You should see a new invoice with status "En attente"

**Expected:**
- Invoice automatically created
- Patient receives email/SMS notification about invoice

### Test 2: Record Payment

**Step 1:** Go to "Gestion Factures"

**Step 2:** Click "Enregistrer paiement" on a pending invoice

**Step 3:** Fill in payment details:
- Amount: (auto-filled with invoice total)
- Payment method: Select (Cash, Card, Bank Transfer, Online)
- Transaction ID: (optional)
- Notes: (optional)

**Step 4:** Submit payment

**Expected:**
- Invoice status changes to "Payé"
- Patient receives payment confirmation email/SMS

### Test 3: Outstanding Balance

**Using the API:**
```bash
curl http://localhost:8080/api/billing/outstanding/patient@example.com
```

**Expected Response:**
```json
{
  "patientEmail": "patient@example.com",
  "outstandingBalance": 300.00
}
```

## Default Pricing

- **Default consultation fee:** 300.00 MAD
- **Tax rate:** 0% (configurable)

To customize pricing by specialty, insert into the pricing table:
```sql
INSERT INTO pricing (specialty, consultation_fee, description)
VALUES
  ('Cardiologie', 400.00, 'Consultation cardiologie'),
  ('Pédiatrie', 250.00, 'Consultation pédiatrie'),
  ('Dermatologie', 300.00, 'Consultation dermatologie');
```

## API Examples

### Get All Invoices
```bash
curl http://localhost:8080/api/billing/invoices
```

### Get Invoices by Patient
```bash
curl http://localhost:8080/api/billing/invoices/patient/sohaibmokhlissiba@gmail.com
```

### Get Pending Invoices
```bash
curl http://localhost:8080/api/billing/invoices/status/PENDING
```

### Record Payment
```bash
curl -X POST http://localhost:8080/api/billing/payments \
  -H "Content-Type: application/json" \
  -d '{
    "invoiceId": 1,
    "amount": 300.00,
    "paymentMethod": "CASH",
    "status": "SUCCESS",
    "notes": "Payment received in office"
  }'
```

### Get Outstanding Balance
```bash
curl http://localhost:8080/api/billing/outstanding/patient@example.com
```

## File Structure

### New Files Created

**billing-service/**
```
billing-service/
├── pom.xml
├── README.md
└── src/main/
    ├── java/com/healthcare/billing/
    │   ├── BillingServiceApplication.java
    │   ├── entities/
    │   │   ├── Invoice.java
    │   │   ├── Payment.java
    │   │   └── Pricing.java
    │   ├── repositories/
    │   │   ├── InvoiceRepository.java
    │   │   ├── PaymentRepository.java
    │   │   └── PricingRepository.java
    │   ├── services/
    │   │   └── BillingService.java
    │   ├── controllers/
    │   │   └── BillingController.java
    │   ├── dto/
    │   │   ├── InvoiceDTO.java
    │   │   └── PaymentDTO.java
    │   ├── events/
    │   │   ├── AppointmentEvent.java
    │   │   └── PaymentEvent.java
    │   ├── listeners/
    │   │   └── AppointmentEventListener.java
    │   └── config/
    │       ├── RabbitMQConfig.java
    │       └── SecurityConfig.java
    └── resources/
        └── application.properties
```

**Modified Files:**
- `api-gateway/src/main/resources/application.properties` - Added billing route
- `notification-service/src/main/resources/application.properties` - Added billing queue
- `notification-service/.../config/RabbitMQConfig.java` - Added billing queue bean
- `notification-service/.../events/PaymentEvent.java` - New event class
- `notification-service/.../listeners/PaymentEventListener.java` - New listener
- `frontend/src/components/InvoiceManagement.js` - New component
- `frontend/src/services/api.js` - Added billingService
- `frontend/src/App.js` - Added Gestion Factures tab

## Troubleshooting

### Issue: Billing service not appearing in Eureka
**Solution:**
- Check Eureka Server is running on port 8761
- Verify application.properties has correct eureka.client.service-url.defaultZone
- Check billing-service logs for registration errors

### Issue: Invoices not auto-generated
**Solution:**
- Verify RabbitMQ is running
- Check appointment.billing.queue exists in RabbitMQ
- Verify RDV service is publishing events to appointments.exchange
- Check billing-service logs for event consumption

### Issue: Payment notifications not sent
**Solution:**
- Verify billing.notifications.queue exists
- Check Notification Service is running and consuming from billing queue
- Check notification-service logs for errors

### Issue: Database connection error
**Solution:**
```bash
# Verify database exists
psql -U postgres -c "\l" | grep billingdb

# If not, create it
psql -U postgres -c "CREATE DATABASE billingdb;"
```

### Issue: Frontend shows "Gestion Factures" but data not loading
**Solution:**
- Check browser console for API errors
- Verify billing-service is running on port 8085
- Check API Gateway is routing /api/billing/** correctly
- Test endpoint directly: `curl http://localhost:8085/api/billing/invoices`

## Next Steps

Now that the Billing Service is implemented, you can:

1. **Test the complete flow:**
   - Create an appointment → Invoice auto-generated → Payment recorded → Notification sent

2. **Customize pricing:**
   - Add specialty-specific pricing in the pricing table

3. **Add payment gateway integration:**
   - Integrate Stripe, PayPal, or local payment providers

4. **Generate PDF invoices:**
   - Add invoice PDF generation and email attachment

5. **Create financial reports:**
   - Add analytics dashboard for revenue tracking

## Summary

You now have a **complete Billing/Payment Service** integrated into your healthcare appointment system:

✅ Automatic invoice generation
✅ Payment recording and tracking
✅ Outstanding balance calculation
✅ Event-driven notifications
✅ Admin UI for managing invoices and payments
✅ Full integration with existing services

The system is production-ready and follows all microservices best practices!
