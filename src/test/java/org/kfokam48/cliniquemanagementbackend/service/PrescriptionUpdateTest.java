package org.kfokam48.cliniquemanagementbackend.service;

import org.junit.jupiter.api.Test;
import org.kfokam48.cliniquemanagementbackend.dto.prescription.PrescriptionUpdateDTO;
import org.kfokam48.cliniquemanagementbackend.enums.StatutRendezVous;
import org.kfokam48.cliniquemanagementbackend.mapper.LignePrescriptionMapper;
import org.kfokam48.cliniquemanagementbackend.mapper.PrescriptionMapperImpl;
import org.kfokam48.cliniquemanagementbackend.model.Prescription;
import org.kfokam48.cliniquemanagementbackend.model.RendezVous;
import org.kfokam48.cliniquemanagementbackend.repository.PrescriptionRepository;
import org.kfokam48.cliniquemanagementbackend.repository.RendezVousRepository;
import org.kfokam48.cliniquemanagementbackend.service.impl.PrescriptionServiceImpl;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Alignement frontend : la page de modification de prescription n'envoie pas de rendezVousId.
 * Absent, la prescription garde son rendez-vous (avant : findById(null) faisait échouer la requête).
 */
class PrescriptionUpdateTest {

    @Test
    void updateWithoutAppointmentKeepsTheCurrentOne() {
        PrescriptionRepository prescriptionRepository = mock(PrescriptionRepository.class);
        RendezVousRepository rendezVousRepository = mock(RendezVousRepository.class);
        LignePrescriptionMapper lignePrescriptionMapper = mock(LignePrescriptionMapper.class);
        when(lignePrescriptionMapper.lignePrescriptionUpdateDTOListToLignePrescriptionList(any())).thenReturn(List.of());

        RendezVous rendezVous = new RendezVous();
        rendezVous.setId(10L);
        rendezVous.setStatutRendezVous(StatutRendezVous.TERMINE);
        Prescription prescription = new Prescription();
        prescription.setId(4L);
        prescription.setRendezVous(rendezVous);
        when(prescriptionRepository.findById(4L)).thenReturn(Optional.of(prescription));

        PrescriptionServiceImpl service = new PrescriptionServiceImpl(prescriptionRepository,
                new PrescriptionMapperImpl(lignePrescriptionMapper),
                rendezVousRepository, lignePrescriptionMapper);
        PrescriptionUpdateDTO dto = new PrescriptionUpdateDTO();
        dto.setDescription("Fièvre persistante");
        dto.setLignes(List.of());

        service.update(4L, dto);

        assertThat(prescription.getRendezVous()).isSameAs(rendezVous);
        assertThat(prescription.getDescription()).isEqualTo("Fièvre persistante");
        verify(rendezVousRepository, never()).findById(any());
        verify(prescriptionRepository).save(prescription);
    }
}
