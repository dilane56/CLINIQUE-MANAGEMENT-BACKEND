package org.kfokam48.cliniquemanagementbackend.exception;

/** Connexion avec le bon mot de passe sur un compte désactivé par l'administrateur (I5) : 403. */
public class CompteDesactiveException extends RuntimeException {
    public CompteDesactiveException(String message) {
        super(message);
    }
}
