package org.kfokam48.cliniquemanagementbackend.service.auth;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.validation.Valid;
import org.kfokam48.cliniquemanagementbackend.dto.auth.LoginRequest;
import org.kfokam48.cliniquemanagementbackend.dto.auth.LoginResponse;
import org.kfokam48.cliniquemanagementbackend.enums.Roles;
import org.kfokam48.cliniquemanagementbackend.exception.AuthenticationFailedException;
import org.kfokam48.cliniquemanagementbackend.exception.RessourceNotFoundException;
import org.kfokam48.cliniquemanagementbackend.mapper.UtilisateurMapper;
import org.kfokam48.cliniquemanagementbackend.model.Utilisateur;
import org.kfokam48.cliniquemanagementbackend.repository.UtilisateurRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import io.jsonwebtoken.io.Decoders; // Important pour décoder la chaîne Base64
import org.springframework.beans.factory.annotation.Value;
import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class AuthService {


    @Value("${jwt.secret}")
    private String jwtSecretString; // La clé secrète lue depuis la configuration
    // Dans AuthService
    @Value("${jwt.expiration.milliseconds}")
    private long jwtExpirationMs;

    private SecretKey signingKey; // Pour stocker la clé décodée une fois

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final UtilisateurRepository utilisateurRepository ;
    private final UtilisateurMapper utilisateurMapper;


    public AuthService(AuthenticationManager authenticationManager, UserDetailsService userDetailsService, UtilisateurRepository utilisateurRepository, UtilisateurMapper utilisateurMapper) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.utilisateurRepository = utilisateurRepository;
        this.utilisateurMapper = utilisateurMapper;
    }

    // Initialisation de la clé au démarrage du service
    // @PostConstruct est une bonne pratique pour l'initialisation après l'injection de dépendances
    @jakarta.annotation.PostConstruct // Utilisez jakarta.annotation.PostConstruct si Spring Boot 3+
    private void init() {
        // Décoder la chaîne Base64 en bytes, puis créer la SecretKey
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecretString);
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        // Vous pouvez vérifier la longueur ici pour vous assurer qu'elle est correcte
        System.out.println("Clé JWT chargée. Longueur (octets) : " + this.signingKey.getEncoded().length);
    }

    public LoginResponse authenticateUser(@Valid LoginRequest authRequest) {
        try {
            // Authentification
            System.out.println("Authentification en cours...");
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getEmail(), authRequest.getPassword())
            );
            // Récupération des détails de l'utilisateur
            Utilisateur user = utilisateurRepository.findByEmail(authRequest.getEmail()).orElseThrow(()-> new RessourceNotFoundException("user not found"));
            UserDetails userDetails = userDetailsService.loadUserByUsername(authRequest.getEmail());
            System.out.println("Authentification réussie pour l'utilisateur : " + userDetails.getUsername());
//            // Génération du token JWT
            String token = Jwts.builder()
                    .issuer("CLINIQUE-MANAGEMENT")
                    .subject(userDetails.getUsername())
                    .issuedAt(new Date())
                    .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs)) // Expire dans 1 jour
                    .signWith(signingKey) // Utilisez la clé chargée et initialisée
                    .compact();

            // Construction de la réponse
            LoginResponse loginResponse = new LoginResponse();
            loginResponse.setToken(token);
            loginResponse.setUser(utilisateurMapper.utilisateurToUserDTO(user));
            return loginResponse;

        } catch (Exception e) {
            // Gestion des erreurs avec un message explicite
            System.out.println("Erreur d'authentification : " + e.getMessage());
            throw new AuthenticationFailedException("Identifiants invalides : vérifiez l'e-mail ou le mot de passe.");
        }

    }
    public Roles getUserRole(LoginRequest loginRequest){
        Utilisateur user = utilisateurRepository.findByEmail(loginRequest.getEmail()).orElseThrow(()-> new RessourceNotFoundException("user not found"));
        return user.getRole();
    }



}