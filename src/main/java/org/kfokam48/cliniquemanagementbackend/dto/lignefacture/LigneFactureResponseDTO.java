package org.kfokam48.cliniquemanagementbackend.dto.lignefacture;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class LigneFactureResponseDTO {
    private Long id;
    private String serviceName;
    private int quantite;
    private BigDecimal prixUnitaire;
    private BigDecimal prixTotal;
    private Long factureId;
}
