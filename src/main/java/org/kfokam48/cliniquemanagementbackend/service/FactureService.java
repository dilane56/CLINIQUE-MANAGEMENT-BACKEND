package org.kfokam48.cliniquemanagementbackend.service;


import org.kfokam48.cliniquemanagementbackend.enums.StatutFacture;
import java.util.Map;
import org.springframework.data.domain.Pageable;
import org.kfokam48.cliniquemanagementbackend.dto.PageResponse;
import org.kfokam48.cliniquemanagementbackend.dto.facture.FactureDTO;
import org.kfokam48.cliniquemanagementbackend.dto.facture.FactureResponseDto;
import org.kfokam48.cliniquemanagementbackend.dto.facture.FacturePaiementUpdateDTO;
import com.lowagie.text.DocumentException;
import java.io.ByteArrayOutputStream;
import java.util.List;

public interface FactureService {
    FactureResponseDto save(FactureDTO factureDTO);
    FactureResponseDto findById(Long id);
    List<FactureResponseDto> findAll();
    FactureResponseDto update(Long id, FactureDTO factureDTO);
    void deleteById(Long id);
    List<FactureResponseDto> findByMedecinId(Long medecinId);
    FactureResponseDto updatePaiement(Long id, FacturePaiementUpdateDTO paiementUpdateDTO);
    ByteArrayOutputStream generatePdf(Long id) throws DocumentException;
    PageResponse<FactureResponseDto> findAll(Pageable pageable);
    PageResponse<FactureResponseDto> findByMedecinId(Long medecinId, Pageable pageable);

    // Nombre de factures par statut (tous les statuts, 0 si aucune)
    Map<StatutFacture, Long> compterParStatut();
}
