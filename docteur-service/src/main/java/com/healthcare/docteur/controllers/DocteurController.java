package com.healthcare.docteur.controllers;

import com.healthcare.docteur.entities.Docteur;
import com.healthcare.docteur.repositories.DocteurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/docteurs")
public class DocteurController {

    @Autowired
    private DocteurRepository docteurRepository;

    @GetMapping
    public List<Docteur> getAllDocteurs() {
        return docteurRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Docteur> getDocteurById(@PathVariable Long id) {
        return docteurRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Docteur> createDocteur(@RequestBody Docteur docteur) {
        try {
            Docteur savedDocteur = docteurRepository.save(docteur);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedDocteur);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Docteur> updateDocteur(@PathVariable Long id, @RequestBody Docteur docteurDetails) {
        return docteurRepository.findById(id)
                .map(docteur -> {
                    docteur.setNom(docteurDetails.getNom());
                    docteur.setPrenom(docteurDetails.getPrenom());
                    docteur.setSpecialite(docteurDetails.getSpecialite());
                    docteur.setEmail(docteurDetails.getEmail());
                    docteur.setTelephone(docteurDetails.getTelephone());
                    return ResponseEntity.ok(docteurRepository.save(docteur));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocteur(@PathVariable Long id) {
        return docteurRepository.findById(id)
                .map(docteur -> {
                    docteurRepository.delete(docteur);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
