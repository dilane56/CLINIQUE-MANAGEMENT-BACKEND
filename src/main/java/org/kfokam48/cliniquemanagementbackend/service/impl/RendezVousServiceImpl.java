package org.kfokam48.cliniquemanagementbackend.service.impl;


import jakarta.validation.Valid;
import org.kfokam48.cliniquemanagementbackend.dto.rendezvous.RendezVousDTO;
import org.kfokam48.cliniquemanagementbackend.dto.rendezvous.RendezVousResponseDTO;
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

    public RendezVousServiceImpl(RendezVousRepository rendezVousRepository, RendezVousMapper rendezVousMapper, PatientRepository patientRepository, MedecinRepository medecinRepository, TypeRendezVousRepository typeRendezVousRepository) {
        this.rendezVousRepository = rendezVousRepository;
        this.rendezVousMapper = rendezVousMapper;
        this.patientRepository = patientRepository;
        this.medecinRepository = medecinRepository;
        this.typeRendezVousRepository = typeRendezVousRepository;
    }

    @Override
    public RendezVousResponseDTO save(@Valid RendezVousDTO rendezVousDTO) {
       RendezVous rendezVous = rendezVousMapper.rendezVousDtoToRendezvous(rendezVousDTO);
        LocalDateTime currentDateTime = LocalDateTime.now();
        if (rendezVous.getDateRendezVous().isBefore(currentDateTime.plusMinutes(30))) {
            throw new IllegalArgumentException("La date du rendez-vous ne peut pas être dans le passé et doit être au moins 30 minutes dans le futur.");
        }

        // Vérification : le patient a-t-il déjà un rendez-vous ce jour-là ?
        LocalDateTime startOfDay = rendezVous.getDateRendezVous().toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1).minusSeconds(1);
        if (rendezVousRepository.existsByPatientIdAndDateRendezVousBetween(
                rendezVous.getPatient().getId(), startOfDay, endOfDay)) {
            throw new IllegalArgumentException("Le patient possède déjà un rendez-vous ce jour-là.");
        }

        rendezVous.setDateTimeFinRendezVousPossible(
                rendezVous.getDateRendezVous().plusMinutes(rendezVous.getTypeRendezVous().getDuree())
        );
        // 5. Vérifier les conflits
        boolean conflit = rendezVousRepository.existsByMedecinAndDateRendezVousBetween(
                        rendezVous.getMedecin(),
                        rendezVous.getDateRendezVous(),
                        rendezVous.getDateTimeFinRendezVousPossible()
                );
        if (conflit) {
            throw new IllegalArgumentException("Ce créneau est déjà pris pour ce médecin.");
        }
        rendezVous.setStatutRendezVous(StatutRendezVous.EN_ATTENTE);
        return rendezVousMapper.rendezVousToRendezVousResponseDto(
                rendezVousRepository.save(rendezVous)
        );
    }

    @Override
    public RendezVousResponseDTO findById(Long Id) {
        return rendezVousMapper.rendezVousToRendezVousResponseDto(
                rendezVousRepository.findById(Id)
                        .orElseThrow(() -> new RessourceNotFoundException("Rendez-vous not found"))
        );
    }

    @Override
    public RendezVousResponseDTO update(Long id,@Valid RendezVousDTO rendezVousDTO) {
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
        LocalDateTime currentDateTime = LocalDateTime.now();
        if (rendezVous.getDateRendezVous().isBefore(currentDateTime.plusMinutes(30))) {
            throw new IllegalArgumentException("La date du rendez-vous ne peut pas être dans le passé et doit être au moins 30 minutes dans le futur.");
        }
        // Vérification : le patient a-t-il déjà un rendez-vous ce jour-là (hors ce rendez-vous-ci) ?
        LocalDateTime startOfDay = rendezVous.getDateRendezVous().toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1).minusSeconds(1);
        boolean dejaRdv = rendezVousRepository.existsByPatientIdAndDateRendezVousBetween(
                rendezVous.getPatient().getId(), startOfDay, endOfDay);
        if (dejaRdv) {
            // On doit vérifier que le rendez-vous trouvé n'est pas celui qu'on est en train de modifier
            List<RendezVous> rdvs = rendezVousRepository.findByMedecinIdAndDateRendezVousBetween(
                rendezVous.getMedecin().getId(), startOfDay, endOfDay);
            boolean autreRdv = rdvs.stream().anyMatch(r -> !r.getId().equals(rendezVous.getId()) && r.getPatient().getId().equals(rendezVous.getPatient().getId()));
            if (autreRdv) {
                throw new IllegalArgumentException("Le patient possède déjà un rendez-vous ce jour-là.");
            }
        }
        rendezVous.setDateTimeFinRendezVousPossible(
                rendezVous.getDateRendezVous().plusMinutes(rendezVous.getTypeRendezVous().getDuree())
        );
        // Vérifier les conflits
        boolean conflit = rendezVousRepository.existsByMedecinAndDateRendezVousBetween(
                rendezVous.getMedecin(),
                rendezVous.getDateRendezVous(),
                rendezVous.getDateTimeFinRendezVousPossible()
        );
        if (conflit) {
            throw new IllegalArgumentException("Ce créneau est déjà pris pour ce médecin.");
        }
        return rendezVousMapper.rendezVousToRendezVousResponseDto(
                rendezVousRepository.save(rendezVous)
        );
    }

    @Override
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
        // Vérification pour le statut EN_COURS
        if (statut == StatutRendezVous.EN_COURS) {
            LocalDateTime maintenant = LocalDateTime.now();
            if (rendezVous.getDateRendezVous().isAfter(maintenant)) {
                throw new IllegalStateException("Impossible de passer le statut à EN_COURS avant la date et l'heure du rendez-vous.");
            }
        }
        rendezVous.setStatutRendezVous(statut);
        RendezVous updated = rendezVousRepository.save(rendezVous);
        return rendezVousMapper.rendezVousToRendezVousResponseDto(updated);
    }

    @Override
    public List<RendezVousResponseDTO> findRendezVousDuJourByMedecin(Long medecinId) {
        LocalDateTime debutJour = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime finJour = debutJour.plusDays(1).minusSeconds(1);
        List<RendezVous> rendezVousList = rendezVousRepository.findByMedecinIdAndDateRendezVousBetween(medecinId, debutJour, finJour);
        return rendezVousMapper.rendezVousListToRendezVousResponseDtoList(rendezVousList);
    }
}
