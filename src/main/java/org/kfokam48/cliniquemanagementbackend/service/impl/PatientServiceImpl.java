package org.kfokam48.cliniquemanagementbackend.service.impl;

import org.kfokam48.cliniquemanagementbackend.repository.PatientSpecifications;
import org.springframework.data.domain.Pageable;
import org.kfokam48.cliniquemanagementbackend.dto.PageResponse;
import jakarta.validation.Valid;
import org.kfokam48.cliniquemanagementbackend.service.notification.NotificationService;
import org.kfokam48.cliniquemanagementbackend.dto.patient.PatientDTO;
import org.kfokam48.cliniquemanagementbackend.dto.patient.PatientResponseDTO;
import org.kfokam48.cliniquemanagementbackend.exception.ResourceAlreadyExistException;
import org.kfokam48.cliniquemanagementbackend.exception.RessourceNotFoundException;
import org.kfokam48.cliniquemanagementbackend.mapper.PatientMapper;
import org.kfokam48.cliniquemanagementbackend.model.Patient;
import org.kfokam48.cliniquemanagementbackend.repository.PatientRepository;
import org.kfokam48.cliniquemanagementbackend.repository.UtilisateurRepository;
import org.kfokam48.cliniquemanagementbackend.service.PatientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@Transactional
public class PatientServiceImpl implements PatientService {
    private static final Logger log = LoggerFactory.getLogger(PatientServiceImpl.class);
    private final PatientRepository patientRepository;
    private final PatientMapper patientMapper;
    private final UtilisateurRepository utilisateurRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final NotificationService notificationService ;

    public PatientServiceImpl(PatientRepository patientRepository, PatientMapper patientMapper, UtilisateurRepository utilisateurRepository, NotificationService notificationService) {
        this.patientRepository = patientRepository;
        this.patientMapper = patientMapper;
        this.utilisateurRepository = utilisateurRepository;
        this.notificationService = notificationService;
    }

    @Override
    public PatientResponseDTO save(@Valid PatientDTO patientDto) {
        log.debug("Création patient : {}", patientDto.getEmail());
        if (utilisateurRepository.existsByEmail(patientDto.getEmail())) {
            throw new ResourceAlreadyExistException("Un patient existe déjà avec cet e-mail");
        }

        Patient patient = patientMapper.patientDtoToPatient(patientDto);
        patientRepository.save(patient);
        log.info("Patient créé avec succès : {}", patient.getEmail());
        notificationService.sendNotificationToAdmins("Nouveau patient", "Un nouveau patient a été ajouté");
        return patientMapper.patientToPatientResponseDTO(patient);
    }

    @Override
    public PatientResponseDTO findById(Long id) {
        log.debug("Recherche patient id={}", id);
        return   patientMapper.patientToPatientResponseDTO(patientRepository.findById(id)
                .orElseThrow(() -> new RessourceNotFoundException("Patient introuvable")));
    }

    @Override
    public PatientResponseDTO update(Long id,@Valid PatientDTO patientDTO) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new RessourceNotFoundException("Patient introuvable"));
       if(!Objects.equals(patient.getEmail(), patientDTO.getEmail()) && utilisateurRepository.existsByEmail(patientDTO.getEmail())){
            throw new ResourceAlreadyExistException("Un patient existe déjà avec cet e-mail");
        }
        patient.setEmail(patientDTO.getEmail());
       patient.setPrenom(patientDTO.getPrenom());
       patient.setNom(patientDTO.getNom());
        patient.setSexe(patientDTO.getSexe());
        patient.setDateNaissance(patientDTO.getDateNaissance());
        patient.setAdresse(patientDTO.getAdresse());
        patient.setTelephone(patientDTO.getTelephone());
        patient.setAntecedents(patientDTO.getAntecedents());
        patient.setAllergies(patientDTO.getAllergies());
        return patientMapper.patientToPatientResponseDTO(patientRepository.save(patient));
    }

    @Override
    public void deleteById(Long id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new RessourceNotFoundException("Patient introuvable"));
        patientRepository.deleteById(id);
        log.info("Patient supprimé id={}", id);

    }

    @Override
    public List<PatientResponseDTO> findAll() {
        return patientMapper.patientListToPatientResponseDtoList(patientRepository.findAll());
    }

    @Override
    public List<PatientResponseDTO> findByMedecinId(Long medecinId) {
        List<Patient> patients = patientRepository.findPatientsByMedecinId(medecinId);
        return patients.stream()
                .map(patientMapper::patientToPatientResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PatientResponseDTO> findAll(Pageable pageable) {
        return PageResponse.of(patientRepository.findAll(pageable), patientMapper::patientListToPatientResponseDtoList);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PatientResponseDTO> findByMedecinId(Long medecinId, Pageable pageable) {
        return PageResponse.of(patientRepository.findPatientsByMedecinId(medecinId, pageable), patientMapper::patientListToPatientResponseDtoList);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PatientResponseDTO> rechercher(String texte, Long medecinId, Pageable pageable) {
        return PageResponse.of(patientRepository.findAll(PatientSpecifications.rechercher(texte, medecinId), pageable),
                patientMapper::patientListToPatientResponseDtoList);
    }
}
