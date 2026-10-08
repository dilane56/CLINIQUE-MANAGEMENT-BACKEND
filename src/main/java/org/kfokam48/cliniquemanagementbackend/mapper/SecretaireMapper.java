package org.kfokam48.cliniquemanagementbackend.mapper;

import org.kfokam48.cliniquemanagementbackend.dto.secretaire.MedecinInSecretaireDTO;
import org.kfokam48.cliniquemanagementbackend.dto.secretaire.SecretaireDTO;
import org.kfokam48.cliniquemanagementbackend.dto.secretaire.SecretaireResponseDTO;
import org.kfokam48.cliniquemanagementbackend.model.Medecin;
import org.kfokam48.cliniquemanagementbackend.model.Secretaire;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(config = MappingConfig.class)
public interface SecretaireMapper {

    @Mapping(target = "medecins", ignore = true)
    Secretaire secretaireDtoToSecretaire(SecretaireDTO secretaireDTO);

    SecretaireResponseDTO secretaireToSecretaireResponseDto(Secretaire secretaire);

    List<SecretaireResponseDTO> secretaireListToSecretaireResponseDtoList(List<Secretaire> secretaireList);

    @Mapping(target = "nomMedecin", source = "nom")
    @Mapping(target = "prenomMedecin", source = "prenom")
    MedecinInSecretaireDTO medecinToMedecinInSecretaireDTO(Medecin medecin);
}
