package org.kfokam48.cliniquemanagementbackend.service.impl;


import jakarta.validation.Valid;
import org.kfokam48.cliniquemanagementbackend.controlleur.notification.NotificationController;
import org.kfokam48.cliniquemanagementbackend.dto.facture.FactureDTO;
import org.kfokam48.cliniquemanagementbackend.dto.facture.FactureResponseDto;
import org.kfokam48.cliniquemanagementbackend.dto.facture.FacturePaiementUpdateDTO;
import org.kfokam48.cliniquemanagementbackend.dto.lignefacture.LigneFactureDTO;
import org.kfokam48.cliniquemanagementbackend.enums.StatutFacture;
import org.kfokam48.cliniquemanagementbackend.enums.StatutRendezVous;
import org.kfokam48.cliniquemanagementbackend.exception.RessourceNotFoundException;
import org.kfokam48.cliniquemanagementbackend.exception.RendezVousNonTermineException;
import org.kfokam48.cliniquemanagementbackend.mapper.FactureMapper;
import org.kfokam48.cliniquemanagementbackend.mapper.LigneFactureMapper;
import org.kfokam48.cliniquemanagementbackend.model.Facture;
import org.kfokam48.cliniquemanagementbackend.model.LigneFacture;
import org.kfokam48.cliniquemanagementbackend.model.RendezVous;
import org.kfokam48.cliniquemanagementbackend.repository.FactureRepository;
import org.kfokam48.cliniquemanagementbackend.repository.RendezVousRepository;
import org.kfokam48.cliniquemanagementbackend.service.FactureService;
import org.kfokam48.cliniquemanagementbackend.service.pdf.PdfService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.itextpdf.text.DocumentException;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class FactureServiceImpl implements FactureService {
    private final FactureRepository factureRepository;
    private final RendezVousRepository rendezVousRepository;
    private final FactureMapper factureMapper;
    private final LigneFactureMapper ligneFactureMapper;
    private final NotificationController notificationController;
    private final PdfService pdfService;

    public FactureServiceImpl(FactureRepository factureRepository, RendezVousRepository rendezVousRepository, FactureMapper factureMapper, LigneFactureMapper ligneFactureMapper, NotificationController notificationController, PdfService pdfService) {
        this.factureRepository = factureRepository;
        this.rendezVousRepository = rendezVousRepository;
        this.factureMapper = factureMapper;
        this.ligneFactureMapper = ligneFactureMapper;
        this.notificationController = notificationController;
        this.pdfService = pdfService;
    }

    @Override
    public FactureResponseDto save(@Valid FactureDTO factureDTO) {
        RendezVous rendezVous = rendezVousRepository.findById(factureDTO.getRendezVousId())
                .orElseThrow(() -> new RessourceNotFoundException("Rendez-vous not found with id: " + factureDTO.getRendezVousId()));

        if (rendezVous.getStatutRendezVous() != StatutRendezVous.TERMINE) {
            throw new RendezVousNonTermineException("Impossible de créer une facture pour un rendez-vous qui n'est pas terminé. Statut actuel: " + rendezVous.getStatutRendezVous());
        }

        Facture facture = buildFactureFromRendezVous(new Facture(), rendezVous, factureDTO.getLignesFacture());
        facture.setDateEmission(LocalDateTime.now());
        facture.setStatut(StatutFacture.NON_PAYEE);
        facture.setMontantPayement(BigDecimal.ZERO);
        factureRepository.save(facture);
        return factureMapper.factureToFactureResponseDto(facture);
    }

    @Override
    public FactureResponseDto findById(Long id) {
        return factureMapper.factureToFactureResponseDto(factureRepository.findById(id).orElseThrow(() -> new RessourceNotFoundException("Facture not found")));
    }

    @Override
    public List<FactureResponseDto> findAll() {
        return factureMapper.factureListToFactureResponseDtoList(factureRepository.findAll());
    }

    @Override
    public FactureResponseDto update(Long id, @Valid FactureDTO factureDTO) {
        Facture facture = factureRepository.findById(id)
                .orElseThrow(() -> new RessourceNotFoundException("Facture not found"));

        RendezVous rendezVous = rendezVousRepository.findById(factureDTO.getRendezVousId())
                .orElseThrow(() -> new RessourceNotFoundException("Rendez-vous not found with id: " + factureDTO.getRendezVousId()));

        facture.getLignes().clear();
        buildFactureFromRendezVous(facture, rendezVous, factureDTO.getLignesFacture());
        facture.setStatut(StatutFacture.NON_PAYEE);
        facture.setMontantPayement(BigDecimal.ZERO);
        facture.setDateEmission(LocalDateTime.now());
        facture.setDatePayement(null);

        factureRepository.save(facture);
        return factureMapper.factureToFactureResponseDto(facture);
    }

    @Override
    public ResponseEntity<String> deleteById(Long id) {
        Facture facture = factureRepository.findById(id).orElseThrow(() -> new RessourceNotFoundException("Facture not found"));
        factureRepository.delete(facture);
        return ResponseEntity.ok("Facture deleted successfully");
    }

    @Override
    public List<FactureResponseDto> findByMedecinId(Long medecinId) {
        List<Facture> factures = factureRepository.findByRendezVous_Medecin_Id(medecinId);
        return factureMapper.factureListToFactureResponseDtoList(factures);
    }

    @Override
    public FactureResponseDto updatePaiement(Long id, @Valid FacturePaiementUpdateDTO paiementUpdateDTO) {
        Facture facture = factureRepository.findById(id)
                .orElseThrow(() -> new RessourceNotFoundException("Facture not found with id: " + id));

        if (facture.getStatut() == StatutFacture.PAYEE) {
            throw new IllegalStateException("Cette facture est déjà entièrement payée.");
        }

        // Accumulation du paiement
        BigDecimal nouveauTotal = facture.getMontantPayement().add(paiementUpdateDTO.getMontantPaiement());
        facture.setMontantPayement(nouveauTotal);
        facture.setDatePayement(LocalDateTime.now());

        BigDecimal montantRestant = facture.getMontantTotal().subtract(nouveauTotal);

        if (nouveauTotal.compareTo(facture.getMontantTotal()) >= 0) {
            facture.setStatut(StatutFacture.PAYEE);
            facture.setMontantRestant(BigDecimal.ZERO);
        } else {
            facture.setStatut(StatutFacture.PARTIELLEMENT_PAYE);
            facture.setMontantRestant(montantRestant);
        }

        factureRepository.save(facture);
        notificationController.sendNotification(1L, "Facture", "Un paiement a été enregistré sur la facture #" + id, false);
        notificationController.sendNotification(facture.getRendezVous().getMedecin().getId(), "Facture", "Un paiement a été enregistré sur la facture #" + id, false);
        return factureMapper.factureToFactureResponseDto(facture);
    }

    @Override
    public ByteArrayOutputStream generatePdf(Long id) throws DocumentException {
        Facture facture = factureRepository.findById(id)
                .orElseThrow(() -> new RessourceNotFoundException("Facture not found with id: " + id));
        return pdfService.generateFacturePdf(facture);
    }

    private Facture buildFactureFromRendezVous(Facture facture, RendezVous rendezVous, List<LigneFactureDTO> lignesDTO) {
        facture.setRendezVous(rendezVous);
        BigDecimal total = rendezVous.getTypeRendezVous().getTarif();

        List<LigneFacture> lignes = new ArrayList<>();

        LigneFacture ligneConsult = new LigneFacture();
        ligneConsult.setServiceName(rendezVous.getTypeRendezVous().getLibelle());
        ligneConsult.setPrixUnitaire(rendezVous.getTypeRendezVous().getTarif());
        ligneConsult.setQuantite(1);
        ligneConsult.setPrixTotal(rendezVous.getTypeRendezVous().getTarif());
        ligneConsult.setFacture(facture);
        lignes.add(ligneConsult);

        if (lignesDTO != null) {
            for (LigneFactureDTO ligneDTO : lignesDTO) {
                LigneFacture ligne = ligneFactureMapper.ligneFactureDTOToLigneFacture(ligneDTO);
                ligne.setFacture(facture);
                total = total.add(ligne.getPrixTotal());
                lignes.add(ligne);
            }
        }

        facture.getLignes().addAll(lignes);
        facture.setMontantTotal(total);
        facture.setMontantRestant(total);
        return facture;
    }
}
