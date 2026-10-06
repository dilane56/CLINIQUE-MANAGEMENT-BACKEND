package org.kfokam48.cliniquemanagementbackend.security;

import org.junit.jupiter.api.Test;
import org.kfokam48.cliniquemanagementbackend.config.JwtRequestFillter;
import org.kfokam48.cliniquemanagementbackend.config.SecurityConfig;
import org.kfokam48.cliniquemanagementbackend.controlleur.FactureController;
import org.kfokam48.cliniquemanagementbackend.controlleur.MedecinController;
import org.kfokam48.cliniquemanagementbackend.controlleur.PatientController;
import org.kfokam48.cliniquemanagementbackend.controlleur.RendezVousController;
import org.kfokam48.cliniquemanagementbackend.controlleur.UtilisateurController;
import org.kfokam48.cliniquemanagementbackend.controlleur.PrescriptionController;
import org.kfokam48.cliniquemanagementbackend.controlleur.notification.NotificationRestController;
import org.kfokam48.cliniquemanagementbackend.repository.PrescriptionRepository;
import org.kfokam48.cliniquemanagementbackend.service.impl.PrescriptionServiceImpl;
import org.kfokam48.cliniquemanagementbackend.service.mail.EmailService;
import org.kfokam48.cliniquemanagementbackend.service.notification.NotificationService;
import org.kfokam48.cliniquemanagementbackend.service.pdf.PdfService;
import org.kfokam48.cliniquemanagementbackend.mapper.MedecinMapper;
import org.kfokam48.cliniquemanagementbackend.mapper.UtilisateurMapper;
import org.kfokam48.cliniquemanagementbackend.service.FactureService;
import org.kfokam48.cliniquemanagementbackend.service.auth.AuthorizationService;
import org.kfokam48.cliniquemanagementbackend.service.auth.CustomUserDetailsService;
import org.kfokam48.cliniquemanagementbackend.service.auth.JwtService;
import org.kfokam48.cliniquemanagementbackend.service.impl.MedecinServiceImpl;
import org.kfokam48.cliniquemanagementbackend.service.impl.PatientServiceImpl;
import org.kfokam48.cliniquemanagementbackend.service.impl.RendezVousServiceImpl;
import org.kfokam48.cliniquemanagementbackend.service.impl.UtilisateurServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Vérifie la matrice des droits (points C3 à C8 de AUDIT_BACKEND.md).
 */
@WebMvcTest(controllers = {
        PatientController.class,
        FactureController.class,
        UtilisateurController.class,
        MedecinController.class,
        RendezVousController.class,
        PrescriptionController.class,
        NotificationRestController.class
}, properties = {
        // Clé de test uniquement (Base64, 256 bits)
        "jwt.secret=dGVzdC1zZWNyZXQta2V5LWZvci11bml0LXRlc3RzLW9ubHktMzJieXRlcw==",
        "cors.allowed.origins=http://localhost:3000",
        "cors.allowed.methods=GET,POST,PUT,DELETE,OPTIONS,PATCH",
        "cors.allowed.headers=*",
        "cors.allow.credentials=true"
})
@Import({SecurityConfig.class, JwtRequestFillter.class, JwtService.class})
class AccessControlTest {

    private static final long MEDECIN_CONNECTE = 1L;
    private static final long AUTRE_MEDECIN = 2L;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean private CustomUserDetailsService customUserDetailsService;
    @MockitoBean(name = "authz") private AuthorizationService authz;
    @MockitoBean private PatientServiceImpl patientService;
    @MockitoBean private FactureService factureService;
    @MockitoBean private UtilisateurServiceImpl utilisateurService;
    @MockitoBean private UtilisateurMapper utilisateurMapper;
    @MockitoBean private MedecinServiceImpl medecinService;
    @MockitoBean private MedecinMapper medecinMapper;
    @MockitoBean private RendezVousServiceImpl rendezVousService;
    @MockitoBean private PrescriptionServiceImpl prescriptionService;
    @MockitoBean private PdfService pdfService;
    @MockitoBean private PrescriptionRepository prescriptionRepository;
    @MockitoBean private NotificationService notificationService;
    @MockitoBean private EmailService emailService;

    // --- C3 : plus aucune route métier publique ---

    @Test
    void anonymousCannotListUsers() throws Exception {
        mockMvc.perform(get("/api/utilisateurs")).andExpect(status().isForbidden());
    }

    @Test
    void anonymousCannotListMedecins() throws Exception {
        mockMvc.perform(get("/api/medecins")).andExpect(status().isForbidden());
    }

    // --- C4 : seul l'admin supprime un utilisateur ---

    @Test
    @WithMockUser(roles = "MEDECIN")
    void medecinCannotDeleteUser() throws Exception {
        mockMvc.perform(delete("/api/utilisateurs/5")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanDeleteUser() throws Exception {
        when(utilisateurService.deleteById(5L)).thenReturn(ResponseEntity.ok("ok"));
        mockMvc.perform(delete("/api/utilisateurs/5")).andExpect(status().isOk());
    }

    // --- Règle métier : un médecin ne supprime pas de patient ---

    @Test
    @WithMockUser(roles = "MEDECIN")
    void medecinCannotDeletePatient() throws Exception {
        mockMvc.perform(delete("/api/patients/3")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "SECRETAIRE")
    void secretaireCanDeletePatient() throws Exception {
        when(patientService.deleteById(3L)).thenReturn(ResponseEntity.ok("ok"));
        mockMvc.perform(delete("/api/patients/3")).andExpect(status().isOk());
    }

    // --- C6 : factures ---

    @Test
    @WithMockUser(roles = "SECRETAIRE")
    void secretaireCanRecordInstallmentPayment() throws Exception {
        mockMvc.perform(put("/api/factures/7/paiement")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"montantPaiement\": 5000}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "MEDECIN")
    void medecinCannotRecordPayment() throws Exception {
        mockMvc.perform(put("/api/factures/7/paiement")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"montantPaiement\": 5000}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "SECRETAIRE")
    void secretaireCannotDeleteFacture() throws Exception {
        mockMvc.perform(delete("/api/factures/7")).andExpect(status().isForbidden());
    }

    // --- C7 : un médecin ne voit que ses propres données ---

    @Test
    @WithMockUser(roles = "MEDECIN")
    void medecinCanSeeOwnRendezVous() throws Exception {
        when(authz.isCurrentUser(MEDECIN_CONNECTE)).thenReturn(true);
        when(rendezVousService.findByMedecinId(anyLong())).thenReturn(List.of());
        mockMvc.perform(get("/api/rendezvous/medecin/" + MEDECIN_CONNECTE)).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "MEDECIN")
    void medecinCannotSeeOtherMedecinRendezVous() throws Exception {
        when(authz.isCurrentUser(AUTRE_MEDECIN)).thenReturn(false);
        mockMvc.perform(get("/api/rendezvous/medecin/" + AUTRE_MEDECIN)).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "MEDECIN")
    void medecinCannotSeeOtherMedecinFactures() throws Exception {
        when(authz.isCurrentUser(AUTRE_MEDECIN)).thenReturn(false);
        mockMvc.perform(get("/api/factures/medecin/" + AUTRE_MEDECIN)).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "SECRETAIRE")
    void secretaireCanSeeAnyMedecinRendezVous() throws Exception {
        when(rendezVousService.findByMedecinId(anyLong())).thenReturn(List.of());
        mockMvc.perform(get("/api/rendezvous/medecin/" + AUTRE_MEDECIN)).andExpect(status().isOk());
    }

    // --- C8 : faute de frappe SECRETARE corrigée ---

    @Test
    @WithMockUser(roles = "SECRETAIRE")
    void secretaireCanViewMedecin() throws Exception {
        mockMvc.perform(get("/api/medecins/1")).andExpect(status().isOk());
    }

    // --- Prescriptions : la secrétaire consulte mais ne modifie pas ---

    @Test
    @WithMockUser(roles = "SECRETAIRE")
    void secretaireCanViewPrescription() throws Exception {
        mockMvc.perform(get("/api/prescriptions/4")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "SECRETAIRE")
    void secretaireCannotUpdatePrescription() throws Exception {
        mockMvc.perform(put("/api/prescriptions/4")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\": \"modif\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "SECRETAIRE")
    void secretaireCannotDeletePrescription() throws Exception {
        mockMvc.perform(delete("/api/prescriptions/4")).andExpect(status().isForbidden());
    }

    // --- Revenus : réservés à l'admin ---
    // (RevenuController non chargé ici : règle hasRole('ADMIN') vérifiée par revue)

    // --- C13 : seul le destinataire marque sa notification comme lue ---

    @Test
    @WithMockUser(roles = "MEDECIN")
    void recipientCanMarkNotificationAsRead() throws Exception {
        when(authz.isNotificationRecipient(9L)).thenReturn(true);
        mockMvc.perform(put("/api/notifications/9/read")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void nonRecipientCannotMarkNotificationAsRead() throws Exception {
        when(authz.isNotificationRecipient(9L)).thenReturn(false);
        mockMvc.perform(put("/api/notifications/9/read")).andExpect(status().isForbidden());
    }

    // --- C7b : contrôle de propriété sur les routes par identifiant ---

    @Test
    @WithMockUser(roles = "MEDECIN")
    void medecinCanViewOwnRendezVous() throws Exception {
        when(authz.ownsRendezVous(10L)).thenReturn(true);
        mockMvc.perform(get("/api/rendezvous/10")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "MEDECIN")
    void medecinCannotViewOtherMedecinRendezVous() throws Exception {
        when(authz.ownsRendezVous(11L)).thenReturn(false);
        mockMvc.perform(get("/api/rendezvous/11")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "MEDECIN")
    void medecinCannotReassignRendezVousToAnotherMedecin() throws Exception {
        when(authz.ownsRendezVous(10L)).thenReturn(true);
        when(authz.isCurrentUser(AUTRE_MEDECIN)).thenReturn(false);
        mockMvc.perform(put("/api/rendezvous/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"dateRendezVous\":\"2026-11-02T10:00:00\",\"patientId\":3,"
                                + "\"medecinId\":" + AUTRE_MEDECIN + ",\"typeRendezVousId\":1}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "MEDECIN")
    void medecinCannotChangeStatusOfOtherMedecinRendezVous() throws Exception {
        when(authz.ownsRendezVous(11L)).thenReturn(false);
        mockMvc.perform(patch("/api/rendezvous/11/statut").param("statut", "TERMINE"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "MEDECIN")
    void medecinCannotViewOtherMedecinFacture() throws Exception {
        when(authz.ownsFacture(7L)).thenReturn(false);
        mockMvc.perform(get("/api/factures/7")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "MEDECIN")
    void medecinCannotViewOtherMedecinPrescription() throws Exception {
        when(authz.ownsPrescription(4L)).thenReturn(false);
        mockMvc.perform(get("/api/prescriptions/4")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "MEDECIN")
    void medecinCanViewOwnPrescription() throws Exception {
        when(authz.ownsPrescription(4L)).thenReturn(true);
        mockMvc.perform(get("/api/prescriptions/4")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "MEDECIN")
    void medecinCannotDeleteOtherMedecinPrescription() throws Exception {
        when(authz.ownsPrescription(4L)).thenReturn(false);
        mockMvc.perform(delete("/api/prescriptions/4")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "SECRETAIRE")
    void secretaireStillViewsAnyRendezVous() throws Exception {
        mockMvc.perform(get("/api/rendezvous/11")).andExpect(status().isOk());
    }

    // --- C14 : envoi d'e-mail libre réservé à l'admin ---

    private static final String MAIL_VALIDE =
            "{\"destinataireEmail\":\"patient@test.com\",\"sujet\":\"Rappel\",\"message\":\"Bonjour\"}";

    @Test
    @WithMockUser(roles = "MEDECIN")
    void medecinCannotSendFreeEmail() throws Exception {
        mockMvc.perform(post("/api/notifications/send")
                        .contentType(MediaType.APPLICATION_JSON).content(MAIL_VALIDE))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanSendEmail() throws Exception {
        mockMvc.perform(post("/api/notifications/send")
                        .contentType(MediaType.APPLICATION_JSON).content(MAIL_VALIDE))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void invalidEmailIsRejected() throws Exception {
        mockMvc.perform(post("/api/notifications/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"destinataireEmail\":\"pas-un-email\",\"sujet\":\"\",\"message\":\"x\"}"))
                .andExpect(status().isBadRequest());
    }
}
