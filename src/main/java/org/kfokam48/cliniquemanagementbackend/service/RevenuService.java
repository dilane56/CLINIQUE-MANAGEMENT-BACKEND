package org.kfokam48.cliniquemanagementbackend.service;

import org.kfokam48.cliniquemanagementbackend.dto.RevenuDTO;
import org.kfokam48.cliniquemanagementbackend.dto.RevenuMedecinDTO;
import org.kfokam48.cliniquemanagementbackend.repository.PaiementRepository;
import org.kfokam48.cliniquemanagementbackend.repository.PaiementRepository.LignePaiementParService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Revenus calculés à partir des paiements réellement encaissés, à leur date (I22) :
 * un paiement par tranche compte dans le mois de chaque versement.
 */
@Service
@Transactional(readOnly = true)
public class RevenuService {

    private final PaiementRepository paiementRepository;

    public RevenuService(PaiementRepository paiementRepository) {
        this.paiementRepository = paiementRepository;
    }

    public RevenuDTO getRevenuMensuel() {
        LocalDate debutMois = LocalDate.now().withDayOfMonth(1);

        // Revenus du mois actuel et du mois précédent
        BigDecimal revenuMensuel = encaisseEntre(debutMois, debutMois.plusMonths(1));
        BigDecimal revenuMoisPrecedent = encaisseEntre(debutMois.minusMonths(1), debutMois);

        // Calcul du pourcentage d'évolution
        Double pourcentageEvolution = calculerPourcentageEvolution(revenuMensuel, revenuMoisPrecedent);

        return new RevenuDTO(revenuMensuel, revenuMoisPrecedent, pourcentageEvolution);
    }

    /**
     * Revenus encaissés par chaque médecin entre debut et fin (inclus), répartis par service.
     * Un paiement est réparti entre les lignes de sa facture au prorata de leur montant.
     * Triés du médecin qui a le plus encaissé au moins.
     */
    public List<RevenuMedecinDTO> getRevenusParMedecin(LocalDate debut, LocalDate fin) {
        if (fin.isBefore(debut)) {
            throw new IllegalArgumentException("La date de fin doit être postérieure ou égale à la date de début.");
        }
        List<LignePaiementParService> lignes = paiementRepository.lignesPaiementParService(
                debut.atStartOfDay(), fin.plusDays(1).atStartOfDay());

        Map<Long, CumulMedecin> parMedecin = new LinkedHashMap<>();
        for (LignePaiementParService ligne : lignes) {
            CumulMedecin cumul = parMedecin.computeIfAbsent(ligne.getMedecinId(),
                    id -> new CumulMedecin(id, ligne.getMedecinNom(), ligne.getMedecinPrenom()));
            // Total exact : chaque paiement n'est compté qu'une fois, quel que soit son nombre de lignes
            if (cumul.paiementsComptes.add(ligne.getPaiementId())) {
                cumul.total = cumul.total.add(ligne.getMontantPaiement());
            }
            BigDecimal montantFacture = ligne.getMontantFacture();
            if (montantFacture != null && montantFacture.signum() > 0 && ligne.getMontantLigne() != null) {
                BigDecimal part = ligne.getMontantPaiement().multiply(ligne.getMontantLigne())
                        .divide(montantFacture, 10, RoundingMode.HALF_UP);
                cumul.parService.merge(ligne.getService(), part, BigDecimal::add);
            }
        }

        return parMedecin.values().stream()
                .map(CumulMedecin::versDto)
                .sorted(Comparator.comparing(RevenuMedecinDTO::totalEncaisse).reversed())
                .toList();
    }

    private BigDecimal encaisseEntre(LocalDate debutInclus, LocalDate finExclue) {
        return paiementRepository.sommeEncaisseeEntre(debutInclus.atStartOfDay(), finExclue.atStartOfDay());
    }

    private Double calculerPourcentageEvolution(BigDecimal revenuActuel, BigDecimal revenuPrecedent) {
        if (revenuPrecedent.compareTo(BigDecimal.ZERO) == 0) {
            return revenuActuel.compareTo(BigDecimal.ZERO) > 0 ? 100.0 : 0.0;
        }

        BigDecimal difference = revenuActuel.subtract(revenuPrecedent);
        BigDecimal pourcentage = difference.divide(revenuPrecedent, 4, RoundingMode.HALF_UP)
                                          .multiply(BigDecimal.valueOf(100));

        return pourcentage.doubleValue();
    }

    private static final class CumulMedecin {
        private final Long id;
        private final String nom;
        private final String prenom;
        private final Set<Long> paiementsComptes = new HashSet<>();
        private final Map<String, BigDecimal> parService = new TreeMap<>(Comparator.nullsLast(Comparator.naturalOrder()));
        private BigDecimal total = BigDecimal.ZERO;

        private CumulMedecin(Long id, String nom, String prenom) {
            this.id = id;
            this.nom = nom;
            this.prenom = prenom;
        }

        private RevenuMedecinDTO versDto() {
            List<RevenuMedecinDTO.RevenuParService> services = parService.entrySet().stream()
                    .map(e -> new RevenuMedecinDTO.RevenuParService(e.getKey(), e.getValue().setScale(2, RoundingMode.HALF_UP)))
                    .toList();
            return new RevenuMedecinDTO(id, nom, prenom, total.setScale(2, RoundingMode.HALF_UP), services);
        }
    }
}
