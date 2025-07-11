package org.kfokam48.cliniquemanagementbackend.config;
import org.kfokam48.cliniquemanagementbackend.enums.Roles;
import org.kfokam48.cliniquemanagementbackend.model.Administrateur;
import org.kfokam48.cliniquemanagementbackend.repository.AdministrateurRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DefaultUserInitializer implements CommandLineRunner {

    private final AdministrateurRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DefaultUserInitializer(AdministrateurRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }


    @Override
    public void run(String... args) throws Exception {
        // Vérifiez si l'utilisateur par défaut existe déjà
        if (userRepository.findByEmail("admin@gmail.com") == null) {
            Administrateur user = new Administrateur();
            user.setNom("admin");
            user.setEmail("admin@gmail.com");
            user.setPassword(passwordEncoder.encode("password")); // Encodez le mot de passe
            user.setRole(Roles.valueOf("ADMIN")); // Définissez le rôle

            userRepository.save(user); // Enregistrez l'utilisateur
        }
    }
}

