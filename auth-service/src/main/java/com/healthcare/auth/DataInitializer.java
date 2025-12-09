package com.healthcare.auth;

import com.healthcare.auth.entities.User;
import com.healthcare.auth.repositories.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(UserRepository repository, PasswordEncoder passwordEncoder) {
        return args -> {
            // Only initialize if database is empty
            if (repository.count() > 0) {
                return;
            }

            // Create admin user (only one, hardcoded)
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setEmail("admin@hospital.ma");
            admin.setRole("ADMIN");
            admin.setEnabled(true);
            repository.save(admin);

            // Create receptionist users
            User u1 = new User();
            u1.setUsername("sohaib");
            u1.setPassword(passwordEncoder.encode("root1312"));
            u1.setEmail("sohaib@hospital.ma");
            u1.setRole("RECEPTIONIST");
            u1.setEnabled(true);
            repository.save(u1);

            User u2 = new User();
            u2.setUsername("othmane");
            u2.setPassword(passwordEncoder.encode("root1312"));
            u2.setEmail("othmane@hospital.ma");
            u2.setRole("RECEPTIONIST");
            u2.setEnabled(true);
            repository.save(u2);

            System.out.println("✓ DataInitializer: Created 1 admin and 2 receptionist users");
        };
    }
}
