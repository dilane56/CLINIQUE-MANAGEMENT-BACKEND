package org.kfokam48.cliniquemanagementbackend.config;
import org.kfokam48.cliniquemanagementbackend.enums.Roles;
import org.kfokam48.cliniquemanagementbackend.model.Administrateur;
import org.kfokam48.cliniquemanagementbackend.model.TypeRendezVous;
import org.kfokam48.cliniquemanagementbackend.repository.AdministrateurRepository;
import org.kfokam48.cliniquemanagementbackend.repository.TypeRendezVousRepository;
import org.kfokam48.cliniquemanagementbackend.repository.UtilisateurRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Component
public class DefaultUserInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DefaultUserInitializer.class);
    private static final int MIN_PASSWORD_LENGTH = 8;

    private final AdministrateurRepository userRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final TypeRendezVousRepository typeRendezVousRepository;

    // Identifiants du premier administrateur, fournis par variables d'environnement (jamais en dur)
    @Value("${DEFAULT_ADMIN_EMAIL:}")
    private String defaultAdminEmail;

    @Value("${DEFAULT_ADMIN_PASSWORD:}")
    private String defaultAdminPassword;

    public DefaultUserInitializer(AdministrateurRepository userRepository, UtilisateurRepository utilisateurRepository, PasswordEncoder passwordEncoder, TypeRendezVousRepository typeRendezVousRepository) {
        this.userRepository = userRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.passwordEncoder = passwordEncoder;
        this.typeRendezVousRepository = typeRendezVousRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        createDefaultAdmin();

        List<TypeRendezVous> types = List.of(
            new TypeRendezVous(null, "Consultation générale",      30, new BigDecimal("15000.00")),
            new TypeRendezVous(null, "Consultation spécialisée",   45, new BigDecimal("25000.00")),
            new TypeRendezVous(null, "Visite de suivi",             20, new BigDecimal("10000.00")),
            new TypeRendezVous(null, "Examen dentaire",             30, new BigDecimal("20000.00")),
            new TypeRendezVous(null, "Détartrage dentaire",         45, new BigDecimal("30000.00")),
            new TypeRendezVous(null, "Nettoyage de plaie",          15, new BigDecimal("8000.00")),
            new TypeRendezVous(null, "Prise de tension",            10, new BigDecimal("3000.00")),
            new TypeRendezVous(null, "Prélèvement sanguin",         15, new BigDecimal("5000.00")),
            new TypeRendezVous(null, "Injection/Piqûre",           10, new BigDecimal("5000.00")),
            new TypeRendezVous(null, "Pansement",                   20, new BigDecimal("7000.00")),
            new TypeRendezVous(null, "Échographie",                 30, new BigDecimal("35000.00")),
            new TypeRendezVous(null, "Radiographie",                20, new BigDecimal("25000.00")),
            new TypeRendezVous(null, "Test de grossesse",           10, new BigDecimal("5000.00")),
            new TypeRendezVous(null, "Vaccination",                 15, new BigDecimal("10000.00"))
        );

        for (TypeRendezVous type : types) {
            if (!typeRendezVousRepository.existsByLibelle(type.getLibelle())) {
                typeRendezVousRepository.save(type);
            }
        }
    }

    private void createDefaultAdmin() {
        if (defaultAdminEmail.isBlank() || defaultAdminPassword.isBlank()) {
            log.warn("DEFAULT_ADMIN_EMAIL / DEFAULT_ADMIN_PASSWORD non définis : aucun administrateur par défaut n'est créé.");
            return;
        }
        if (defaultAdminPassword.length() < MIN_PASSWORD_LENGTH) {
            log.warn("DEFAULT_ADMIN_PASSWORD doit contenir au moins {} caractères : administrateur par défaut non créé.", MIN_PASSWORD_LENGTH);
            return;
        }
        if (utilisateurRepository.existsByEmail(defaultAdminEmail)) {
            return;
        }
        Administrateur admin = new Administrateur();
        admin.setNom("Administrateur");
        admin.setPrenom("Principal");
        admin.setEmail(defaultAdminEmail);
        admin.setPassword(passwordEncoder.encode(defaultAdminPassword));
        admin.setRole(Roles.ADMIN);
        admin.setDateCreation(LocalDate.now());
        userRepository.save(admin);
        log.info("Administrateur par défaut créé : {}", defaultAdminEmail);
    }
}
