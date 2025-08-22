package org.kfokam48.cliniquemanagementbackend.repository;

import jakarta.transaction.Transactional;
import org.kfokam48.cliniquemanagementbackend.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {




    @Query("SELECT m FROM Message m " +
            "WHERE (m.expediteur.id = :user1Id AND m.destinataire.id = :user2Id) " +
            "   OR (m.expediteur.id = :user2Id AND m.destinataire.id = :user1Id) " +
            "ORDER BY m.dateEnvoi ASC")
    List<Message> findConversation(Long user1Id, Long user2Id);


    // Nouvelle méthode pour récupérer les messages par conversation
    @Query("SELECT m FROM Message m WHERE m.conversation.id = :conversationId ORDER BY m.dateEnvoi ASC")
    List<Message> findByConversationIdOrderByDateEnvoiAsc(Long conversationId);

    // Nouvelle méthode pour récupérer tous les messages d'un utilisateur
    @Query("SELECT m FROM Message m WHERE m.destinataire.id = :userId OR m.expediteur.id = :userId ORDER BY m.dateEnvoi DESC")
    List<Message> findByDestinataireIdOrExpediteurIdOrderByDateEnvoiDesc(Long userId, Long userId2);

    // Dans votre MessageRepository.java
    @Modifying
    @Transactional
    @Query("UPDATE Message m SET m.messageStatus = 'READ', m.lu = true WHERE m.expediteur.id = :expediteurId AND m.destinataire.id = :destinataireId AND m.lu = false")
    void updateMessageStatusToRead(@Param("expediteurId") Long expediteurId, @Param("destinataireId") Long destinataireId);

}
