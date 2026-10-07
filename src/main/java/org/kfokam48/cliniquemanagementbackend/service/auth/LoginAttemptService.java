package org.kfokam48.cliniquemanagementbackend.service.auth;

import org.kfokam48.cliniquemanagementbackend.exception.TooManyLoginAttemptsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Anti force brute sur /api/auth/login : au-delà de MAX_ECHECS échecs pour un même compte
 * (e-mail) sur la FENETRE glissante, les tentatives sont refusées (429) jusqu'à ce que le plus
 * ancien échec sorte de la fenêtre. Un succès remet le compteur du compte à zéro.
 *
 * Limitation par compte et non par adresse IP : derrière le proxy de l'hébergeur, toutes les
 * requêtes partagent la même IP. Une limite par IP se configure au niveau du proxy.
 * Compteurs en mémoire : propres à chaque instance de l'application.
 */
@Component
public class LoginAttemptService {

    static final int MAX_ECHECS = 5;
    static final Duration FENETRE = Duration.ofMinutes(15);

    private final Map<String, Deque<Instant>> echecsParCompte = new ConcurrentHashMap<>();
    private final Clock clock;

    @Autowired
    public LoginAttemptService() {
        this(Clock.systemUTC());
    }

    LoginAttemptService(Clock clock) {
        this.clock = clock;
    }

    // Lève TooManyLoginAttemptsException si le compte est temporairement bloqué
    public void verifierAutorise(String email) {
        Deque<Instant> echecs = echecsParCompte.get(cle(email));
        if (echecs == null) {
            return;
        }
        synchronized (echecs) {
            purger(echecs);
            if (echecs.size() >= MAX_ECHECS) {
                Instant deblocage = echecs.peekFirst().plus(FENETRE);
                throw new TooManyLoginAttemptsException(Duration.between(clock.instant(), deblocage).toSeconds());
            }
        }
    }

    public void enregistrerEchec(String email) {
        Deque<Instant> echecs = echecsParCompte.computeIfAbsent(cle(email), k -> new ArrayDeque<>());
        synchronized (echecs) {
            purger(echecs);
            echecs.addLast(clock.instant());
        }
    }

    public void enregistrerSucces(String email) {
        echecsParCompte.remove(cle(email));
    }

    // Libère la mémoire des comptes dont tous les échecs sont expirés (e-mails inconnus compris)
    @Scheduled(fixedDelay = 15 * 60 * 1000)
    public void nettoyer() {
        echecsParCompte.entrySet().removeIf(entree -> {
            synchronized (entree.getValue()) {
                purger(entree.getValue());
                return entree.getValue().isEmpty();
            }
        });
    }

    private void purger(Deque<Instant> echecs) {
        Instant limite = clock.instant().minus(FENETRE);
        while (!echecs.isEmpty() && !echecs.peekFirst().isAfter(limite)) {
            echecs.pollFirst();
        }
    }

    private static String cle(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }
}
