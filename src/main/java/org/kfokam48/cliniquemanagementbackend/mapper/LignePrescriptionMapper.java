package org.kfokam48.cliniquemanagementbackend.mapper;

import org.kfokam48.cliniquemanagementbackend.dto.ligneprescription.LignePrescriptionDTO;
import org.kfokam48.cliniquemanagementbackend.dto.ligneprescription.LignePrescriptionResponseDTO;
import org.kfokam48.cliniquemanagementbackend.dto.ligneprescription.LignePrescriptionUpdateDTO;
import org.kfokam48.cliniquemanagementbackend.model.LignePrescription;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(config = MappingConfig.class)
public interface LignePrescriptionMapper {

    // La prescription est associée à chaque ligne par le PrescriptionMapper
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "prescription", ignore = true)
    LignePrescription lignePrescriptionDTOToLignePrescription(LignePrescriptionDTO lignePrescriptionDTO);

    List<LignePrescription> lignePrescriptionDTOListToLignePrescriptionList(List<LignePrescriptionDTO> lignePrescriptionDTOS);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "prescription", ignore = true)
    LignePrescription lignePrescriptionUpdateDTOToLignePrescription(LignePrescriptionUpdateDTO lignePrescriptionUpdateDTO);

    List<LignePrescription> lignePrescriptionUpdateDTOListToLignePrescriptionList(List<LignePrescriptionUpdateDTO> lignes);

    @Mapping(target = "prescriptionId", source = "prescription.id")
    LignePrescriptionResponseDTO lignePrescriptionToLignePrescriptionResponseDTO(LignePrescription lignePrescription);

    List<LignePrescriptionResponseDTO> lignePrescriptionsToLignePrescriptionResponseDTOs(List<LignePrescription> lignePrescriptions);
}
