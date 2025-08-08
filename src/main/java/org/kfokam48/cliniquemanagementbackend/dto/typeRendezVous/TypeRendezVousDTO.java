package org.kfokam48.cliniquemanagementbackend.dto.typeRendezVous;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TypeRendezVousDTO {
    private String libelle;
    private int duree;// Libellé du type de rendez-vous
    private BigDecimal tarif;
}
