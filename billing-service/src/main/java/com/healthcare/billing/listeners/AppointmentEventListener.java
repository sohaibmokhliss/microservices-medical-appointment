package com.healthcare.billing.listeners;

import com.healthcare.billing.entities.Invoice;
import com.healthcare.billing.events.AppointmentEvent;
import com.healthcare.billing.services.BillingService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AppointmentEventListener {

    @Autowired
    private BillingService billingService;

    @RabbitListener(queues = "appointment.billing.queue")
    public void handleAppointmentEvent(AppointmentEvent event) {
        System.out.println("Received appointment event: " + event.getEventType() + " for RDV ID: " + event.getRdvId());

        try {
            if ("CREATED".equals(event.getEventType())) {
                // Auto-generate invoice for new appointment
                Invoice invoice = new Invoice();
                invoice.setRdvId(event.getRdvId());
                invoice.setPatientEmail(event.getPatientEmail());
                invoice.setPatientName(event.getPatientNom() + " " + event.getPatientPrenom());
                invoice.setDoctorName("Dr. " + event.getDocteurNom() + " " + event.getDocteurPrenom());
                invoice.setSpecialty(event.getSpecialite());
                invoice.setDescription("Consultation - " + event.getMotif());
                invoice.setStatus("PENDING");

                billingService.createInvoice(invoice);
                System.out.println("Invoice created successfully for RDV ID: " + event.getRdvId());
            }
        } catch (Exception e) {
            System.err.println("Error processing appointment event: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
