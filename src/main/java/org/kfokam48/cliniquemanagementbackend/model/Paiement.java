package org.kfokam48.cliniquemanagementbackend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Un versement enregistré sur une facture (paiement complet ou tranche).
 * Les revenus sont calculés à partir des paiements, à leur date réelle (I22) : avant, seul le
 * cumul payé et la date du dernier versement étaient conservés sur la facture.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
public class Paiement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "facture_id")
    private Facture facture;

    @Column(nullable = false)
    private BigDecimal montant;

    @Column(nullable = false)
    private LocalDateTime datePaiement;

    public Paiement(Facture facture, BigDecimal montant, LocalDateTime datePaiement) {
        this.facture = facture;
        this.montant = montant;
        this.datePaiement = datePaiement;
    }
}
