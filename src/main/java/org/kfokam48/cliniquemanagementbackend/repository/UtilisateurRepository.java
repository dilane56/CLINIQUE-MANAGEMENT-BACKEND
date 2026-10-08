package org.kfokam48.cliniquemanagementbackend.repository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.kfokam48.cliniquemanagementbackend.model.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {
    Optional<Utilisateur> findByEmail(String email);
//    Optional<Utilisateur> findByUsername(String username);
    boolean existsByEmail(String email);
//    boolean existsByUsername(String username);

    // Nombre de comptes par rôle (tableau de bord de l'administrateur)
    @Query("SELECT u.role, COUNT(u) FROM Utilisateur u GROUP BY u.role")
    List<Object[]> compterParRole();
}
