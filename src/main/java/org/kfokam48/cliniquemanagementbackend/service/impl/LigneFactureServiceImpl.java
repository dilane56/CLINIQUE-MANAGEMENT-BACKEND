package org.kfokam48.cliniquemanagementbackend.service.impl;

import org.kfokam48.cliniquemanagementbackend.dto.lignefacture.LigneFactureDTO;
import org.kfokam48.cliniquemanagementbackend.dto.lignefacture.LigneFactureResponseDTO;
import org.kfokam48.cliniquemanagementbackend.exception.RessourceNotFoundException;
import org.kfokam48.cliniquemanagementbackend.mapper.LigneFactureMapper;
import org.kfokam48.cliniquemanagementbackend.model.LigneFacture;
import org.kfokam48.cliniquemanagementbackend.repository.LigneFactureRepository;
import org.kfokam48.cliniquemanagementbackend.service.LigneFactureService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LigneFactureServiceImpl implements LigneFactureService {

    private  final LigneFactureRepository repository;

    private final LigneFactureMapper mapper;

    public LigneFactureServiceImpl(LigneFactureRepository repository, LigneFactureMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public LigneFactureResponseDTO ajouterLigne(LigneFactureDTO dto) {
       LigneFacture ligneFacture = mapper.ligneFactureDTOToLigneFacture(dto);
       repository.save(ligneFacture);
       return mapper.toResponseDTO(ligneFacture);
    }

    @Override
    public LigneFactureResponseDTO modifierLigne(Long id, LigneFactureDTO dto) {
        LigneFacture entity = repository.findById(id).orElseThrow();
        entity.setServiceName(dto.getServiceName());
        entity.setPrixUnitaire(dto.getPrixUnitaire());
        entity.setQuantite(dto.getQuantite());
        return mapper.toResponseDTO(repository.save(entity));
    }

    @Override
    public void supprimerLigne(Long id) {
        repository.findById(id).orElseThrow(() -> new RessourceNotFoundException("LigneFacture not found with id: " + id));
        repository.deleteById(id);
    }

    @Override
    public LigneFactureResponseDTO getLigne(Long id) {
        repository.findById(id).orElseThrow(() -> new RessourceNotFoundException("LigneFacture not found with id: " + id));
        return mapper.toResponseDTO(repository.findById(id).orElseThrow());
    }

    @Override
    public List<LigneFactureResponseDTO> listerLignes() {
       return mapper.toResponseDTOList(repository.findAll());
    }
}

