package org.kfokam48.cliniquemanagementbackend.mapper;

import org.kfokam48.cliniquemanagementbackend.dto.ligneprescription.LignePrescriptionDTO;
import org.kfokam48.cliniquemanagementbackend.dto.ligneprescription.LignePrescriptionResponseDTO;
import org.kfokam48.cliniquemanagementbackend.dto.ligneprescription.LignePrescriptionUpdateDTO;
import org.kfokam48.cliniquemanagementbackend.exception.RessourceNotFoundException;
import org.kfokam48.cliniquemanagementbackend.model.LignePrescription;
import org.kfokam48.cliniquemanagementbackend.repository.PrescriptionRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class LignePrescriptionMapper {
    private final ModelMapper modelMapper;


    public LignePrescriptionMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;

    }
    public LignePrescription lignePrescriptionDTOToLignePrescription(LignePrescriptionDTO lignePrescriptionDTO){
        LignePrescription lignePrescription = new LignePrescription();
        lignePrescription.setDosage(lignePrescriptionDTO.getDosage());
        lignePrescription.setMedicament(lignePrescriptionDTO.getMedicament());
        lignePrescription.setDuree(lignePrescriptionDTO.getDuree());
        lignePrescription.setFrequence(lignePrescriptionDTO.getFrequence());
        // Ne pas setter prescription ici, il sera associé dans le PrescriptionMapper
        return lignePrescription;
    }

    public LignePrescriptionResponseDTO lignePrescriptionToLignePrescriptionResponseDTO(LignePrescription lignePrescription) {
        LignePrescriptionResponseDTO lignePrescriptionResponseDTO = new LignePrescriptionResponseDTO();
        lignePrescriptionResponseDTO.setId(lignePrescription.getId());
        lignePrescriptionResponseDTO.setDosage(lignePrescription.getDosage());
        lignePrescriptionResponseDTO.setMedicament(lignePrescription.getMedicament());
        lignePrescriptionResponseDTO.setDuree(lignePrescription.getDuree());
        lignePrescriptionResponseDTO.setFrequence(lignePrescription.getFrequence());
        lignePrescriptionResponseDTO.setPrescriptionId(lignePrescription.getPrescription() != null ? lignePrescription.getPrescription().getId() : null);
        return lignePrescriptionResponseDTO;
    }
    public List<LignePrescriptionResponseDTO> lignePrescriptionsToLignePrescriptionResponseDTOs(List<LignePrescription> lignePrescriptions) {
        return lignePrescriptions == null ? new ArrayList<>() : lignePrescriptions.stream()
                .map(this::lignePrescriptionToLignePrescriptionResponseDTO)
                .collect(Collectors.toList());
    }

    public List<LignePrescription> lignePrescriptionDTOListToLignePrescriptionList(List<LignePrescriptionDTO>lignePrescriptionDTOS){
        return lignePrescriptionDTOS == null ? new ArrayList<>() : lignePrescriptionDTOS.stream()
                .map(this::lignePrescriptionDTOToLignePrescription)
                .collect(Collectors.toList());
    }


    public LignePrescription lignePrescriptionUpdateDTOToLignePrescription(LignePrescriptionUpdateDTO lignePrescriptionUpdateDTO) {
        LignePrescription lignePrescription = new LignePrescription();
        lignePrescription.setMedicament(lignePrescriptionUpdateDTO.getMedicament());
        lignePrescription.setDosage(lignePrescriptionUpdateDTO.getDosage());
        lignePrescription.setFrequence(lignePrescriptionUpdateDTO.getFrequence());
        lignePrescription.setDuree(lignePrescriptionUpdateDTO.getDuree());
        // Ne pas toucher à prescription ici
        return lignePrescription;
    }




    public List<LignePrescription> lignePrescriptionUpdateDTOListToLignePrescriptionList(List<LignePrescriptionUpdateDTO> lignes) {
        if (lignes == null) {
            return new ArrayList<>();
        }
        return lignes.stream()
                .map(this::lignePrescriptionUpdateDTOToLignePrescription)
                .collect(Collectors.toList());
    }
}
