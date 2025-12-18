# Billing Service - Healthcare Appointment System

## Overview
The Billing Service is a microservice that manages invoices and payments for the healthcare appointment booking system. It automatically generates invoices when appointments are created and tracks payment status.

## Features
- **Automatic Invoice Generation**: Invoices are automatically created when new appointments are booked
- **Payment Recording**: Record payments with multiple payment methods (cash, card, bank transfer, online)
- **Payment Status Tracking**: Track invoice status (PENDING, PAID, PARTIALLY_PAID, OVERDUE, CANCELLED)
- **Outstanding Balance**: Calculate outstanding balance per patient
- **Event-Driven Architecture**: Integrates with RabbitMQ for asynchronous event processing
- **Notification Integration**: Sends payment notifications via email and SMS

## Architecture

### Service Configuration
- **Port**: 8085
- **Service Name**: billing-service
- **Database**: PostgreSQL (billingdb)
- **Eureka Registration**: Yes
- **RabbitMQ**: Yes (consumer and publisher)

### Database Schema

#### Invoice Table
- `id` (Long) - Primary Key
- `rdv_id` (Long) - Reference to appointment
- `patient_email` (String) - Patient email address
- `patient_name` (String) - Patient full name
- `doctor_name` (String) - Doctor name
- `specialty` (String) - Medical specialty
- `amount` (BigDecimal) - Base amount
- `tax` (BigDecimal) - Tax amount
- `total` (BigDecimal) - Total amount (amount + tax)
- `status` (String) - Invoice status
- `description` (String) - Invoice description
- `created_date` (LocalDateTime) - Creation timestamp
- `due_date` (LocalDate) - Payment due date
- `paid_date` (LocalDateTime) - Payment completion date

#### Payment Table
- `id` (Long) - Primary Key
- `invoice_id` (Long) - Foreign Key to Invoice
- `amount` (BigDecimal) - Payment amount
- `payment_method` (String) - Payment method (CASH, CARD, BANK_TRANSFER, ONLINE)
- `payment_date` (LocalDateTime) - Payment timestamp
- `transaction_id` (String) - Transaction ID
- `status` (String) - Payment status (SUCCESS, PENDING, FAILED)
- `notes` (String) - Additional notes

#### Pricing Table
- `id` (Long) - Primary Key
- `specialty` (String) - Medical specialty
- `consultation_fee` (BigDecimal) - Consultation fee
- `description` (String) - Pricing description

## API Endpoints

### Invoice Management

#### Get All Invoices
```
GET /api/billing/invoices
```

#### Get Invoice by ID
```
GET /api/billing/invoices/{id}
```

#### Get Invoices by Patient Email
```
GET /api/billing/invoices/patient/{email}
```

#### Get Invoices by Status
```
GET /api/billing/invoices/status/{status}
```
Status values: PENDING, PAID, PARTIALLY_PAID, OVERDUE, CANCELLED

#### Create Invoice (Manual)
```
POST /api/billing/invoices
Content-Type: application/json

{
  "rdvId": 1,
  "patientEmail": "patient@example.com",
  "patientName": "John Doe",
  "doctorName": "Dr. Smith",
  "specialty": "Cardiologie",
  "description": "Consultation"
}
```

#### Update Invoice
```
PUT /api/billing/invoices/{id}
Content-Type: application/json

{
  "status": "PAID",
  "description": "Updated description"
}
```

### Payment Management

#### Get Payments by Invoice ID
```
GET /api/billing/payments/invoice/{invoiceId}
```

#### Record Payment
```
POST /api/billing/payments
Content-Type: application/json

{
  "invoiceId": 1,
  "amount": 300.00,
  "paymentMethod": "CASH",
  "transactionId": "TXN123456",
  "status": "SUCCESS",
  "notes": "Payment received"
}
```

### Outstanding Balance

#### Get Outstanding Balance by Patient Email
```
GET /api/billing/outstanding/{patientEmail}
```

Response:
```json
{
  "patientEmail": "patient@example.com",
  "outstandingBalance": 600.00
}
```

## Event Integration

### Consuming Appointment Events
The billing service listens to the `appointment.billing.queue` for appointment events from the RDV service:

**Event Type**: `CREATED`
- Automatically creates an invoice when a new appointment is created
- Calculates pricing based on doctor's specialty
- Sets default payment term to 30 days

### Publishing Payment Events
The billing service publishes events to the `billing.exchange`:

**Event Types**:
- `invoice.created` - When a new invoice is generated
- `payment.received` - When a payment is recorded
- `payment.overdue` - When an invoice is overdue (for future implementation)

**Event Structure**:
```json
{
  "eventType": "invoice.created",
  "invoiceId": 1,
  "patientEmail": "patient@example.com",
  "patientName": "John Doe",
  "amount": 300.00,
  "status": "PENDING",
  "timestamp": "2025-12-13T10:30:00"
}
```

## Pricing Configuration

Default consultation fee: **300.00 MAD**
Tax rate: **0%** (configurable)

To set custom pricing by specialty:
```sql
INSERT INTO pricing (specialty, consultation_fee, description)
VALUES ('Cardiologie', 400.00, 'Consultation cardiologie');
```

## Running the Service

### Prerequisites
- Java 17
- PostgreSQL database
- RabbitMQ running on localhost:5672
- Eureka Server running on localhost:8761

### Start the Service
```bash
cd billing-service
mvn spring-boot:run
```

The service will:
1. Register with Eureka Server
2. Connect to PostgreSQL (billingdb)
3. Start listening on port 8085
4. Begin consuming appointment events from RabbitMQ

### Verify Service is Running
```bash
curl http://localhost:8085/actuator/health
```

## Integration with Other Services

### API Gateway
The API Gateway routes all billing requests:
```
/api/billing/** → billing-service:8085
```

### RDV Service
When an appointment is created, the RDV service publishes an event that triggers automatic invoice generation.

### Notification Service
The Notification Service listens to billing events and sends:
- **Invoice Created**: Email and SMS notification with invoice details
- **Payment Received**: Payment confirmation notification
- **Payment Overdue**: Payment reminder notification

## Frontend Integration

A new "Gestion Factures" tab is available for Admin users in the frontend, providing:
- View all invoices
- Filter invoices by status (PENDING, PAID, etc.)
- Record payments for invoices
- View payment history
- Track outstanding balances

## Testing the Service

### Test Invoice Creation
1. Create a new appointment via the RDV service
2. Check that an invoice is automatically created:
```bash
curl http://localhost:8080/api/billing/invoices
```

### Test Payment Recording
```bash
curl -X POST http://localhost:8080/api/billing/payments \
  -H "Content-Type: application/json" \
  -d '{
    "invoiceId": 1,
    "amount": 300.00,
    "paymentMethod": "CASH",
    "status": "SUCCESS"
  }'
```

### Test Outstanding Balance
```bash
curl http://localhost:8080/api/billing/outstanding/patient@example.com
```

## Future Enhancements

1. **Payment Gateway Integration**: Stripe/PayPal for online payments
2. **Recurring Billing**: Support for subscription-based services
3. **Multi-Currency**: Support for multiple currencies
4. **Payment Plans**: Installment payment options
5. **Automated Reminders**: Scheduled payment reminder notifications
6. **Invoice PDF Generation**: Generate PDF invoices
7. **Financial Reporting**: Advanced analytics and reports

## Security

- All endpoints are protected via API Gateway JWT authentication
- Role-based access control (ADMIN, RECEPTIONIST, PATIENT)
- Sensitive payment data should be encrypted (PCI DSS compliance)

## Troubleshooting

### Service not registering with Eureka
- Check Eureka Server is running on port 8761
- Verify `eureka.client.service-url.defaultZone` in application.properties

### Database connection issues
- Ensure PostgreSQL is running
- Verify database credentials in application.properties
- Check that `billingdb` database exists

### RabbitMQ connection issues
- Verify RabbitMQ is running on port 5672
- Check queue bindings are correctly configured
- Ensure appointment.billing.queue exists

### Events not being consumed
- Check RabbitMQ management console for queue status
- Verify RDV service is publishing events to appointments.exchange
- Check application logs for errors
