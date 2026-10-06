package org.kfokam48.cliniquemanagementbackend.repository;



import java.util.Optional;
import org.kfokam48.cliniquemanagementbackend.model.Secretaire;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SecretaireRepository extends JpaRepository<Secretaire, Long> {
    Optional<Secretaire> findByEmail(String email);
}
