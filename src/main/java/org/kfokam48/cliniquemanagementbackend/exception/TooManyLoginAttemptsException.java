package org.kfokam48.cliniquemanagementbackend.exception;

// Trop d'échecs de connexion récents pour ce compte (réponse 429 avec Retry-After)
public class TooManyLoginAttemptsException extends RuntimeException {

    private final long retryAfterSeconds;

    public TooManyLoginAttemptsException(long retryAfterSeconds) {
        super("Trop de tentatives de connexion échouées. Réessayez dans " + Math.max(1, (retryAfterSeconds + 59) / 60) + " minute(s).");
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}
