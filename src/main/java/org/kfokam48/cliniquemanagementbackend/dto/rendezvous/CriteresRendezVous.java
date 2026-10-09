package org.kfokam48.cliniquemanagementbackend.dto.rendezvous;

import org.kfokam48.cliniquemanagementbackend.enums.StatutRendezVous;

import java.time.LocalDate;
import java.util.List;

/**
 * Critères de GET /api/rendezvous/recherche. Chaque critère absent (null ou vide) est ignoré.
 *
 * @param texte       nom ou prénom du patient ou du médecin, ou libellé du type
 * @param statuts     un ou plusieurs statuts (?statut=EN_COURS&statut=TERMINE)
 * @param date        un seul jour
 * @param debut       premier jour de la période (inclus)
 * @param fin         dernier jour de la période (inclus)
 * @param medecinId   rendez-vous de ce médecin
 * @param sansFacture true : seulement les rendez-vous qui n'ont pas encore de facture
 */
public record CriteresRendezVous(
        String texte,
        List<StatutRendezVous> statuts,
        LocalDate date,
        LocalDate debut,
        LocalDate fin,
        Long medecinId,
        Boolean sansFacture) {

    public static CriteresRendezVous de(String texte, StatutRendezVous statut, LocalDate date) {
        return new CriteresRendezVous(texte, statut == null ? null : List.of(statut), date, null, null, null, null);
    }
}
