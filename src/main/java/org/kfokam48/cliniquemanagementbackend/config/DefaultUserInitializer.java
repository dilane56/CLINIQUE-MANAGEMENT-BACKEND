package org.kfokam48.cliniquemanagementbackend.config;
import org.kfokam48.cliniquemanagementbackend.enums.Roles;
import org.kfokam48.cliniquemanagementbackend.model.Administrateur;
import org.kfokam48.cliniquemanagementbackend.model.TypeRendezVous;
import org.kfokam48.cliniquemanagementbackend.repository.AdministrateurRepository;
import org.kfokam48.cliniquemanagementbackend.repository.TypeRendezVousRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class DefaultUserInitializer implements CommandLineRunner {

    private final AdministrateurRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TypeRendezVousRepository typeRendezVousRepository;

    public DefaultUserInitializer(AdministrateurRepository userRepository, PasswordEncoder passwordEncoder, TypeRendezVousRepository typeRendezVousRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.typeRendezVousRepository = typeRendezVousRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.findByEmail("admin@gmail.com") == null) {
            Administrateur user = new Administrateur();
            user.setNom("admin");
            user.setPrenom("Romaric");
            user.setEmail("admin@gmail.com");
            user.setTelephone("123456789");
            user.setPassword(passwordEncoder.encode("password"));
            user.setRole(Roles.valueOf("ADMIN"));
            userRepository.save(user);
        }

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
}

