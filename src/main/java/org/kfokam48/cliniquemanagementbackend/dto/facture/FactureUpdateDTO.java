package org.kfokam48.cliniquemanagementbackend.dto.facture;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.kfokam48.cliniquemanagementbackend.enums.StatutFacture;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Data
public class FactureUpdateDTO {
    @NotNull(message = "Le montant payement ne doit pas être nul")
    private BigDecimal montantPayement;
    @NotNull(message = "L'id du rendez-vous ne doit pas être nul")
    private Long RendezVousId;
}
