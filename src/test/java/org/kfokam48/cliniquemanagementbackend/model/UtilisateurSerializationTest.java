package org.kfokam48.cliniquemanagementbackend.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UtilisateurSerializationTest {

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Test
    void passwordIsNeverSerialized() throws Exception {
        Administrateur admin = new Administrateur();
        admin.setEmail("admin@test.com");
        admin.setPassword("$2a$10$hashBcrypt");

        String json = objectMapper.writeValueAsString(admin);

        assertThat(json).contains("admin@test.com");
        assertThat(json).doesNotContain("password").doesNotContain("hashBcrypt");
    }

    @Test
    void passwordIsStillReadFromJson() throws Exception {
        Administrateur admin = objectMapper.readValue(
                "{\"email\":\"admin@test.com\",\"password\":\"secret\"}", Administrateur.class);

        assertThat(admin.getPassword()).isEqualTo("secret");
    }
}
