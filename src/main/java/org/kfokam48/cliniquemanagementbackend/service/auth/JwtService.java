package org.kfokam48.cliniquemanagementbackend.service.auth;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;

/**
 * Validation des JWT, partagée entre le filtre HTTP et l'authentification WebSocket.
 */
@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String jwtSecretString;

    private SecretKey signingKey;

    @PostConstruct
    void init() {
        try {
            this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecretString));
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Impossible d'initialiser la clé JWT : 'jwt.secret' doit être une chaîne Base64 valide.", e);
        }
    }

    /**
     * Vérifie la signature et l'expiration du token, puis renvoie son sujet (l'email de l'utilisateur).
     *
     * @throws JwtException si le token est invalide, expiré ou malformé
     */
    public String extractSubject(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }
}
