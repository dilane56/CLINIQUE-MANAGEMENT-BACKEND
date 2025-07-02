package org.kfokam48.cliniquemanagementbackend.service.impl;

import jakarta.validation.Valid;
import org.kfokam48.cliniquemanagementbackend.dto.PatientDTO;
import org.kfokam48.cliniquemanagementbackend.dto.PatientResponseDTO;
import org.kfokam48.cliniquemanagementbackend.enums.Roles;
import org.kfokam48.cliniquemanagementbackend.exception.ResourceAlreadyExistException;
import org.kfokam48.cliniquemanagementbackend.exception.RessourceNotFoundException;
import org.kfokam48.cliniquemanagementbackend.mapper.PatientMapper;
import org.kfokam48.cliniquemanagementbackend.model.Patient;
import org.kfokam48.cliniquemanagementbackend.repository.PatientRepository;
import org.kfokam48.cliniquemanagementbackend.repository.UtilisateurRepository;
import org.kfokam48.cliniquemanagementbackend.service.PatientService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@Transactional
public class PatientServiceImpl implements PatientService {
    private final PatientRepository patientRepository;
    private final PatientMapper patientMapper;
    private final UtilisateurRepository utilisateurRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public PatientServiceImpl(PatientRepository patientRepository, PatientMapper patientMapper, UtilisateurRepository utilisateurRepository) {
        this.patientRepository = patientRepository;
        this.patientMapper = patientMapper;
        this.utilisateurRepository = utilisateurRepository;
    }

    @Override
    public Patient save(@Valid PatientDTO patientDto) {
        if (utilisateurRepository.existsByEmail(patientDto.getEmail())) {
            throw new ResourceAlreadyExistException("User already exists with this email");
        }

        Patient patient = patientMapper.patientDtoToPatient(patientDto);
        patient.setPassword(passwordEncoder.encode(patientDto.getPassword()));
        patient.setRole(Roles.valueOf("PATIENT"));
        patientRepository.save(patient);
        return patient;
    }

    @Override
    public PatientResponseDTO findById(Long id) {
        return   patientMapper.patientToPatientResponseDTO(patientRepository.findById(id)
                .orElseThrow(() -> new RessourceNotFoundException("Patient not found")));
    }

    @Override
    public Patient update(Long id,@Valid PatientDTO patientDTO) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new RessourceNotFoundException("Patient not found"));
       if(!Objects.equals(patient.getEmail(), patientDTO.getEmail()) && utilisateurRepository.existsByEmail(patientDTO.getEmail())){
            throw new ResourceAlreadyExistException("User already exists with this email");
        }

        patient.setEmail(patientDTO.getEmail());
        patient.setPassword(patientDTO.getPassword());
        patient.setNumeroDossierMedical(patientDTO.getNumeroDossierMedical());
        patient.setSexe(patientDTO.getSexe());
        return patientRepository.save(patient);
    }

    @Override
    public ResponseEntity<String> deleteById(Long id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new RessourceNotFoundException("Patient not found"));
        patientRepository.deleteById(id);
        return ResponseEntity.ok("Patient deleted successfully");

    }

    @Override
    public List<PatientResponseDTO> findAll() {
        return patientMapper.patientListToPatientResponseDtoList(patientRepository.findAll());
    }
}
