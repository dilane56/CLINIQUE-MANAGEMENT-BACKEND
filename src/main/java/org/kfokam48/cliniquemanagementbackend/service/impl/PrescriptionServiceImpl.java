package org.kfokam48.cliniquemanagementbackend.service.impl;


import jakarta.validation.Valid;
import org.kfokam48.cliniquemanagementbackend.dto.prescription.PrescriptionDTO;
import org.kfokam48.cliniquemanagementbackend.dto.prescription.PrescriptionResponseDTO;
import org.kfokam48.cliniquemanagementbackend.dto.prescription.PrescriptionUpdateDTO;
import org.kfokam48.cliniquemanagementbackend.enums.StatutRendezVous;
import org.kfokam48.cliniquemanagementbackend.exception.RessourceNotFoundException;
import org.kfokam48.cliniquemanagementbackend.mapper.LignePrescriptionMapper;
import org.kfokam48.cliniquemanagementbackend.mapper.PrescriptionMapper;
import org.kfokam48.cliniquemanagementbackend.model.LignePrescription;
import org.kfokam48.cliniquemanagementbackend.model.Prescription;
import org.kfokam48.cliniquemanagementbackend.model.RendezVous;
import org.kfokam48.cliniquemanagementbackend.repository.*;
import org.kfokam48.cliniquemanagementbackend.service.PrescriptionService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDate;
import java.util.List;
@Service
@Transactional
public class PrescriptionServiceImpl implements PrescriptionService {
    private final PrescriptionRepository prescriptionRepository;
    private final PrescriptionMapper prescriptionMapper;
    private final RendezVousRepository rendezVousRepository;
    private final LignePrescriptionMapper lignePrescriptionMapper;


    public PrescriptionServiceImpl(PrescriptionRepository prescriptionRepository, PrescriptionMapper prescriptionMapper, RendezVousRepository rendezVousRepository, LignePrescriptionMapper lignePrescriptionMapper) {
        this.prescriptionRepository = prescriptionRepository;
        this.prescriptionMapper = prescriptionMapper;
        this.rendezVousRepository = rendezVousRepository;

        this.lignePrescriptionMapper = lignePrescriptionMapper;
    }

    @Override
    public PrescriptionResponseDTO save(@Valid PrescriptionDTO prescriptionDTO) {
        RendezVous rendezVous = rendezVousRepository.findById(prescriptionDTO.getRendezVousId())
                .orElseThrow(() -> new RessourceNotFoundException("RendezVous not found"));
        if (!(rendezVous.getStatutRendezVous() == StatutRendezVous.TERMINE || rendezVous.getStatutRendezVous() == StatutRendezVous.EN_COURS)) {
            throw new IllegalStateException("Impossible d'ajouter une prescription pour un rendez-vous qui n'est pas terminé ou en cours.");
        }
        Prescription prescription = prescriptionMapper.prescriptionDtoToPrescription(prescriptionDTO);
        prescription.setDate(LocalDate.now());
        prescriptionRepository.save(prescription);
        return prescriptionMapper.prescriptionToPrescriptionResponseDto(prescription);
    }

    @Override
    public PrescriptionResponseDTO findById(Long id) {
        return prescriptionMapper.prescriptionToPrescriptionResponseDto(prescriptionRepository.findById(id)
                .orElseThrow(() -> new RessourceNotFoundException("Prescription not found")));
    }

    @Override
    public PrescriptionResponseDTO update(Long id, @Valid PrescriptionUpdateDTO prescriptionUpdateDTO) {
        Prescription prescription = prescriptionRepository.findById(id)
                .orElseThrow(() -> new RessourceNotFoundException("Prescription not found"));
        RendezVous rendezVous = rendezVousRepository.findById(prescriptionUpdateDTO.getRendezVousId())
                .orElseThrow(() -> new RessourceNotFoundException("RendezVous not found"));
        if (!(rendezVous.getStatutRendezVous() == StatutRendezVous.TERMINE || rendezVous.getStatutRendezVous() == StatutRendezVous.EN_COURS)) {
            throw new IllegalStateException("Impossible de modifier une prescription pour un rendez-vous qui n'est pas terminé ou en cours.");
        }
        prescription.setRendezVous(rendezVous);
       prescriptionMapper.updatePrescriptionFromUpdateDTO(prescription, prescriptionUpdateDTO, lignePrescriptionMapper);
        prescription.setDate(LocalDate.now());

        prescriptionRepository.save(prescription);
        return prescriptionMapper.prescriptionToPrescriptionResponseDto(prescription);
    }

    @Override
    public List<PrescriptionResponseDTO> findAll() {
        return prescriptionMapper.prescriptionListToPrescriptionResponseDtoList(prescriptionRepository.findAll());
    }

    @Override
    public ResponseEntity<String> deleteById(Long id) {
        Prescription prescription = prescriptionRepository.findById(id)
                .orElseThrow(() -> new RessourceNotFoundException("Prescription not found"));
        prescriptionRepository.deleteById(id);
        return ResponseEntity.ok("Prescription deleted successfully");

    }

    @Override
    public List<PrescriptionResponseDTO> findByMedecinId(Long medecinId) {
        List<Prescription> prescriptions = prescriptionRepository.findByMedecinId(medecinId);
        return prescriptionMapper.prescriptionListToPrescriptionResponseDtoList(prescriptions);
    }
}
