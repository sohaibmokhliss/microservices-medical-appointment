package com.healthcare.billing.repositories;

import com.healthcare.billing.entities.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    List<Invoice> findByPatientEmail(String patientEmail);
    List<Invoice> findByStatus(String status);
    Optional<Invoice> findByRdvId(Long rdvId);
}
