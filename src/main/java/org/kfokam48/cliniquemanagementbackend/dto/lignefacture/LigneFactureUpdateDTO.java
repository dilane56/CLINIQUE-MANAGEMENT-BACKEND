package org.kfokam48.cliniquemanagementbackend.dto.lignefacture;

import lombok.Data;

@Data
public class LigneFactureUpdateDTO {
    private String serviceName;
    private int quantite;
    private double prixUnitaire;
    private double prixTotal;
    private Long factureId;
}
