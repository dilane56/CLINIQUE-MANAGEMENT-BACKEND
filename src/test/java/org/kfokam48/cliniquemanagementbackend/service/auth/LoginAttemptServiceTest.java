package org.kfokam48.cliniquemanagementbackend.service.auth;

import org.junit.jupiter.api.Test;
import org.kfokam48.cliniquemanagementbackend.exception.TooManyLoginAttemptsException;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * I6 : limitation des tentatives de connexion par compte.
 */
class LoginAttemptServiceTest {

    // Horloge de test que l'on fait avancer à la main
    private static final class HorlogeReglable extends Clock {
        private Instant maintenant = Instant.parse("2030-01-15T09:00:00Z");

        void avancer(Duration duree) {
            maintenant = maintenant.plus(duree);
        }

        @Override public ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(ZoneId zone) { return this; }
        @Override public Instant instant() { return maintenant; }
    }

    private final HorlogeReglable horloge = new HorlogeReglable();
    private final LoginAttemptService service = new LoginAttemptService(horloge);

    private void echouer(String email, int fois) {
        for (int i = 0; i < fois; i++) {
            service.verifierAutorise(email);
            service.enregistrerEchec(email);
        }
    }

    @Test
    void accountIsBlockedAfterFiveFailures() {
        echouer("medecin@clinique.com", 4);
        assertThatCode(() -> service.verifierAutorise("medecin@clinique.com")).doesNotThrowAnyException();

        service.enregistrerEchec("medecin@clinique.com");

        assertThatThrownBy(() -> service.verifierAutorise("medecin@clinique.com"))
                .isInstanceOfSatisfying(TooManyLoginAttemptsException.class,
                        e -> assertThat(e.getRetryAfterSeconds()).isEqualTo(15 * 60));
    }

    @Test
    void accountIsUnblockedOnceTheWindowHasPassed() {
        echouer("medecin@clinique.com", 5);
        horloge.avancer(Duration.ofMinutes(15).plusSeconds(1));
        assertThatCode(() -> service.verifierAutorise("medecin@clinique.com")).doesNotThrowAnyException();
    }

    @Test
    void successResetsTheCounter() {
        echouer("medecin@clinique.com", 4);
        service.enregistrerSucces("medecin@clinique.com");
        echouer("medecin@clinique.com", 4);
        assertThatCode(() -> service.verifierAutorise("medecin@clinique.com")).doesNotThrowAnyException();
    }

    @Test
    void emailIsCaseInsensitiveAndOtherAccountsAreNotAffected() {
        echouer("Medecin@Clinique.com", 5);
        assertThatThrownBy(() -> service.verifierAutorise("medecin@clinique.com"))
                .isInstanceOf(TooManyLoginAttemptsException.class);
        assertThatCode(() -> service.verifierAutorise("secretaire@clinique.com")).doesNotThrowAnyException();
    }

    @Test
    void cleanupForgetsExpiredFailures() {
        echouer("inconnu@clinique.com", 3);
        horloge.avancer(Duration.ofMinutes(16));
        service.nettoyer();
        echouer("inconnu@clinique.com", 4);
        assertThatCode(() -> service.verifierAutorise("inconnu@clinique.com")).doesNotThrowAnyException();
    }
}
