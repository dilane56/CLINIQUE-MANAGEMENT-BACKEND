package org.kfokam48.cliniquemanagementbackend.mapper;

import org.kfokam48.cliniquemanagementbackend.dto.facture.FactureResponseDto;
import org.kfokam48.cliniquemanagementbackend.model.Facture;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(config = MappingConfig.class, uses = LigneFactureMapper.class)
public interface FactureMapper {

    @Mapping(target = "patientNom", source = "rendezVous.patient.nom")
    @Mapping(target = "patientPrenom", source = "rendezVous.patient.prenom")
    @Mapping(target = "montantVerser", source = "montantPayement")
    @Mapping(target = "lignesFacture", source = "lignes")
    @Mapping(target = "rendezVousId", source = "rendezVous.id")
    FactureResponseDto factureToFactureResponseDto(Facture facture);

    List<FactureResponseDto> factureListToFactureResponseDtoList(List<Facture> factures);
}
