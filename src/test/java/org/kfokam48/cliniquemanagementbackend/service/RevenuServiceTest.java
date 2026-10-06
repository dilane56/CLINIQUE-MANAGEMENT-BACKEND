package org.kfokam48.cliniquemanagementbackend.service;

import org.junit.jupiter.api.Test;
import org.kfokam48.cliniquemanagementbackend.dto.RevenuDTO;
import org.kfokam48.cliniquemanagementbackend.repository.FactureRepository;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * I18 : calcul des revenus du mois et de leur évolution par rapport au mois précédent.
 * (La répartition des paiements par tranche entre les mois est un problème distinct : I22.)
 */
class RevenuServiceTest {

    private final FactureRepository factureRepository = mock(FactureRepository.class);
    private final RevenuService revenuService = new RevenuService();

    private RevenuDTO revenus(String moisCourant, String moisPrecedent) {
        ReflectionTestUtils.setField(revenuService, "factureRepository", factureRepository);
        LocalDate maintenant = LocalDate.now();
        LocalDate avant = maintenant.minusMonths(1);
        when(factureRepository.sumRevenuByMoisAndAnnee(maintenant.getMonthValue(), maintenant.getYear()))
                .thenReturn(new BigDecimal(moisCourant));
        when(factureRepository.sumRevenuByMoisAndAnnee(avant.getMonthValue(), avant.getYear()))
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
}
