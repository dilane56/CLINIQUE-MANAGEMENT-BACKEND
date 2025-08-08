package org.kfokam48.cliniquemanagementbackend.model;

import jakarta.persistence.*;

import java.util.List;

@Entity
public class Conversation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToMany
    private List<Utilisateur> participants;

    @OneToMany(mappedBy = "conversation")
    private List<Message> messages;
}

