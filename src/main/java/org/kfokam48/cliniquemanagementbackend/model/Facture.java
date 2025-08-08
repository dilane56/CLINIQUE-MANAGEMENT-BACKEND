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



}
