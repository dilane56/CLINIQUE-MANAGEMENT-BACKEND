package org.kfokam48.cliniquemanagementbackend.mapper;


import org.kfokam48.cliniquemanagementbackend.dto.PatientDTO;
import org.kfokam48.cliniquemanagementbackend.dto.PatientResponseDTO;
import org.kfokam48.cliniquemanagementbackend.dto.RendezVousInUserDto;
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
        patientResponseDTO.setDateNaissance(patient.getDateNaissance());
        patientResponseDTO.setNumeroDossierMedical(patient.getNumeroDossierMedical());
        patientResponseDTO.setRole(patient.getRole());
        patientResponseDTO.setPassword(patient.getPassword());
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
