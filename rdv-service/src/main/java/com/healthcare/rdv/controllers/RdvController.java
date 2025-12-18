package com.healthcare.rdv.controllers;

import com.healthcare.rdv.clients.DocteurClient;
import com.healthcare.rdv.clients.DocteurDTO;
import com.healthcare.rdv.entities.Rdv;
import com.healthcare.rdv.exceptions.BadRequestException;
import com.healthcare.rdv.exceptions.ResourceNotFoundException;
import com.healthcare.rdv.repositories.RdvRepository;
import com.healthcare.rdv.services.AppointmentEventPublisher;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rdv")
public class RdvController {

    private static final Logger log = LoggerFactory.getLogger(RdvController.class);

    @Autowired
    private RdvRepository rdvRepository;

    @Autowired
    private DocteurClient docteurClient;
    
    @Autowired
    private AppointmentEventPublisher eventPublisher;

    @GetMapping
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR', 'ADMIN', 'RECEPTIONIST')")
    public List<Rdv> getAllRdv() {
        log.info("Fetching all appointments");
        List<Rdv> rdvs = rdvRepository.findAll();
        log.info("Found {} appointments", rdvs.size());
        return rdvs;
    }

    @GetMapping("/{id}")
    @CircuitBreaker(name = "docteurService", fallbackMethod = "getRdvByIdFallback")
    @Retry(name = "docteurService")
    public ResponseEntity<Map<String, Object>> getRdvById(@PathVariable Long id) {
        log.info("Fetching appointment with ID: {}", id);
        
        Rdv rdv = rdvRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", "id", id));
        
        Map<String, Object> response = new HashMap<>();
        response.put("rdv", rdv);
        
        try {
            DocteurDTO docteur = docteurClient.getDocteur(rdv.getDocteurId());
            response.put("docteur", docteur);
            log.info("Successfully fetched appointment and doctor details for ID: {}", id);
        } catch (Exception e) {
            log.warn("Could not fetch doctor details for appointment {}: {}", id, e.getMessage());
            response.put("docteur", null);
        }
        
        return ResponseEntity.ok(response);
    }
    
    public ResponseEntity<Map<String, Object>> getRdvByIdFallback(Long id, Exception e) {
        log.error("Circuit breaker fallback triggered for getRdvById. ID: {}, Error: {}", id, e.getMessage());
        
        Rdv rdv = rdvRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", "id", id));
        
        Map<String, Object> response = new HashMap<>();
        response.put("rdv", rdv);
        response.put("docteur", null);
        response.put("message", "Doctor service temporarily unavailable");
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/docteur/{docteurId}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    public List<Rdv> getRdvByDocteur(@PathVariable Long docteurId) {
        log.info("Fetching appointments for doctor ID: {}", docteurId);
        List<Rdv> rdvs = rdvRepository.findByDocteurId(docteurId);
        log.info("Found {} appointments for doctor {}", rdvs.size(), docteurId);
        return rdvs;
    }

    @PostMapping
    @CircuitBreaker(name = "docteurService", fallbackMethod = "createRdvFallback")
    @Retry(name = "docteurService")
    public ResponseEntity<Rdv> createRdv(@Valid @RequestBody Rdv rdv) {
        log.info("Creating appointment for patient: {} {}", rdv.getPatientPrenom(), rdv.getPatientNom());
        
        // Validate doctor exists
        try {
            DocteurDTO docteur = docteurClient.getDocteur(rdv.getDocteurId());
            if (docteur == null) {
                log.warn("Doctor with ID {} not found", rdv.getDocteurId());
                throw new BadRequestException("Doctor not found with ID: " + rdv.getDocteurId());
            }
            log.info("Doctor validation successful for ID: {}", rdv.getDocteurId());
        } catch (Exception e) {
            log.error("Error validating doctor: {}", e.getMessage());
            throw new BadRequestException("Unable to validate doctor. Please try again.");
        }
        
        rdv.setStatut("CONFIRMÉ");
        Rdv savedRdv = rdvRepository.save(rdv);
        log.info("Appointment created successfully with ID: {}", savedRdv.getId());
        
        // Publish event for async notification
        eventPublisher.publishAppointmentCreated(savedRdv);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(savedRdv);
    }
    
    public ResponseEntity<Rdv> createRdvFallback(Rdv rdv, Exception e) {
        log.error("Circuit breaker fallback triggered for createRdv. Error: {}", e.getMessage());
        
        rdv.setStatut("EN ATTENTE");
        Rdv savedRdv = rdvRepository.save(rdv);
        log.info("Appointment created with pending status. ID: {}", savedRdv.getId());
        
        // Still publish event even with fallback
        eventPublisher.publishAppointmentCreated(savedRdv);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(savedRdv);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('PATIENT', 'ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<Rdv> updateRdv(@PathVariable Long id, @Valid @RequestBody Rdv rdvDetails) {
        log.info("Updating appointment with ID: {}", id);
        
        Rdv rdv = rdvRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", "id", id));
        
        rdv.setDateHeure(rdvDetails.getDateHeure());
        rdv.setMotif(rdvDetails.getMotif());
        rdv.setStatut(rdvDetails.getStatut());
        rdv.setPatientNom(rdvDetails.getPatientNom());
        rdv.setPatientPrenom(rdvDetails.getPatientPrenom());
        rdv.setPatientEmail(rdvDetails.getPatientEmail());
        rdv.setPatientTelephone(rdvDetails.getPatientTelephone());
        
        Rdv updatedRdv = rdvRepository.save(rdv);
        log.info("Appointment {} updated successfully", id);
        
        // Publish update event
        eventPublisher.publishAppointmentUpdated(updatedRdv);
        
        return ResponseEntity.ok(updatedRdv);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<Void> deleteRdv(@PathVariable Long id) {
        log.info("Deleting appointment with ID: {}", id);
        
        Rdv rdv = rdvRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", "id", id));
        
        // Publish cancellation event before deleting
        eventPublisher.publishAppointmentCancelled(rdv);
        
        rdvRepository.delete(rdv);
        log.info("Appointment {} deleted successfully", id);
        
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/welcome")
    public String welcome() {
        return "Welcome to RDV Service!";
    }
}
