package org.kfokam48.cliniquemanagementbackend.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kfokam48.cliniquemanagementbackend.model.Administrateur;
import org.kfokam48.cliniquemanagementbackend.repository.UtilisateurRepository;
import org.kfokam48.cliniquemanagementbackend.service.auth.CustomUserDetailsService;
import org.kfokam48.cliniquemanagementbackend.service.auth.JwtService;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class WebSocketAuthInterceptorTest {

    // Clé de test uniquement (Base64, 256 bits)
    private static final String SECRET = "dGVzdC1zZWNyZXQta2V5LWZvci11bml0LXRlc3RzLW9ubHktMzJieXRlcw==";
    private static final String EMAIL = "medecin@clinique.com";
    private static final long USER_ID = 5L;

    private WebSocketAuthInterceptor interceptor;
    private Administrateur utilisateur;
    private CustomUserDetailsService userDetailsService;
    private final MessageChannel channel = mock(MessageChannel.class);

    @BeforeEach
    void setUp() {
        JwtService jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "jwtSecretString", SECRET);
        ReflectionTestUtils.invokeMethod(jwtService, "init");

        utilisateur = new Administrateur();
        utilisateur.setId(USER_ID);
        utilisateur.setEmail(EMAIL);
        UtilisateurRepository repository = mock(UtilisateurRepository.class);
        when(repository.findByEmail(EMAIL)).thenReturn(Optional.of(utilisateur));
        when(repository.findById(USER_ID)).thenReturn(Optional.of(utilisateur));

        userDetailsService = mock(CustomUserDetailsService.class);
        when(userDetailsService.loadUserByUsername(EMAIL))
                .thenReturn(new User(EMAIL, "hash", List.of()));

        interceptor = new WebSocketAuthInterceptor(jwtService, userDetailsService, repository);
    }

    private static String validToken() {
        return Jwts.builder()
                .subject(EMAIL)
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET)))
                .compact();
    }

    private static Message<byte[]> frame(StompCommand command, String destination, String authorization, Map<String, Object> session) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(command);
        accessor.setSessionId("s1");
        accessor.setSessionAttributes(session);
        if (destination != null) {
            accessor.setDestination(destination);
        }
        if (authorization != null) {
            accessor.setNativeHeader("Authorization", authorization);
        }
        accessor.setLeaveMutable(true);
        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }

    private Map<String, Object> connectedSession() {
        Map<String, Object> session = new HashMap<>();
        interceptor.preSend(frame(StompCommand.CONNECT, null, "Bearer " + validToken(), session), channel);
        return session;
    }

    @Test
    void connectWithoutTokenIsRejected() {
        assertThatThrownBy(() -> interceptor.preSend(frame(StompCommand.CONNECT, null, null, new HashMap<>()), channel))
                .isInstanceOf(MessagingException.class);
    }

    @Test
    void connectWithInvalidTokenIsRejected() {
        assertThatThrownBy(() -> interceptor.preSend(frame(StompCommand.CONNECT, null, "Bearer faux.token.jwt", new HashMap<>()), channel))
                .isInstanceOf(MessagingException.class);
    }

    @Test
    void connectWithValidTokenStoresUserId() {
        assertThat(connectedSession()).containsEntry(WebSocketAuthInterceptor.USER_ID_ATTRIBUTE, "5");
    }

    @Test
    void userCanSubscribeToOwnQueue() {
        Map<String, Object> session = connectedSession();
        interceptor.preSend(frame(StompCommand.SUBSCRIBE, "/user/5/queue/messages", null, session), channel);
        interceptor.preSend(frame(StompCommand.SUBSCRIBE, "/topic/status", null, session), channel);
    }

    @Test
    void userCannotSubscribeToAnotherUserQueue() {
        Map<String, Object> session = connectedSession();
        assertThatThrownBy(() -> interceptor.preSend(frame(StompCommand.SUBSCRIBE, "/user/6/queue/messages", null, session), channel))
                .isInstanceOf(MessagingException.class);
    }

    @Test
    void unauthenticatedSubscriptionIsRejected() {
        assertThatThrownBy(() -> interceptor.preSend(frame(StompCommand.SUBSCRIBE, "/topic/status", null, new HashMap<>()), channel))
                .isInstanceOf(MessagingException.class);
    }

    @Test
    void sendDirectlyToBrokerIsRejected() {
        Map<String, Object> session = connectedSession();
        assertThatThrownBy(() -> interceptor.preSend(frame(StompCommand.SEND, "/user/6/queue/messages", null, session), channel))
                .isInstanceOf(MessagingException.class);
    }

    @Test
    void sendToApplicationIsAllowed() {
        Map<String, Object> session = connectedSession();
        interceptor.preSend(frame(StompCommand.SEND, "/app/chat.send", null, session), channel);
    }

    // --- I5 : compte désactivé par l'administrateur ---

    @Test
    void connectWithDisabledAccountIsRejected() {
        when(userDetailsService.loadUserByUsername(EMAIL))
                .thenReturn(User.withUsername(EMAIL).password("hash").authorities(List.of()).disabled(true).build());
        assertThatThrownBy(this::connectedSession)
                .isInstanceOf(MessagingException.class)
                .hasMessageContaining("désactivé");
    }

    @Test
    void openSessionCannotSendOnceAccountIsDisabled() {
        Map<String, Object> session = connectedSession();
        utilisateur.setActif(false);
        assertThatThrownBy(() -> interceptor.preSend(frame(StompCommand.SEND, "/app/chat.send", null, session), channel))
                .isInstanceOf(MessagingException.class)
                .hasMessageContaining("désactivé");
    }
}
