package org.kfokam48.cliniquemanagementbackend.mapper;


import org.kfokam48.cliniquemanagementbackend.dto.patient.PatientDTO;
import org.kfokam48.cliniquemanagementbackend.dto.patient.PatientResponseDTO;
import org.kfokam48.cliniquemanagementbackend.model.Patient;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PatientMapper {
    private final ModelMapper modelMapper;
    private final RendezVousMapper rendezVousMapper;

    public PatientMapper(ModelMapper modelMapper, RendezVousMapper rendezVousMapper) {
        this.modelMapper = modelMapper;
        this.rendezVousMapper = rendezVousMapper;
    }
    public Patient patientDtoToPatient(PatientDTO patientDTO){
        return modelMapper.map(patientDTO, Patient.class);
    }

    public PatientDTO patientToPatientDto(Patient patient){
        return modelMapper.map(patient, PatientDTO.class);
    }

    public PatientResponseDTO patientToPatientResponseDTO(Patient patient){
        PatientResponseDTO patientResponseDTO = new PatientResponseDTO();
        patientResponseDTO.setId(patient.getId());
        patientResponseDTO.setEmail(patient.getEmail());
        patientResponseDTO.setAdresse(patient.getAdresse());
        patientResponseDTO.setNom(patient.getNom());
        patientResponseDTO.setPrenom(patient.getPrenom());
        patientResponseDTO.setTelephone(patient.getTelephone());
        patientResponseDTO.setDateNaissance(patient.getDateNaissance());
        patientResponseDTO.setRendezvous(rendezVousMapper.rendezVousListToRendezVousInUserDtoList(patient.getRendezvous()));
        patientResponseDTO.setSexe(patient.getSexe());
        return patientResponseDTO;
    }
    public List<PatientResponseDTO> patientListToPatientResponseDtoList(List<Patient> patientList){
        return patientList.stream()
                .map(this::patientToPatientResponseDTO)
                .toList();
    }
}
