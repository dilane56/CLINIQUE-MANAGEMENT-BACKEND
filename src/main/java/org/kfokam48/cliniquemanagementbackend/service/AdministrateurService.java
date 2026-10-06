package org.kfokam48.cliniquemanagementbackend.service;


import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.kfokam48.cliniquemanagementbackend.dto.AdministrateurDTO;
import org.kfokam48.cliniquemanagementbackend.model.Administrateur;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface AdministrateurService {
    Administrateur save(AdministrateurDTO administrateurDTO);
    List<Administrateur> findAll();
    Administrateur findById(Long id);
    Administrateur update(Long id, AdministrateurDTO administrateurDTO);
    Administrateur findByEmail(String email);
    ResponseEntity<String> deleteById(Long id);

    Page<Administrateur> findAll(Pageable pageable);
}

