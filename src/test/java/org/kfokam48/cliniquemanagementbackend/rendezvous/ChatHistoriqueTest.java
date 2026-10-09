package org.kfokam48.cliniquemanagementbackend.rendezvous;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kfokam48.cliniquemanagementbackend.enums.MessageStatus;
import org.kfokam48.cliniquemanagementbackend.model.Medecin;
import org.kfokam48.cliniquemanagementbackend.model.Message;
import org.kfokam48.cliniquemanagementbackend.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Historique paginé du chat (P7) et passage SENT -> DELIVERED en une requête, sur H2.
 */
@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:chat;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false"
})
class ChatHistoriqueTest {

    private static final Instant DEBUT = Instant.parse("2030-01-15T09:00:00Z");

    @Autowired private TestEntityManager em;
    @Autowired private MessageRepository repository;

    private Medecin anne;
    private Medecin paul;
    private Message dernier;

    private Message message(Medecin de, Medecin a, int minutes, MessageStatus statut) {
        Message message = new Message();
        message.setExpediteur(de);
        message.setDestinataire(a);
        message.setContenu("message " + minutes);
        message.setDateEnvoi(DEBUT.plusSeconds(60L * minutes));
        message.setMessageStatus(statut);
        return em.persist(message);
    }

    @BeforeEach
    void setUp() {
        anne = em.persist(RendezVousTestData.medecin("anne@test.com"));
        paul = em.persist(RendezVousTestData.medecin("paul@test.com"));
        Medecin autre = em.persist(RendezVousTestData.medecin("autre@test.com"));
        // 5 messages entre Anne et Paul (3 de Paul encore SENT), 1 avec un tiers
        message(anne, paul, 1, MessageStatus.READ);
        message(paul, anne, 2, MessageStatus.SENT);
        message(paul, anne, 3, MessageStatus.SENT);
        message(anne, paul, 4, MessageStatus.SENT);
        dernier = message(paul, anne, 5, MessageStatus.SENT);
        message(autre, anne, 6, MessageStatus.SENT);
        em.flush();
    }

    @Test
    void historiquePagineLesPlusRecentsDabord() {
        Page<Message> premiere = repository.findConversation(anne.getId(), paul.getId(),
                PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "dateEnvoi")));

        assertThat(premiere.getTotalElements()).isEqualTo(5);
        assertThat(premiere.getTotalPages()).isEqualTo(3);
        assertThat(premiere.getContent()).extracting(Message::getContenu).containsExactly("message 5", "message 4");

        Page<Message> derniere = repository.findConversation(paul.getId(), anne.getId(),
                PageRequest.of(2, 2, Sort.by(Sort.Direction.DESC, "dateEnvoi")));
        assertThat(derniere.getContent()).extracting(Message::getContenu).containsExactly("message 1");
    }

    @Test
    void seulsLesMessagesRecusDeLautrePassentDistribues() {
        int modifies = repository.marquerDistribues(paul.getId(), anne.getId());
        em.clear();

        assertThat(modifies).isEqualTo(3);
        assertThat(repository.findById(dernier.getId()).orElseThrow().getMessageStatus()).isEqualTo(MessageStatus.DELIVERED);
        // Message d'Anne vers Paul et message du tiers : inchangés
        assertThat(repository.findAll()).filteredOn(m -> m.getMessageStatus() == MessageStatus.SENT)
                .extracting(Message::getContenu).containsExactlyInAnyOrder("message 4", "message 6");
    }
}
