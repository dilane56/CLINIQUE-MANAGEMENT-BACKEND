package org.kfokam48.cliniquemanagementbackend.service.impl;


import jakarta.validation.Valid;
import org.kfokam48.cliniquemanagementbackend.dto.facture.FactureDTO;
import org.kfokam48.cliniquemanagementbackend.dto.facture.FactureResponseDto;
import org.kfokam48.cliniquemanagementbackend.dto.facture.FactureUpdateDTO;
import org.kfokam48.cliniquemanagementbackend.dto.facture.FacturePaiementUpdateDTO;
import org.kfokam48.cliniquemanagementbackend.dto.lignefacture.LigneFactureDTO;
import org.kfokam48.cliniquemanagementbackend.enums.StatutFacture;
import org.kfokam48.cliniquemanagementbackend.enums.StatutRendezVous;
import org.kfokam48.cliniquemanagementbackend.exception.RessourceNotFoundException;
import org.kfokam48.cliniquemanagementbackend.exception.RendezVousNonTermineException;
import org.kfokam48.cliniquemanagementbackend.mapper.FactureMapper;
import org.kfokam48.cliniquemanagementbackend.mapper.LigneFactureMapper;
import org.kfokam48.cliniquemanagementbackend.model.Facture;
import org.kfokam48.cliniquemanagementbackend.model.LigneFacture;
import org.kfokam48.cliniquemanagementbackend.model.RendezVous;
import org.kfokam48.cliniquemanagementbackend.repository.FactureRepository;
import org.kfokam48.cliniquemanagementbackend.repository.PatientRepository;
import org.kfokam48.cliniquemanagementbackend.repository.RendezVousRepository;
import org.kfokam48.cliniquemanagementbackend.service.FactureService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class FactureServiceImpl implements FactureService {
     private final FactureRepository factureRepository;
     private final PatientRepository patientRepository;
     private final RendezVousRepository rendezVousRepository;
    private final FactureMapper factureMapper;
    private final LigneFactureMapper ligneFactureMapper;

    public FactureServiceImpl(FactureRepository factureRepository, PatientRepository patientRepository, RendezVousRepository rendezVousRepository, FactureMapper factureMapper, LigneFactureMapper ligneFactureMapper) {
        this.factureRepository = factureRepository;
        this.patientRepository = patientRepository;
        this.rendezVousRepository = rendezVousRepository;
        this.factureMapper = factureMapper;
        this.ligneFactureMapper = ligneFactureMapper;
    }

    @Override
    public FactureResponseDto save(@Valid FactureDTO factureDTO) {
        // Vérifier que le rendez-vous existe et qu'il est terminé
        RendezVous rendezVous = rendezVousRepository.findById(factureDTO.getRendezVousId())
                .orElseThrow(() -> new RessourceNotFoundException("Rendez-vous not found with id: " + factureDTO.getRendezVousId()));
        
        if (rendezVous.getStatutRendezVous() != StatutRendezVous.TERMINE) {
            throw new RendezVousNonTermineException("Impossible de créer une facture pour un rendez-vous qui n'est pas terminé. Statut actuel: " + rendezVous.getStatutRendezVous());
        }
        
        Facture facture = factureMapper.factureDtoToFacture(factureDTO);
        facture.setDateEmission(LocalDateTime.now());
        facture.setStatut(StatutFacture.NON_PAYEE);
        factureRepository.save(facture);
        return factureMapper.factureToFactureResponseDto(facture);
    }

    @Override
    public FactureResponseDto findById(Long id) {
        return factureMapper.factureToFactureResponseDto(factureRepository.findById(id).orElseThrow(() -> new RessourceNotFoundException("Facture not found")));
    }

    @Override
    public List<FactureResponseDto> findAll() {
        return factureMapper.factureListToFactureResponseDtoList(factureRepository.findAll());
    }

    @Override
    public FactureResponseDto update(Long id, @Valid FactureDTO factureDTO) {
        Facture facture = factureRepository.findById(id)
                .orElseThrow(() -> new RessourceNotFoundException("Facture not found"));

        // Récupérer le rendez-vous depuis la base de données pour éviter l'erreur TransientObjectException
        RendezVous rendezVous = rendezVousRepository.findById(factureDTO.getRendezVousId())
                .orElseThrow(() -> new RessourceNotFoundException("Rendez-vous not found with id: " + factureDTO.getRendezVousId()));

        // Mettre à jour le rendez-vous de la facture
        facture.setRendezVous(rendezVous);
        BigDecimal total =rendezVous.getTypeRendezVous().getTarif();

        // Gérer les lignes de facture : on remplace complètement la liste
        if (factureDTO.getLignesFacture() != null) {
            // Supprimer toutes les anciennes lignes (pour que la suppression soit effective en base, il faut orphanRemoval=true sur la relation)
            facture.getLignes().clear();

            // Ligne de base (consultation)
            LigneFacture ligneConsult = new LigneFacture();
            ligneConsult.setServiceName(rendezVous.getTypeRendezVous().getLibelle());
            ligneConsult.setPrixUnitaire(rendezVous.getTypeRendezVous().getTarif());
            ligneConsult.setQuantite(1);
            ligneConsult.setPrixTotal(rendezVous.getTypeRendezVous().getTarif());
            ligneConsult.setFacture(facture);
            facture.getLignes().add(ligneConsult);


            // Ajouter les nouvelles lignes
            for (LigneFactureDTO ligneDTO : factureDTO.getLignesFacture()) {
                LigneFacture ligne = ligneFactureMapper.ligneFactureDTOToLigneFacture(ligneDTO);
                ligne.setFacture(facture);
                total = total.add(ligne.getPrixTotal());
                facture.getLignes().add(ligne);
            }
        }

        // Mettre à jour d'autres champs si nécessaire (ex: statut, date, etc.)
        facture.setStatut(StatutFacture.NON_PAYEE);
        facture.setMontantTotal(total);
        facture.setMontantRestant(total);
        facture.setMontantPayement(BigDecimal.ZERO);
        facture.setDateEmission(LocalDateTime.now());
        facture.setDatePayement(null); // Réinitialiser la date de paiement
        // Enregistrer la facture mise à jour

        factureRepository.save(facture);
        return factureMapper.factureToFactureResponseDto(facture);
    }

    @Override
    public ResponseEntity<String> deleteById(Long id) {
        Facture facture = factureRepository.findById(id).orElseThrow(() -> new RessourceNotFoundException("Facture not found"));
        factureRepository.delete(facture);
        return ResponseEntity.ok("Facture deleted successfully");
    }

    @Override
    public List<FactureResponseDto> findByMedecinId(Long medecinId) {
        List<Facture> factures = factureRepository.findByRendezVous_Medecin_Id(medecinId);
        return factureMapper.factureListToFactureResponseDtoList(factures);
    }

    @Override
    public FactureResponseDto updatePaiement(Long id, @Valid FacturePaiementUpdateDTO paiementUpdateDTO) {
        Facture facture = factureRepository.findById(id)
                .orElseThrow(() -> new RessourceNotFoundException("Facture not found with id: " + id));
        
        // Mettre à jour le montant du paiement
        facture.setMontantPayement(paiementUpdateDTO.getMontantPaiement());
        facture.setDatePayement(LocalDateTime.now());
        
        // Calculer le montant restant
        BigDecimal montantRestant = facture.getMontantTotal().subtract(facture.getMontantPayement());
        facture.setMontantRestant(montantRestant);
        
        // Mettre à jour le statut en fonction du montant payé
        if (facture.getMontantPayement().compareTo(facture.getMontantTotal()) >= 0) {
            // Paiement complet ou supérieur
            facture.setStatut(StatutFacture.PAYEE);
            facture.setMontantRestant(BigDecimal.ZERO);
        } else if (facture.getMontantPayement().compareTo(BigDecimal.ZERO) > 0) {
            // Paiement partiel
            facture.setStatut(StatutFacture.PARTIELLEMENT_PAYE);
        } else {
            // Aucun paiement
            facture.setStatut(StatutFacture.NON_PAYEE);
        }
        
        factureRepository.save(facture);
        return factureMapper.factureToFactureResponseDto(facture);
    }
}
