package org.kfokam48.cliniquemanagementbackend.mapper;


import org.kfokam48.cliniquemanagementbackend.dto.facture.FactureDTO;
import org.kfokam48.cliniquemanagementbackend.dto.facture.FactureResponseDto;
import org.kfokam48.cliniquemanagementbackend.dto.lignefacture.LigneFactureDTO;
import org.kfokam48.cliniquemanagementbackend.exception.RessourceNotFoundException;
import org.kfokam48.cliniquemanagementbackend.model.Facture;
import org.kfokam48.cliniquemanagementbackend.model.LigneFacture;
import org.kfokam48.cliniquemanagementbackend.model.RendezVous;

import org.kfokam48.cliniquemanagementbackend.repository.RendezVousRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class FactureMapper {

   private final RendezVousRepository rendezVousRepository;
   private final LigneFactureMapper ligneFactureMapper;


    public FactureMapper(RendezVousRepository rendezVousRepository) {
        this.rendezVousRepository = rendezVousRepository;
        this.ligneFactureMapper = new LigneFactureMapper();

    }

    public Facture factureDtoToFacture (FactureDTO factureDTO){
        Facture facture = new Facture();
        RendezVous rendezVous = rendezVousRepository.findById(factureDTO.getRendezVousId())
                .orElseThrow(() -> new RessourceNotFoundException("Rendez-vous not found"));
        facture.setRendezVous(rendezVous);
        BigDecimal total = rendezVous.getTypeRendezVous().getTarif();
        facture.setMontantPayement(new BigDecimal(0));


        List<LigneFacture> ligneFactureList = new ArrayList<>();

        // Ligne de base (consultation)
        LigneFacture ligneConsult = new LigneFacture();
        ligneConsult.setServiceName(rendezVous.getTypeRendezVous().getLibelle());
        ligneConsult.setPrixUnitaire(rendezVous.getTypeRendezVous().getTarif());
        ligneConsult.setQuantite(1);
        ligneConsult.setPrixTotal(rendezVous.getTypeRendezVous().getTarif());
        ligneConsult.setFacture(facture);
        ligneFactureList.add(ligneConsult);

        // Ajout des autre ligne de facture
        for (LigneFactureDTO ligneFactureDTO : factureDTO.getLignesFacture()) {
           LigneFacture ligneFacture = ligneFactureMapper.ligneFactureDTOToLigneFacture(ligneFactureDTO);
           ligneFacture.setFacture(facture);
           ligneFactureList.add(ligneFacture);
           total = total.add(ligneFacture.getPrixTotal());

        }
        facture.setLignes(ligneFactureList);
        facture.setMontantTotal(total);
        facture.setMontantRestant(total);


        return facture;
    }

    public FactureResponseDto factureToFactureResponseDto (Facture facture) {
        FactureResponseDto factureResponseDto = new FactureResponseDto();
        factureResponseDto.setId(facture.getId());
        factureResponseDto.setMontantTotal(facture.getMontantTotal());
        factureResponseDto.setDateEmission(facture.getDateEmission());
        factureResponseDto.setPatientNom(facture.getRendezVous().getPatient().getNom());
        factureResponseDto.setPatientPrenom(facture.getRendezVous().getPatient().getPrenom());
        factureResponseDto.setDatePayement(facture.getDatePayement());
        factureResponseDto.setMontantVerser(facture.getMontantPayement());
        factureResponseDto.setMontantRestant(facture.getMontantRestant());
        factureResponseDto.setStatut(facture.getStatut());
        factureResponseDto.setLignesFacture(ligneFactureMapper.toResponseDTOList(facture.getLignes()));
        factureResponseDto.setRendezVousId(facture.getRendezVous().getId());
        return factureResponseDto;
   }


    public List<FactureResponseDto> factureListToFactureResponseDtoList(List<Facture> factures) {
        return factures.stream()
                .map(this::factureToFactureResponseDto)
                .toList();
    }


}
