package org.kfokam48.cliniquemanagementbackend.mapper;


import org.kfokam48.cliniquemanagementbackend.dto.secretaire.MedecinInSecretaireDTO;
import org.kfokam48.cliniquemanagementbackend.dto.secretaire.SecretaireDTO;
import org.kfokam48.cliniquemanagementbackend.dto.secretaire.SecretaireResponseDTO;
import org.kfokam48.cliniquemanagementbackend.model.Medecin;
import org.kfokam48.cliniquemanagementbackend.model.Secretaire;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public class SecretaireMapper {
    private final ModelMapper modelMapper;

    public SecretaireMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }


    public Secretaire secretaireDtoToSecretaire(SecretaireDTO secretaireDTO) {
        return modelMapper.map(secretaireDTO, Secretaire.class);
    }

    public SecretaireDTO secretaireToSecretaireDto(Secretaire secretaire) {
        return modelMapper.map(secretaire, SecretaireDTO.class);
    }

    public MedecinInSecretaireDTO medecinToMedecinInSecretaireDTO(Medecin medecin) {
        MedecinInSecretaireDTO medecinInSecretaireDTO = new MedecinInSecretaireDTO();
        medecinInSecretaireDTO.setNomMedecin(medecin.getNom());
        medecinInSecretaireDTO.setPrenomMedecin(medecin.getPrenom());
        medecinInSecretaireDTO.setSpecialite(medecin.getSpecialite());
        return medecinInSecretaireDTO;
    }

    public SecretaireResponseDTO secretaireToSecretaireResponseDto(Secretaire secretaire) {
        SecretaireResponseDTO secretaireResponseDTO = new SecretaireResponseDTO();
        secretaireResponseDTO.setId(secretaire.getId());
        secretaireResponseDTO.setEmail(secretaire.getEmail());
        secretaireResponseDTO.setRole(String.valueOf(secretaire.getRole()));
        secretaireResponseDTO.setNom(secretaire.getNom());
        secretaireResponseDTO.setPrenom(secretaire.getPrenom());
        secretaireResponseDTO.setTelephone(secretaire.getTelephone());
        secretaireResponseDTO.setMedecins(medecinListToMedecinInSecretaireDTOList(secretaire.getMedecins()));
        return secretaireResponseDTO;
    }


    public List<MedecinInSecretaireDTO> medecinListToMedecinInSecretaireDTOList(Set<Medecin> medecins) {
        return medecins.stream()
                .map(this::medecinToMedecinInSecretaireDTO)
                .toList();
    }
    public List<SecretaireResponseDTO> secretaireListToSecretaireResponseDtoList(List<Secretaire> secretaireList) {
        return secretaireList.stream()
                .map(this::secretaireToSecretaireResponseDto)
                .toList();
    }

}