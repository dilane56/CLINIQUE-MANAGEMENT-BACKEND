package org.kfokam48.cliniquemanagementbackend.repository;

import org.kfokam48.cliniquemanagementbackend.model.LignePrescription;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LignePrescriptionRepository extends JpaRepository<LignePrescription, Long> {
    // Contrôle de propriété (@authz) : cette ligne appartient-elle à une prescription de ce médecin ?
    boolean existsByIdAndPrescription_RendezVous_Medecin_Id(Long id, Long medecinId);
}

