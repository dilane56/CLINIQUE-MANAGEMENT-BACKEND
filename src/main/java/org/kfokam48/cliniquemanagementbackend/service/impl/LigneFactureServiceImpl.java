package org.kfokam48.cliniquemanagementbackend.service.impl;

import org.kfokam48.cliniquemanagementbackend.dto.lignefacture.LigneFactureDTO;
import org.kfokam48.cliniquemanagementbackend.dto.lignefacture.LigneFactureResponseDTO;
import org.kfokam48.cliniquemanagementbackend.exception.RessourceNotFoundException;
import org.kfokam48.cliniquemanagementbackend.mapper.LigneFactureMapper;
import org.kfokam48.cliniquemanagementbackend.model.Facture;
import org.kfokam48.cliniquemanagementbackend.model.LigneFacture;
import org.kfokam48.cliniquemanagementbackend.repository.FactureRepository;
import org.kfokam48.cliniquemanagementbackend.repository.LigneFactureRepository;
import org.kfokam48.cliniquemanagementbackend.service.LigneFactureService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class LigneFactureServiceImpl implements LigneFactureService {

    private  final LigneFactureRepository repository;

    private final LigneFactureMapper mapper;

    private final FactureRepository factureRepository;

    public LigneFactureServiceImpl(LigneFactureRepository repository, LigneFactureMapper mapper, FactureRepository factureRepository) {
        this.repository = repository;
        this.mapper = mapper;
        this.factureRepository = factureRepository;
    }

    // Modifiable uniquement si la facture n'a reçu aucun paiement ; le total de la facture est recalculé
    @Override
    @Transactional
    public LigneFactureResponseDTO modifierLigne(Long id, LigneFactureDTO dto) {
        LigneFacture entity = repository.findById(id)
                .orElseThrow(() -> new RessourceNotFoundException("Ligne de facture introuvable : " + id));
        Facture facture = entity.getFacture();
        if (facture != null) {
            facture.verifierModifiable();
        }
        entity.setServiceName(dto.getServiceName());
        entity.setPrixUnitaire(dto.getPrixUnitaire());
        entity.setQuantite(dto.getQuantite());
        entity.setPrixTotal(dto.getPrixUnitaire().multiply(BigDecimal.valueOf(dto.getQuantite())));
        LigneFacture saved = repository.save(entity);
        if (facture != null) {
            facture.recalculerMontants();
            factureRepository.save(facture);
        }
        return mapper.toResponseDTO(saved);
    }

    // Supprimable uniquement si la facture n'a reçu aucun paiement ; le total de la facture est recalculé
    @Override
    @Transactional
    public void supprimerLigne(Long id) {
        LigneFacture entity = repository.findById(id)
                .orElseThrow(() -> new RessourceNotFoundException("Ligne de facture introuvable : " + id));
        Facture facture = entity.getFacture();
        if (facture == null) {
            repository.delete(entity);
            return;
        }
        facture.verifierModifiable();
        // orphanRemoval sur Facture.lignes : retirer la ligne de la facture la supprime
        facture.getLignes().remove(entity);
        facture.recalculerMontants();
        factureRepository.save(facture);
    }

    @Override
    public LigneFactureResponseDTO getLigne(Long id) {
        repository.findById(id).orElseThrow(() -> new RessourceNotFoundException("Ligne de facture introuvable : " + id));
        return mapper.toResponseDTO(repository.findById(id).orElseThrow());
    }

    @Override
    public List<LigneFactureResponseDTO> listerLignes() {
       return mapper.toResponseDTOList(repository.findAll());
    }
}

