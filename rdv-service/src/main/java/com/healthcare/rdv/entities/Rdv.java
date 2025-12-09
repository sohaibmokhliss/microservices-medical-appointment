package com.healthcare.rdv.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

@Entity
public class Rdv {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Doctor ID is required")
    private Long docteurId;

    @NotBlank(message = "Patient last name is required")
    @Size(min = 2, max = 100, message = "Patient last name must be between 2 and 100 characters")
    private String patientNom;

    @NotBlank(message = "Patient first name is required")
    @Size(min = 2, max = 100, message = "Patient first name must be between 2 and 100 characters")
    private String patientPrenom;

    @NotBlank(message = "Patient email is required")
    @Email(message = "Invalid email format")
    private String patientEmail;

    @NotBlank(message = "Patient phone number is required")
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Invalid phone number format")
    private String patientTelephone;

    @NotNull(message = "Appointment date and time is required")
    @Future(message = "Appointment must be in the future")
    private LocalDateTime dateHeure;

    @NotBlank(message = "Appointment reason is required")
    @Size(min = 5, max = 500, message = "Reason must be between 5 and 500 characters")
    private String motif;

    private String statut;

    public Rdv() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDocteurId() {
        return docteurId;
    }

    public void setDocteurId(Long docteurId) {
        this.docteurId = docteurId;
    }

    public String getPatientNom() {
        return patientNom;
    }

    public void setPatientNom(String patientNom) {
        this.patientNom = patientNom;
    }

    public String getPatientPrenom() {
        return patientPrenom;
    }

    public void setPatientPrenom(String patientPrenom) {
        this.patientPrenom = patientPrenom;
    }

    public String getPatientEmail() {
        return patientEmail;
    }

    public void setPatientEmail(String patientEmail) {
        this.patientEmail = patientEmail;
    }

    public String getPatientTelephone() {
        return patientTelephone;
    }

    public void setPatientTelephone(String patientTelephone) {
        this.patientTelephone = patientTelephone;
    }

    public LocalDateTime getDateHeure() {
        return dateHeure;
    }

    public void setDateHeure(LocalDateTime dateHeure) {
        this.dateHeure = dateHeure;
    }

    public String getMotif() {
        return motif;
    }

    public void setMotif(String motif) {
        this.motif = motif;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }
}
