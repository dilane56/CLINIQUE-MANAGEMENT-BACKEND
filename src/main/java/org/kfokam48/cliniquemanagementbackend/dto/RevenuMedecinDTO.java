package org.kfokam48.cliniquemanagementbackend.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Revenus encaissés par un médecin sur une période, répartis par service (lignes de facture).
 * totalEncaisse est la somme exacte des paiements ; la répartition par service est calculée au
 * prorata du montant de chaque ligne dans sa facture (arrondie au centime).
 */
public record RevenuMedecinDTO(Long medecinId, String nom, String prenom, BigDecimal totalEncaisse,
                               List<RevenuParService> services) {

    public record RevenuParService(String service, BigDecimal montantEncaisse) {
    }
}
