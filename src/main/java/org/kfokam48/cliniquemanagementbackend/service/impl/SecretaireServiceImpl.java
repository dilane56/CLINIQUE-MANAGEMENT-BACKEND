package org.kfokam48.cliniquemanagementbackend.service.impl;

import jakarta.validation.Valid;
import org.kfokam48.cliniquemanagementbackend.dto.secretaire.SecretaireDTO;
import org.kfokam48.cliniquemanagementbackend.dto.secretaire.SecretaireResponseDTO;
import org.kfokam48.cliniquemanagementbackend.enums.Roles;
import org.kfokam48.cliniquemanagementbackend.exception.ResourceAlreadyExistException;
import org.kfokam48.cliniquemanagementbackend.exception.RessourceNotFoundException;
import org.kfokam48.cliniquemanagementbackend.mapper.SecretaireMapper;
import org.kfokam48.cliniquemanagementbackend.model.Secretaire;
import org.kfokam48.cliniquemanagementbackend.repository.SecretaireRepository;
import org.kfokam48.cliniquemanagementbackend.repository.UtilisateurRepository;
import org.kfokam48.cliniquemanagementbackend.service.SecretaireService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@Transactional
public class SecretaireServiceImpl implements SecretaireService {

    private final SecretaireRepository secretaireRepository;
    private final SecretaireMapper secretaireMapper;
    private final UtilisateurRepository utilisateurRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public SecretaireServiceImpl(SecretaireRepository secretaireRepository, SecretaireMapper secretaireMapper, UtilisateurRepository utilisateurRepository) {
        this.secretaireRepository = secretaireRepository;
        this.secretaireMapper = secretaireMapper;
        this.utilisateurRepository = utilisateurRepository;
    }

    @Override
    public SecretaireResponseDTO save(@Valid SecretaireDTO secretaireDTO) {
        if (utilisateurRepository.existsByEmail(secretaireDTO.getEmail())) {
            throw new ResourceAlreadyExistException("User already exists with this email");
        }

        Secretaire secretaire = secretaireMapper.secretaireDtoToSecretaire(secretaireDTO);
        secretaire.setPassword(passwordEncoder.encode(secretaireDTO.getPassword()));
        secretaire.setRole(Roles.valueOf("SECRETAIRE"));
        secretaireRepository.save(secretaire);
        return secretaireMapper.secretaireToSecretaireResponseDto(secretaire);
    }

    @Override
    public SecretaireResponseDTO findById(Long id) {
        Secretaire secretaire= secretaireRepository.findById(id)
                .orElseThrow(() -> new RessourceNotFoundException("Secretaire not found"));
        return secretaireMapper.secretaireToSecretaireResponseDto(secretaire);
    }

    @Override
    public SecretaireResponseDTO update(Long id,@Valid SecretaireDTO secretaireDTO) {
        Secretaire secretaire = secretaireRepository.findById(id)
                .orElseThrow(() -> new RessourceNotFoundException("Secretaire not found"));
        if (Objects.equals(secretaire.getEmail(), secretaireDTO.getEmail()) || !utilisateurRepository.existsByEmail(secretaireDTO.getEmail())) {

            secretaire.setEmail(secretaireDTO.getEmail());
            secretaire.setPassword(passwordEncoder.encode(secretaireDTO.getPassword()));
            secretaire.setRole(secretaireDTO.getRole());
            secretaire.setPrenom(secretaireDTO.getPrenom());
            secretaire.setTelephone(secretaireDTO.getTelephone());
            secretaire.setNom(secretaireDTO.getNom());
            secretaireRepository.save(secretaire);
            return secretaireMapper.secretaireToSecretaireResponseDto(secretaire);
        } else {
            throw new ResourceAlreadyExistException("User already exists with this email");
        }


    }

    @Override
    public List<SecretaireResponseDTO> findAll() {
       return secretaireMapper.secretaireListToSecretaireResponseDtoList(secretaireRepository.findAll());
    }

    @Override
    public ResponseEntity<String> deleteById(Long id) {
       secretaireRepository.findById(id)
                .orElseThrow(() -> new RessourceNotFoundException("Secretaire not found"));
        secretaireRepository.deleteById(id);
        return ResponseEntity.ok("Secretaire deleted successfully");
    }
}
