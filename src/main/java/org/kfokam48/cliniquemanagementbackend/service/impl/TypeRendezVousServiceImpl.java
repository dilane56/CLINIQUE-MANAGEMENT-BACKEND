package org.kfokam48.cliniquemanagementbackend.service.impl;

import org.kfokam48.cliniquemanagementbackend.dto.typeRendezVous.TypeRendezVousDTO;
import org.kfokam48.cliniquemanagementbackend.dto.typeRendezVous.TypeRendezVousResponseDTO;
import org.kfokam48.cliniquemanagementbackend.exception.RessourceNotFoundException;
import org.kfokam48.cliniquemanagementbackend.mapper.TypeRencezVousMapper;
import org.kfokam48.cliniquemanagementbackend.model.TypeRendezVous;
import org.kfokam48.cliniquemanagementbackend.repository.TypeRendezVousRepository;
import org.kfokam48.cliniquemanagementbackend.service.TypeRendezVousService;

import org.springframework.stereotype.Service;
import java.util.List;


@Service
public class TypeRendezVousServiceImpl implements TypeRendezVousService {

    private  final TypeRendezVousRepository typeRendezVousRepository;
    private final TypeRencezVousMapper typeRencezVousMapper;

    public TypeRendezVousServiceImpl(TypeRendezVousRepository typeRendezVousRepository, TypeRencezVousMapper typeRencezVousMapper) {
        this.typeRendezVousRepository = typeRendezVousRepository;
        this.typeRencezVousMapper = typeRencezVousMapper;
    }

    @Override
    public TypeRendezVousResponseDTO ajouterTypeRendezVous(TypeRendezVousDTO dto) {
        TypeRendezVous entity = typeRencezVousMapper.typeRendezVousDtoToTypeRendezVous(dto);
        TypeRendezVous savedEntity = typeRendezVousRepository.save(entity);
        return typeRencezVousMapper.typeRendezVousToTypeRendezVousResponseDTO(savedEntity);
    }

    @Override
    public TypeRendezVousResponseDTO modifierTypeRendezVous(Long id, TypeRendezVousDTO dto) {
        TypeRendezVous entity = typeRendezVousRepository.findById(id).orElseThrow(() -> new RessourceNotFoundException("Type de rendez-vous non trouvé"));
        entity.setLibelle(dto.getLibelle());
        entity.setDuree(dto.getDuree());
        entity.setTarif(dto.getTarif());
        typeRendezVousRepository.save(entity);
        return typeRencezVousMapper.typeRendezVousToTypeRendezVousResponseDTO(entity);
    }

    @Override
    public void supprimerTypeRendezVous(Long id) {
        typeRendezVousRepository.deleteById(id);
    }

    @Override
    public List<TypeRendezVousResponseDTO> listerTypeRendezVous() {
       return  typeRencezVousMapper.typeRendezVousListToTypeRendezVousResponseDTOList(
                typeRendezVousRepository.findAll()
        );
    }
}

