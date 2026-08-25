package org.kfokam48.cliniquemanagementbackend.mapper;

import org.kfokam48.cliniquemanagementbackend.dto.*;
import org.kfokam48.cliniquemanagementbackend.dto.rendezvous.MedecinInRendezVousDto;
import org.kfokam48.cliniquemanagementbackend.dto.rendezvous.PatientInRendezVousDTO;
import org.kfokam48.cliniquemanagementbackend.dto.rendezvous.RendezVousResponseDTO;
import org.kfokam48.cliniquemanagementbackend.dto.typeRendezVous.TypeRendezVousDTO;
import org.kfokam48.cliniquemanagementbackend.model.RendezVous;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RendezVousMapper {

    private final ModelMapper modelMapper;

    public RendezVousMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public RendezVousResponseDTO rendezVousToRendezVousResponseDto (RendezVous rendezVous){
       RendezVousResponseDTO rendezVousResponseDTO = new RendezVousResponseDTO();
        rendezVousResponseDTO.setId(rendezVous.getId());
        rendezVousResponseDTO.setDateRendezVous(rendezVous.getDateRendezVous());
        rendezVousResponseDTO.setMotif(rendezVous.getMotif());
        rendezVousResponseDTO.setPatient(modelMapper.map(rendezVous.getPatient(), PatientInRendezVousDTO.class));
        rendezVousResponseDTO.setMedecin(modelMapper.map(rendezVous.getMedecin(), MedecinInRendezVousDto.class));
        rendezVousResponseDTO.setStatutRendezVous(rendezVous.getStatutRendezVous());
        rendezVousResponseDTO.setDateTimeFinRendezVousPossible(rendezVous.getDateTimeFinRendezVousPossible());
        rendezVousResponseDTO.setTypeRendezVous(modelMapper.map(rendezVous.getTypeRendezVous(), TypeRendezVousDTO.class));
       // rendezVousResponseDTO.setDureeEstimerRendezVousEnMin(rendezVous.getDureeEstimerRendezVousEnMin());

        return rendezVousResponseDTO;
    }

    public List<RendezVousResponseDTO> rendezVousListToRendezVousResponseDtoList(List<RendezVous> rendezVousList) {
        return rendezVousList.stream()
                .map(this::rendezVousToRendezVousResponseDto)
                .toList();
    }

    public RendezVousInUserDto rendezVousToRendezVousInUserDto(RendezVous rendezVous) {
        RendezVousInUserDto rendezVousInUserDto = new RendezVousInUserDto();
        rendezVousInUserDto.setId(rendezVous.getId());
        rendezVousInUserDto.setDateRendezVous(rendezVous.getDateRendezVous());
        rendezVousInUserDto.setMotif(rendezVous.getMotif());
        rendezVousInUserDto.setMedecinNom(rendezVous.getMedecin().getNom());
        rendezVousInUserDto.setPatientNom(rendezVousInUserDto.getPatientNom());
        rendezVousInUserDto.setMedecinPrenon(rendezVous.getMedecin().getPrenom());
        rendezVousInUserDto.setPatientPrenom(rendezVous.getPatient().getPrenom());

        return rendezVousInUserDto;
    }

    public List<RendezVousInUserDto> rendezVousListToRendezVousInUserDtoList(List<RendezVous> rendezVousList) {
        return rendezVousList.stream()
                .map(this::rendezVousToRendezVousInUserDto)
                .toList();
    }
}
