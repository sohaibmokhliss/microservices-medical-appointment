package com.healthcare.rdv.controllers;

import com.healthcare.rdv.clients.DocteurClient;
import com.healthcare.rdv.clients.DocteurDTO;
import com.healthcare.rdv.entities.Rdv;
import com.healthcare.rdv.repositories.RdvRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rdv")
@CrossOrigin(origins = "*")
public class RdvController {

    @Autowired
    private RdvRepository rdvRepository;

    @Autowired
    private DocteurClient docteurClient;

    @GetMapping
    public List<Rdv> getAllRdv() {
        return rdvRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getRdvById(@PathVariable Long id) {
        return rdvRepository.findById(id)
                .map(rdv -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("rdv", rdv);
                    try {
                        DocteurDTO docteur = docteurClient.getDocteur(rdv.getDocteurId());
                        response.put("docteur", docteur);
                    } catch (Exception e) {
                        response.put("docteur", null);
                    }
                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/docteur/{docteurId}")
    public List<Rdv> getRdvByDocteur(@PathVariable Long docteurId) {
        return rdvRepository.findByDocteurId(docteurId);
    }

    @PostMapping
    public ResponseEntity<Rdv> createRdv(@RequestBody Rdv rdv) {
        try {
            DocteurDTO docteur = docteurClient.getDocteur(rdv.getDocteurId());
            if (docteur == null) {
                return ResponseEntity.badRequest().build();
            }
            rdv.setStatut("CONFIRMÉ");
            Rdv savedRdv = rdvRepository.save(rdv);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedRdv);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Rdv> updateRdv(@PathVariable Long id, @RequestBody Rdv rdvDetails) {
        return rdvRepository.findById(id)
                .map(rdv -> {
                    rdv.setDateHeure(rdvDetails.getDateHeure());
                    rdv.setMotif(rdvDetails.getMotif());
                    rdv.setStatut(rdvDetails.getStatut());
                    rdv.setPatientNom(rdvDetails.getPatientNom());
                    rdv.setPatientPrenom(rdvDetails.getPatientPrenom());
                    rdv.setPatientEmail(rdvDetails.getPatientEmail());
                    rdv.setPatientTelephone(rdvDetails.getPatientTelephone());
                    return ResponseEntity.ok(rdvRepository.save(rdv));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRdv(@PathVariable Long id) {
        return rdvRepository.findById(id)
                .map(rdv -> {
                    rdvRepository.delete(rdv);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
