package org.kfokam48.cliniquemanagementbackend.dto.lignefacture;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class LigneFactureDTO {
    private String serviceName;
    private int quantite;
    private BigDecimal prixUnitaire;


}

