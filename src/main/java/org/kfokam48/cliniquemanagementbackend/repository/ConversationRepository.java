package org.kfokam48.cliniquemanagementbackend.repository;

import org.kfokam48.cliniquemanagementbackend.model.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    // Méthodes personnalisées pour la gestion des conversations peuvent être ajoutées ici
}
