package org.kfokam48.cliniquemanagementbackend.config;

import io.jsonwebtoken.JwtException;
import org.kfokam48.cliniquemanagementbackend.model.Utilisateur;
import org.kfokam48.cliniquemanagementbackend.repository.UtilisateurRepository;
import org.kfokam48.cliniquemanagementbackend.service.auth.CustomUserDetailsService;
import org.kfokam48.cliniquemanagementbackend.service.auth.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Sécurité du chat STOMP :
 * - CONNECT : un JWT valide est obligatoire (en-tête "Authorization: Bearer <token>") ;
 * - SUBSCRIBE : uniquement /topic/** et ses propres files /user/{sonId}/** (ou /user/queue/**) ;
 * - SEND : uniquement vers /app/** (jamais directement vers le broker).
 */
@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    public static final String USER_ID_ATTRIBUTE = "user_id";

    private static final Logger log = LoggerFactory.getLogger(WebSocketAuthInterceptor.class);
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String USER_PREFIX = "/user/";

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    private final UtilisateurRepository utilisateurRepository;

    public WebSocketAuthInterceptor(JwtService jwtService, CustomUserDetailsService userDetailsService, UtilisateurRepository utilisateurRepository) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.utilisateurRepository = utilisateurRepository;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null || accessor.getCommand() == null) {
            return message;
        }

        switch (accessor.getCommand()) {
            case CONNECT -> authenticate(accessor);
            case SUBSCRIBE -> checkSubscription(accessor);
            case SEND -> checkSend(accessor);
            default -> { }
        }
        return message;
    }

    private void authenticate(StompHeaderAccessor accessor) {
        String header = accessor.getFirstNativeHeader("Authorization");
        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            throw new MessagingException("Connexion WebSocket refusée : token JWT manquant.");
        }
        try {
            String email = jwtService.extractSubject(header.substring(BEARER_PREFIX.length()));
            Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
                    .orElseThrow(() -> new MessagingException("Connexion WebSocket refusée : utilisateur inconnu."));
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);
            if (!userDetails.isEnabled()) {
                throw new MessagingException("Connexion WebSocket refusée : compte désactivé.");
            }

            accessor.setUser(new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities()));
            sessionAttributes(accessor).put(USER_ID_ATTRIBUTE, String.valueOf(utilisateur.getId()));
            log.debug("Connexion WebSocket authentifiée : {} (session {})", email, accessor.getSessionId());
        } catch (JwtException | IllegalArgumentException e) {
            throw new MessagingException("Connexion WebSocket refusée : token invalide ou expiré.");
        }
    }

    private void checkSubscription(StompHeaderAccessor accessor) {
        String destination = accessor.getDestination();
        String userId = authenticatedUserId(accessor);

        if (destination != null && destination.startsWith("/topic/")) {
            return;
        }
        if (destination != null && destination.startsWith(USER_PREFIX)) {
            String rest = destination.substring(USER_PREFIX.length());
            String owner = rest.contains("/") ? rest.substring(0, rest.indexOf('/')) : rest;
            // "/user/queue/..." (résolu par Spring) ou "/user/{sonId}/..." uniquement
            if (owner.equals("queue") || owner.equals(userId)) {
                return;
            }
        }
        throw new MessagingException("Abonnement refusé : " + destination);
    }

    private void checkSend(StompHeaderAccessor accessor) {
        String userId = authenticatedUserId(accessor);
        String destination = accessor.getDestination();
        if (destination == null || !destination.startsWith("/app/")) {
            throw new MessagingException("Envoi refusé : " + destination);
        }
        // Une session ouverte avant la désactivation du compte ne peut plus envoyer de message
        if (!utilisateurRepository.findById(Long.valueOf(userId)).map(Utilisateur::isActif).orElse(false)) {
            throw new MessagingException("Envoi refusé : compte désactivé ou supprimé.");
        }
    }

    private String authenticatedUserId(StompHeaderAccessor accessor) {
        Object userId = sessionAttributes(accessor).get(USER_ID_ATTRIBUTE);
        if (userId == null) {
            throw new MessagingException("Session WebSocket non authentifiée.");
        }
        return userId.toString();
    }

    private Map<String, Object> sessionAttributes(StompHeaderAccessor accessor) {
        Map<String, Object> attributes = accessor.getSessionAttributes();
        if (attributes == null) {
            throw new MessagingException("Session WebSocket invalide.");
        }
        return attributes;
    }
}
