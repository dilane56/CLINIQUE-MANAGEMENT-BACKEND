package org.kfokam48.cliniquemanagementbackend.repository;

import org.kfokam48.cliniquemanagementbackend.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    // Custom query to find messages by sender and receiver
    @Query("SELECT m FROM Message m WHERE (m.expediteur.id = :senderId AND m.destinataire.id = :receiverId) OR (m.expediteur.id = :receiverId AND m.destinataire.id = :senderId)")
    List<Message> findBySenderAndReceiver(Long senderId, Long receiverId);

    // Custom query to find messages by receiver and sender
    @Query("SELECT m FROM Message m WHERE (m.destinataire.email = :receiverUsername AND m.expediteur.email = :senderUsername) OR (m.destinataire.email = :senderUsername AND m.expediteur.email = :receiverUsername)")
    List<Message> findByReceiverAndSender(String receiverUsername, String senderUsername);

    // Custom query to find messages by sender
    @Query("SELECT m FROM Message m WHERE m.expediteur.id = :senderId")
    List<Message> findBySender(Long senderId);

    // Custom query to find messages by receiver
    @Query("SELECT m FROM Message m WHERE m.destinataire.id = :receiverId")
    List<Message> findByReceiver(Long receiverId);

    @Query("SELECT m FROM Message m " +
            "WHERE (m.expediteur.id = :user1Id AND m.destinataire.id = :user2Id) " +
            "   OR (m.expediteur.id = :user2Id AND m.destinataire.id = :user1Id) " +
            "ORDER BY m.dateEnvoi ASC")
    List<Message> findConversation(Long user1Id, Long user2Id);

    List<Message> findByExpediteurIdAndDestinataireIdOrExpediteurIdAndDestinataireIdOrderByDateEnvoi(
            Long expediteurId1, Long destinataireId1,
            Long expediteurId2, Long destinataireId2
    );


}
