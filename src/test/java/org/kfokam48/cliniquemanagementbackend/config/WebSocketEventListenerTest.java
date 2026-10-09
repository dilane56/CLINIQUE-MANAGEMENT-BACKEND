package org.kfokam48.cliniquemanagementbackend.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kfokam48.cliniquemanagementbackend.enums.UserStatus;
import org.kfokam48.cliniquemanagementbackend.model.Administrateur;
import org.kfokam48.cliniquemanagementbackend.repository.UtilisateurRepository;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.simp.user.SimpSession;
import org.springframework.messaging.simp.user.SimpUser;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Présence (EN_LIGNE / HORS_LIGNE) : le frontend ouvre une connexion WebSocket par onglet,
 * l'utilisateur ne passe hors ligne qu'à la fermeture de la dernière.
 */
class WebSocketEventListenerTest {

    private static final String EMAIL = "medecin@clinique.com";
    private static final long USER_ID = 5L;

    private UtilisateurRepository repository;
    private SimpMessageSendingOperations messagingTemplate;
    private SimpUserRegistry registry;
    private WebSocketEventListener listener;
    private Administrateur utilisateur;

    @BeforeEach
    void setUp() {
        utilisateur = new Administrateur();
        utilisateur.setId(USER_ID);
        utilisateur.setEmail(EMAIL);
        utilisateur.setStatus(UserStatus.EN_LIGNE);

        repository = mock(UtilisateurRepository.class);
        when(repository.findById(USER_ID)).thenReturn(Optional.of(utilisateur));
        messagingTemplate = mock(SimpMessageSendingOperations.class);
        registry = mock(SimpUserRegistry.class);
        listener = new WebSocketEventListener(messagingTemplate, repository, registry);
    }

    @Test
    void derniereSessionFermee_passeHorsLigne() {
        enregistrerSessions("s1");

        listener.handleWebSocketDisconnectListener(deconnexion("s1"));

        assertThat(utilisateur.getStatus()).isEqualTo(UserStatus.HORS_LIGNE);
        verify(repository).save(utilisateur);
        verify(messagingTemplate).convertAndSend(any(String.class), any(Object.class));
    }

    @Test
    void autreOngletEncoreConnecte_resteEnLigne() {
        enregistrerSessions("s1", "s2");

        listener.handleWebSocketDisconnectListener(deconnexion("s1"));

        assertThat(utilisateur.getStatus()).isEqualTo(UserStatus.EN_LIGNE);
        verify(repository, never()).save(any());
        verify(messagingTemplate, never()).convertAndSend(anyString(), any(Object.class));
    }

    @Test
    void utilisateurAbsentDuRegistre_passeHorsLigne() {
        when(registry.getUser(EMAIL)).thenReturn(null);

        listener.handleWebSocketDisconnectListener(deconnexion("s1"));

        assertThat(utilisateur.getStatus()).isEqualTo(UserStatus.HORS_LIGNE);
    }

    private void enregistrerSessions(String... ids) {
        SimpUser simpUser = mock(SimpUser.class);
        Set<SimpSession> sessions = new java.util.HashSet<>();
        for (String id : ids) {
            SimpSession session = mock(SimpSession.class);
            when(session.getId()).thenReturn(id);
            sessions.add(session);
        }
        when(simpUser.getSessions()).thenReturn(sessions);
        when(registry.getUser(EMAIL)).thenReturn(simpUser);
    }

    private SessionDisconnectEvent deconnexion(String sessionId) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.DISCONNECT);
        accessor.setSessionId(sessionId);
        Map<String, Object> attributs = new HashMap<>();
        attributs.put(WebSocketAuthInterceptor.USER_ID_ATTRIBUTE, String.valueOf(USER_ID));
        accessor.setSessionAttributes(attributs);
        Message<byte[]> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
        Principal principal = () -> EMAIL;
        return new SessionDisconnectEvent(this, message, sessionId, CloseStatus.NORMAL, principal);
    }
}
