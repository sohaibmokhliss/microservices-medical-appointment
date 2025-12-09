package com.healthcare.docteur;

import com.healthcare.docteur.entities.Docteur;
import com.healthcare.docteur.repositories.DocteurRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(DocteurRepository repository) {
        return args -> {
            // Only initialize if database is empty
            if (repository.count() > 0) {
                return;
            }

            Docteur d1 = new Docteur();
            d1.setNom("Alami");
            d1.setPrenom("Ahmed");
            d1.setSpecialite("Cardiologie");
            d1.setEmail("ahmed.alami@hospital.ma");
            d1.setTelephone("0661234567");
            repository.save(d1);

            Docteur d2 = new Docteur();
            d2.setNom("Bennani");
            d2.setPrenom("Fatima");
            d2.setSpecialite("Pédiatrie");
            d2.setEmail("fatima.bennani@hospital.ma");
            d2.setTelephone("0662345678");
            repository.save(d2);

            Docteur d3 = new Docteur();
            d3.setNom("Cohen");
            d3.setPrenom("David");
            d3.setSpecialite("Dermatologie");
            d3.setEmail("david.cohen@hospital.ma");
            d3.setTelephone("0663456789");
            repository.save(d3);

            Docteur d4 = new Docteur();
            d4.setNom("Douiri");
            d4.setPrenom("Sanaa");
            d4.setSpecialite("Gynécologie");
            d4.setEmail("sanaa.douiri@hospital.ma");
            d4.setTelephone("0664567890");
            repository.save(d4);

            Docteur d5 = new Docteur();
            d5.setNom("El Amrani");
            d5.setPrenom("Karim");
            d5.setSpecialite("Neurologie");
            d5.setEmail("karim.elamrani@hospital.ma");
            d5.setTelephone("0665678901");
            repository.save(d5);
        };
    }
}
