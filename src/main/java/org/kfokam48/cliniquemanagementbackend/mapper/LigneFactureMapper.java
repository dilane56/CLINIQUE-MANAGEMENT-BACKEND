package org.kfokam48.cliniquemanagementbackend.mapper;

import org.kfokam48.cliniquemanagementbackend.dto.lignefacture.LigneFactureDTO;
import org.kfokam48.cliniquemanagementbackend.dto.lignefacture.LigneFactureResponseDTO;
import org.kfokam48.cliniquemanagementbackend.model.LigneFacture;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

import java.util.List;

@Component
public class LigneFactureMapper {




    public LigneFacture ligneFactureDTOToLigneFacture(LigneFactureDTO dto) {
        LigneFacture ligneFacture = new LigneFacture();
        ligneFacture.setServiceName(dto.getServiceName());
        ligneFacture.setQuantite(dto.getQuantite());
        ligneFacture.setPrixUnitaire(dto.getPrixUnitaire());
        ligneFacture.setPrixTotal(ligneFacture.getPrixUnitaire().multiply(BigDecimal.valueOf(dto.getQuantite())));

        return ligneFacture;

    }

    public LigneFactureResponseDTO toResponseDTO(LigneFacture ligneFacture) {
        LigneFactureResponseDTO ligneFactureResponseDTO =new LigneFactureResponseDTO();
        ligneFactureResponseDTO.setId(ligneFacture.getId());
        ligneFactureResponseDTO.setServiceName(ligneFacture.getServiceName());
        ligneFactureResponseDTO.setQuantite(ligneFacture.getQuantite());
        ligneFactureResponseDTO.setPrixUnitaire(ligneFacture.getPrixUnitaire());
        ligneFactureResponseDTO.setPrixTotal(ligneFacture.getPrixTotal());
        ligneFactureResponseDTO.setFactureId((ligneFacture.getFacture().getId()));
        return ligneFactureResponseDTO;

    }

    public List<LigneFactureResponseDTO> toResponseDTOList(List<LigneFacture> ligneFactures) {
        return ligneFactures.stream()
                .map(this::toResponseDTO)
                .toList();
    }





}

