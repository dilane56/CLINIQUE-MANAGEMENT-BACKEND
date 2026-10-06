package org.kfokam48.cliniquemanagementbackend.migration;

import org.junit.jupiter.api.Test;
import org.kfokam48.cliniquemanagementbackend.repository.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * I9 : la migration Flyway db/migration/postgresql crée un schéma qu'Hibernate accepte en
 * ddl-auto=validate (tables, colonnes et types conformes aux entités).
 * Exécuté sur H2 en mode de compatibilité PostgreSQL.
 */
@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:flywaypostgresql;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver",
        // Indépendant du profil actif (la CI utilise "prod", qui impose le dialecte PostgreSQL)
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.flyway.enabled=true",
        "spring.flyway.locations=classpath:db/migration/postgresql",
        // V2 (contrainte d'exclusion btree_gist) n'existe pas sous H2 : à vérifier sur un vrai PostgreSQL
        "spring.flyway.target=1",
        "spring.jpa.hibernate.ddl-auto=validate"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class FlywayMigrationPostgreSqlTest {

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Test
    void schemaCreatedByFlywayMatchesTheEntities() {
        // Le contexte ne démarre que si la validation Hibernate du schéma a réussi
        assertThat(utilisateurRepository.count()).isZero();
    }
}
