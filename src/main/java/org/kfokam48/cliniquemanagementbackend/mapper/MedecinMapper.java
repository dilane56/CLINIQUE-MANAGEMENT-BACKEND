package org.kfokam48.cliniquemanagementbackend.mapper;

import org.kfokam48.cliniquemanagementbackend.dto.medecin.MedecinDTO;
import org.kfokam48.cliniquemanagementbackend.dto.medecin.MedecinResponseDTO;
import org.kfokam48.cliniquemanagementbackend.dto.medecin.SecretaireInMedecinDTO;
import org.kfokam48.cliniquemanagementbackend.model.Medecin;
import org.kfokam48.cliniquemanagementbackend.model.Secretaire;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(config = MappingConfig.class, uses = RendezVousMapper.class)
public interface MedecinMapper {

    @Mapping(target = "rendezvous", ignore = true)
    @Mapping(target = "secretaires", ignore = true)
    Medecin medecinDtoToMedecin(MedecinDTO medecinDTO);

    MedecinResponseDTO medecinToMedecinResponseDto(Medecin medecin);

    List<MedecinResponseDTO> medecinListToMedecinResponseDtoList(List<Medecin> medecinList);

    SecretaireInMedecinDTO secretaireToSecretaireInMedecinDTO(Secretaire secretaire);
}
