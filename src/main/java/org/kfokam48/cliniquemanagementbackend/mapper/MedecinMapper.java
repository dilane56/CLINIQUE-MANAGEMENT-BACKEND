package org.kfokam48.cliniquemanagementbackend.mapper;


import org.kfokam48.cliniquemanagementbackend.dto.medecin.MedecinDTO;
import org.kfokam48.cliniquemanagementbackend.dto.medecin.MedecinResponseDTO;
import org.kfokam48.cliniquemanagementbackend.dto.medecin.SecretaireInMedecinDTO;
import org.kfokam48.cliniquemanagementbackend.model.Medecin;
import org.kfokam48.cliniquemanagementbackend.model.Secretaire;
import org.kfokam48.cliniquemanagementbackend.repository.SecretaireRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public class MedecinMapper {
    private final ModelMapper modelMapper;
    private final RendezVousMapper rendezVousMapper;


    public MedecinMapper(ModelMapper modelMapper, RendezVousMapper rendezVousMapper) {
        this.modelMapper = modelMapper;
        this.rendezVousMapper = rendezVousMapper;

    }

    public Medecin medecinDtoToMedecin (MedecinDTO medecinDTO){
        return modelMapper.map(medecinDTO, Medecin.class );
    }

    public MedecinDTO medecinToMedecinDto (Medecin medecin){
        return modelMapper.map(medecin, MedecinDTO.class);
    }

    public MedecinResponseDTO medecinToMedecinResponseDto (Medecin medecin){
       MedecinResponseDTO medecinResponseDTO = new MedecinResponseDTO();
        medecinResponseDTO.setId(medecin.getId());
        medecinResponseDTO.setEmail(medecin.getEmail());
        medecinResponseDTO.setSpecialite(medecin.getSpecialite());
        medecinResponseDTO.setRole(String.valueOf(medecin.getRole()));
        medecinResponseDTO.setRendezvous(rendezVousMapper.rendezVousListToRendezVousInUserDtoList(medecin.getRendezvous()));
        medecinResponseDTO.setNom(medecin.getNom());
        medecinResponseDTO.setPrenom(medecin.getPrenom());
        medecinResponseDTO.setTelephone(medecin.getTelephone());
        medecinResponseDTO.setSecretaires(secretaireListToSecretaireInMedecinDTOList(medecin.getSecretaires()));
        return medecinResponseDTO;
    }

    public List<MedecinResponseDTO> medecinListToMedecinResponseDtoList(List<Medecin> medecinList){
        return medecinList.stream()
                .map(this::medecinToMedecinResponseDto)
                .toList();
    }

    public SecretaireInMedecinDTO secretaireToSecretaireInMedecinDTO(Secretaire secretaire){
        return modelMapper.map(secretaire, SecretaireInMedecinDTO.class);
    }

    public List<SecretaireInMedecinDTO> secretaireListToSecretaireInMedecinDTOList(Set<Secretaire> secretaires){
        return secretaires.stream()
                .map(this::secretaireToSecretaireInMedecinDTO)
                .toList();
    }


}
