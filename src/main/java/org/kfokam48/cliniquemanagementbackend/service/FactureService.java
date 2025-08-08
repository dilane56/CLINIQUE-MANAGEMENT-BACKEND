package org.kfokam48.cliniquemanagementbackend.service;


import org.kfokam48.cliniquemanagementbackend.dto.facture.FactureDTO;
import org.kfokam48.cliniquemanagementbackend.dto.facture.FactureResponseDto;
import org.kfokam48.cliniquemanagementbackend.dto.facture.FactureUpdateDTO;
import org.kfokam48.cliniquemanagementbackend.dto.facture.FacturePaiementUpdateDTO;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface FactureService {
    FactureResponseDto save(FactureDTO factureDTO);
    FactureResponseDto findById(Long id);
    List<FactureResponseDto> findAll();
    FactureResponseDto update(Long id, FactureDTO factureDTO);
    ResponseEntity<String> deleteById(Long id);
    List<FactureResponseDto> findByMedecinId(Long medecinId);
    FactureResponseDto updatePaiement(Long id, FacturePaiementUpdateDTO paiementUpdateDTO);
}
