package org.kfokam48.cliniquemanagementbackend.service;


import org.springframework.data.domain.Pageable;
import org.kfokam48.cliniquemanagementbackend.dto.PageResponse;
import org.kfokam48.cliniquemanagementbackend.dto.secretaire.SecretaireDTO;
import org.kfokam48.cliniquemanagementbackend.dto.secretaire.SecretaireResponseDTO;

import java.util.List;

public interface SecretaireService {
    SecretaireResponseDTO save(SecretaireDTO secretaireDTO);
   SecretaireResponseDTO findById(Long id);
    SecretaireResponseDTO update(Long id , SecretaireDTO secretaireDTO);
    List<SecretaireResponseDTO> findAll();
    void deleteById(Long id);
    PageResponse<SecretaireResponseDTO> findAll(Pageable pageable);
}
