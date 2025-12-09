package com.healthcare.rdv.repositories;

import com.healthcare.rdv.entities.Rdv;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RdvRepository extends JpaRepository<Rdv, Long> {
    List<Rdv> findByDocteurId(Long docteurId);
}
