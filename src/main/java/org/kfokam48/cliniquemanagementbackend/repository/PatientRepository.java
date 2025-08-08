package org.kfokam48.cliniquemanagementbackend.repository;


import org.kfokam48.cliniquemanagementbackend.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;



@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {
    Patient findByEmail(String email);
    @Query("SELECT DISTINCT r.patient FROM RendezVous r WHERE r.medecin.id = :medecinId")
    List<Patient> findPatientsByMedecinId(@Param("medecinId") Long medecinId);


}
