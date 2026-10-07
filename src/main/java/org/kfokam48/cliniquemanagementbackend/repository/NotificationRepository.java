package org.kfokam48.cliniquemanagementbackend.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.kfokam48.cliniquemanagementbackend.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByDestinataireIdOrderByDateEnvoiDesc(Long destinataireId);
    Page<Notification> findByDestinataireId(Long destinataireId, Pageable pageable);
}
