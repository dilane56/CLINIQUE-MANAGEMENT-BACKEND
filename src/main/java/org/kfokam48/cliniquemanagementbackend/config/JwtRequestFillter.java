package org.kfokam48.cliniquemanagementbackend.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders; // Import pour décoder la chaîne Base64
import io.jsonwebtoken.security.SignatureException; // Pour une gestion plus spécifique des erreurs de signature
import jakarta.annotation.PostConstruct; // Pour l'initialisation de la clé
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.kfokam48.cliniquemanagementbackend.service.auth.CustomUserDetailsService;
import org.slf4j.Logger; // Pour les logs
import org.slf4j.LoggerFactory; // Pour les logs
import org.springframework.beans.factory.annotation.Value; // Pour injecter la clé depuis la configuration
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import io.jsonwebtoken.security.Keys; // Import pour Keys.hmacShaKeyFor
import java.io.IOException;

@Component
public class JwtRequestFillter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtRequestFillter.class);

    @Value("${jwt.secret}")
    private String jwtSecretString; // La clé secrète lue depuis la configuration

    private SecretKey signingKey; // Pour stocker la clé décodée une fois

    private final CustomUserDetailsService userDetailsService;

    public JwtRequestFillter(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    // Initialisation de la clé au démarrage du filtre
    @PostConstruct
    private void init() {
        try {
            // Décoder la chaîne Base64 en bytes, puis créer la SecretKey
            byte[] keyBytes = Decoders.BASE64.decode(jwtSecretString);
            this.signingKey = Keys.hmacShaKeyFor(keyBytes);
            logger.debug("Clé JWT chargée dans JwtRequestFilter. Longueur (octets) : {}", this.signingKey.getEncoded().length);
        } catch (IllegalArgumentException e) {
            logger.error("Erreur lors du décodage de la clé JWT. Assurez-vous que 'jwt.secret' est une chaîne Base64 valide.", e);
            // Gérer l'erreur, par exemple en lançant une RuntimeException pour empêcher l'application de démarrer avec une clé invalide
            throw new IllegalStateException("Impossible d'initialiser la clé JWT.", e);
        }
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        final String authorizationHeader = request.getHeader("Authorization");
        String jwt = null;
        String userEmail = null;
        String path = request.getRequestURI();

        // Ignorer les endpoints publics (ex: /api/auth/login, /api/auth/register)
        if (path.startsWith("/api/auth/")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // Extraire le token s'il existe
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                jwt = authorizationHeader.substring(7);

                // Utiliser la clé secrète initialisée dans ce filtre
                Claims claims = Jwts.parser()
                        .verifyWith(this.signingKey) // Utilisez la clé initialisée ici
                        .build()
                        .parseSignedClaims(jwt)
                        .getPayload();

                userEmail = claims.getSubject();
                logger.debug("Token JWT décodé. Sujet : {}", userEmail);
            }

            // Authentifier si le token est valide et l'utilisateur n'est pas déjà authentifié
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

                // Vérifier si le token est valide pour l'utilisateur (optionnel mais recommandé pour des validations plus poussées)
                // Par exemple, vérifier si l'utilisateur est actif, etc.
                // Pour l'instant, nous nous basons sur la validité de la signature et l'existence de l'utilisateur.

                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                logger.debug("Utilisateur {} authentifié via JWT.", userEmail);
            }

            // Laisser la requête continuer vers le prochain filtre de la chaîne
            filterChain.doFilter(request, response);

        } catch (SignatureException e) {
            // Erreur spécifique si la signature du token est invalide
            logger.warn("Signature JWT invalide : {}", e.getMessage());
            sendUnauthorizedResponse(response, "Signature du token invalide.");
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            // Erreur spécifique si le token est expiré
            logger.warn("Token JWT expiré : {}", e.getMessage());
            sendUnauthorizedResponse(response, "Token expiré.");
        } catch (io.jsonwebtoken.MalformedJwtException e) {
            // Erreur spécifique si le token est malformé
            logger.warn("Token JWT malformé : {}", e.getMessage());
            sendUnauthorizedResponse(response, "Token malformé.");
        } catch (Exception e) {
            // Gérer toute autre exception inattendue
            logger.error("Erreur lors du traitement du JWT : ", e);
            sendUnauthorizedResponse(response, "Token invalide ou erreur interne.");
        }
    }

    private void sendUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        if (!response.isCommitted()) {
            response.resetBuffer();
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"" + message + "\"}");
        }
    }
}