package org.kfokam48.cliniquemanagementbackend.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Entity
public class LigneFacture {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String serviceName;
    private int quantite;
    private BigDecimal prixUnitaire;
    private BigDecimal prixTotal;

    @ManyToOne
    private Facture facture;
}

