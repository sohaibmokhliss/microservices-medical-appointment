package com.healthcare.billing.services;

import com.healthcare.billing.dto.InvoiceDTO;
import com.healthcare.billing.dto.PaymentDTO;
import com.healthcare.billing.entities.Invoice;
import com.healthcare.billing.entities.Payment;
import com.healthcare.billing.entities.Pricing;
import com.healthcare.billing.events.PaymentEvent;
import com.healthcare.billing.repositories.InvoiceRepository;
import com.healthcare.billing.repositories.PaymentRepository;
import com.healthcare.billing.repositories.PricingRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BillingService {

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PricingRepository pricingRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private static final String EXCHANGE = "billing.exchange";
    private static final BigDecimal DEFAULT_CONSULTATION_FEE = new BigDecimal("0.00"); // No default cap - use specialty pricing or specify amount
    private static final BigDecimal TAX_RATE = new BigDecimal("0.00"); // 0% tax for now

    // Invoice Methods
    public List<InvoiceDTO> getAllInvoices() {
        return invoiceRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public InvoiceDTO getInvoiceById(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Invoice not found with id: " + id));
        return convertToDTO(invoice);
    }

    public List<InvoiceDTO> getInvoicesByPatientEmail(String email) {
        return invoiceRepository.findByPatientEmail(email).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<InvoiceDTO> getInvoicesByStatus(String status) {
        return invoiceRepository.findByStatus(status).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public InvoiceDTO createInvoice(Invoice invoice) {
        // Calculate pricing based on specialty if pricing exists
        if (invoice.getSpecialty() != null) {
            Pricing pricing = pricingRepository.findBySpecialty(invoice.getSpecialty()).orElse(null);
            if (pricing != null) {
                invoice.setAmount(pricing.getConsultationFee());
            } else {
                invoice.setAmount(DEFAULT_CONSULTATION_FEE);
            }
        } else {
            invoice.setAmount(DEFAULT_CONSULTATION_FEE);
        }

        // Calculate tax
        BigDecimal taxAmount = invoice.getAmount().multiply(TAX_RATE);
        invoice.setTax(taxAmount);
        invoice.setTotal(invoice.getAmount().add(taxAmount));

        if (invoice.getStatus() == null) {
            invoice.setStatus("PENDING");
        }

        Invoice savedInvoice = invoiceRepository.save(invoice);

        // Publish invoice created event
        publishPaymentEvent("invoice.created", savedInvoice);

        return convertToDTO(savedInvoice);
    }

    @Transactional
    public InvoiceDTO updateInvoice(Long id, Invoice updatedInvoice) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Invoice not found with id: " + id));

        if (updatedInvoice.getStatus() != null) {
            invoice.setStatus(updatedInvoice.getStatus());
        }
        if (updatedInvoice.getDescription() != null) {
            invoice.setDescription(updatedInvoice.getDescription());
        }
        if (updatedInvoice.getDueDate() != null) {
            invoice.setDueDate(updatedInvoice.getDueDate());
        }

        Invoice savedInvoice = invoiceRepository.save(invoice);
        return convertToDTO(savedInvoice);
    }

    // Payment Methods
    public List<PaymentDTO> getPaymentsByInvoiceId(Long invoiceId) {
        return paymentRepository.findByInvoiceId(invoiceId).stream()
                .map(this::convertToPaymentDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public PaymentDTO recordPayment(PaymentDTO paymentDTO) {
        Invoice invoice = invoiceRepository.findById(paymentDTO.getInvoiceId())
                .orElseThrow(() -> new RuntimeException("Invoice not found with id: " + paymentDTO.getInvoiceId()));

        Payment payment = new Payment();
        payment.setInvoiceId(paymentDTO.getInvoiceId());
        payment.setAmount(paymentDTO.getAmount());
        payment.setPaymentMethod(paymentDTO.getPaymentMethod());
        payment.setTransactionId(paymentDTO.getTransactionId());
        payment.setStatus(paymentDTO.getStatus() != null ? paymentDTO.getStatus() : "SUCCESS");
        payment.setNotes(paymentDTO.getNotes());

        Payment savedPayment = paymentRepository.save(payment);

        // Update invoice status based on total payments
        updateInvoicePaymentStatus(invoice);

        // Publish payment received event
        publishPaymentEvent("payment.received", invoice);

        return convertToPaymentDTO(savedPayment);
    }

    private void updateInvoicePaymentStatus(Invoice invoice) {
        List<Payment> payments = paymentRepository.findByInvoiceId(invoice.getId());
        BigDecimal totalPaid = payments.stream()
                .filter(p -> "SUCCESS".equals(p.getStatus()))
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalPaid.compareTo(invoice.getTotal()) >= 0) {
            invoice.setStatus("PAID");
            invoice.setPaidDate(LocalDateTime.now());
        } else if (totalPaid.compareTo(BigDecimal.ZERO) > 0) {
            invoice.setStatus("PARTIALLY_PAID");
        }

        invoiceRepository.save(invoice);
    }

    public BigDecimal getOutstandingBalance(String patientEmail) {
        List<Invoice> invoices = invoiceRepository.findByPatientEmail(patientEmail);
        BigDecimal totalOutstanding = BigDecimal.ZERO;

        for (Invoice invoice : invoices) {
            if (!"PAID".equals(invoice.getStatus()) && !"CANCELLED".equals(invoice.getStatus())) {
                List<Payment> payments = paymentRepository.findByInvoiceId(invoice.getId());
                BigDecimal totalPaid = payments.stream()
                        .filter(p -> "SUCCESS".equals(p.getStatus()))
                        .map(Payment::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal outstanding = invoice.getTotal().subtract(totalPaid);
                totalOutstanding = totalOutstanding.add(outstanding);
            }
        }

        return totalOutstanding;
    }

    // Helper methods
    private InvoiceDTO convertToDTO(Invoice invoice) {
        InvoiceDTO dto = new InvoiceDTO();
        dto.setId(invoice.getId());
        dto.setRdvId(invoice.getRdvId());
        dto.setPatientEmail(invoice.getPatientEmail());
        dto.setPatientName(invoice.getPatientName());
        dto.setDoctorName(invoice.getDoctorName());
        dto.setSpecialty(invoice.getSpecialty());
        dto.setAmount(invoice.getAmount());
        dto.setTax(invoice.getTax());
        dto.setTotal(invoice.getTotal());
        dto.setStatus(invoice.getStatus());
        dto.setDescription(invoice.getDescription());
        dto.setCreatedDate(invoice.getCreatedDate());
        dto.setDueDate(invoice.getDueDate());
        dto.setPaidDate(invoice.getPaidDate());
        return dto;
    }

    private PaymentDTO convertToPaymentDTO(Payment payment) {
        PaymentDTO dto = new PaymentDTO();
        dto.setId(payment.getId());
        dto.setInvoiceId(payment.getInvoiceId());
        dto.setAmount(payment.getAmount());
        dto.setPaymentMethod(payment.getPaymentMethod());
        dto.setPaymentDate(payment.getPaymentDate());
        dto.setTransactionId(payment.getTransactionId());
        dto.setStatus(payment.getStatus());
        dto.setNotes(payment.getNotes());
        return dto;
    }

    private void publishPaymentEvent(String routingKey, Invoice invoice) {
        try {
            PaymentEvent event = new PaymentEvent();
            event.setEventType(routingKey);
            event.setInvoiceId(invoice.getId());
            event.setPatientEmail(invoice.getPatientEmail());
            event.setPatientName(invoice.getPatientName());
            event.setAmount(invoice.getTotal());
            event.setStatus(invoice.getStatus());
            event.setTimestamp(LocalDateTime.now());

            rabbitTemplate.convertAndSend(EXCHANGE, routingKey, event);
        } catch (Exception e) {
            // Log error but don't fail the transaction
            System.err.println("Failed to publish payment event: " + e.getMessage());
        }
    }
}
