package org.kfokam48.cliniquemanagementbackend.repository;


import org.kfokam48.cliniquemanagementbackend.model.Medecin;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MedecinRepository extends JpaRepository<Medecin, Long> {
    Optional<Medecin> findByEmail(String email);

    // Verrou en écriture (SELECT ... FOR UPDATE) : sérialise les réservations d'un même médecin
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT m FROM Medecin m WHERE m.id = :id")
    Optional<Medecin> findByIdForUpdate(@Param("id") Long id);



}
