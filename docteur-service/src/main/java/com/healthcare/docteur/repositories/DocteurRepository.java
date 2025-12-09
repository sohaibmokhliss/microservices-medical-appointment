package com.healthcare.docteur.repositories;

import com.healthcare.docteur.entities.Docteur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocteurRepository extends JpaRepository<Docteur, Long> {
}
