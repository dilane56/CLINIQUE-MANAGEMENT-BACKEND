package org.kfokam48.cliniquemanagementbackend.mapper;


import org.kfokam48.cliniquemanagementbackend.dto.MedecinDTO;
import org.kfokam48.cliniquemanagementbackend.dto.MedecinResponseDTO;
import org.kfokam48.cliniquemanagementbackend.dto.UtilisateurDTO;
import org.kfokam48.cliniquemanagementbackend.model.Medecin;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.List;

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
        medecinResponseDTO.setRole(medecin.getRole());
        medecinResponseDTO.setPassword(medecin.getPassword());
        medecinResponseDTO.setRendezvous(rendezVousMapper.rendezVousListToRendezVousInUserDtoList(medecin.getRendezvous()));
        return medecinResponseDTO;
    }

    public List<MedecinResponseDTO> medecinListToMedecinResponseDtoList(List<Medecin> medecinList){
        return medecinList.stream()
                .map(this::medecinToMedecinResponseDto)
                .toList();
    }


}
