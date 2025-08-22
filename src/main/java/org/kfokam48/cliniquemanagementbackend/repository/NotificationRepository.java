package org.kfokam48.cliniquemanagementbackend.repository;

import org.kfokam48.cliniquemanagementbackend.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByDestinataireIdOrderByDateEnvoiDesc(Long destinataireId);
}
