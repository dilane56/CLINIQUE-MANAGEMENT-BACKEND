package org.kfokam48.cliniquemanagementbackend.mapper;


import org.kfokam48.cliniquemanagementbackend.dto.*;
import org.kfokam48.cliniquemanagementbackend.dto.rendezvous.MedecinInRendezVousDto;
import org.kfokam48.cliniquemanagementbackend.dto.rendezvous.PatientInRendezVousDTO;
import org.kfokam48.cliniquemanagementbackend.dto.rendezvous.RendezVousDTO;
import org.kfokam48.cliniquemanagementbackend.dto.rendezvous.RendezVousResponseDTO;
import org.kfokam48.cliniquemanagementbackend.dto.typeRendezVous.TypeRendezVousDTO;
import org.kfokam48.cliniquemanagementbackend.exception.RessourceNotFoundException;
import org.kfokam48.cliniquemanagementbackend.model.RendezVous;
import org.kfokam48.cliniquemanagementbackend.repository.MedecinRepository;
import org.kfokam48.cliniquemanagementbackend.repository.PatientRepository;
import org.kfokam48.cliniquemanagementbackend.repository.TypeRendezVousRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RendezVousMapper {
    private final PatientRepository patientRepository;
    private final MedecinRepository medecinRepository;
    private final TypeRendezVousRepository typeRendezVousRepository;
    private final ModelMapper modelMapper;

    public RendezVousMapper(PatientRepository patientRepository, MedecinRepository medecinRepository, TypeRendezVousRepository typeRendezVousRepository, ModelMapper modelMapper) {
        this.patientRepository = patientRepository;
        this.medecinRepository = medecinRepository;
        this.typeRendezVousRepository = typeRendezVousRepository;
        this.modelMapper = modelMapper;
    }

    public RendezVous rendezVousDtoToRendezvous (RendezVousDTO rendezVousDTO){
        RendezVous rendezVous = new RendezVous();
        rendezVous.setDateRendezVous(rendezVousDTO.getDateRendezVous());
        rendezVous.setMotif(rendezVousDTO.getMotif());
        rendezVous.setPatient(patientRepository.findById(rendezVousDTO.getPatientId()).orElseThrow(()->new RessourceNotFoundException("Patient not found")));

        rendezVous.setMedecin(medecinRepository.findById(rendezVousDTO.getMedecinId())
                .orElseThrow(() -> new RuntimeException("Medecin not found")));
        rendezVous.setDateRendezVous(rendezVousDTO.getDateRendezVous());
        rendezVous.setTypeRendezVous(typeRendezVousRepository.findById(rendezVousDTO.getTypeRendezVousId())
                .orElseThrow(() -> new RessourceNotFoundException("Type de rendez-vous not found")));
        rendezVous.setSecretaireId(rendezVousDTO.getSecretaireId());


        return rendezVous;



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
