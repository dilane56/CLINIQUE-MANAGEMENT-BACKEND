package org.kfokam48.cliniquemanagementbackend.mapper;

import org.kfokam48.cliniquemanagementbackend.dto.RendezVousInUserDto;
import org.kfokam48.cliniquemanagementbackend.dto.rendezvous.MedecinInRendezVousDto;
import org.kfokam48.cliniquemanagementbackend.dto.rendezvous.PatientInRendezVousDTO;
import org.kfokam48.cliniquemanagementbackend.dto.rendezvous.RendezVousResponseDTO;
import org.kfokam48.cliniquemanagementbackend.dto.typeRendezVous.TypeRendezVousDTO;
import org.kfokam48.cliniquemanagementbackend.model.Medecin;
import org.kfokam48.cliniquemanagementbackend.model.Patient;
import org.kfokam48.cliniquemanagementbackend.model.RendezVous;
import org.kfokam48.cliniquemanagementbackend.model.TypeRendezVous;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(config = MappingConfig.class)
public interface RendezVousMapper {

    RendezVousResponseDTO rendezVousToRendezVousResponseDto(RendezVous rendezVous);

    List<RendezVousResponseDTO> rendezVousListToRendezVousResponseDtoList(List<RendezVous> rendezVousList);

    @Mapping(target = "patientNom", source = "patient.nom")
    @Mapping(target = "patientPrenom", source = "patient.prenom")
    @Mapping(target = "medecinNom", source = "medecin.nom")
    @Mapping(target = "medecinPrenon", source = "medecin.prenom")
    RendezVousInUserDto rendezVousToRendezVousInUserDto(RendezVous rendezVous);

    List<RendezVousInUserDto> rendezVousListToRendezVousInUserDtoList(List<RendezVous> rendezVousList);

    // Résumés du patient, du médecin et du type affichés dans un rendez-vous

    // Le numéro de dossier médical n'existe pas (encore) sur le patient : toujours null
    @Mapping(target = "numeroDossierMedical", ignore = true)
    PatientInRendezVousDTO patientToPatientInRendezVousDTO(Patient patient);

    MedecinInRendezVousDto medecinToMedecinInRendezVousDto(Medecin medecin);

    TypeRendezVousDTO typeRendezVousToTypeRendezVousDTO(TypeRendezVous typeRendezVous);
}
