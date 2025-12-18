package com.healthcare.billing.repositories;

import com.healthcare.billing.entities.Pricing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PricingRepository extends JpaRepository<Pricing, Long> {
    Optional<Pricing> findBySpecialty(String specialty);
}
