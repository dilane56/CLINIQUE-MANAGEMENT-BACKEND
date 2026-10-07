package org.kfokam48.cliniquemanagementbackend.service;

import org.springframework.data.domain.Pageable;
import org.kfokam48.cliniquemanagementbackend.dto.PageResponse;
import org.kfokam48.cliniquemanagementbackend.dto.medecin.MedecinDTO;
import org.kfokam48.cliniquemanagementbackend.dto.medecin.MedecinResponseDTO;
import org.kfokam48.cliniquemanagementbackend.model.Medecin;

import java.util.List;

public interface MedecinService {
    Medecin save(MedecinDTO medecinDTO);
    MedecinResponseDTO findById(Long id);
    MedecinResponseDTO update(Long id, MedecinDTO medecinDTO);
    void deleteById(Long id);
    List<MedecinResponseDTO> findAll();
    PageResponse<MedecinResponseDTO> findAll(Pageable pageable);
}
