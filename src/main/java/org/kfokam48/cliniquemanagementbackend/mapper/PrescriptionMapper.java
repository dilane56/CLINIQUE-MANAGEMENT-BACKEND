package org.kfokam48.cliniquemanagementbackend.mapper;


import org.kfokam48.cliniquemanagementbackend.dto.ligneprescription.LignePrescriptionResponseDTO;
import org.kfokam48.cliniquemanagementbackend.dto.prescription.PrescriptionDTO;
import org.kfokam48.cliniquemanagementbackend.dto.prescription.PrescriptionResponseDTO;
import org.kfokam48.cliniquemanagementbackend.dto.prescription.PrescriptionUpdateDTO;
import org.kfokam48.cliniquemanagementbackend.model.LignePrescription;
import org.kfokam48.cliniquemanagementbackend.model.Prescription;
import org.kfokam48.cliniquemanagementbackend.repository.RendezVousRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PrescriptionMapper {
    private final ModelMapper modelMapper;
    private final RendezVousRepository rendezVousRepository;
    private final LignePrescriptionMapper lignePrescriptionMapper;

    public PrescriptionMapper(ModelMapper modelMapper, RendezVousRepository rendezVousRepository, LignePrescriptionMapper lignePrescriptionMapper) {
        this.modelMapper = modelMapper;
        this.rendezVousRepository = rendezVousRepository;
        this.lignePrescriptionMapper = lignePrescriptionMapper;
    }
    public Prescription prescriptionDtoToPrescription(PrescriptionDTO prescriptionDTO){
        Prescription prescription = new Prescription();
        prescription.setRendezVous(rendezVousRepository.findById(prescriptionDTO.getRendezVousId())
                .orElseThrow(() -> new RuntimeException("RendezVous not found")));
        prescription.setDescription(prescriptionDTO.getDescription());
        List<LignePrescription> lignes = lignePrescriptionMapper.lignePrescriptionDTOListToLignePrescriptionList(prescriptionDTO.getLignes());
        // Associer la prescription à chaque ligne
        for (LignePrescription ligne : lignes) {
            ligne.setPrescription(prescription);
        }
        prescription.setLignes(lignes);
        return prescription;
    }

    public PrescriptionDTO prescriptionToPrescriptionDto(Prescription prescription){
        return modelMapper.map(prescription, PrescriptionDTO.class);
    }

    public PrescriptionResponseDTO prescriptionToPrescriptionResponseDto(Prescription prescription){
        PrescriptionResponseDTO prescriptionResponseDTO = new PrescriptionResponseDTO();
        prescriptionResponseDTO.setId(prescription.getId());
        prescriptionResponseDTO.setDescription(prescription.getDescription());
        if (prescription.getRendezVous() != null) {
            if (prescription.getRendezVous().getPatient() != null) {
                prescriptionResponseDTO.setPatientNom(prescription.getRendezVous().getPatient().getNom());
                prescriptionResponseDTO.setPatientPrenom(prescription.getRendezVous().getPatient().getPrenom());
            }
            if (prescription.getRendezVous().getMedecin() != null) {
                prescriptionResponseDTO.setMedecinNom(prescription.getRendezVous().getMedecin().getNom());
                prescriptionResponseDTO.setMedecinPrenom(prescription.getRendezVous().getMedecin().getPrenom());
            }
        }
        prescriptionResponseDTO.setDate(prescription.getDate());
        // Mapping des lignes
        List<LignePrescriptionResponseDTO> lignes = lignePrescriptionMapper.lignePrescriptionsToLignePrescriptionResponseDTOs(prescription.getLignes());
        prescriptionResponseDTO.setLignes(lignes);

        return prescriptionResponseDTO;
    }

    public List<PrescriptionResponseDTO> prescriptionListToPrescriptionResponseDtoList(List<Prescription> prescriptions){
        return prescriptions.stream()
                .map(this::prescriptionToPrescriptionResponseDto)
                .toList();
    }

    public void updatePrescriptionFromUpdateDTO(Prescription prescription, PrescriptionUpdateDTO dto, LignePrescriptionMapper lignePrescriptionMapper) {
        prescription.setDescription(dto.getDescription());
        
        // Vider d'abord la liste existante pour éviter les problèmes avec orphanRemoval
        prescription.getLignes().clear();
        
        // Ajouter les nouvelles lignes
        List<LignePrescription> nouvellesLignes = lignePrescriptionMapper.lignePrescriptionUpdateDTOListToLignePrescriptionList(dto.getLignes());
        for (LignePrescription ligne : nouvellesLignes) {
            ligne.setPrescription(prescription);
            prescription.getLignes().add(ligne);
        }
    }
}
