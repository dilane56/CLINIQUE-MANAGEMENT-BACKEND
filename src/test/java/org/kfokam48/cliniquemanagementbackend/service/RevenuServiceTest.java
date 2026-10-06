package org.kfokam48.cliniquemanagementbackend.service;

import org.junit.jupiter.api.Test;
import org.kfokam48.cliniquemanagementbackend.dto.RevenuDTO;
import org.kfokam48.cliniquemanagementbackend.dto.RevenuMedecinDTO;
import org.kfokam48.cliniquemanagementbackend.repository.PaiementRepository;
import org.kfokam48.cliniquemanagementbackend.repository.PaiementRepository.LignePaiementParService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * I18 / I22 : revenus calculés à partir des paiements.
 */
class RevenuServiceTest {

    private final PaiementRepository paiementRepository = mock(PaiementRepository.class);
    private final RevenuService revenuService = new RevenuService(paiementRepository);

    private RevenuDTO revenus(String moisCourant, String moisPrecedent) {
        LocalDate debutMois = LocalDate.now().withDayOfMonth(1);
        when(paiementRepository.sommeEncaisseeEntre(debutMois.atStartOfDay(), debutMois.plusMonths(1).atStartOfDay()))
                .thenReturn(new BigDecimal(moisCourant));
        when(paiementRepository.sommeEncaisseeEntre(debutMois.minusMonths(1).atStartOfDay(), debutMois.atStartOfDay()))
                .thenReturn(new BigDecimal(moisPrecedent));
        return revenuService.getRevenuMensuel();
    }

    @Test
    void growthIsComputedAgainstThePreviousMonth() {
        RevenuDTO revenu = revenus("150000", "100000");
        assertThat(revenu.getRevenuMensuel()).isEqualByComparingTo("150000");
        assertThat(revenu.getRevenuMoisPrecedent()).isEqualByComparingTo("100000");
        assertThat(revenu.getPourcentageEvolution()).isEqualTo(50.0);
    }

    @Test
    void declineIsNegative() {
        assertThat(revenus("75000", "100000").getPourcentageEvolution()).isEqualTo(-25.0);
    }

    @Test
    void noRevenueLastMonthGives100PercentOrZero() {
        assertThat(revenus("20000", "0").getPourcentageEvolution()).isEqualTo(100.0);
        assertThat(revenus("0", "0").getPourcentageEvolution()).isEqualTo(0.0);
    }

    private record Ligne(Long getMedecinId, String getMedecinNom, String getMedecinPrenom, Long getPaiementId,
                         BigDecimal getMontantPaiement, String getService, BigDecimal getMontantLigne,
                         BigDecimal getMontantFacture) implements LignePaiementParService {
    }

    @Test
    void paymentIsSplitBetweenServicesProRata() {
        // Facture de 15 000 : consultation 10 000 + pansement 5 000. Paiement de 6 000 → 4 000 / 2 000.
        // Autre médecin : paiement de 1 000 sur une consultation seule.
        BigDecimal facture = new BigDecimal("15000");
        when(paiementRepository.lignesPaiementParService(any(), any())).thenReturn(List.of(
                new Ligne(7L, "Martin", "Paul", 1L, new BigDecimal("6000"), "Consultation générale", new BigDecimal("10000"), facture),
                new Ligne(7L, "Martin", "Paul", 1L, new BigDecimal("6000"), "Pansement", new BigDecimal("5000"), facture),
                new Ligne(8L, "Durand", "Anne", 2L, new BigDecimal("1000"), "Consultation générale", new BigDecimal("15000"), new BigDecimal("15000"))));

        List<RevenuMedecinDTO> revenus = revenuService.getRevenusParMedecin(LocalDate.of(2030, 1, 1), LocalDate.of(2030, 1, 31));

        assertThat(revenus).extracting(RevenuMedecinDTO::medecinId).containsExactly(7L, 8L);
        RevenuMedecinDTO martin = revenus.get(0);
        assertThat(martin.totalEncaisse()).isEqualByComparingTo("6000"); // payé une seule fois, pas une fois par ligne
        assertThat(martin.services()).extracting(RevenuMedecinDTO.RevenuParService::service)
                .containsExactly("Consultation générale", "Pansement");
        assertThat(martin.services().get(0).montantEncaisse()).isEqualByComparingTo("4000");
        assertThat(martin.services().get(1).montantEncaisse()).isEqualByComparingTo("2000");
    }

    @Test
    void endBeforeStartIsRejected() {
        assertThatThrownBy(() -> revenuService.getRevenusParMedecin(LocalDate.of(2030, 2, 1), LocalDate.of(2030, 1, 1)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
