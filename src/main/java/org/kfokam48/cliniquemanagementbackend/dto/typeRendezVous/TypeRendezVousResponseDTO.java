package org.kfokam48.cliniquemanagementbackend.dto.typeRendezVous;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TypeRendezVousResponseDTO {
   private Long id; // Identifiant unique du type de rendez-vous
    private String libelle;
    private int duree;// Libellé du type de rendez-vous
    private BigDecimal tarif;
}
