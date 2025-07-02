package org.kfokam48.cliniquemanagementbackend.mapper;


import org.kfokam48.cliniquemanagementbackend.dto.FactureDTO;
import org.kfokam48.cliniquemanagementbackend.dto.FactureResponseDto;
import org.kfokam48.cliniquemanagementbackend.dto.FactureUpdateDTO;
import org.kfokam48.cliniquemanagementbackend.exception.RessourceNotFoundException;
import org.kfokam48.cliniquemanagementbackend.model.Facture;
import org.kfokam48.cliniquemanagementbackend.model.Patient;
import org.kfokam48.cliniquemanagementbackend.model.RendezVous;
import org.kfokam48.cliniquemanagementbackend.repository.PatientRepository;

import org.kfokam48.cliniquemanagementbackend.repository.RendezVousRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class FactureMapper {

   private final RendezVousRepository rendezVousRepository;

    public FactureMapper(RendezVousRepository rendezVousRepository) {
        this.rendezVousRepository = rendezVousRepository;
    }

    public Facture factureDtoToFacture (FactureDTO factureDTO){
        Facture facture = new Facture();
        RendezVous rendezVous = rendezVousRepository.findById(factureDTO.getRendezVousId())
                .orElseThrow(() -> new RessourceNotFoundException("Rendez-vous not found"));
        facture.setMontantTotal(factureDTO.getMontantTotal());
        facture.setRendezVous(rendezVous);
        facture.setDescription(rendezVous.getMotif());
        return facture;
    }

    public FactureResponseDto factureToFactureResponseDto (Facture facture) {
        FactureResponseDto factureResponseDto = new FactureResponseDto();
        factureResponseDto.setId(facture.getId());
        factureResponseDto.setMontantTotal(facture.getMontantTotal());
        factureResponseDto.setDateEmission(facture.getDateEmission());
        factureResponseDto.setPatientEmail(facture.getRendezVous().getPatient().getEmail());
        factureResponseDto.setRendezvousMotif(facture.getRendezVous().getMotif());
        factureResponseDto.setDatePayement(facture.getDatePayement());
        factureResponseDto.setMontantVerser(facture.getMontantPayement());
        factureResponseDto.setMontantRestant(facture.getMontantRestant());
        factureResponseDto.setStatutPayement(facture.getStatutPayement());
        factureResponseDto.setModePayement(facture.getModePayement());
        factureResponseDto.setMedecinEmail(facture.getRendezVous().getMedecin().getEmail());
        return factureResponseDto;
   }
    public Facture factureUpdateDTOToFacture(FactureUpdateDTO factureUpdateDTO){
        Facture facture = new Facture();
        facture.setMontantTotal(factureUpdateDTO.getMontantTotal());
        facture.setDatePayement(factureUpdateDTO.getDatePayement());
        facture.setMontantPayement(factureUpdateDTO.getMontantPayement());
        facture.setStatutPayement(factureUpdateDTO.getStatutPayement());
        facture.setModePayement(factureUpdateDTO.getModePayement());
        facture.setDescription(factureUpdateDTO.getDescription());
        facture.setMontantRestant(factureUpdateDTO.getMontantRestant());
        facture.setDateEmission(LocalDateTime.now());
        facture.setRendezVous(rendezVousRepository.findById(factureUpdateDTO.getRendezVousId())
                .orElseThrow(() -> new RessourceNotFoundException("Rendez-vous not found")));
        return facture;
    }

    public List<FactureResponseDto> factureListToFactureResponseDtoList(List<Facture> factures) {
        return factures.stream()
                .map(this::factureToFactureResponseDto)
                .toList();
    }


}
