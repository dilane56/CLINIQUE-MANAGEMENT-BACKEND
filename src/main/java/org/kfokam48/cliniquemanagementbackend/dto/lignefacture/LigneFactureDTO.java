package org.kfokam48.cliniquemanagementbackend.dto.lignefacture;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class LigneFactureDTO {
    @NotBlank(message = "Le nom du service est obligatoire")
    private String serviceName;

    @Min(value = 1, message = "La quantité doit être au moins 1")
    private int quantite;

    @NotNull(message = "Le prix unitaire est obligatoire")
    @DecimalMin(value = "0.0", message = "Le prix unitaire ne peut pas être négatif")
    private BigDecimal prixUnitaire;


}
