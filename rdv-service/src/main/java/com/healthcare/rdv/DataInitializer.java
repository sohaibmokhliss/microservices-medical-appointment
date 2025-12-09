package com.healthcare.rdv;

import com.healthcare.rdv.entities.Rdv;
import com.healthcare.rdv.repositories.RdvRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(RdvRepository repository) {
        return args -> {
            // Only initialize if database is empty
            if (repository.count() > 0) {
                return;
            }

            Rdv rdv1 = new Rdv();
            rdv1.setDocteurId(1L);
            rdv1.setPatientNom("Idrissi");
            rdv1.setPatientPrenom("Mohamed");
            rdv1.setPatientEmail("mohamed.idrissi@email.com");
            rdv1.setPatientTelephone("0661111111");
            rdv1.setDateHeure(LocalDateTime.now().plusDays(1));
            rdv1.setMotif("Consultation cardiologie");
            rdv1.setStatut("CONFIRMÉ");
            repository.save(rdv1);

            Rdv rdv2 = new Rdv();
            rdv2.setDocteurId(2L);
            rdv2.setPatientNom("Lahlou");
            rdv2.setPatientPrenom("Amina");
            rdv2.setPatientEmail("amina.lahlou@email.com");
            rdv2.setPatientTelephone("0662222222");
            rdv2.setDateHeure(LocalDateTime.now().plusDays(2));
            rdv2.setMotif("Vaccination enfant");
            rdv2.setStatut("CONFIRMÉ");
            repository.save(rdv2);

            Rdv rdv3 = new Rdv();
            rdv3.setDocteurId(3L);
            rdv3.setPatientNom("Benjelloun");
            rdv3.setPatientPrenom("Hassan");
            rdv3.setPatientEmail("hassan.benjelloun@email.com");
            rdv3.setPatientTelephone("0663333333");
            rdv3.setDateHeure(LocalDateTime.now().plusDays(3));
            rdv3.setMotif("Problème de peau");
            rdv3.setStatut("EN ATTENTE");
            repository.save(rdv3);
        };
    }
}
