package org.kfokam48.cliniquemanagementbackend.mapper;

import org.kfokam48.cliniquemanagementbackend.dto.prescription.PrescriptionDTO;
import org.kfokam48.cliniquemanagementbackend.dto.prescription.PrescriptionResponseDTO;
import org.kfokam48.cliniquemanagementbackend.dto.prescription.PrescriptionUpdateDTO;
import org.kfokam48.cliniquemanagementbackend.model.LignePrescription;
import org.kfokam48.cliniquemanagementbackend.model.Prescription;
import org.kfokam48.cliniquemanagementbackend.model.RendezVous;
import org.kfokam48.cliniquemanagementbackend.repository.RendezVousRepository;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper(config = MappingConfig.class, uses = LignePrescriptionMapper.class)
public abstract class PrescriptionMapper {

    @Autowired
    protected RendezVousRepository rendezVousRepository;

    // La date est fixée par le service au moment de l'enregistrement
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "date", ignore = true)
    @Mapping(target = "rendezVous", source = "rendezVousId")
    public abstract Prescription prescriptionDtoToPrescription(PrescriptionDTO prescriptionDTO);

    protected RendezVous rendezVousParId(Long rendezVousId) {
        return rendezVousRepository.findById(rendezVousId)
                .orElseThrow(() -> new RuntimeException("RendezVous not found"));
    }

    // Associer la prescription à chaque ligne (côté propriétaire de la relation)
    @AfterMapping
    protected void rattacherLignes(@MappingTarget Prescription prescription) {
        prescription.getLignes().forEach(ligne -> ligne.setPrescription(prescription));
    }

    @Mapping(target = "patientNom", source = "rendezVous.patient.nom")
    @Mapping(target = "patientPrenom", source = "rendezVous.patient.prenom")
    @Mapping(target = "medecinNom", source = "rendezVous.medecin.nom")
    @Mapping(target = "medecinPrenom", source = "rendezVous.medecin.prenom")
    public abstract PrescriptionResponseDTO prescriptionToPrescriptionResponseDto(Prescription prescription);

    public abstract List<PrescriptionResponseDTO> prescriptionListToPrescriptionResponseDtoList(List<Prescription> prescriptions);

    /**
     * Remplace la description et les lignes. La liste existante est vidée puis remplie (et non
     * remplacée) : avec orphanRemoval, Hibernate exige de conserver la même collection.
     */
    public void updatePrescriptionFromUpdateDTO(Prescription prescription, PrescriptionUpdateDTO dto, LignePrescriptionMapper lignePrescriptionMapper) {
        prescription.setDescription(dto.getDescription());
        prescription.getLignes().clear();
        List<LignePrescription> nouvellesLignes = lignePrescriptionMapper.lignePrescriptionUpdateDTOListToLignePrescriptionList(dto.getLignes());
        for (LignePrescription ligne : nouvellesLignes) {
            ligne.setPrescription(prescription);
            prescription.getLignes().add(ligne);
        }
    }
}
