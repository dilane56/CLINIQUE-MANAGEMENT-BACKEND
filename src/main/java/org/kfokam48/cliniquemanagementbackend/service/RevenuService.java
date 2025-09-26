package org.kfokam48.cliniquemanagementbackend.service;

import org.kfokam48.cliniquemanagementbackend.dto.RevenuDTO;
import org.kfokam48.cliniquemanagementbackend.repository.FactureRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Service
public class RevenuService {

    @Autowired
    private FactureRepository factureRepository;

    public RevenuDTO getRevenuMensuel() {
        LocalDate maintenant = LocalDate.now();
        LocalDate moisPrecedent = maintenant.minusMonths(1);

        // Revenus du mois actuel
        BigDecimal revenuMensuel = factureRepository.sumRevenuByMoisAndAnnee(
            maintenant.getMonthValue(), 
            maintenant.getYear()
        );

        // Revenus du mois précédent
        BigDecimal revenuMoisPrecedent = factureRepository.sumRevenuByMoisAndAnnee(
            moisPrecedent.getMonthValue(), 
            moisPrecedent.getYear()
        );

        // Calcul du pourcentage d'évolution
        Double pourcentageEvolution = calculerPourcentageEvolution(revenuMensuel, revenuMoisPrecedent);

        return new RevenuDTO(revenuMensuel, revenuMoisPrecedent, pourcentageEvolution);
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
}