package org.kfokam48.cliniquemanagementbackend.repository;

import org.kfokam48.cliniquemanagementbackend.model.Conversation;
import org.kfokam48.cliniquemanagementbackend.model.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    @Query("""
        SELECT c FROM Conversation c
        WHERE :user1 MEMBER OF c.participants
        AND :user2 MEMBER OF c.participants
    """)
    Optional<Conversation> findByParticipants(@Param("user1") Utilisateur user1,
                                              @Param("user2") Utilisateur user2);

    // Nouvelle méthode pour récupérer toutes les conversations d'un utilisateur
    @Query("SELECT c FROM Conversation c WHERE :user MEMBER OF c.participants")
    List<Conversation> findByParticipantsContaining(@Param("user") Utilisateur user);

}
