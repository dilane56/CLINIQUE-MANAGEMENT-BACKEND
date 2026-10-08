package org.kfokam48.cliniquemanagementbackend.service.impl;

import java.util.Objects;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
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
        // Son propre e-mail est accepté ; seul un e-mail déjà pris par un autre compte est refusé
        if (!Objects.equals(administrateur.getEmail(), administrateurDTO.getEmail())
                && utilisateurRepository.existsByEmail(administrateurDTO.getEmail())) {
            throw new ResourceAlreadyExistException("Administrateur already exists with this email");
        }

        administrateur.setEmail(administrateurDTO.getEmail());
        administrateur.setPassword(passwordEncoder.encode(administrateurDTO.getPassword()));
        administrateur.setNom(administrateurDTO.getNom());
        administrateur.setPrenom(administrateurDTO.getPrenom());
        administrateur.setTelephone(administrateurDTO.getTelephone());
        // Le rôle n'est jamais repris de la requête (il est fixé par le type de compte)
        administrateurRepository.save(administrateur);
        return administrateur;
    }

    @Override
    public Administrateur findByEmail(String email) {
        return administrateurRepository.findByEmail(email)
                .orElseThrow(() -> new RessourceNotFoundException("Administrateur not found"));
    }

    @Override
    public void deleteById(Long id) {
        Administrateur administrateur = administrateurRepository.findById(id).orElseThrow(()->new RessourceNotFoundException("Administrateur not found"));
        administrateurRepository.deleteById(id);

    }

    @Override
    @Transactional(readOnly = true)
    public Page<Administrateur> findAll(Pageable pageable) {
        return administrateurRepository.findAll(pageable);
    }
}
