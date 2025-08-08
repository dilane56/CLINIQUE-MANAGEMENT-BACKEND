package org.kfokam48.cliniquemanagementbackend.service.impl;

import org.kfokam48.cliniquemanagementbackend.dto.ligneprescription.LignePrescriptionDTO;
import org.kfokam48.cliniquemanagementbackend.dto.ligneprescription.LignePrescriptionResponseDTO;
import org.kfokam48.cliniquemanagementbackend.dto.ligneprescription.LignePrescriptionUpdateDTO;
import org.kfokam48.cliniquemanagementbackend.exception.RessourceNotFoundException;
import org.kfokam48.cliniquemanagementbackend.mapper.LignePrescriptionMapper;
import org.kfokam48.cliniquemanagementbackend.model.LignePrescription;
import org.kfokam48.cliniquemanagementbackend.repository.LignePrescriptionRepository;
import org.kfokam48.cliniquemanagementbackend.repository.PrescriptionRepository;
import org.kfokam48.cliniquemanagementbackend.service.LignePrescriptionService;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class LignePrescriptionServiceImpl implements LignePrescriptionService {

    private final LignePrescriptionRepository repository;
    private final LignePrescriptionMapper lignePrescriptionMapper;
    private final PrescriptionRepository prescriptionRepository;

    public LignePrescriptionServiceImpl(LignePrescriptionRepository repository, LignePrescriptionMapper lignePrescriptionMapper, PrescriptionRepository prescriptionRepository) {
        this.repository = repository;
        this.lignePrescriptionMapper = lignePrescriptionMapper;
        this.prescriptionRepository = prescriptionRepository;
    }

    @Override
    public LignePrescriptionResponseDTO ajouterLigne(LignePrescriptionDTO dto) {
        LignePrescription lignePrescription = lignePrescriptionMapper.lignePrescriptionDTOToLignePrescription(dto);
        repository.save(lignePrescription);
        return  lignePrescriptionMapper.lignePrescriptionToLignePrescriptionResponseDTO(lignePrescription);
    }

    @Override
    public LignePrescriptionResponseDTO modifierLigne(Long id, LignePrescriptionUpdateDTO dto) {
        LignePrescription lignePrescription = repository.findById(id).orElseThrow(()-> new RessourceNotFoundException("LignePrescription not found"));
        lignePrescription.setMedicament(dto.getMedicament());
        lignePrescription.setDosage(dto.getDosage());
        lignePrescription.setFrequence(dto.getFrequence());
        lignePrescription.setDuree(dto.getDuree());
        lignePrescription.setPrescription(prescriptionRepository.findById(dto.getPrescriptionId())
                .orElseThrow(() -> new RessourceNotFoundException("Prescription not found")));
        repository.save(lignePrescription);
        return lignePrescriptionMapper.lignePrescriptionToLignePrescriptionResponseDTO(lignePrescription);

    }

    @Override
    public void supprimerLigne(Long id) {
        repository.deleteById(id);
    }

    @Override
    public LignePrescriptionResponseDTO getLigne(Long id) {
       LignePrescription lignePrescription = repository.findById(id).orElseThrow(()-> new RessourceNotFoundException("LignePrescription not found"));
        return lignePrescriptionMapper.lignePrescriptionToLignePrescriptionResponseDTO(lignePrescription);
    }

    @Override
    public List<LignePrescriptionResponseDTO> listerLignes() {
       return  lignePrescriptionMapper.lignePrescriptionsToLignePrescriptionResponseDTOs(repository.findAll());
    }
}

