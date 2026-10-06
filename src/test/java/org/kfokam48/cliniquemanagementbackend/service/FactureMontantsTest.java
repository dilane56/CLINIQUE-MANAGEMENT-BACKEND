package org.kfokam48.cliniquemanagementbackend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kfokam48.cliniquemanagementbackend.service.notification.NotificationService;
import org.kfokam48.cliniquemanagementbackend.dto.facture.FactureDTO;
import org.kfokam48.cliniquemanagementbackend.dto.lignefacture.LigneFactureDTO;
import org.kfokam48.cliniquemanagementbackend.enums.StatutFacture;
import org.kfokam48.cliniquemanagementbackend.mapper.FactureMapper;
import org.kfokam48.cliniquemanagementbackend.mapper.LigneFactureMapper;
import org.kfokam48.cliniquemanagementbackend.model.Facture;
import org.kfokam48.cliniquemanagementbackend.model.LigneFacture;
import org.kfokam48.cliniquemanagementbackend.repository.FactureRepository;
import org.kfokam48.cliniquemanagementbackend.repository.LigneFactureRepository;
import org.kfokam48.cliniquemanagementbackend.repository.RendezVousRepository;
import org.kfokam48.cliniquemanagementbackend.service.impl.FactureServiceImpl;
import org.kfokam48.cliniquemanagementbackend.service.impl.LigneFactureServiceImpl;
import org.kfokam48.cliniquemanagementbackend.service.pdf.PdfService;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * I19 : les montants d'une facture suivent ses lignes tant qu'elle n'est pas payée,
 * et sont figés dès qu'un paiement a été enregistré.
 */
class FactureMontantsTest {

    private LigneFactureRepository ligneFactureRepository;
    private FactureRepository factureRepository;
    private LigneFactureServiceImpl ligneFactureService;

    private Facture facture;
    private LigneFacture consultation;
    private LigneFacture pansement;

    @BeforeEach
    void setUp() {
        ligneFactureRepository = mock(LigneFactureRepository.class);
        factureRepository = mock(FactureRepository.class);
        ligneFactureService = new LigneFactureServiceImpl(ligneFactureRepository, new LigneFactureMapper(), factureRepository);
        when(ligneFactureRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // Facture : consultation 15 000 + pansement 7 000 = 22 000
        facture = new Facture();
        facture.setId(1L);
        facture.setStatut(StatutFacture.NON_PAYEE);
        facture.setMontantPayement(BigDecimal.ZERO);
        consultation = ligne(10L, "Consultation générale", 1, "15000");
        pansement = ligne(11L, "Pansement", 1, "7000");
        facture.recalculerMontants();

        when(ligneFactureRepository.findById(10L)).thenReturn(Optional.of(consultation));
        when(ligneFactureRepository.findById(11L)).thenReturn(Optional.of(pansement));
    }

    private LigneFacture ligne(Long id, String service, int quantite, String prix) {
        LigneFacture ligne = new LigneFacture();
        ligne.setId(id);
        ligne.setServiceName(service);
        ligne.setQuantite(quantite);
        ligne.setPrixUnitaire(new BigDecimal(prix));
        ligne.setPrixTotal(new BigDecimal(prix).multiply(BigDecimal.valueOf(quantite)));
        ligne.setFacture(facture);
        facture.getLignes().add(ligne);
        return ligne;
    }

    private static LigneFactureDTO dto(String service, int quantite, String prix) {
        LigneFactureDTO dto = new LigneFactureDTO();
        dto.setServiceName(service);
        dto.setQuantite(quantite);
        dto.setPrixUnitaire(new BigDecimal(prix));
        return dto;
    }

    @Test
    void modifyingALineOfAnUnpaidInvoiceRecalculatesTotals() {
        ligneFactureService.modifierLigne(11L, dto("Pansement", 3, "7000"));

        assertThat(pansement.getPrixTotal()).isEqualByComparingTo("21000");
        assertThat(facture.getMontantTotal()).isEqualByComparingTo("36000");
        assertThat(facture.getMontantRestant()).isEqualByComparingTo("36000");
        verify(factureRepository).save(facture);
    }

    @Test
    void deletingALineOfAnUnpaidInvoiceRecalculatesTotals() {
        ligneFactureService.supprimerLigne(11L);

        assertThat(facture.getLignes()).containsExactly(consultation);
        assertThat(facture.getMontantTotal()).isEqualByComparingTo("15000");
        assertThat(facture.getMontantRestant()).isEqualByComparingTo("15000");
        verify(factureRepository).save(facture);
    }

    @Test
    void linesOfAPartiallyPaidInvoiceCannotBeModified() {
        facture.setStatut(StatutFacture.PARTIELLEMENT_PAYE);
        facture.setMontantPayement(new BigDecimal("10000"));

        assertThatThrownBy(() -> ligneFactureService.modifierLigne(11L, dto("Pansement", 3, "7000")))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> ligneFactureService.supprimerLigne(11L))
                .isInstanceOf(IllegalStateException.class);

        assertThat(pansement.getQuantite()).isEqualTo(1);
        assertThat(facture.getLignes()).hasSize(2);
        assertThat(facture.getMontantTotal()).isEqualByComparingTo("22000");
        verify(factureRepository, never()).save(any());
    }

    @Test
    void linesOfAPaidInvoiceCannotBeModified() {
        facture.setStatut(StatutFacture.PAYEE);
        assertThatThrownBy(() -> ligneFactureService.supprimerLigne(10L))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void updatingAPartiallyPaidInvoiceNoLongerErasesPayments() {
        facture.setStatut(StatutFacture.PARTIELLEMENT_PAYE);
        facture.setMontantPayement(new BigDecimal("10000"));
        when(factureRepository.findById(1L)).thenReturn(Optional.of(facture));

        FactureServiceImpl factureService = new FactureServiceImpl(factureRepository, mock(RendezVousRepository.class),
                mock(FactureMapper.class), new LigneFactureMapper(), mock(NotificationService.class), mock(PdfService.class));
        FactureDTO factureDTO = new FactureDTO();
        factureDTO.setRendezVousId(5L);

        assertThatThrownBy(() -> factureService.update(1L, factureDTO))
                .isInstanceOf(IllegalStateException.class);
        assertThat(facture.getMontantPayement()).isEqualByComparingTo("10000");
        assertThat(facture.getStatut()).isEqualTo(StatutFacture.PARTIELLEMENT_PAYE);
    }
}
