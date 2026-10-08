package org.kfokam48.cliniquemanagementbackend.mapper;

import org.kfokam48.cliniquemanagementbackend.dto.lignefacture.LigneFactureDTO;
import org.kfokam48.cliniquemanagementbackend.dto.lignefacture.LigneFactureResponseDTO;
import org.kfokam48.cliniquemanagementbackend.model.LigneFacture;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.math.BigDecimal;
import java.util.List;

@Mapper(config = MappingConfig.class)
public interface LigneFactureMapper {

    // La facture est rattachée par le service ; le prix total est calculé après la copie
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "facture", ignore = true)
    @Mapping(target = "prixTotal", ignore = true)
    LigneFacture ligneFactureDTOToLigneFacture(LigneFactureDTO dto);

    @AfterMapping
    default void calculerPrixTotal(LigneFactureDTO dto, @MappingTarget LigneFacture ligneFacture) {
        ligneFacture.setPrixTotal(ligneFacture.getPrixUnitaire().multiply(BigDecimal.valueOf(dto.getQuantite())));
    }

    @Mapping(target = "factureId", source = "facture.id")
    LigneFactureResponseDTO toResponseDTO(LigneFacture ligneFacture);

    List<LigneFactureResponseDTO> toResponseDTOList(List<LigneFacture> ligneFactures);
}
