package org.kfokam48.cliniquemanagementbackend.repository;


import org.kfokam48.cliniquemanagementbackend.model.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {
    @Query("SELECT p FROM Prescription p WHERE p.rendezVous.medecin.id = :medecinId")
    List<Prescription> findByMedecinId(@Param("medecinId") Long medecinId);
}
