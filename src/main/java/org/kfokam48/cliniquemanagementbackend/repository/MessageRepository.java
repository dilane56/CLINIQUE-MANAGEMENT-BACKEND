package org.kfokam48.cliniquemanagementbackend.repository;

import org.kfokam48.cliniquemanagementbackend.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    // Custom query to find messages by sender and receiver
    @Query("SELECT m FROM Message m WHERE (m.sender.id = :senderId AND m.receiver.id = :receiverId) OR (m.sender.id = :receiverId AND m.receiver.id = :senderId)")
    List<Message> findBySenderAndReceiver(Long senderId, Long receiverId);

    // Custom query to find messages by receiver and sender
    @Query("SELECT m FROM Message m WHERE (m.receiver.email = :receiverUsername AND m.sender.email = :senderUsername) OR (m.receiver.email = :senderUsername AND m.sender.email = :receiverUsername)")
    List<Message> findByReceiverAndSender(String receiverUsername, String senderUsername);

    // Custom query to find messages by sender
    @Query("SELECT m FROM Message m WHERE m.sender.id = :senderId")
    List<Message> findBySender(Long senderId);

    // Custom query to find messages by receiver
    @Query("SELECT m FROM Message m WHERE m.receiver.id = :receiverId")
    List<Message> findByReceiver(Long receiverId);


}
