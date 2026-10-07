package org.kfokam48.cliniquemanagementbackend.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.kfokam48.cliniquemanagementbackend.enums.ModePayement;
import org.kfokam48.cliniquemanagementbackend.enums.StatutFacture;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Facture {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private BigDecimal montantTotal;
    private LocalDateTime dateEmission;
    private LocalDateTime datePayement;
    private BigDecimal montantPayement;
    private BigDecimal montantRestant;
    @Enumerated(EnumType.STRING)
    private StatutFacture statut;

    @OneToMany(mappedBy = "facture", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LigneFacture> lignes = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "rendezvous_id")
    private RendezVous rendezVous;

    /**
     * Une facture n'est modifiable (lignes ou contenu) que tant qu'aucun paiement n'a été enregistré :
     * une facture payée, partiellement payée ou annulée ne change plus de montant.
     */
    public void verifierModifiable() {
        if (statut != StatutFacture.NON_PAYEE) {
            throw new IllegalStateException("La facture #" + id + " ne peut plus être modifiée : statut " + statut
                    + ". Les montants sont figés dès qu'un paiement a été enregistré.");
        }
    }

    // Recalcule le total (somme des lignes) et le reste à payer
    public void recalculerMontants() {
        BigDecimal total = lignes.stream()
                .map(LigneFacture::getPrixTotal)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal dejaPaye = montantPayement != null ? montantPayement : BigDecimal.ZERO;
        this.montantTotal = total;
        this.montantRestant = total.subtract(dejaPaye);
    }
}
