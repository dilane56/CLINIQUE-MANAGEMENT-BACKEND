package org.kfokam48.cliniquemanagementbackend.repository;


import org.kfokam48.cliniquemanagementbackend.enums.StatutRendezVous;
import org.kfokam48.cliniquemanagementbackend.model.Medecin;
import org.kfokam48.cliniquemanagementbackend.model.RendezVous;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RendezVousRepository extends JpaRepository<RendezVous, Long> {
    boolean existsByMedecinAndDateRendezVousBetween(Medecin medecin, LocalDateTime dateRendezVous, LocalDateTime dateTimeFinRendezVousPossible);
    List<RendezVous> findByMedecinId(Long medecinId);
    List<RendezVous> findByMedecinIdAndDateRendezVousBetween(Long medecinId, LocalDateTime debut, LocalDateTime fin);
    boolean existsByPatientIdAndDateRendezVousBetween(Long patientId, LocalDateTime startOfDay, LocalDateTime endOfDay);
    //List<RendezVous> findAllByStatutRendezVous_En_ATTENTE();
    List<RendezVous> findAllByStatutRendezVous(StatutRendezVous statutRendezVous);

}
