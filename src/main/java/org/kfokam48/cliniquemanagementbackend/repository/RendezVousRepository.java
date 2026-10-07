package org.kfokam48.cliniquemanagementbackend.repository;


import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.kfokam48.cliniquemanagementbackend.enums.StatutRendezVous;
import org.kfokam48.cliniquemanagementbackend.model.RendezVous;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Repository
public interface RendezVousRepository extends JpaRepository<RendezVous, Long>, JpaSpecificationExecutor<RendezVous> {
    List<RendezVous> findByMedecinId(Long medecinId);
    Page<RendezVous> findByMedecinId(Long medecinId, Pageable pageable);
    List<RendezVous> findByMedecinIdAndDateRendezVousBetween(Long medecinId, LocalDateTime debut, LocalDateTime fin);
    List<RendezVous> findAllByStatutRendezVous(StatutRendezVous statutRendezVous);

    // Nombre de rendez-vous par statut (compteurs du tableau de bord) : [statut, nombre]
    @Query("SELECT r.statutRendezVous, COUNT(r) FROM RendezVous r GROUP BY r.statutRendezVous")
    List<Object[]> compterParStatut();

    /**
     * Vrai si le médecin a déjà un rendez-vous actif qui chevauche [debut, fin[.
     * Deux créneaux se chevauchent si l'un commence avant la fin de l'autre et finit après son début :
     * des rendez-vous qui se suivent (fin de l'un = début de l'autre) ne se chevauchent pas.
     * excludeId : rendez-vous à ignorer (celui en cours de modification), 0 pour une création.
     */
    @Query("""
            SELECT COUNT(r) > 0 FROM RendezVous r
            WHERE r.medecin.id = :medecinId
              AND r.id <> :excludeId
              AND r.statutRendezVous NOT IN :statutsLibres
              AND r.dateRendezVous < :fin
              AND r.dateTimeFinRendezVousPossible > :debut
            """)
    boolean existsChevauchementMedecin(@Param("medecinId") Long medecinId,
                                       @Param("debut") LocalDateTime debut,
                                       @Param("fin") LocalDateTime fin,
                                       @Param("excludeId") Long excludeId,
                                       @Param("statutsLibres") Collection<StatutRendezVous> statutsLibres);

    // Vrai si le patient a déjà un rendez-vous actif sur la période (un seul rendez-vous par jour)
    @Query("""
            SELECT COUNT(r) > 0 FROM RendezVous r
            WHERE r.patient.id = :patientId
              AND r.id <> :excludeId
              AND r.statutRendezVous NOT IN :statutsLibres
              AND r.dateRendezVous >= :debut
              AND r.dateRendezVous < :fin
            """)
    boolean existsRendezVousPatientSurPeriode(@Param("patientId") Long patientId,
                                              @Param("debut") LocalDateTime debut,
                                              @Param("fin") LocalDateTime fin,
                                              @Param("excludeId") Long excludeId,
                                              @Param("statutsLibres") Collection<StatutRendezVous> statutsLibres);

    // Contrôle de propriété (@authz) : ce rendez-vous appartient-il à ce médecin ?
    boolean existsByIdAndMedecin_Id(Long id, Long medecinId);
}
