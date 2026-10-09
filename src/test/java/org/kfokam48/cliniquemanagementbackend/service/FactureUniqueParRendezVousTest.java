package org.kfokam48.cliniquemanagementbackend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kfokam48.cliniquemanagementbackend.dto.facture.FactureDTO;
import org.kfokam48.cliniquemanagementbackend.enums.StatutFacture;
import org.kfokam48.cliniquemanagementbackend.enums.StatutRendezVous;
import org.kfokam48.cliniquemanagementbackend.exception.ResourceAlreadyExistException;
import org.kfokam48.cliniquemanagementbackend.mapper.FactureMapper;
import org.kfokam48.cliniquemanagementbackend.mapper.LigneFactureMapperImpl;
import org.kfokam48.cliniquemanagementbackend.model.Facture;
import org.kfokam48.cliniquemanagementbackend.model.RendezVous;
import org.kfokam48.cliniquemanagementbackend.model.TypeRendezVous;
import org.kfokam48.cliniquemanagementbackend.repository.FactureRepository;
import org.kfokam48.cliniquemanagementbackend.repository.PaiementRepository;
import org.kfokam48.cliniquemanagementbackend.repository.RendezVousRepository;
import org.kfokam48.cliniquemanagementbackend.service.impl.FactureServiceImpl;
import org.kfokam48.cliniquemanagementbackend.service.notification.NotificationService;
import org.kfokam48.cliniquemanagementbackend.service.pdf.PdfService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Un rendez-vous n'est facturé qu'une seule fois. */
class FactureUniqueParRendezVousTest {

    private static final long RDV_ID = 5L;

    private FactureRepository factureRepository;
    private FactureServiceImpl factureService;

    @BeforeEach
    void setUp() {
        TypeRendezVous type = new TypeRendezVous();
        type.setLibelle("Consultation");
        type.setTarif(new BigDecimal("15000"));
        RendezVous rendezVous = new RendezVous();
        rendezVous.setId(RDV_ID);
        rendezVous.setStatutRendezVous(StatutRendezVous.TERMINE);
        rendezVous.setTypeRendezVous(type);

        RendezVousRepository rendezVousRepository = mock(RendezVousRepository.class);
        when(rendezVousRepository.findById(RDV_ID)).thenReturn(Optional.of(rendezVous));
        factureRepository = mock(FactureRepository.class);
        when(factureRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        factureService = new FactureServiceImpl(factureRepository, rendezVousRepository, mock(FactureMapper.class),
                new LigneFactureMapperImpl(), mock(NotificationService.class), mock(PdfService.class),
                mock(PaiementRepository.class));
    }

    @Test
    void creation_rendezVousDejaFacture_refusee() {
        when(factureRepository.existsByRendezVous_Id(RDV_ID)).thenReturn(true);

        assertThatThrownBy(() -> factureService.save(dto()))
                .isInstanceOf(ResourceAlreadyExistException.class);
        verify(factureRepository, never()).save(any());
    }

    @Test
    void creation_premiereFacture_enregistree() {
        when(factureRepository.existsByRendezVous_Id(RDV_ID)).thenReturn(false);

        factureService.save(dto());

        verify(factureRepository).save(any(Facture.class));
    }

    @Test
    void modification_versRendezVousDejaFactureParUneAutre_refusee() {
        Facture facture = new Facture();
        facture.setId(1L);
        facture.setStatut(StatutFacture.NON_PAYEE);
        when(factureRepository.findById(1L)).thenReturn(Optional.of(facture));
        when(factureRepository.existsByRendezVous_IdAndIdNot(RDV_ID, 1L)).thenReturn(true);

        assertThatThrownBy(() -> factureService.update(1L, dto()))
                .isInstanceOf(ResourceAlreadyExistException.class);
        verify(factureRepository, never()).save(any());
    }

    private static FactureDTO dto() {
        FactureDTO dto = new FactureDTO();
        dto.setRendezVousId(RDV_ID);
        dto.setLignesFacture(List.of());
        return dto;
    }
}
