package com.healthcare.billing.controllers;

import com.healthcare.billing.dto.InvoiceDTO;
import com.healthcare.billing.dto.PaymentDTO;
import com.healthcare.billing.entities.Invoice;
import com.healthcare.billing.services.BillingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/billing")
@CrossOrigin(origins = "*")
public class BillingController {

    @Autowired
    private BillingService billingService;

    // Invoice endpoints
    @GetMapping("/invoices")
    public ResponseEntity<List<InvoiceDTO>> getAllInvoices() {
        return ResponseEntity.ok(billingService.getAllInvoices());
    }

    @GetMapping("/invoices/{id}")
    public ResponseEntity<InvoiceDTO> getInvoiceById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(billingService.getInvoiceById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/invoices/patient/{email}")
    public ResponseEntity<List<InvoiceDTO>> getInvoicesByPatientEmail(@PathVariable String email) {
        return ResponseEntity.ok(billingService.getInvoicesByPatientEmail(email));
    }

    @GetMapping("/invoices/status/{status}")
    public ResponseEntity<List<InvoiceDTO>> getInvoicesByStatus(@PathVariable String status) {
        return ResponseEntity.ok(billingService.getInvoicesByStatus(status));
    }

    @PostMapping("/invoices")
    public ResponseEntity<InvoiceDTO> createInvoice(@Valid @RequestBody Invoice invoice) {
        try {
            InvoiceDTO createdInvoice = billingService.createInvoice(invoice);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdInvoice);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/invoices/{id}")
    public ResponseEntity<InvoiceDTO> updateInvoice(@PathVariable Long id, @RequestBody Invoice invoice) {
        try {
            return ResponseEntity.ok(billingService.updateInvoice(id, invoice));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Payment endpoints
    @GetMapping("/payments/invoice/{invoiceId}")
    public ResponseEntity<List<PaymentDTO>> getPaymentsByInvoiceId(@PathVariable Long invoiceId) {
        return ResponseEntity.ok(billingService.getPaymentsByInvoiceId(invoiceId));
    }

    @PostMapping("/payments")
    public ResponseEntity<PaymentDTO> recordPayment(@Valid @RequestBody PaymentDTO paymentDTO) {
        try {
            PaymentDTO payment = billingService.recordPayment(paymentDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(payment);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Outstanding balance endpoint
    @GetMapping("/outstanding/{patientEmail}")
    public ResponseEntity<OutstandingBalanceResponse> getOutstandingBalance(@PathVariable String patientEmail) {
        BigDecimal balance = billingService.getOutstandingBalance(patientEmail);
        return ResponseEntity.ok(new OutstandingBalanceResponse(patientEmail, balance));
    }

    // Helper class for outstanding balance response
    public static class OutstandingBalanceResponse {
        private String patientEmail;
        private BigDecimal outstandingBalance;

        public OutstandingBalanceResponse(String patientEmail, BigDecimal outstandingBalance) {
            this.patientEmail = patientEmail;
            this.outstandingBalance = outstandingBalance;
        }

        public String getPatientEmail() {
            return patientEmail;
        }

        public void setPatientEmail(String patientEmail) {
            this.patientEmail = patientEmail;
        }

        public BigDecimal getOutstandingBalance() {
            return outstandingBalance;
        }

        public void setOutstandingBalance(BigDecimal outstandingBalance) {
            this.outstandingBalance = outstandingBalance;
        }
    }
}
