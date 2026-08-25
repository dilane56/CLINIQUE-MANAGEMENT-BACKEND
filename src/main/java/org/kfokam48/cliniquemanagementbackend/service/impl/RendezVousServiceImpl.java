package org.kfokam48.cliniquemanagementbackend.service.impl;


import jakarta.validation.Valid;
import org.kfokam48.cliniquemanagementbackend.controlleur.notification.NotificationController;
import org.kfokam48.cliniquemanagementbackend.dto.rendezvous.RendezVousDTO;
import org.kfokam48.cliniquemanagementbackend.dto.rendezvous.RendezVousResponseDTO;
import org.kfokam48.cliniquemanagementbackend.dto.rendezvous.RendezVousUpdateDto;
import org.kfokam48.cliniquemanagementbackend.enums.StatutRendezVous;
import org.kfokam48.cliniquemanagementbackend.exception.RessourceNotFoundException;
import org.kfokam48.cliniquemanagementbackend.mapper.RendezVousMapper;
import org.kfokam48.cliniquemanagementbackend.model.RendezVous;
import org.kfokam48.cliniquemanagementbackend.repository.MedecinRepository;
import org.kfokam48.cliniquemanagementbackend.repository.PatientRepository;
import org.kfokam48.cliniquemanagementbackend.repository.RendezVousRepository;
import org.kfokam48.cliniquemanagementbackend.repository.TypeRendezVousRepository;
import org.kfokam48.cliniquemanagementbackend.service.RendezVousService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class RendezVousServiceImpl implements RendezVousService {
    private final RendezVousRepository rendezVousRepository;
    private final RendezVousMapper rendezVousMapper;
    private final PatientRepository patientRepository;
    private final MedecinRepository medecinRepository;
    private final TypeRendezVousRepository typeRendezVousRepository;
    private final NotificationController notificationController;

    public RendezVousServiceImpl(RendezVousRepository rendezVousRepository, RendezVousMapper rendezVousMapper, PatientRepository patientRepository, MedecinRepository medecinRepository, TypeRendezVousRepository typeRendezVousRepository, NotificationController notificationController) {
        this.rendezVousRepository = rendezVousRepository;
        this.rendezVousMapper = rendezVousMapper;
        this.patientRepository = patientRepository;
        this.medecinRepository = medecinRepository;
        this.typeRendezVousRepository = typeRendezVousRepository;
        this.notificationController = notificationController;
    }

    @Override
    public RendezVousResponseDTO save(@Valid RendezVousDTO rendezVousDTO) {
        RendezVous rendezVous = new RendezVous();
        rendezVous.setDateRendezVous(rendezVousDTO.getDateRendezVous());
        rendezVous.setMotif(rendezVousDTO.getMotif());
        rendezVous.setSecretaireId(rendezVousDTO.getSecretaireId());
        rendezVous.setPatient(patientRepository.findById(rendezVousDTO.getPatientId())
                .orElseThrow(() -> new RessourceNotFoundException("Patient not found")));
        rendezVous.setMedecin(medecinRepository.findById(rendezVousDTO.getMedecinId())
                .orElseThrow(() -> new RessourceNotFoundException("Medecin not found")));
        rendezVous.setTypeRendezVous(typeRendezVousRepository.findById(rendezVousDTO.getTypeRendezVousId())
                .orElseThrow(() -> new RessourceNotFoundException("Type de rendez-vous not found")));

        if (rendezVous.getDateRendezVous().isBefore(LocalDateTime.now().plusMinutes(30))) {
            throw new IllegalArgumentException("La date du rendez-vous doit être au moins 30 minutes dans le futur.");
        }

        LocalDateTime startOfDay = rendezVous.getDateRendezVous().toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1).minusSeconds(1);
        if (rendezVousRepository.existsByPatientIdAndDateRendezVousBetween(
                rendezVous.getPatient().getId(), startOfDay, endOfDay)) {
            throw new IllegalArgumentException("Le patient possède déjà un rendez-vous ce jour-là.");
        }

        rendezVous.setDateTimeFinRendezVousPossible(
                rendezVous.getDateRendezVous().plusMinutes(rendezVous.getTypeRendezVous().getDuree()));

        if (rendezVousRepository.existsByMedecinAndDateRendezVousBetween(
                rendezVous.getMedecin(), rendezVous.getDateRendezVous(), rendezVous.getDateTimeFinRendezVousPossible())) {
            throw new IllegalArgumentException("Ce créneau est déjà pris pour ce médecin.");
        }

        rendezVous.setStatutRendezVous(StatutRendezVous.EN_ATTENTE);
        RendezVousResponseDTO response = rendezVousMapper.rendezVousToRendezVousResponseDto(
                rendezVousRepository.save(rendezVous));
        notificationController.sendNotification(rendezVousDTO.getMedecinId(), "Rendez-vous", "Vous avez un nouveau rendez-vous", true);
        notificationController.sendNotification(rendezVousDTO.getPatientId(), "Rendez-vous", "Vous avez un nouveau rendez-vous", true);
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public RendezVousResponseDTO findById(Long Id) {
        return rendezVousMapper.rendezVousToRendezVousResponseDto(
                rendezVousRepository.findById(Id)
                        .orElseThrow(() -> new RessourceNotFoundException("Rendez-vous not found"))
        );
    }

    @Override
    public RendezVousResponseDTO update(Long id, @Valid RendezVousUpdateDto rendezVousDTO) {
        RendezVous rendezVous = rendezVousRepository.findById(id)
                .orElseThrow(() -> new RessourceNotFoundException("Rendez-vous not found"));

        rendezVous.setDateRendezVous(rendezVousDTO.getDateRendezVous());
        rendezVous.setMotif(rendezVousDTO.getMotif());
        rendezVous.setPatient(patientRepository.findById(rendezVousDTO.getPatientId())
                .orElseThrow(() -> new RessourceNotFoundException("Patient not found")));
        rendezVous.setMedecin(medecinRepository.findById(rendezVousDTO.getMedecinId())
                .orElseThrow(() -> new RessourceNotFoundException("Medecin not found")));
        rendezVous.setTypeRendezVous(typeRendezVousRepository.findById(rendezVousDTO.getTypeRendezVousId())
                .orElseThrow(() -> new RessourceNotFoundException("Type de rendez-vous not found")));

        if (rendezVous.getDateRendezVous().isBefore(LocalDateTime.now().plusMinutes(30))) {
            throw new IllegalArgumentException("La date du rendez-vous doit être au moins 30 minutes dans le futur.");
        }

        LocalDateTime startOfDay = rendezVous.getDateRendezVous().toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1).minusSeconds(1);
        // Vérification doublon patient en excluant le RDV courant (filtre correct par patientId)
        if (rendezVousRepository.existsByPatientIdAndDateRendezVousBetweenAndIdNot(
                rendezVous.getPatient().getId(), startOfDay, endOfDay, rendezVous.getId())) {
            throw new IllegalArgumentException("Le patient possède déjà un rendez-vous ce jour-là.");
        }

        rendezVous.setDateTimeFinRendezVousPossible(
                rendezVous.getDateRendezVous().plusMinutes(rendezVous.getTypeRendezVous().getDuree()));

        if (rendezVousRepository.existsByMedecinAndDateRendezVousBetweenAndIdNot(
                rendezVous.getMedecin(), rendezVous.getDateRendezVous(),
                rendezVous.getDateTimeFinRendezVousPossible(), rendezVous.getId())) {
            throw new IllegalArgumentException("Ce créneau est déjà pris pour ce médecin.");
        }

        rendezVous.setStatutRendezVous(StatutRendezVous.EN_ATTENTE);
        RendezVousResponseDTO response = rendezVousMapper.rendezVousToRendezVousResponseDto(
                rendezVousRepository.save(rendezVous));
        notificationController.sendNotification(rendezVousDTO.getMedecinId(), "Rendez-vous", "Le rendez-vous avec le patient " + rendezVous.getPatient().getNom() + " a été mis à jour", true);
        notificationController.sendNotification(rendezVousDTO.getPatientId(), "Rendez-vous", "Votre rendez-vous a été mis à jour", true);
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RendezVousResponseDTO> findAll() {
        return rendezVousMapper.rendezVousListToRendezVousResponseDtoList(rendezVousRepository.findAll());
    }

    @Override
    public ResponseEntity<String> deleteById(Long id) {
        RendezVous rendezVous = rendezVousRepository.findById(id)
                .orElseThrow(() -> new RessourceNotFoundException("Rendez-vous not found"));
        rendezVousRepository.deleteById(id);
        return ResponseEntity.ok("Rendez-vous deleted successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public List<RendezVousResponseDTO> findByMedecinId(Long medecinId) {
        List<RendezVous> rendezVousList = rendezVousRepository.findByMedecinId(medecinId);
        return rendezVousList.stream()
                .map(rendezVousMapper::rendezVousToRendezVousResponseDto)
                .toList();
    }

    @Override
    public RendezVousResponseDTO updateStatut(Long id, StatutRendezVous statut) {
        RendezVous rendezVous = rendezVousRepository.findById(id)
                .orElseThrow(() -> new RessourceNotFoundException("Rendez-vous not found"));

        if (statut == StatutRendezVous.EN_COURS && rendezVous.getDateRendezVous().isAfter(LocalDateTime.now())) {
            throw new IllegalStateException("Impossible de passer le statut à EN_COURS avant la date et l'heure du rendez-vous.");
        }

        rendezVous.setStatutRendezVous(statut);
        RendezVous updated = rendezVousRepository.save(rendezVous);

        // Guard NPE : secretaireId est nullable
        if (rendezVous.getSecretaireId() != null) {
            notificationController.sendNotification(rendezVous.getSecretaireId(), "Rendez-vous", "Le statut du rendez-vous avec le patient " + rendezVous.getPatient().getNom() + " a été mis à jour", false);
        }
        notificationController.sendNotification(rendezVous.getPatient().getId(), "Rendez-vous", "Le statut de votre rendez-vous avec le Dr " + rendezVous.getMedecin().getNom() + " a été mis à jour", true);
        notificationController.sendNotification(rendezVous.getMedecin().getId(), "Rendez-vous", "Le rendez-vous avec le patient " + rendezVous.getPatient().getNom() + " a changé de statut", true);
        return rendezVousMapper.rendezVousToRendezVousResponseDto(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RendezVousResponseDTO> findRendezVousDuJourByMedecin(Long medecinId) {
        LocalDateTime debutJour = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime finJour = debutJour.plusDays(1).minusSeconds(1);
        List<RendezVous> rendezVousList = rendezVousRepository.findByMedecinIdAndDateRendezVousBetween(medecinId, debutJour, finJour);
        return rendezVousMapper.rendezVousListToRendezVousResponseDtoList(rendezVousList);
    }
}
