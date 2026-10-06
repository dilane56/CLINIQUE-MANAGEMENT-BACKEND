package org.kfokam48.cliniquemanagementbackend.config;

import io.jsonwebtoken.security.SignatureException; // Pour une gestion plus spécifique des erreurs de signature
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.kfokam48.cliniquemanagementbackend.service.auth.CustomUserDetailsService;
import org.kfokam48.cliniquemanagementbackend.service.auth.JwtService;
import org.slf4j.Logger; // Pour les logs
import org.slf4j.LoggerFactory; // Pour les logs
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtRequestFillter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtRequestFillter.class);

    private final CustomUserDetailsService userDetailsService;
    private final JwtService jwtService;

    public JwtRequestFillter(CustomUserDetailsService userDetailsService, JwtService jwtService) {
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
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

                userEmail = jwtService.extractSubject(jwt);
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