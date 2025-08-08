package org.kfokam48.cliniquemanagementbackend.dto.facture;

import lombok.Data;
import org.kfokam48.cliniquemanagementbackend.dto.lignefacture.LigneFactureResponseDTO;
import org.kfokam48.cliniquemanagementbackend.enums.StatutFacture;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


@Data
public class FactureResponseDto {
    private Long id;
    private BigDecimal montantTotal;
    private LocalDateTime dateEmission;
    private String patientNom;
    private String patientPrenom;
    private LocalDateTime datePayement;
    private BigDecimal montantVerser;
    private BigDecimal montantRestant;
    private StatutFacture statut;
    private List<LigneFactureResponseDTO> lignesFacture;    
    private Long rendezVousId;
}
