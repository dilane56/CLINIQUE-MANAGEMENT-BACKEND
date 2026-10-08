package org.kfokam48.cliniquemanagementbackend.mapper;

import org.kfokam48.cliniquemanagementbackend.dto.patient.PatientDTO;
import org.kfokam48.cliniquemanagementbackend.dto.patient.PatientResponseDTO;
import org.kfokam48.cliniquemanagementbackend.model.Patient;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(config = MappingConfig.class, uses = RendezVousMapper.class)
public interface PatientMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "rendezvous", ignore = true)
    Patient patientDtoToPatient(PatientDTO patientDTO);

    PatientResponseDTO patientToPatientResponseDTO(Patient patient);

    List<PatientResponseDTO> patientListToPatientResponseDtoList(List<Patient> patientList);
}
