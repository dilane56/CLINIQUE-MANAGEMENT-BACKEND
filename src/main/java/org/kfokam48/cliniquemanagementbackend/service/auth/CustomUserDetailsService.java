package org.kfokam48.cliniquemanagementbackend.service.auth;

import org.kfokam48.cliniquemanagementbackend.model.auth.CustomUserDetails;
import org.kfokam48.cliniquemanagementbackend.model.Utilisateur;
import org.kfokam48.cliniquemanagementbackend.repository.UtilisateurRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UtilisateurRepository userRepository;

    public CustomUserDetailsService(UtilisateurRepository userRepository) {
        this.userRepository = userRepository;
    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Utilisateur user = userRepository.findByEmail(username).orElseThrow(()->new UsernameNotFoundException("Utilisateur introuvable : " + username));



        return new CustomUserDetails(user.getEmail(), user.getPassword(), getGrantedAuthorities(String.valueOf(user.getRole())), user.isActif());
    }

    private List<GrantedAuthority> getGrantedAuthorities(String role) {
        // Ajout automatique du préfixe "ROLE_" pour la sécurité Spring.
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role));
    }
}