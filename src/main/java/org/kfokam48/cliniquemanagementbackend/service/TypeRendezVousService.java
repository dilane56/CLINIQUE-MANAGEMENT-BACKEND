package org.kfokam48.cliniquemanagementbackend.service;

import org.kfokam48.cliniquemanagementbackend.dto.typeRendezVous.TypeRendezVousDTO;
import org.kfokam48.cliniquemanagementbackend.dto.typeRendezVous.TypeRendezVousResponseDTO;
import org.kfokam48.cliniquemanagementbackend.model.TypeRendezVous;

import java.util.List;

public interface TypeRendezVousService {
    TypeRendezVousResponseDTO ajouterTypeRendezVous(TypeRendezVousDTO typeRendezVousDTO);
    TypeRendezVousResponseDTO modifierTypeRendezVous(Long id, TypeRendezVousDTO typeRendezVousDTO);
    void supprimerTypeRendezVous(Long id);
    List<TypeRendezVousResponseDTO> listerTypeRendezVous();
}

