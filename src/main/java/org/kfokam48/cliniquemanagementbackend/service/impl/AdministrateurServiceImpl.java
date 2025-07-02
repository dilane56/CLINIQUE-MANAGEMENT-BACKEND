package org.kfokam48.cliniquemanagementbackend.service.impl;

import jakarta.validation.Valid;
import org.kfokam48.cliniquemanagementbackend.dto.AdministrateurDTO;
import org.kfokam48.cliniquemanagementbackend.enums.Roles;
import org.kfokam48.cliniquemanagementbackend.exception.ResourceAlreadyExistException;
import org.kfokam48.cliniquemanagementbackend.exception.RessourceNotFoundException;
import org.kfokam48.cliniquemanagementbackend.mapper.AdministrateurMapper;
import org.kfokam48.cliniquemanagementbackend.model.Administrateur;
import org.kfokam48.cliniquemanagementbackend.repository.AdministrateurRepository;
import org.kfokam48.cliniquemanagementbackend.repository.UtilisateurRepository;
import org.kfokam48.cliniquemanagementbackend.service.AdministrateurService;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@Transactional
public class AdministrateurServiceImpl implements AdministrateurService {
    private final AdministrateurRepository administrateurRepository;
    private final AdministrateurMapper administrateurMapper;
   private final UtilisateurRepository  utilisateurRepository;


    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();



    public AdministrateurServiceImpl(AdministrateurRepository administrateurRepository, AdministrateurMapper administrateurMapper, UtilisateurRepository utilisateurRepository) {
        this.administrateurRepository = administrateurRepository;
        this.administrateurMapper = administrateurMapper;
        this.utilisateurRepository = utilisateurRepository;
    }

    @Override
    public Administrateur save(@Valid AdministrateurDTO administrateurDTO) {
        if (utilisateurRepository.existsByEmail(administrateurDTO.getEmail())) {
            throw new ResourceAlreadyExistException("Administrateur already exists with this email");
        }

        Administrateur administrateur = administrateurMapper.administrateurDtoToAdministrateur(administrateurDTO);
        administrateur.setPassword(passwordEncoder.encode(administrateurDTO.getPassword()));
        administrateur.setRole(Roles.valueOf("ADMIN"));
        administrateurRepository.save(administrateur);

        return administrateur;
    }

    @Override
    public List<Administrateur> findAll() {
        return administrateurRepository.findAll();
    }

    @Override
    public Administrateur findById(Long id) {
        return administrateurRepository.findById(id).orElseThrow(()->new RessourceNotFoundException("Administrateur not found"));
    }

    @Override
    public Administrateur update(Long id,@Valid AdministrateurDTO administrateurDTO) {
        Administrateur administrateur = administrateurRepository.findById(id).orElseThrow(()->new RessourceNotFoundException("Administrateur not found"));
        if (utilisateurRepository.existsByEmail(administrateurDTO.getEmail())) {
            throw new ResourceAlreadyExistException("Administrateur already exists with this email");
        }

        administrateur.setEmail(administrateurDTO.getEmail());
        administrateur.setPassword(passwordEncoder.encode(administrateurDTO.getPassword()));
        administrateurRepository.save(administrateur);
        return administrateur;
    }

    @Override
    public Administrateur findByEmail(String email) {
        Administrateur admin =administrateurRepository.findByEmail(email);
        if(admin == null){
            throw new RessourceNotFoundException("Administrateur not found");
        }else{
            return admin;
        }
    }

    @Override
    public ResponseEntity<String> deleteById(Long id) {
        Administrateur administrateur = administrateurRepository.findById(id).orElseThrow(()->new RessourceNotFoundException("Administrateur not found"));
        administrateurRepository.deleteById(id);
        return ResponseEntity.ok("Administrateur deleted successfully");

    }
}
