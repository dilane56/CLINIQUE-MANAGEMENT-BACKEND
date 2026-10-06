package org.kfokam48.cliniquemanagementbackend.repository;


import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.kfokam48.cliniquemanagementbackend.model.Patient;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;



@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {
    Optional<Patient> findByEmail(String email);

    // Verrou en écriture (SELECT ... FOR UPDATE) : sérialise les réservations d'un même patient
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Patient p WHERE p.id = :id")
    Optional<Patient> findByIdForUpdate(@Param("id") Long id);
    @Query("SELECT DISTINCT r.patient FROM RendezVous r WHERE r.medecin.id = :medecinId")
    List<Patient> findPatientsByMedecinId(@Param("medecinId") Long medecinId);

    @Query("SELECT p FROM Patient p WHERE EXISTS (SELECT 1 FROM RendezVous r WHERE r.patient = p AND r.medecin.id = :medecinId)")
    Page<Patient> findPatientsByMedecinId(@Param("medecinId") Long medecinId, Pageable pageable);


}
