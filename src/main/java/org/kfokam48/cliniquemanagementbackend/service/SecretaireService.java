package org.kfokam48.cliniquemanagementbackend.service;


import org.kfokam48.cliniquemanagementbackend.dto.secretaire.SecretaireDTO;
import org.kfokam48.cliniquemanagementbackend.dto.secretaire.SecretaireResponseDTO;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface SecretaireService {
    SecretaireResponseDTO save(SecretaireDTO secretaireDTO);
   SecretaireResponseDTO findById(Long id);
    SecretaireResponseDTO update(Long id , SecretaireDTO secretaireDTO);
    List<SecretaireResponseDTO> findAll();
    ResponseEntity<String > deleteById(Long id);
}
