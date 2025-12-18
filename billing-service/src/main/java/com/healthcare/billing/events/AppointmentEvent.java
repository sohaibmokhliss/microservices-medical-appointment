package com.healthcare.billing.events;

import java.io.Serializable;
import java.time.LocalDateTime;

public class AppointmentEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    private String eventType; // CREATED, UPDATED, CANCELLED
    private Long rdvId;
    private Long docteurId;
    private String docteurNom;
    private String docteurPrenom;
    private String specialite;
    private String patientNom;
    private String patientPrenom;
    private String patientEmail;
    private String patientTelephone;
    private LocalDateTime dateHeure;
    private String motif;
    private String statut;

    // Getters and Setters
    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public Long getRdvId() {
        return rdvId;
    }

    public void setRdvId(Long rdvId) {
        this.rdvId = rdvId;
    }

    public Long getDocteurId() {
        return docteurId;
    }

    public void setDocteurId(Long docteurId) {
        this.docteurId = docteurId;
    }

    public String getDocteurNom() {
        return docteurNom;
    }

    public void setDocteurNom(String docteurNom) {
        this.docteurNom = docteurNom;
    }

    public String getDocteurPrenom() {
        return docteurPrenom;
    }

    public void setDocteurPrenom(String docteurPrenom) {
        this.docteurPrenom = docteurPrenom;
    }

    public String getSpecialite() {
        return specialite;
    }

    public void setSpecialite(String specialite) {
        this.specialite = specialite;
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
