package org.kfokam48.cliniquemanagementbackend.model.auth;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

public class CustomUserDetails implements UserDetails {

    private final String username;
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;
    // false : compte désactivé par l'administrateur (I5)
    private final boolean actif;

    public CustomUserDetails(String username, String password, Collection<? extends GrantedAuthority> authorities, boolean actif) {
        this.username = username;
        this.password = password;
        this.authorities = authorities;
        this.actif = actif;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        // Supposons que le compte n'expire jamais.
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        // Supposons que le compte n'est jamais verrouillé.
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        // Supposons que les informations d'identification n'expirent jamais.
        return true;
    }

    @Override
    public boolean isEnabled() {
        // Compte désactivé : Spring Security refuse la connexion (DisabledException)
        return actif;
    }
}