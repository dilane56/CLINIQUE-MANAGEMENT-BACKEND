package org.kfokam48.cliniquemanagementbackend.mapper;


import org.kfokam48.cliniquemanagementbackend.dto.facture.FactureResponseDto;
import org.kfokam48.cliniquemanagementbackend.model.Facture;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FactureMapper {

    private final LigneFactureMapper ligneFactureMapper;

    public FactureMapper(LigneFactureMapper ligneFactureMapper) {
        this.ligneFactureMapper = ligneFactureMapper;
    }

    public FactureResponseDto factureToFactureResponseDto(Facture facture) {
        FactureResponseDto factureResponseDto = new FactureResponseDto();
        factureResponseDto.setId(facture.getId());
        factureResponseDto.setMontantTotal(facture.getMontantTotal());
        factureResponseDto.setDateEmission(facture.getDateEmission());
        factureResponseDto.setPatientNom(facture.getRendezVous().getPatient().getNom());
        factureResponseDto.setPatientPrenom(facture.getRendezVous().getPatient().getPrenom());
        factureResponseDto.setDatePayement(facture.getDatePayement());
        factureResponseDto.setMontantVerser(facture.getMontantPayement());
        factureResponseDto.setMontantRestant(facture.getMontantRestant());
        factureResponseDto.setStatut(facture.getStatut());
        factureResponseDto.setLignesFacture(ligneFactureMapper.toResponseDTOList(facture.getLignes()));
        factureResponseDto.setRendezVousId(facture.getRendezVous().getId());
        return factureResponseDto;
   }


    public List<FactureResponseDto> factureListToFactureResponseDtoList(List<Facture> factures) {
        return factures.stream()
                .map(this::factureToFactureResponseDto)
                .toList();
    }


}
