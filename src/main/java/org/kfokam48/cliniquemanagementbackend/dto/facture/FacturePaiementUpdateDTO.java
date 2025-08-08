package org.kfokam48.cliniquemanagementbackend.dto.facture;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class FacturePaiementUpdateDTO {
    @NotNull(message = "Le montant du paiement ne doit pas être nul")
    @DecimalMin(value = "0.0", inclusive = false, message = "Le montant du paiement doit être supérieur à 0")
    private BigDecimal montantPaiement;
} 